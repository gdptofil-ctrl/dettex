#version 150

uniform sampler2D MenuSampler;

layout(std140) uniform MenuParticlesData {
    vec4 rect;
    vec4 screen;
    vec4 grid;
    vec4 reserved;
};

in vec2 texCoord;
in vec2 sourcePosition;
in float particleAlpha;
out vec4 fragColor;

void main() {
    vec2 halfSize = rect.zw * 0.5;
    float radius = min(screen.z, min(halfSize.x, halfSize.y));
    vec2 q = abs(sourcePosition - halfSize) - halfSize + radius;
    float distance = length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - radius;
    float mask = 1.0 - smoothstep(-0.5, 0.5, distance);
    vec4 color = texture(MenuSampler, texCoord);
    fragColor = vec4(color.rgb, color.a * mask * particleAlpha);
    if (fragColor.a <= 0.001) discard;
}
