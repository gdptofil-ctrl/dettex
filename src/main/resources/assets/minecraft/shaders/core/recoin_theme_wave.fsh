#version 150

in vec2 localPos;
in vec2 size;
in float progress;
in float time;
in vec4 waveColor;
out vec4 fragColor;

float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1, 0)), f.x),
               mix(hash21(i + vec2(0, 1)), hash21(i + vec2(1)), f.x), f.y);
}

void main() {
    vec2 q = localPos / max(size, vec2(1.0));
    // The crest itself evolves while travelling: several waves move at
    // different speeds instead of translating one rigid vertical stripe.
    float smallWaves = sin(q.y * 31.0 + time * 5.2 + progress * 9.0) * 0.018
                     + sin(q.y * 71.0 - time * 3.35 + progress * 17.0) * 0.008;
    float swell = sin(q.y * 8.0 - time * 1.72 + progress * 5.0) * 0.048
                + sin(q.y * 3.1 + time * 0.9) * 0.025;

    // The lower-left edge enters first, then a curved sea crest crosses the screen.
    float coordinate = q.x + (1.0 - q.y) * 0.20 + smallWaves + swell;
    float front = progress * 1.42 - 0.025;
    float d = coordinate - front;

    float crestWidth = 0.010 + 0.007 * (0.5 + 0.5 * sin(q.y * 13.0 - time * 2.4));
    float crest = exp(-pow(d / crestWidth, 2.0));
    float innerGlow = exp(-pow((d + 0.018) / 0.034, 2.0));
    float rollingBody = exp(-pow((d + 0.075) / 0.095, 2.0));
    float backRipple = exp(-pow((d + 0.165 + sin(q.y * 48.0 - time * 4.0) * 0.012) / 0.055, 2.0));

    float detail = noise(vec2(q.y * 145.0, floor(time * 12.0))) * 0.65
                 + noise(vec2(q.y * 53.0 + time, time * 2.0)) * 0.35;
    float spray = crest * smoothstep(0.73, 1.0, detail);
    float life = smoothstep(0.0, 0.08, progress) * smoothstep(1.0, 0.88, progress);

    float alpha = (crest * 0.72 + innerGlow * 0.34 + rollingBody * 0.14
                 + backRipple * 0.07 + spray * 0.35) * life * waveColor.a;
    if (alpha < 0.003) discard;

    vec3 deepColor = waveColor.rgb * vec3(0.38, 0.56, 0.92);
    vec3 color = mix(deepColor, waveColor.rgb, innerGlow + rollingBody * 0.35);
    color = mix(color, vec3(0.90, 0.97, 1.0), clamp(crest * 0.82 + spray, 0.0, 1.0));
    fragColor = vec4(color, alpha);
}
