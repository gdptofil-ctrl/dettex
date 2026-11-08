#version 150

// Atmosphere post pass: colour grade, aerial haze, sun glow + god rays, snow cover, autumn leaves,
// wet world for rain/storm (darker wet ground, sky reflections, puddles with rain ripples, distant
// rain sheets), lightning flashes and a vignette. One full-screen pass, no extra buffers.

layout(std140) uniform Uniforms {
    mat4 uInvProjection;
    mat4 uProjection;
    mat4 uViewRot;
    vec4 uCameraPos;
    vec4 uScreen;       // xy: size, zw: sun direction
    vec4 uParams;       // density, amount, time, mode
    vec4 uExtraParams;  // vignette, softness, grade, scatter height
    vec4 uFogParams;    // haze, god rays, speed, wetness
    vec4 uFogColor;     // rgb: light tint, a: lightning flash
};

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
in vec2 vUV;
flat in vec4 vSun;
out vec4 fragColor;

vec3 viewPosition(vec2 uv, float depth) {
    vec4 position = uInvProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return position.xyz / (abs(position.w) < 0.00001 ? 0.00001 : position.w);
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 position) {
    vec2 cell = floor(position);
    vec2 blend = smoothstep(vec2(0.0), vec2(1.0), fract(position));
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), blend.x),
               mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0)), blend.x), blend.y);
}

float fbm(vec2 p) {
    return noise(p) * 0.6 + noise(p * 2.03 + 7.1) * 0.3 + noise(p * 4.1 + 3.7) * 0.1;
}

float luma(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

// expanding rings: one raindrop impact per cell, each with its own timing
float ripples(vec2 p, float time) {
    float result = 0.0;
    vec2 cell = floor(p);
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 c = cell + vec2(float(x), float(y));
            float seed = hash(c);
            vec2 center = c + vec2(hash(c + 3.1), hash(c + 5.7));
            float t = fract(time * (0.7 + seed * 0.6) + seed * 7.0);
            float radius = t * 0.55;
            float d = length(p - center);
            float ring = smoothstep(0.05, 0.0, abs(d - radius)) * (1.0 - t) * (1.0 - t);
            result += ring;
        }
    }
    return result;
}

void main() {
    vec4 source = texture(Sampler0, vUV);
    float depth = texture(Sampler1, vUV).r;
    vec3 position = viewPosition(vUV, depth);
    vec3 worldPosition = (uViewRot * vec4(position, 0.0)).xyz + uCameraPos.xyz;
    vec3 viewDir = normalize((uViewRot * vec4(position, 0.0)).xyz);
    float sky = step(0.99999, depth);
    float density = uParams.x;
    float amount = uParams.y;
    float time = uParams.z;
    int mode = int(uParams.w + 0.5);
    float softness = uExtraParams.y;
    float grade = clamp(uExtraParams.z * 0.22, 0.0, 0.65);
    float wet = clamp(uFogParams.w, 0.0, 1.0);
    float flash = clamp(uFogColor.a, 0.0, 1.0);
    vec3 tint = uFogColor.rgb;
    vec3 color = source.rgb;
    float distance = length(position);
    vec3 normal = normalize(cross(dFdx(worldPosition), dFdy(worldPosition)) + vec3(0.000001));
    float up = sky < 0.5 ? smoothstep(0.55, 0.9, abs(normal.y)) : 0.0;

    // ---- snow cover with a little sparkle
    if (mode == 5 && amount > 0.0 && sky < 0.5) {
        float cover = smoothstep(0.45, 0.85, abs(normal.y));
        if (cover > 0.0) {
            float variation = fbm(worldPosition.xz * 0.7);
            float coverage = cover * clamp(amount, 0.0, 1.0) * mix(0.6, 0.92, variation);
            vec3 snow = vec3(0.84, 0.89, 0.95) * (0.62 + luma(source.rgb) * 0.38);
            float sparkle = step(0.985, hash(floor(worldPosition.xz * 24.0) + floor(time * 2.0))) * 0.35 * cover;
            color = mix(color, snow, coverage) + sparkle * coverage;
        }
    }
    // ---- autumn leaves
    if (mode == 7) {
        float foliage = smoothstep(0.015, 0.13, source.g - max(source.r, source.b));
        color = mix(color, vec3(0.79, 0.37, 0.12) * max(0.2, source.g * 1.8), foliage * clamp(amount * 0.75, 0.0, 1.0));
    }

    // ---- wet world (rain / storm)
    if (wet > 0.0) {
        if (sky < 0.5) {
            // wet surfaces get darker and more saturated
            float l = luma(color);
            color = mix(color, mix(vec3(l), color, 1.25) * 0.68, wet * (0.45 + 0.4 * up));
            // puddles on flat ground
            float puddle = smoothstep(0.52, 0.66, fbm(worldPosition.xz * 0.28)) * up * wet;
            // sky reflection: strong at grazing angles (fresnel)
            float fresnel = pow(1.0 - clamp(abs(viewDir.y), 0.0, 1.0), 4.0);
            vec3 reflection = mix(tint * 0.55, tint * 0.9, fresnel);
            float rings = ripples(worldPosition.xz * 2.2, time * 1.6) * (0.35 + puddle);
            color = mix(color, reflection, clamp(up * wet * (0.08 + fresnel * 0.25) + puddle * (0.35 + fresnel * 0.35), 0.0, 0.8));
            color += vec3(0.75, 0.82, 0.9) * rings * 0.18 * wet * up;
        }
        // distant rain sheets: thin streaks falling at a slight angle, stronger far away
        vec2 suv = vec2(vUV.x * 160.0 + vUV.y * 12.0, vUV.y * 5.0 + time * 9.0);
        float streak = smoothstep(0.78, 1.0, noise(vec2(suv.x, suv.y))) * smoothstep(0.3, 1.0, noise(vec2(suv.x * 0.13, time * 0.7)));
        float far = sky > 0.5 ? 1.0 : clamp(distance / 48.0, 0.0, 1.0);
        color = mix(color, vec3(0.78, 0.82, 0.88), streak * wet * 0.1 * far);
        // overcast: cooler and flatter light
        color = mix(color, vec3(luma(color)) * vec3(0.92, 0.96, 1.04), wet * 0.3);
    }

    // ---- colour grade towards the preset light + gentle filmic contrast
    vec3 graded = color * mix(vec3(1.0), tint / max(0.0001, max(max(tint.r, tint.g), tint.b)), 0.35);
    color = mix(color, graded, grade);
    color = mix(color, color * color * (3.0 - 2.0 * color), grade * 0.35);

    // ---- aerial haze: denser near the ground, bluer far away, glowing towards the sun
    if (density > 0.0 && uFogParams.x > 0.0) {
        float haze;
        if (sky > 0.5) {
            haze = clamp(density * uFogParams.x * 0.08, 0.0, 0.35);
        } else {
            float heightFalloff = exp(-max(worldPosition.y - uExtraParams.w, 0.0) * 0.012);
            haze = (1.0 - exp(-distance * 0.0035 * density * uFogParams.x)) * heightFalloff;
            float flow = fbm(worldPosition.xz * 0.015 + vec2(time * 0.012, time * 0.008));
            haze *= mix(0.65, 1.0, flow) * mix(0.7, 1.0, clamp(softness / 3.0, 0.0, 1.0));
            haze = clamp(haze * (1.0 + wet * 0.8), 0.0, 0.6 + wet * 0.2);
        }
        vec3 hazeColor = mix(tint, tint * vec3(0.85, 0.93, 1.08), clamp(distance / 160.0, 0.0, 1.0));
        if (vSun.w > 0.5) {
            float toSun = 1.0 - clamp(length((vUV - vSun.xy) * vec2(uScreen.x / max(uScreen.y, 1.0), 1.0)), 0.0, 1.0);
            hazeColor += tint * pow(toSun, 3.0) * 0.35;
        }
        color = mix(color, hazeColor, haze);
    }

    // ---- sun glow and god rays (dithered to avoid banding)
    if (vSun.w > 0.5 && vSun.z > 0.0005 && wet < 0.9) {
        vec2 sunUV = vSun.xy;
        vec2 aspect = vec2(uScreen.x / max(uScreen.y, 1.0), 1.0);
        float glow = exp(-length((vUV - sunUV) * aspect) * 6.0);
        color += tint * glow * vSun.z * 18.0 * (1.0 - wet);
        float dither = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
        vec2 stepUV = (sunUV - vUV) / 12.0;
        vec2 sampleUV = vUV + stepUV * dither;
        float light = 0.0;
        float weight = 1.25;
        for (int i = 0; i < 12; i++) {
            sampleUV += stepUV;
            if (all(greaterThanEqual(sampleUV, vec2(0.0))) && all(lessThanEqual(sampleUV, vec2(1.0)))) {
                light += step(0.99999, texture(Sampler1, sampleUV).r) * weight;
            }
            weight *= 0.9;
        }
        color += tint * light * vSun.z * 0.75 * (1.0 - wet);
    }

    // ---- lightning flash
    if (flash > 0.0) {
        color += vec3(0.72, 0.78, 0.95) * flash * (sky > 0.5 ? 0.9 : 0.45);
    }

    float edge = smoothstep(0.3, 0.78, length(vUV - vec2(0.5)));
    color *= 1.0 - edge * clamp(uExtraParams.x * 0.13 + wet * 0.08, 0.0, 0.45);
    fragColor = vec4(clamp(color, 0.0, 1.0), source.a);
}
