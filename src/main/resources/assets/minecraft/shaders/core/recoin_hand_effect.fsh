#version 150

uniform sampler2D SceneSampler;
uniform sampler2D SceneBeforeSampler;
uniform sampler2D MaskSampler;
uniform sampler2D GlowSampler;
uniform sampler2D CoverageSampler;

layout(std140) uniform HandEffectData {
    vec4 data;
    vec4 color1;
    vec4 color2;
    vec4 params;
};

in vec2 texCoord;
in vec2 texelSize;
out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

vec3 rainbow(float t) {
    return 0.55 + 0.45 * cos(6.28318 * (vec3(0.00, 0.33, 0.67) + t));
}

float sampleMask(vec2 uv) {
    return texture(MaskSampler, clamp(uv, texelSize * 0.5, vec2(1.0) - texelSize * 0.5)).r;
}

vec3 smoothHandScene(vec2 uv) {
    vec2 radius = texelSize * 2.65;
    vec2 minUv = texelSize * 0.5;
    vec2 maxUv = vec2(1.0) - minUv;
    vec3 result = texture(SceneSampler, clamp(uv, minUv, maxUv)).rgb * 0.28;
    result += texture(SceneSampler, clamp(uv + vec2(radius.x, 0.0), minUv, maxUv)).rgb * 0.12;
    result += texture(SceneSampler, clamp(uv - vec2(radius.x, 0.0), minUv, maxUv)).rgb * 0.12;
    result += texture(SceneSampler, clamp(uv + vec2(0.0, radius.y), minUv, maxUv)).rgb * 0.12;
    result += texture(SceneSampler, clamp(uv - vec2(0.0, radius.y), minUv, maxUv)).rgb * 0.12;
    result += texture(SceneSampler, clamp(uv + radius, minUv, maxUv)).rgb * 0.06;
    result += texture(SceneSampler, clamp(uv - radius, minUv, maxUv)).rgb * 0.06;
    result += texture(SceneSampler, clamp(uv + vec2(radius.x, -radius.y), minUv, maxUv)).rgb * 0.06;
    result += texture(SceneSampler, clamp(uv + vec2(-radius.x, radius.y), minUv, maxUv)).rgb * 0.06;
    return result;
}

float antialiasedMask(vec2 uv) {
    vec2 px = texelSize;
    float center = sampleMask(uv) * 4.0;
    float axial = sampleMask(uv + vec2(px.x, 0.0))
                + sampleMask(uv - vec2(px.x, 0.0))
                + sampleMask(uv + vec2(0.0, px.y))
                + sampleMask(uv - vec2(0.0, px.y));
    float diagonal = sampleMask(uv + px)
                   + sampleMask(uv - px)
                   + sampleMask(uv + vec2(px.x, -px.y))
                   + sampleMask(uv + vec2(-px.x, px.y));
    float coverage = (center + axial * 2.0 + diagonal) / 16.0;
    float aa = max(fwidth(coverage) * 0.85, 1.0 / 255.0);
    return smoothstep(0.08 - aa, 0.92 + aa, coverage);
}

float handAuraHash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float handAuraNoise21(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = handAuraHash21(i);
    float b = handAuraHash21(i + vec2(1.0, 0.0));
    float c = handAuraHash21(i + vec2(0.0, 1.0));
    float d = handAuraHash21(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float handAuraFbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += handAuraNoise21(p) * amplitude;
        p = p * 2.03 + vec2(17.1, 9.2);
        amplitude *= 0.5;
    }
    return value;
}

float handAuraSampleGlow(vec2 uv) {
    return texture(GlowSampler,
            clamp(uv, texelSize * 0.5, vec2(1.0) - texelSize * 0.5)).r;
}

float handAuraSoftEdge(vec2 uv) {
    vec2 radius = texelSize * 2.1;
    float center = handAuraSampleGlow(uv);
    float around = handAuraSampleGlow(uv + vec2(radius.x, 0.0))
                 + handAuraSampleGlow(uv - vec2(radius.x, 0.0))
                 + handAuraSampleGlow(uv + vec2(0.0, radius.y))
                 + handAuraSampleGlow(uv - vec2(0.0, radius.y));
    around *= 0.25;
    return clamp(abs(center - around) * 2.4, 0.0, 1.0);
}

float handAuraUpwardTrail(vec2 uv) {
    float trail = 0.0;
    float weight = 0.0;
    for (int i = 1; i <= 10; i++) {
        float fi = float(i);
        float w = exp(-fi * 0.36);
        float sampleA = handAuraSampleGlow(uv - vec2(0.0, texelSize.y * fi * 7.4));
        float sampleB = handAuraSampleGlow(uv - vec2(texelSize.x * fi * 0.8,
                                                     texelSize.y * fi * 5.3));
        float sampleC = handAuraSampleGlow(uv + vec2(texelSize.x * fi * 0.8,
                                                    -texelSize.y * fi * 5.3));
        trail += max(sampleA, max(sampleB, sampleC)) * w;
        weight += w;
    }
    return weight > 0.0 ? trail / weight : 0.0;
}

bool handAuraHasCoverage(vec2 uv) {
    vec2 glowSize = vec2(textureSize(GlowSampler, 0));
    vec2 pixel = uv * glowSize;
    vec2 lowerReach = vec2(8.0, 74.0) * texelSize * glowSize + vec2(1.0);
    vec2 upperReach = vec2(8.0, 0.0) * texelSize * glowSize + vec2(1.0);
    ivec2 lastTile = textureSize(CoverageSampler, 0) - ivec2(1);
    ivec2 first = clamp(ivec2(floor((pixel - lowerReach) / 16.0)), ivec2(0), lastTile);
    ivec2 last = clamp(ivec2(floor((pixel + upperReach) / 16.0)), ivec2(0), lastTile);
    for (int y = first.y; y <= last.y; y++) {
        for (int x = first.x; x <= last.x; x++) {
            if (texelFetch(CoverageSampler, ivec2(x, y), 0).r > 0.0) return true;
        }
    }
    return false;
}

float handAuraFlowField(vec2 uv, float glowMask, float time) {
    float rise = uv.y * 4.8 - time * 1.45;
    vec2 p = vec2(uv.x * (8.0 + glowMask * 4.0), rise);
    float swirl = handAuraFbm(p + vec2(handAuraFbm(p * 0.62 + time * 0.31), 0.0));
    float ribbon = max(0.0, sin(uv.y * 26.0 - time * 6.2 + swirl * 5.0 + uv.x * 6.0));
    float veins = max(0.0, sin(uv.x * 42.0 + time * 1.6 + swirl * 6.5));
    float veil = smoothstep(0.28, 0.9, swirl * 0.9 + ribbon * 0.55);
    return clamp(veil + veins * 0.16, 0.0, 1.0);
}

vec3 handAuraTint(vec4 color, float flow, float edge) {
    vec3 base = clamp(color.rgb * 0.88, 0.0, 1.0);
    vec3 highlight = clamp(color.rgb * 1.16, 0.0, 1.0);
    float accent = clamp(0.12 + flow * 0.16 + edge * 0.08, 0.0, 0.34);
    return mix(base, highlight, accent);
}

void main() {
    vec4 scene = texture(SceneSampler, texCoord);
#ifdef AURA_ONLY
    const int mode = 16;
#else
    int mode = int(data.w + 0.5);
#endif
    float mask = mode == 16 ? 0.0 : antialiasedMask(texCoord);
    bool glowActive = mode == 6 || mode == 11 || mode == 12 || mode == 13 || mode == 14 || mode == 15 || mode == 16;
    if (mask < 0.01 && !glowActive) {
        fragColor = scene;
        return;
    }

    float time = data.z;
    float intensity = clamp(params.x, 0.0, 3.0);
    vec3 base = scene.rgb;
    int baseMode = mode == 13 ? 1 : mode == 14 ? 2 : mode == 15 ? 10 : mode;

    if (mode == 16) {
        float rawMask = sampleMask(texCoord);
        if (rawMask < 0.004 && !handAuraHasCoverage(texCoord)) {
            fragColor = scene;
            return;
        }
        float blurMask = handAuraSampleGlow(texCoord) * 0.72;
        float trailMask = handAuraUpwardTrail(texCoord) * 0.86;
        float liftedMask = max(blurMask, trailMask * 1.12);
        if (liftedMask < 0.004 && rawMask < 0.004) {
            fragColor = scene;
            return;
        }
        vec4 sceneBefore = texture(SceneBeforeSampler, texCoord);
        float handCover = smoothstep(0.025, 0.38, rawMask);
        float fillHands = params.w > 0.5 ? 1.0 : 0.0;
        float handEffectCover = mix(handCover, 0.0, fillHands);
        float handOverlay = handCover * fillHands;
        vec2 driftUv = clamp(texCoord, texelSize * 0.5, vec2(1.0) - texelSize * 0.5);
        float softFieldNoise = handAuraFbm(vec2(
                driftUv.x * 6.2 + time * 0.05,
                driftUv.y * 5.8 - time * 0.04));
        vec4 activeColor = color1;

        float edge = handAuraSoftEdge(texCoord);
        float outerField = clamp(
                blurMask * 0.86 + trailMask * 0.52 + softFieldNoise * 0.024,
                0.0, 1.0);
        float outer = smoothstep(0.07, 0.64, outerField);
        outer *= 1.0 - handEffectCover * 0.975;
        outer = pow(clamp(outer, 0.0, 1.0), 1.16);
        float verticalFade = 0.58 + smoothstep(0.10, 0.60, trailMask) * 0.46;

        float flow = handAuraFlowField(
                driftUv,
                max(trailMask, max(liftedMask * 0.82, rawMask * 0.65)),
                time);
        float mist = handAuraFbm(vec2(
                driftUv.x * 5.7 - time * 0.18,
                driftUv.y * 2.8 - time * 0.32));
        float streaks = pow(max(0.0, sin(
                driftUv.y * 21.0 - time * 7.1 + mist * 4.2 + driftUv.x * 5.5)), 4.0);
        float fade = smoothstep(0.10, 0.40, outerField);

        float veil = outer * (0.22 + flow * 0.58 + mist * 0.10) * verticalFade;
        float trail = outer * streaks * 0.20 * smoothstep(0.12, 0.70, trailMask);
        float rim = edge * outer * (0.05 + flow * 0.11);
        float aura = clamp((veil + trail + rim) * fade, 0.0, 1.0)
                   * activeColor.a * 0.84;
        vec3 tint = handAuraTint(activeColor, flow, edge);

        vec3 behindHand = sceneBefore.rgb + tint * aura * 1.08;
        behindHand += tint * edge * outer * 0.035;
        vec3 softenedHand = scene.rgb;
        if (handCover > 0.0) {
            softenedHand = mix(scene.rgb, smoothHandScene(texCoord), 0.68);
        }
        vec3 finalColor = mix(behindHand, softenedHand, handCover);
        finalColor += (tint * aura * 0.38 + tint * edge * outer * 0.018)
                    * handOverlay;

        fragColor = vec4(clamp(finalColor, 0.0, 1.0), scene.a);
        return;
    }

    vec3 effect = mix(color1.rgb, color2.rgb, 0.5 + 0.5 * sin((texCoord.x + texCoord.y + time * 0.25) * 6.28318));
    if (baseMode == 1) {
        effect = mix(color1.rgb, color2.rgb, texCoord.y + sin(time + texCoord.x * 8.0) * 0.15);
    } else if (baseMode == 2) {
        effect = rainbow(texCoord.x * 0.7 + texCoord.y * 0.35 + time * 0.08);
    } else if (baseMode == 3) {
        float n = noise(texCoord * data.xy * 0.012 + vec2(time * 0.35, -time * 0.2));
        effect = mix(base, color1.rgb, smoothstep(0.25, 1.0, n) * 0.95);
    } else if (baseMode == 4) {
        float stripes = step(0.5, fract((texCoord.x + texCoord.y * 0.75 + time * 0.15) * 18.0));
        effect = mix(color1.rgb, color2.rgb, stripes);
    } else if (baseMode == 5) {
        float flakes = step(0.985, noise(texCoord * data.xy * 0.25 + time));
        effect = mix(base * 0.72 + color1.rgb * 0.25, color1.rgb, flakes);
    } else if (baseMode == 7) {
        vec2 drift = vec2(time * 0.18, -time * 0.12);
        float coarse = noise(texCoord * data.xy * 0.022 + drift);
        float detail = noise(texCoord * data.xy * 0.048 - drift * 0.7);
        float camo = smoothstep(0.34, 0.68, coarse * 0.72 + detail * 0.28);
        effect = mix(vec3(0.12, 0.26, 0.12), vec3(0.34, 0.42, 0.22), camo);
    } else if (baseMode == 10) {
        base = smoothHandScene(texCoord);
        float warp = noise(texCoord * vec2(8.0, 6.0) + vec2(time * 0.16, -time * 0.11));
        float bands = 0.5 + 0.5 * sin((texCoord.x - texCoord.y) * 14.0 + warp * 5.5 + time * 0.85);
        effect = mix(color1.rgb, color2.rgb, smoothstep(0.16, 0.84, bands));
        effect *= 0.82 + 0.28 * noise(texCoord * data.xy * 0.018 - time * 0.07);
    }

    if (glowActive) {
        vec3 dynamicGlow = color1.rgb;
        if (mode == 11 || mode == 14) {
            dynamicGlow = rainbow(texCoord.x * 0.55 + texCoord.y * 0.28 + time * 0.09);
        } else if (mode == 12 || mode == 15) {
            float warp = noise(texCoord * vec2(7.0, 5.0) + vec2(time * 0.18, -time * 0.13));
            float bands = 0.5 + 0.5 * sin((texCoord.x - texCoord.y) * 13.0 + warp * 5.0 + time * 0.75);
            dynamicGlow = mix(color1.rgb, color2.rgb, smoothstep(0.18, 0.82, bands));
        } else if (mode == 13) {
            dynamicGlow = mix(color1.rgb, color2.rgb, clamp(texCoord.y + sin(time + texCoord.x * 8.0) * 0.15, 0.0, 1.0));
        }

        float radiusPx = clamp(params.y * 150.0, 2.0, 32.0) * (0.76 + params.z * 0.055);
        float halo = 0.0;
        for (int i = 0; i < 24; i++) {
            float angle = 6.2831853 * (float(i) + 0.5) / 24.0;
            vec2 dir = vec2(cos(angle), sin(angle));
            float innerMask = texture(MaskSampler, texCoord + dir * texelSize * radiusPx * 0.25).r;
            float nearMask = texture(MaskSampler, texCoord + dir * texelSize * radiusPx * 0.55).r;
            float farMask = texture(MaskSampler, texCoord + dir * texelSize * radiusPx).r;
            halo += innerMask * 0.46 + nearMask * 0.36 + farMask * 0.18;
        }
        halo /= 24.0;
        float outside = 1.0 - smoothstep(0.02, 0.34, mask);
        float outerGlow = smoothstep(0.01, 0.56, halo) * outside;
        float edge = max(0.0, halo - mask * 0.28);
        float pulse = 0.94 + 0.06 * sin(time * 2.15);
        float auraBoost = mode == 16 ? 1.45 : 1.0;
        vec3 emitted = dynamicGlow * (outerGlow * 3.05 + edge * 0.62) * intensity * pulse * auraBoost;
        if (mask < 0.01) {
            fragColor = vec4(scene.rgb + emitted, scene.a);
            return;
        }
        if (mode == 6 || mode == 11 || mode == 12 || mode == 16) {
            effect = params.w > 0.5 ? mix(base, dynamicGlow * 1.12, 0.58) : base;
        }
        effect += dynamicGlow * edge * 0.85 * intensity;
    }

    float alpha = clamp(max(color1.a, color2.a) * mask, 0.0, 1.0);
    float mixPower = clamp(0.78 * intensity, 0.0, 1.0);
    vec3 finalColor = mix(base, effect, mixPower);
    fragColor = vec4(mix(scene.rgb, finalColor, alpha), scene.a);
}
