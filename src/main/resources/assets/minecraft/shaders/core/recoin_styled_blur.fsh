#version 150

in vec2 fragCoord;
in vec2 pixelCoord;
in vec2 texCoord;
in vec2 rectSize;
in vec4 cornerRadii;
in float guiScale;
in float blurRadius;
in vec2 texelSize;
in vec4 tintColor;
in vec2 resolution;
in vec2 blurDirection;

out vec4 fragColor;

uniform sampler2D Sampler0;

float roundedBoxSDF(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.yz : r.xw;
    r.x = (p.y > 0.0) ? r.y : r.x;

    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

vec4 sampleBlur(vec2 uv, vec2 offset) {
    return texture(Sampler0, clamp(uv + offset, vec2(0.001), vec2(0.999)));
}

vec4 gaussianBlur(vec2 uv, float radius) {
    vec4 col = vec4(0.0);
    float total = 0.0;
    float sampleScale = max(0.0, radius / 24.0);
    float sigma = 6.0;
    vec2 sourceTexelSize = 1.0 / vec2(textureSize(Sampler0, 0));
    vec2 direction = normalize(max(abs(blurDirection), vec2(0.0001)) * sign(blurDirection));

    for (int i = -12; i <= 12; i++) {
        float d = float(i * i);
        float weight = exp(-d / (2.0 * sigma * sigma));
        vec2 offset = direction * float(i) * sourceTexelSize * sampleScale;

        col += sampleBlur(uv, offset) * weight;
        total += weight;
    }

    return col / total;
}

void main() {
    if (abs(blurDirection.x) + abs(blurDirection.y) > 0.5) {
        // The main world framebuffer may legitimately contain zero alpha in
        // sky pixels.  Intermediate blur passes use translucent blending, so
        // forwarding that alpha leaves those pixels unwritten and exposes
        // stale/uninitialised texture contents.  Intermediate images are an
        // opaque colour buffer; the rounded HUD mask supplies final opacity.
        vec4 blurredPass = gaussianBlur(texCoord, blurRadius);
        fragColor = vec4(blurredPass.rgb, 1.0);
        return;
    }

    vec2 halfSize = rectSize * 0.5;
    vec2 center = pixelCoord - halfSize;

    float maxRadius = min(halfSize.x, halfSize.y);
    vec4 rRadii = min(cornerRadii, vec4(maxRadius));

    float dist = roundedBoxSDF(center, halfSize, rRadii);

    float pixelWidth = fwidth(dist);
    float smoothing = max(pixelWidth, 0.5 / guiScale);
    float alpha = 1.0 - smoothstep(-smoothing, smoothing, dist);

    if (alpha < 0.01) {
        discard;
    }

    vec4 blurred = texture(Sampler0, texCoord);

    // Blur tint is emitted with a base alpha of 0.58.  Its alpha is also
    // multiplied by the HUD animation opacity on the CPU, so recover that
    // opacity here and apply it to the actual framebuffer composite too.
    // Without this, text faded correctly while the blurred panel vanished in
    // a single frame at the end of the animation.
    float renderOpacity = clamp(tintColor.a / 0.58, 0.0, 1.0);
    float tintStrength = 0.58;
    bool colorlessBlur = tintColor.r > 0.97 && tintColor.r < 0.99
            && tintColor.g > 0.99 && tintColor.b > 0.99;
    vec3 finalColor = colorlessBlur
            ? blurred.rgb
            : mix(blurred.rgb, tintColor.rgb, tintStrength);

    fragColor = vec4(finalColor, alpha * renderOpacity);
}
