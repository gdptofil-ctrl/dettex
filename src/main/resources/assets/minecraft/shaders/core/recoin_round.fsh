#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec2 localCoord;
flat in vec2 halfSize;
flat in vec4 cornerRadii;
flat in float outlineThickness;
in vec4 vertexColor;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, vec4 radii) {
    // radii order: TL, TR, BL, BR
    // Select radius based on which corner we are closest to.
    float r = (center.x > 0.0) ? ((center.y > 0.0) ? radii.w : radii.y)
                               : ((center.y > 0.0) ? radii.z : radii.x);
    float maxR = min(size.x, size.y);
    r = min(r, maxR);
    vec2 q = abs(center) - size + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    float distance = roundedBoxSDF(localCoord, halfSize, cornerRadii);
    float smoothing = max(length(vec2(dFdx(distance), dFdy(distance))) * 0.75, 0.0001);
    float alpha = 1.0 - smoothstep(-smoothing, smoothing, distance);

    if (outlineThickness > 0.0) {
        vec2 innerHalfSize = max(halfSize - vec2(outlineThickness), vec2(0.0));
        vec4 innerRadii = max(cornerRadii - vec4(outlineThickness), vec4(0.0));
        float innerDist = roundedBoxSDF(localCoord, innerHalfSize, innerRadii);
        float innerAlpha = 1.0 - smoothstep(-smoothing, smoothing, innerDist);
        alpha *= (1.0 - innerAlpha);
    }

    vec4 color = vertexColor * ColorModulator;

    if (color.a * alpha <= 0.0005) {
        discard;
    }

    fragColor = vec4(color.rgb, color.a * alpha);
}
