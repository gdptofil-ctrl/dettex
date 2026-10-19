#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 localCoord;
in vec2 halfSize;
in float radius;
in vec4 vertexColor;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float r) {
    vec2 q = abs(center) - size + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

vec4 blur9(vec2 uv) {
    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec4 color = texture(Sampler0, uv) * 0.227027;

    color += texture(Sampler0, uv + vec2(texel.x * 2.0, 0.0)) * 0.1216216;
    color += texture(Sampler0, uv - vec2(texel.x * 2.0, 0.0)) * 0.1216216;
    color += texture(Sampler0, uv + vec2(0.0, texel.y * 2.0)) * 0.1216216;
    color += texture(Sampler0, uv - vec2(0.0, texel.y * 2.0)) * 0.1216216;

    color += texture(Sampler0, uv + vec2(texel.x * 5.0, texel.y * 5.0)) * 0.071351;
    color += texture(Sampler0, uv + vec2(-texel.x * 5.0, texel.y * 5.0)) * 0.071351;
    color += texture(Sampler0, uv + vec2(texel.x * 5.0, -texel.y * 5.0)) * 0.071351;
    color += texture(Sampler0, uv - vec2(texel.x * 5.0, texel.y * 5.0)) * 0.071351;
    return color;
}

void main() {
    float r = min(radius, min(halfSize.x, halfSize.y));
    float distance = roundedBoxSDF(localCoord, halfSize, r);
    float mask = 1.0 - smoothstep(-1.0, 1.5, distance);

    if (mask <= 0.01) {
        discard;
    }

    vec2 texCoord = gl_FragCoord.xy / vec2(textureSize(Sampler0, 0));
    texCoord.y = 1.0 - texCoord.y;
    vec4 blurred = blur9(texCoord);
    vec4 tint = vertexColor * ColorModulator;
    vec3 rgb = mix(blurred.rgb, tint.rgb, tint.a);
    float alpha = mask;

    fragColor = vec4(rgb, alpha);
}
