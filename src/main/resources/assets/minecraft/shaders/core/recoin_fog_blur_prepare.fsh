#version 150

uniform sampler2D MainSampler;
uniform sampler2D DepthSampler;
layout(std140) uniform FogBlurData {
    mat4 InverseProjection;
    vec4 Params;
};
in vec2 texCoord;
out vec4 fragColor;

float visibility(vec2 uv) {
    float depth = texture(DepthSampler, uv).r;
    if (depth >= 0.999999) return 1.0;
    vec4 view = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    float distance = length(view.xyz / view.w);
    return smoothstep(1.0, clamp(Params.x * 0.08, 2.0, 4.0), distance);
}

void main() {
    vec2 texel = 1.0 / Params.zw;
    vec4 sum = vec4(0);
    vec2 footprint = 0.25 / vec2(textureSize(MainSampler, 0)) / 0.35;
    for (int x = -1; x <= 1; x += 2) {
        for (int y = -1; y <= 1; y += 2) {
            vec2 uv = clamp(texCoord + vec2(x, y) * footprint, texel * 0.5, vec2(1) - texel * 0.5);
            float weight = visibility(uv);
            sum += vec4(texture(MainSampler, uv).rgb * weight, weight);
        }
    }
    fragColor = sum * 0.25;
}
