#version 150

layout(std140) uniform BloodScene {
    mat4 ViewProjection;
    vec4 SunLight;
    vec4 CameraModulo;
};

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

out vec3 vPosition;
out vec3 vNormal;
out vec4 vColor;
out vec2 vMaterial;

void main() {
    gl_Position = ViewProjection * vec4(Position, 1.0);
    vPosition = Position;
    vNormal = Normal;
    vColor = Color;
    vMaterial = UV0;
}
