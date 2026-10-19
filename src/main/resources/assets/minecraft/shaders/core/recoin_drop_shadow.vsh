#version 150

layout(std140) uniform ShadowData {
    vec4 rect;
    vec4 screen;
    vec4 source;
    vec4 radii;
    vec4 color;
};

out vec2 vLocalPx;
out vec2 vSize;
out vec2 vSourceSize;
out vec2 vShadowParams;
out vec4 vRadii;
out vec4 vColor;

void main() {
    vec2 positions[6] = vec2[](
        vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0),
        vec2(0.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 1.0)
    );
    vec2 pad = (rect.zw - source.xy) * 0.5;
    float maxRadius = min(max(max(radii.x, radii.y), max(radii.z, radii.w)), min(source.x, source.y) * 0.5);
    float inset = max(maxRadius, 0.0) + 2.0 * (screen.z + screen.w);
    vec2 holeMin = pad + vec2(inset);
    vec2 holeMax = rect.zw - pad - vec2(inset);
    int strip = gl_VertexID / 6;
    vec2 origin = vec2(0.0);
    vec2 size = rect.zw;
    if (all(greaterThan(holeMax, holeMin))) {
        if (strip == 0) {
            size.y = holeMin.y;
        } else if (strip == 1) {
            origin.y = holeMax.y;
            size.y -= holeMax.y;
        } else if (strip == 2) {
            origin.y = holeMin.y;
            size = vec2(holeMin.x, holeMax.y - holeMin.y);
        } else {
            origin = vec2(holeMax.x, holeMin.y);
            size = vec2(rect.z - holeMax.x, holeMax.y - holeMin.y);
        }
    } else if (strip != 0) {
        size = vec2(0.0);
    }
    vec2 local = origin + positions[gl_VertexID % 6] * size;
    vec2 posPx = rect.xy + local;
    vec2 ndc = vec2((posPx.x / screen.x) * 2.0 - 1.0,
                    1.0 - (posPx.y / screen.y) * 2.0);
    gl_Position = vec4(ndc, 0.0, 1.0);
    vLocalPx = local;
    vSize = rect.zw;
    vSourceSize = source.xy;
    vShadowParams = source.zw;
    vRadii = radii;
    vColor = color;
}
