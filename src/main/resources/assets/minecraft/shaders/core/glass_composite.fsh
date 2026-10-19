#version 150

in vec2 texCoord;
in vec2 texelSize;

out vec4 fragColor;

uniform sampler2D SceneSampler;
uniform sampler2D BlurSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform GlassData {
    vec4 resolution;  // width, height, saturation, reflected blur
    vec4 tintColor;   // glass tint
    vec4 settings;    // tint, edge glow, edge softness (px), time
    vec4 iceParams;   // time, refraction strength, refraction reach (px), chromatic strength
    vec4 smokeParams; // halo reach (px), shimmer strength, unused, unused
};

float maskAt(vec2 uv) {
    return texture(MaskSampler, clamp(uv, vec2(0.001), vec2(0.999))).r;
}

float edgeHalo(vec2 uv, float reachPx) {
    float halo = 0.0;
    for (int ring = 1; ring <= 3; ring++) {
        float radius = float(ring) / 3.0;
        vec2 offset = texelSize * reachPx * radius;
        float falloff = 1.0 - radius * 0.62;
        for (int i = 0; i < 8; i++) {
            float angle = float(i) * 0.7853981634;
            vec2 direction = vec2(cos(angle), sin(angle));
            halo = max(halo, maskAt(uv + direction * offset) * falloff);
        }
    }
    return clamp(halo, 0.0, 1.0);
}

void main() {
    vec4 scene = texture(SceneSampler, texCoord);
    float maskValue = maskAt(texCoord);
    float edgeSoftness = clamp(settings.z, 0.0, 28.0);
    float haloReach = clamp(smokeParams.x, 1.0, 64.0);

    // Most screen pixels are far from the hand. Reject them with two cheap rings
    // before evaluating the wider glow.
    float nearMask = maskValue;
    if (nearMask < 0.01) {
        for (int ring = 1; ring <= 2; ring++) {
            vec2 radius = texelSize * haloReach * (float(ring) * 0.5);
            for (int i = 0; i < 8; i++) {
                float angle = float(i) * 0.7853981634;
                nearMask = max(nearMask, maskAt(texCoord + vec2(cos(angle), sin(angle)) * radius));
            }
        }
        if (nearMask < 0.01) {
            fragColor = scene;
            return;
        }
    }

    float halo = maskValue < 0.5 ? edgeHalo(texCoord, haloReach) : 0.0;
    float filteredMask = maskValue;
    if (edgeSoftness > 0.5) {
        vec2 smoothingRadius = texelSize * edgeSoftness;
        filteredMask = (maskValue * 2.0
                + maskAt(texCoord + vec2(smoothingRadius.x, 0.0))
                + maskAt(texCoord - vec2(smoothingRadius.x, 0.0))
                + maskAt(texCoord + vec2(0.0, smoothingRadius.y))
                + maskAt(texCoord - vec2(0.0, smoothingRadius.y))) / 6.0;
    }
    float maskAA = max(fwidth(filteredMask), 0.006);
    float coverage = smoothstep(0.5 - maskAA, 0.5 + maskAA, filteredMask);
    if (coverage < 0.001) {
        vec3 glow = scene.rgb + tintColor.rgb * halo * settings.y * 0.7
                + vec3(pow(halo, 5.0) * settings.y * 0.55);
        fragColor = vec4(clamp(glow, vec3(0.0), vec3(1.0)), 1.0);
        return;
    }

    vec2 gradientStep = texelSize * max(1.0, edgeSoftness * 0.35);
    float leftMask = maskAt(texCoord - vec2(gradientStep.x, 0.0));
    float rightMask = maskAt(texCoord + vec2(gradientStep.x, 0.0));
    float downMask = maskAt(texCoord - vec2(0.0, gradientStep.y));
    float upMask = maskAt(texCoord + vec2(0.0, gradientStep.y));
    vec2 gradient = vec2(rightMask - leftMask, upMask - downMask);
    float gradientLength = length(gradient);
    vec2 normal = gradientLength > 0.0001 ? gradient / gradientLength : vec2(0.0, 1.0);
    float rim = clamp(gradientLength * 7.0, 0.0, 1.0);

    float time = settings.w;
    float refraction = clamp(iceParams.y, 0.0, 3.0);
    float refractionReach = clamp(iceParams.z, 0.0, 80.0);
    float chromatic = clamp(iceParams.w, 0.0, 1.0);
    float shimmer = clamp(smokeParams.y, 0.0, 1.0);
    vec2 wave = vec2(
        sin(texCoord.y * 58.0 + time * 1.7),
        cos(texCoord.x * 51.0 - time * 1.3)
    ) * (shimmer * 0.45) * texelSize;
    vec2 bend = normal * texelSize * (refractionReach * refraction * 0.18 * rim) + wave;
    vec2 blurUV = texCoord + bend;
    if (resolution.w > 0.5) {
        vec2 fromCenter = texCoord - vec2(0.5);
        blurUV = vec2(0.5) + fromCenter * 0.82 + bend;
    }
    blurUV = clamp(blurUV, vec2(0.002), vec2(0.998));

    vec3 glassColor = texture(BlurSampler, blurUV).rgb;
    if (chromatic > 0.001 && gradientLength > 0.001) {
        vec2 fringe = normal * texelSize * (1.0 + refractionReach * 0.08) * chromatic * rim;
        glassColor.r = texture(BlurSampler, clamp(blurUV + fringe, vec2(0.002), vec2(0.998))).r;
        glassColor.b = texture(BlurSampler, clamp(blurUV - fringe, vec2(0.002), vec2(0.998))).b;
    }

    float gray = dot(glassColor, vec3(0.299, 0.587, 0.114));
    glassColor = mix(vec3(gray), glassColor, resolution.z);
    glassColor = mix(glassColor, tintColor.rgb, clamp(settings.x, 0.0, 0.8));
    glassColor *= 1.08;

    float glintWave = max(0.0, sin(texCoord.x * 142.0 + texCoord.y * 37.0 + time * 2.1));
    float glint = pow(glintWave, 14.0) * rim * shimmer * 0.8;
    vec3 rimColor = mix(tintColor.rgb, vec3(1.0), 0.72);
    glassColor += rimColor * rim * settings.y * 0.48;
    glassColor += vec3(glint);

    vec3 result = mix(scene.rgb, glassColor, coverage);
    if (coverage < 0.999 && halo > 0.001) {
        float glow = halo * settings.y;
        float hotCore = pow(halo, 5.0) * settings.y;
        result += tintColor.rgb * glow * 0.7;
        result += vec3(hotCore * 0.55);
    }

    fragColor = vec4(clamp(result, vec3(0.0), vec3(1.0)), 1.0);
}
