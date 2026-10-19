#version 330

in vec2 blockUv;
in vec4 vertexColor;
flat in float overlayOpacity;
out vec4 fragColor;

float hash31(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

float recoinNoise3(vec3 p) {
    vec3 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash31(i), hash31(i + vec3(1,0,0)), f.x),
                   mix(hash31(i + vec3(0,1,0)), hash31(i + vec3(1,1,0)), f.x), f.y),
               mix(mix(hash31(i + vec3(0,0,1)), hash31(i + vec3(1,0,1)), f.x),
                   mix(hash31(i + vec3(0,1,1)), hash31(i + vec3(1,1,1)), f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.52;
    mat3 rotation = mat3(0.00, 0.80, 0.60, -0.80, 0.36, -0.48, -0.60, -0.48, 0.64);
    for (int i = 0; i < 5; i++) {
        value += amplitude * recoinNoise3(p);
        p = rotation * p * 2.03 + vec3(3.1, 5.7, 1.9);
        amplitude *= 0.48;
    }
    return value;
}

vec3 ribbons(vec2 p, float t, vec3 base) {
    p *= 7.4;
    float warp = fbm(vec3(p * 0.45, t * 0.09)) * 2.0 - 1.0;
    float a = pow(max(0.0, 1.0 - abs(sin((p.x + p.y * 0.55) * 2.2 + t * 2.8 + warp * 2.1))), 5.0);
    float b = pow(max(0.0, 1.0 - abs(sin((p.x * 0.55 - p.y) * 2.8 - t * 2.1 + warp * 1.7))), 6.0);
    return base * 0.16 + vec3(0.30, 1.45, 2.30) * a + vec3(1.85, 0.48, 2.55) * b;
}

vec3 liquid(vec2 p, float t, vec3 base) {
    p *= 6.0;
    float flow = fbm(vec3(p + vec2(t * 0.38, -t * 0.31), 1.7));
    p += vec2(sin(p.y * 1.8 + t * 1.35 + flow * 2.0), cos(p.x * 1.6 - t * 1.1 - flow * 1.7)) * 0.42;
    float current = fbm(vec3(p * 2.1, t * 0.22));
    float vein = pow(max(0.0, sin((p.x + p.y) * 6.8 + current * 5.4 - t * 2.6)), 3.2);
    return mix(base * 0.35, vec3(0.18, 1.05, 1.95), current) + vec3(1.5, 0.62, 2.55) * vein * 0.3;
}

vec3 auroraFill(vec2 p, float t, vec3 base) {
    p *= vec2(5.4, 6.2);
    float haze = fbm(vec3(p * 0.72, t * 0.12));
    float curtain = 0.0;
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float wave = sin(p.x * (1.25 + fi * 0.34) + t * (0.72 + fi * 0.28) + haze * 2.7 + fi * 1.9) * 0.42;
        curtain += exp(-abs(p.y * 0.72 + wave + fi * 0.34 - 0.42) * (2.9 + fi * 0.55)) * (0.58 - fi * 0.09);
    }
    return base * 0.14 + mix(vec3(0.18, 1.25, 1.95), vec3(1.32, 0.48, 2.28), haze) * curtain;
}

vec3 plasmaFill(vec2 p, float t, vec3 base) {
    p *= 6.4;
    float warp = fbm(vec3(p * 0.58, t * 0.16));
    p += vec2(sin(p.y * 0.95 + t * 0.82 + warp * 3.1), cos(p.x * 0.88 - t * 0.76 + warp * 2.4)) * 0.52;
    float cell = 0.5 + 0.5 * sin(sin(p.x * 1.65 + t * 1.55) + sin(p.y * 1.9 - t * 1.2) + sin((p.x + p.y) * 1.12 + warp * 4.0));
    float veins = pow(max(0.0, sin(length(p) * 5.0 - t * 2.9 + warp * 5.8)), 4.2);
    return base * 0.12 + mix(vec3(0.16, 0.85, 2.20), vec3(1.82, 0.42, 2.38), cell) * (0.34 + cell * 0.88) + veins * vec3(2.1, 1.32, 0.36) * 0.22;
}

void main() {
    vec3 p = vec3(blockUv * 5.5, 0.0);
    float cloud = recoinNoise3(p) * 0.62 + recoinNoise3(p * 2.13 + 7.4) * 0.38;
    float veins = pow(max(0.0, 1.0 - abs(cloud - 0.52) * 4.2), 2.0);
    float time = vertexColor.a * 6.28318530718;
    vec3 base = max(vertexColor.rgb, vec3(0.015));
    vec3 bright = min(vec3(1.0), base * 1.72 + vec3(0.07));
    vec3 color;
    float alpha;

#if OVERLAY_MODE == 1
    color = base;
    alpha = 0.34;
#elif OVERLAY_MODE == 2
    float grid = pow(0.5 + 0.5 * sin((blockUv.x + blockUv.y) * 25.0 - time), 3.0);
    color = mix(base * 0.28, bright, 0.34 + grid * 0.66);
    alpha = 0.24 + grid * 0.24;
#elif OVERLAY_MODE == 3
    vec3 warped = vec3(blockUv * 1.65, 0.0) + vec3(cos(time), sin(time * 0.73), -time * 0.14);
    float domain = fbm(warped + vec3(fbm(warped * 1.7)) * 1.55);
    float cloudBand = smoothstep(0.31, 0.78, domain);
    float filament = pow(max(0.0, 1.0 - abs(fbm(warped * 3.4) - 0.52) * 5.0), 2.6);
    vec3 violet = mix(base * 0.20, vec3(0.44, 0.11, 0.96), 0.54);
    vec3 cyan = mix(bright, vec3(0.12, 0.88, 1.0), 0.46);
    color = mix(violet, cyan, cloudBand) + filament * cyan * 0.48;
    alpha = 0.20 + cloudBand * 0.26 + filament * 0.12;
#elif OVERLAY_MODE == 4
    color = ribbons(blockUv, time, base);
    alpha = 0.34;
#elif OVERLAY_MODE == 5
    color = liquid(blockUv, time, base);
    alpha = 0.34;
#elif OVERLAY_MODE == 6
    color = auroraFill(blockUv, time, base);
    alpha = 0.34;
#elif OVERLAY_MODE == 7
    // Alpha DLC Energy: a dense animated field of bright flowing filaments.
    float t = time * 0.5 + 23.0;
    vec2 energyUv = blockUv * 2.0;
    vec2 ep = mod(energyUv * 6.28318, 6.28318) - 250.0;
    vec2 ei = ep;
    float energy = 1.0;
    const float intensity = 0.005;
    for (int n = 0; n < 5; n++) {
        float tn = t * (1.0 - 3.5 / float(n + 1));
        ei = ep + vec2(cos(tn - ei.x) + sin(tn + ei.y),
                       sin(tn - ei.y) + cos(tn + ei.x));
        energy += 1.0 / length(vec2(
                ep.x / (sin(ei.x + tn) / intensity),
                ep.y / (cos(ei.y + tn) / intensity)));
    }
    energy /= 5.0;
    energy = 1.17 - pow(energy, 1.4);
    float glowEnergy = pow(abs(energy), 8.0);
    color = mix(base, vec3(1.0), glowEnergy * 0.7);
    alpha = 0.25 + glowEnergy * 1.5;
#else
    color = base;
    alpha = 0.0;
#endif
    fragColor = vec4(color, alpha * overlayOpacity);
}
