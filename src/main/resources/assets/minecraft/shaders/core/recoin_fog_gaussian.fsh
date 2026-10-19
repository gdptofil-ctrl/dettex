#version 150

uniform sampler2D MainSampler;
layout(std140) uniform FogBlurPass {
    vec4 DirectionSigma;
};
in vec2 texCoord;
out vec4 fragColor;

vec4 sampleAt(vec2 uv) {
    vec2 halfTexel = 0.5 / vec2(textureSize(MainSampler, 0));
    return texture(MainSampler, clamp(uv, halfTexel, vec2(1) - halfTexel));
}

void main() {
    vec4 sum = sampleAt(texCoord);
    float total = 1.0;
    float denominator = 2.0 * DirectionSigma.z * DirectionSigma.z;
    for (int i = 1; i <= 18; i += 2) {
        if (float(i) > DirectionSigma.w) break;
        float a = float(i);
        float b = a + 1.0;
        float first = exp(-a * a / denominator);
        float second = b <= DirectionSigma.w ? exp(-b * b / denominator) : 0.0;
        float weight = first + second;
        vec2 offset = DirectionSigma.xy * (a + second / weight);
        sum += (sampleAt(texCoord - offset) + sampleAt(texCoord + offset)) * weight;
        total += 2.0 * weight;
    }
    fragColor = sum / total;
}
