#version 150

in vec2 fragCoord;
in vec2 pixelCoord;
in vec2 rectSize;
in vec4 cornerRadii;
flat in vec4 fragColors[9];
in float guiScale;
in float innerBlur;

out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.yz : r.xw;
    r.x = (p.y > 0.0) ? r.y : r.x;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

vec3 toLinear(vec3 srgb) {
    return pow(srgb, vec3(2.2));
}

vec3 toSRGB(vec3 linear) {
    return pow(linear, vec3(1.0 / 2.2));
}

vec4 toLinear(vec4 srgb) {
    return vec4(toLinear(srgb.rgb), srgb.a);
}

vec4 toSRGB(vec4 linear) {
    return vec4(toSRGB(linear.rgb), linear.a);
}

float smoothInterpolate(float t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

vec4 sampleGradientAt(vec2 uv) {
    float x = smoothInterpolate(clamp(uv.x, 0.0, 1.0));
    float y = smoothInterpolate(clamp(uv.y, 0.0, 1.0));
    vec4 c0 = fragColors[0];
    vec4 c1 = fragColors[1];
    vec4 c2 = fragColors[2];
    vec4 c3 = fragColors[3];
    vec4 c4 = fragColors[4];
    vec4 c5 = fragColors[5];
    vec4 c6 = fragColors[6];
    vec4 c7 = fragColors[7];
    vec4 c8 = fragColors[8];
    vec4 top = mix(mix(c0, c1, x), mix(c1, c2, x), x);
    vec4 middle = mix(mix(c3, c4, x), mix(c4, c5, x), x);
    vec4 bottom = mix(mix(c6, c7, x), mix(c7, c8, x), x);
    return mix(mix(top, middle, y), mix(middle, bottom, y), y);
}

vec4 sampleGradient(vec2 uv) {
    if (innerBlur <= 0.0) return toSRGB(sampleGradientAt(uv));
    float blur = innerBlur * 0.01;
    vec4 sum = sampleGradientAt(uv) * 4.0;
    float weight = 4.0;
    sum += sampleGradientAt(uv + vec2(-blur, 0.0)) * 2.0;
    sum += sampleGradientAt(uv + vec2(blur, 0.0)) * 2.0;
    sum += sampleGradientAt(uv + vec2(0.0, -blur)) * 2.0;
    sum += sampleGradientAt(uv + vec2(0.0, blur)) * 2.0;
    weight += 8.0;
    sum += sampleGradientAt(uv + vec2(-blur, -blur));
    sum += sampleGradientAt(uv + vec2(blur, -blur));
    sum += sampleGradientAt(uv + vec2(-blur, blur));
    sum += sampleGradientAt(uv + vec2(blur, blur));
    weight += 4.0;
    return toSRGB(sum / weight);
}

float dither(vec2 coord) {
    return fract(sin(dot(coord, vec2(12.9898, 78.233))) * 43758.5453) / 255.0;
}

void main() {
    vec2 halfSize = rectSize * 0.5;
    vec2 center = pixelCoord - halfSize;
    vec4 radii = min(cornerRadii, vec4(min(halfSize.x, halfSize.y)));
    float dist = roundedBoxSDF(center, halfSize, radii);
    float smoothing = max(fwidth(dist), 0.5 / guiScale);
    float alpha = 1.0 - smoothstep(-smoothing, smoothing, dist);

    if (innerBlur < 0.0) {
        float thickness = max(-innerBlur, 0.25);
        vec2 innerHalfSize = max(halfSize - vec2(thickness), vec2(0.0));
        vec4 innerRadii = max(radii - vec4(thickness), vec4(0.0));
        float innerDist = roundedBoxSDF(center, innerHalfSize, innerRadii);
        float innerAlpha = 1.0 - smoothstep(-smoothing, smoothing, innerDist);
        alpha *= 1.0 - innerAlpha;
    }

    if (alpha < 0.001) discard;

    vec4 color = sampleGradient(fragCoord);
    color.rgb += dither(gl_FragCoord.xy) - 0.5 / 255.0;
    fragColor = vec4(color.rgb, color.a * alpha);
}
