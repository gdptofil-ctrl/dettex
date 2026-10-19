#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV2;

out vec2 localCoord;
out vec2 halfSize;
out float radius;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);

    halfSize = vec2(UV2) / 16.0;
    localCoord = (UV0 - vec2(0.5)) * halfSize * 2.0;
    radius = Position.z;
    vertexColor = Color;
}
