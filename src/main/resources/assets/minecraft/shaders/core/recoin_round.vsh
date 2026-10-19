#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV2;

out vec2 localCoord;
flat out vec2 halfSize;
flat out vec4 cornerRadii;
flat out float outlineThickness;
out vec4 vertexColor;

void main() {
    vec2 padding = sign(UV0) * 0.75;
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy + padding, 0.0, 1.0);

    localCoord = UV0 + padding;
    halfSize = abs(UV0);
    outlineThickness = Position.z;

    int uvx = UV2.x & 0xFFFF;
    int uvy = UV2.y & 0xFFFF;
    float rTL = float((uvx >> 8) & 0xFF);
    float rTR = float(uvx & 0xFF);
    float rBL = float((uvy >> 8) & 0xFF);
    float rBR = float(uvy & 0xFF);
    cornerRadii = vec4(rTL, rTR, rBL, rBR) * (min(halfSize.x, halfSize.y) / 255.0);

    vertexColor = Color;
}
