#version 150

layout(std140) uniform RectData {
    vec4 rect;
    vec4 screen;
    vec4 radii;
    vec4 colors[9];
};

out vec2 fragCoord;
out vec2 pixelCoord;
out vec2 rectSize;
out vec4 cornerRadii;
flat out vec4 fragColors[9];
out float guiScale;
out float innerBlur;

void main() {
    vec2 positions[6] = vec2[](
        vec2(0.0, 0.0),
        vec2(1.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 1.0)
    );

    vec2 pos = positions[gl_VertexID];

    // Rounded outline rings need samples just outside the mathematical edge.
    // Keep the SDF based on the original rect, but grow only the raster quad
    // by two physical pixels so no side loses its AA coverage.
    float aaPadding = screen.w < 0.0
        ? 2.0 / max(screen.z, 0.001)
        : 0.0;
    vec2 drawOrigin = rect.xy - vec2(aaPadding);
    vec2 drawSize = rect.zw + vec2(aaPadding * 2.0);
    vec2 screenPos = drawOrigin + pos * drawSize;
    vec2 ndcPos = (screenPos / screen.xy) * 2.0 - 1.0;
    ndcPos.y = -ndcPos.y;

    gl_Position = vec4(ndcPos, 0.0, 1.0);
    pixelCoord = pos * drawSize - vec2(aaPadding);
    fragCoord = pixelCoord / rect.zw;
    rectSize = rect.zw;
    cornerRadii = radii;
    guiScale = screen.z;
    innerBlur = screen.w;

    for (int i = 0; i < 9; i++) {
        fragColors[i] = vec4(pow(colors[i].rgb, vec3(2.2)), colors[i].a);
    }
}
