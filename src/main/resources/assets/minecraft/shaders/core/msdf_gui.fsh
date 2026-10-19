#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec4 charColor;
in float pxRange;

out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float screenPxRange() {
    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    float range = pxRange > 0.1 ? pxRange : 4.0;
    vec2 dx = dFdx(texCoord) * atlasSize;
    vec2 dy = dFdy(texCoord) * atlasSize;
    float footprint = max(dot(dx, dx) + dot(dy, dy), 0.000001);
    return max(range * inversesqrt(footprint), 1.0);
}

float coverage(vec2 uv, float range) {
    vec3 msd = texture(Sampler0, uv).rgb;
    float t = clamp(range * (median(msd.r, msd.g, msd.b) - 0.5) + 0.5, 0.0, 1.0);
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

void main() {
#ifdef MSDF_GLOW
    vec3 msd = texture(Sampler0, texCoord).rgb;
    float sdf = median(msd.r, msd.g, msd.b);
    float distance = max(0.5 - sdf, 0.0);
    float opacity = exp(-distance * distance / 0.045) * smoothstep(0.0, 0.12, sdf);
#else
    float range = screenPxRange();
    float opacity = coverage(texCoord, range);
    if (range < 2.5) {
        vec2 dx = dFdx(texCoord) * 0.25;
        vec2 dy = dFdy(texCoord) * 0.25;
        float filtered = (coverage(texCoord + dx + dy, range)
                + coverage(texCoord + dx - dy, range)
                + coverage(texCoord - dx + dy, range)
                + coverage(texCoord - dx - dy, range)) * 0.25;
        opacity = mix(opacity, filtered, 1.0 - smoothstep(1.0, 2.5, range));
    }
#endif
    vec4 color = charColor * ColorModulator;

    fragColor = vec4(color.rgb, color.a * opacity);
    if (fragColor.a < 0.004) {
        discard;
    }
}
