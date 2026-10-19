#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec2 localCoord;
flat in vec2 halfSize;
flat in float radius;
in vec4 vertexColor;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float r) {
    vec2 q = abs(center) - size + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec4 color = texture(Sampler0, texCoord) * vertexColor * ColorModulator;
    if (radius > 0.0) {
        float r = min(radius, min(halfSize.x, halfSize.y));
        float distance = roundedBoxSDF(localCoord, halfSize, r);
        float smoothing = max(fwidth(distance), 0.75);
        float mask = 1.0 - smoothstep(-smoothing, smoothing, distance);
        if (color.a * mask <= 0.01) {
            discard;
        }
        fragColor = vec4(color.rgb, color.a * mask);
    } else {
        fragColor = vec4(color.rgb, vertexColor.a * ColorModulator.a);
    }
}
