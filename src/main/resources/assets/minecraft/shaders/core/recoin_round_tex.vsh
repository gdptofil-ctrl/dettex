#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

out vec2 texCoord;
out vec2 localCoord;
flat out vec2 halfSize;
flat out float radius;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);

    texCoord = UV0;
    halfSize = vec2(UV2) / 16.0;
    localCoord = (Normal.xy - vec2(0.5)) * halfSize * 2.0;
    radius = Position.z;
    vertexColor = Color;
}
