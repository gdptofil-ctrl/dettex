#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D CurrentSampler;
uniform sampler2D HistorySampler;

layout(std140) uniform HandMaskSmoothData {
    vec4 smoothData;
};

const vec2 dirs[8] = vec2[](
    vec2( 1.000,  0.000), vec2( 0.707,  0.707),
    vec2( 0.000,  1.000), vec2(-0.707,  0.707),
    vec2(-1.000,  0.000), vec2(-0.707, -0.707),
    vec2( 0.000, -1.000), vec2( 0.707, -0.707)
);

float sampleChannel(sampler2D source, vec2 uv) {
    return texture(source, clamp(uv, vec2(0.0), vec2(1.0))).r;
}

float radialBlur(sampler2D source, vec2 uv, vec2 px, float radiusPx) {
    float value = sampleChannel(source, uv) * 1.70;
    float weight = 1.70;

    for (int i = 0; i < 8; i++) {
        vec2 direction = dirs[i];
        value += sampleChannel(source, uv + direction * px * radiusPx * 0.45) * 0.70;
        value += sampleChannel(source, uv + direction * px * radiusPx) * 0.30;
        weight += 1.0;
    }

    return value / weight;
}

float historyBlur(vec2 uv, vec2 px, float radiusPx) {
    vec2 softPx = px * (2.1 + radiusPx * 0.16);
    float value = sampleChannel(HistorySampler, uv) * 0.34;
    value += sampleChannel(HistorySampler, uv + vec2( softPx.x, 0.0)) * 0.105;
    value += sampleChannel(HistorySampler, uv + vec2(-softPx.x, 0.0)) * 0.105;
    value += sampleChannel(HistorySampler, uv + vec2(0.0,  softPx.y)) * 0.105;
    value += sampleChannel(HistorySampler, uv + vec2(0.0, -softPx.y)) * 0.105;
    value += sampleChannel(HistorySampler, uv + vec2( softPx.x,  softPx.y)) * 0.045;
    value += sampleChannel(HistorySampler, uv + vec2(-softPx.x, -softPx.y)) * 0.045;
    value += sampleChannel(HistorySampler, uv + vec2( softPx.x, -softPx.y)) * 0.045;
    value += sampleChannel(HistorySampler, uv + vec2(-softPx.x,  softPx.y)) * 0.045;
    return value;
}

void main() {
    vec2 px = 1.0 / max(smoothData.xy, vec2(1.0));
    float radiusPx = smoothData.z;
    float historyFade = smoothData.w;

    float current = radialBlur(CurrentSampler, texCoord, px, radiusPx);
    if (historyFade <= 0.001) {
        float spatial = smoothstep(0.015, 0.86, current);
        fragColor = vec4(spatial, spatial, spatial, spatial);
        return;
    }

    float previous = historyBlur(texCoord, px, radiusPx) * historyFade;
    float source = smoothstep(0.008, 0.72, current);
    float temporal = max(source, previous);
    temporal = clamp(mix(previous, temporal, 0.58), 0.0, 1.0);
    fragColor = vec4(temporal, temporal, temporal, temporal);
}
