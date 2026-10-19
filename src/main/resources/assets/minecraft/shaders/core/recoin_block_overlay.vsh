#version 330
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;
out vec2 blockUv;
out vec4 vertexColor;
flat out float overlayOpacity;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;
    // UV0 is a block-local cube-net atlas. Unlike clip/screen coordinates it
    // stays attached to the selected block when the camera or crosshair moves,
    // and every face consumes a different part of one shared pattern.
    blockUv = UV0;
    vertexColor = Color;
    overlayOpacity = clamp(Normal.x, 0.0, 1.0);
}
