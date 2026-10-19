#version 150

uniform sampler2D MainSampler;

layout(std140) uniform ContrastData {
    vec4 Params;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(MainSampler, texCoord);
    color.rgb = clamp(color.rgb + max(color.rgb - vec3(0.5), vec3(0.0)) * Params.x, vec3(0.0), vec3(1.0));
    fragColor = color;
}
