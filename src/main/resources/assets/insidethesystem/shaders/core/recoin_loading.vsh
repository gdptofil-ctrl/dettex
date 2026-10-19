#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

out vec2 uv;
out float time;
out float opacity;
flat out vec3 accent;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    uv = gl_Position.xy / gl_Position.w * 0.5 + 0.5;
    time = float(UV2.x & 65535) / 65535.0 * 60.0;
    opacity = Color.a;
    accent = Color.rgb;
}
