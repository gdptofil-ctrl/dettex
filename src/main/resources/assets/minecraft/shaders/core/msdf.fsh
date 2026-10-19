#version 150

in vec2 texCoord;
in vec4 charColor;
in float outlineWidth;
in vec4 outColor;
in float pxRange;
in vec2 atlasSize;

out vec4 fragColor;

uniform sampler2D Sampler0;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float screenPxRange() {
    vec2 dx = dFdx(texCoord) * atlasSize;
    vec2 dy = dFdy(texCoord) * atlasSize;
    float footprint = max(dot(dx, dx) + dot(dy, dy), 0.000001);
    return max(pxRange * inversesqrt(footprint), 1.0);
}

vec2 coverage(vec2 uv, float range) {
    vec3 msd = texture(Sampler0, uv).rgb;
    float distance = range * (median(msd.r, msd.g, msd.b) - 0.5);
    vec2 t = clamp(vec2(distance, distance + outlineWidth) + 0.5, 0.0, 1.0);
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

void main() {
    float range = screenPxRange();
    vec2 alpha = coverage(texCoord, range);
    if (range < 2.5) {
        vec2 dx = dFdx(texCoord) * 0.25;
        vec2 dy = dFdy(texCoord) * 0.25;
        vec2 filtered = (coverage(texCoord + dx + dy, range)
                + coverage(texCoord + dx - dy, range)
                + coverage(texCoord - dx + dy, range)
                + coverage(texCoord - dx - dy, range)) * 0.25;
        alpha = mix(alpha, filtered, 1.0 - smoothstep(1.0, 2.5, range));
    }
    float opacity = alpha.x;

    if (outlineWidth > 0.0) {
        float outlineOpacity = alpha.y;

        vec4 fill = vec4(charColor.rgb, charColor.a * opacity);
        vec4 outline = vec4(outColor.rgb, outColor.a * max(0.0, outlineOpacity - opacity));

        fragColor = fill + outline * (1.0 - fill.a);
    } else {
        fragColor = vec4(charColor.rgb, charColor.a * opacity);
    }

    if (fragColor.a < 0.004) {
        discard;
    }
}
