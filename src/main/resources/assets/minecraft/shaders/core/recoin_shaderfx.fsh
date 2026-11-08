#version 150

// Shader module: realistic post processing in one full-screen pass.
//  - sky: deeper blue at the top, warm horizon, glow around the sun
//  - bloom: bright things (lava, torches, sun, glowstone) glow softly
//  - sun rays through gaps in the terrain
//  - aerial perspective: far terrain fades into a bluish haze
//  - wet look in rain: darker, more contrasty ground, cooler light
//  - filmic tone mapping (ACES), saturation, contrast, light sharpening, vignette,
//    a hint of chromatic aberration at the screen edges

layout(std140) uniform Uniforms {
    mat4 uInvProjection;
    mat4 uProjection;
    mat4 uViewRot;
    vec4 uCameraPos;
    vec4 uScreen;
    vec4 uParams;
    vec4 uExtraParams;
    vec4 uFxParams;
    vec4 uTint;
};

uniform sampler2D Sampler0;   // scene colour
uniform sampler2D Sampler1;   // scene depth
in vec2 vUV;
flat in vec4 vSun;
out vec4 fragColor;

vec3 viewPosition(vec2 uv, float depth) {
    vec4 position = uInvProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return position.xyz / (abs(position.w) < 0.00001 ? 0.00001 : position.w);
}

float luma(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

vec3 aces(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

vec3 toLinear(vec3 c) {
    return pow(max(c, vec3(0.0)), vec3(2.2));
}

vec3 toGamma(vec3 c) {
    return pow(max(c, vec3(0.0)), vec3(1.0 / 2.2));
}

void main() {
    vec2 px = 1.0 / uScreen.xy;
    float bloomAmount = uParams.y;
    float exposure = uParams.w;
    float vignette = uExtraParams.x;
    float sharpen = uExtraParams.y;
    float saturation = uExtraParams.z;
    float contrast = uExtraParams.w;
    float rays = uFxParams.x;
    float wet = uFxParams.y;
    float haze = uFxParams.z;
    float aberration = uFxParams.w;

    // chromatic aberration grows towards the edges
    vec2 fromCenter = vUV - 0.5;
    vec2 shift = fromCenter * aberration * 0.006;
    vec3 base;
    base.r = texture(Sampler0, vUV + shift).r;
    base.g = texture(Sampler0, vUV).g;
    base.b = texture(Sampler0, vUV - shift).b;

    // light sharpening (unsharp mask)
    if (sharpen > 0.0) {
        vec3 blur = (texture(Sampler0, vUV + vec2(px.x, 0.0)).rgb + texture(Sampler0, vUV - vec2(px.x, 0.0)).rgb
                   + texture(Sampler0, vUV + vec2(0.0, px.y)).rgb + texture(Sampler0, vUV - vec2(0.0, px.y)).rgb) * 0.25;
        base += (base - blur) * sharpen;
    }

    float depth = texture(Sampler1, vUV).r;
    bool sky = depth >= 0.99999;
    vec3 color = toLinear(base);

    // ---- sky: richer gradient and a sun glow
    vec3 viewDir = normalize(viewPosition(vUV, 1.0));
    vec3 worldDir = mat3(uViewRot) * viewDir;
    float sunUp = vSun.z;
    if (sky && uTint.a > 0.0) {
        float up = clamp(worldDir.y, 0.0, 1.0);
        vec3 zenith = mix(vec3(0.02, 0.03, 0.08), vec3(0.10, 0.28, 0.75), sunUp);
        vec3 horizon = mix(vec3(0.20, 0.12, 0.10), vec3(0.75, 0.62, 0.45), sunUp);
        vec3 gradient = mix(horizon, zenith, pow(up, 0.55));
        color = mix(color, color * 0.5 + gradient * 0.8, uTint.a * 0.55);
        if (vSun.w > 0.0) {
            float d = length((vUV - vSun.xy) * vec2(uScreen.x / uScreen.y, 1.0));
            color += vec3(1.0, 0.82, 0.55) * exp(-d * 9.0) * 0.6 * uTint.a * (0.4 + sunUp);
        }
    }

    // ---- aerial perspective: distant terrain fades into the sky colour
    if (!sky && haze > 0.0) {
        float dist = length(viewPosition(vUV, depth));
        float fog = 1.0 - exp(-max(0.0, dist - 24.0) * 0.0035 * haze);
        vec3 hazeColor = toLinear(mix(vec3(0.55, 0.62, 0.72), vec3(0.85, 0.80, 0.70), 0.3)) * mix(0.15, 1.0, sunUp);
        color = mix(color, hazeColor, clamp(fog, 0.0, 0.85));
    }

    // ---- wet world in rain: darker, more saturated ground, cooler light, faint sky sheen near the horizon
    if (wet > 0.0 && !sky) {
        float facing = clamp(1.0 - abs(worldDir.y) * 2.5, 0.0, 1.0);
        color *= mix(1.0, 0.78, wet);
        color = mix(color, color * vec3(0.92, 0.97, 1.08), wet);
        color += toLinear(vec3(0.45, 0.52, 0.62)) * facing * wet * 0.08;
    }

    // ---- bloom: bright parts glow (two rings of taps with a soft threshold)
    if (bloomAmount > 0.0) {
        vec3 glow = vec3(0.0);
        float total = 0.0;
        for (int ring = 1; ring <= 3; ring++) {
            float radius = float(ring) * 5.0;
            for (int i = 0; i < 8; i++) {
                float a = float(i) * 0.785398 + float(ring) * 0.39;
                vec2 off = vec2(cos(a), sin(a)) * radius * px;
                vec3 s = toLinear(texture(Sampler0, vUV + off).rgb);
                float l = luma(s);
                float weight = 1.0 / float(ring);
                glow += s * smoothstep(0.55, 1.0, l) * weight;
                total += weight;
            }
        }
        glow /= total;
        // lava, fire and torches are orange: they glow a bit warmer
        color += glow * bloomAmount * 2.2;
    }

    // ---- sun rays through gaps (radial sampling of the sky mask towards the sun)
    if (rays > 0.0 && vSun.w > 0.0 && sunUp > 0.0) {
        vec2 delta = (vSun.xy - vUV) / 24.0;
        vec2 coord = vUV;
        float light = 0.0;
        float decay = 1.0;
        for (int i = 0; i < 24; i++) {
            coord += delta;
            if (coord.x < 0.0 || coord.x > 1.0 || coord.y < 0.0 || coord.y > 1.0) break;
            light += step(0.99999, texture(Sampler1, coord).r) * decay;
            decay *= 0.94;
        }
        light /= 12.0;
        float facingSun = exp(-length(vSun.xy - vec2(0.5)) * 1.2);
        color += vec3(1.0, 0.86, 0.62) * light * rays * 0.12 * facingSun * sunUp;
    }

    // ---- tone mapping and grading
    color *= exposure * uTint.rgb;
    color = aces(color);
    color = toGamma(color);
    float l = luma(color);
    color = mix(vec3(l), color, saturation);
    color = (color - 0.5) * contrast + 0.5;

    // ---- vignette
    float v = 1.0 - dot(fromCenter, fromCenter) * vignette * 1.6;
    color *= clamp(v, 0.0, 1.0);

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
