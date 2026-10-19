#version 150
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 glowCoord;
out vec4 glowColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    glowCoord = UV0;
    glowColor = Color;
}
