#version 150

layout(std140) uniform Uniforms {
    mat4 uInvProjection;
    mat4 uProjection;
    mat4 uViewRot;
    vec4 uCameraPos;
    vec4 uScreen;
    vec4 uParams;
    vec4 uExtraParams;
    vec4 uFogParams;
    vec4 uFogColor;
    vec4 uPlayers[16];
};

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec2 vUV;
out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash21(i), hash21(i + vec2(1.0, 0.0)), f.x),
        mix(hash21(i + vec2(0.0, 1.0)), hash21(i + vec2(1.0, 1.0)), f.x),
        f.y
    );
}

vec3 getViewPosFromRawDepth(vec2 uv, float d) {
    vec2 ndc = uv * 2.0 - 1.0;
    vec4 clip = vec4(ndc, d * 2.0 - 1.0, 1.0);
    vec4 v = uInvProjection * clip;
    float w = abs(v.w) < 0.00001 ? (v.w < 0.0 ? -0.00001 : 0.00001) : v.w;
    return v.xyz / w;
}

vec3 getViewPosFromDepth(vec2 uv) {
    float d = texture(Sampler1, clamp(uv, vec2(0.0001), vec2(0.9999))).r;
    return getViewPosFromRawDepth(uv, d);
}

vec3 getNormalFromDepth(vec2 uv, vec3 viewPos) {
    vec2 texel = 1.0 / max(uScreen.xy, vec2(1.0));

    float dL = texture(Sampler1, clamp(uv - vec2(texel.x, 0.0), vec2(0.0001), vec2(0.9999))).r;
    float dR = texture(Sampler1, clamp(uv + vec2(texel.x, 0.0), vec2(0.0001), vec2(0.9999))).r;
    float dD = texture(Sampler1, clamp(uv - vec2(0.0, texel.y), vec2(0.0001), vec2(0.9999))).r;
    float dU = texture(Sampler1, clamp(uv + vec2(0.0, texel.y), vec2(0.0001), vec2(0.9999))).r;

    vec3 pL = getViewPosFromRawDepth(uv - vec2(texel.x, 0.0), dL);
    vec3 pR = getViewPosFromRawDepth(uv + vec2(texel.x, 0.0), dR);
    vec3 pD = getViewPosFromRawDepth(uv - vec2(0.0, texel.y), dD);
    vec3 pU = getViewPosFromRawDepth(uv + vec2(0.0, texel.y), dU);

    vec3 vL = viewPos - pL;
    vec3 vR = pR - viewPos;
    vec3 vD = viewPos - pD;
    vec3 vU = pU - viewPos;

    vec3 dX = (abs(vL.z) < abs(vR.z)) ? vL : vR;
    vec3 dY = (abs(vD.z) < abs(vU.z)) ? vD : vU;

    vec3 N = cross(dX, dY);
    float nLen = length(N);
    if (nLen > 1e-6) {
        N /= nLen;
    } else {
        N = normalize(-viewPos);
    }

    if (dot(N, viewPos) > 0.0) {
        N = -N;
    }
    return N;
}

void main() {
    vec4 originalColor = texture(Sampler0, vUV);
    float depthVal = texture(Sampler1, vUV).r;

    float snowAmount = uParams.x;
    float sparkleIntensity = uParams.y;
    float time = uParams.z;
    float snowThreshold = uExtraParams.y;
    float coldGrading = uExtraParams.z;

    if (depthVal >= 0.99999) {
        vec3 skyCol = originalColor.rgb;
        if (coldGrading > 0.01) {
            float lum = dot(skyCol, vec3(0.299, 0.587, 0.114));
            skyCol = mix(skyCol, vec3(lum * 0.92, lum * 0.96, lum * 1.05 + 0.015), 0.20 * coldGrading);
        }
        fragColor = vec4(skyCol, originalColor.a);
        return;
    }

    vec3 col = originalColor.rgb;
    vec3 viewPos = getViewPosFromDepth(vUV);
    vec3 worldOffset = (uViewRot * vec4(viewPos, 0.0)).xyz;
    vec3 worldPos = uCameraPos.xyz + worldOffset;

    if (snowAmount > 0.01) {
        vec3 N_view = getNormalFromDepth(vUV, viewPos);
        vec3 N_world = normalize((uViewRot * vec4(N_view, 0.0)).xyz);

        float upFacing = clamp(N_world.y, 0.0, 1.0);
        float sNoise = noise(worldPos.xz * 2.2) * 0.16 + noise(worldPos.xz * 5.5) * 0.08;
        float coverage = smoothstep(0.27, 0.60, upFacing + sNoise);

        if (coverage > 0.01) {
            float lum = dot(col, vec3(0.299, 0.587, 0.114));
            float lightLevel = clamp(lum * 1.45 + 0.15, 0.30, 1.05);
            vec3 snowWhite = vec3(0.91, 0.95, 1.0) * lightLevel;

            col = mix(col, snowWhite, coverage * clamp(snowAmount, 0.0, 1.0));

            if (sparkleIntensity > 0.01) {
                vec2 grain = worldPos.xz * 14.0;
                vec2 cell = floor(grain);
                float sHash = hash21(cell);
                vec2 center = vec2(hash21(cell + 7.3), hash21(cell + 19.1)) * 0.5 + 0.25;
                float radius = 0.025 + sHash * 0.03;
                float edge = max(length(fwidth(grain)), 0.01);
                float point = 1.0 - smoothstep(radius, radius + edge, length(fract(grain) - center));
                float glimmer = 0.5 + 0.5 * sin(time * 1.1 + sHash * 6.28318);
                float sparkle = point * step(0.975, sHash) * (0.2 + 0.8 * pow(glimmer, 8.0))
                        * sparkleIntensity * 0.12;
                col += vec3(sparkle * coverage * snowAmount);
            }
        }
    }

    if (uFogParams.z > 0.5) {
        float dist = length(viewPos);
        float fogFactor = clamp((dist - uFogParams.x) / max(uFogParams.y - uFogParams.x, 0.001), 0.0, 1.0);
        fogFactor = fogFactor * fogFactor * (3.0 - 2.0 * fogFactor);
        float hazeNoise = noise(worldPos.xz * 0.018 + vec2(time * 0.006, -time * 0.004));
        fogFactor = clamp(fogFactor * mix(0.82, 1.12, hazeNoise), 0.0, 1.0);
        col = mix(col, uFogColor.rgb, fogFactor * uFogColor.a);
    }

    if (coldGrading > 0.01) {
        float lum = dot(col, vec3(0.299, 0.587, 0.114));
        vec3 coldTint = vec3(lum * 0.93, lum * 0.96, lum * 1.05 + 0.018);
        col = mix(col, mix(col, coldTint, 0.42), coldGrading);
    }

    fragColor = vec4(col, originalColor.a);
}
