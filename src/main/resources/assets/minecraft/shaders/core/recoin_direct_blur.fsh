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

out vec4 fragColor;

uniform sampler2D Sampler0;

float roundedBoxSDF(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.yz : r.xw;
    r.x = (p.y > 0.0) ? r.y : r.x;

    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

vec4 gaussianBlur(vec2 uv, float radius) {
    vec4 col = vec4(0.0);
    float total = 0.0;

    float sigma = max(radius * 0.5, 0.1);
    float twoSigma2 = 2.0 * sigma * sigma;

    int samples = int(ceil(radius));
    samples = clamp(samples, 1, 10);

    for (int x = -samples; x <= samples; x++) {
        for (int y = -samples; y <= samples; y++) {
            float d = float(x * x + y * y);
            float weight = exp(-d / twoSigma2);

            vec2 offset = vec2(float(x), float(y)) * texelSize;
            vec2 sampleUV = uv + offset;

            sampleUV = clamp(sampleUV, vec2(0.001), vec2(0.999));

            col += texture(Sampler0, sampleUV) * weight;
            total += weight;
        }
    }

    return col / total;
}

void main() {
    vec2 halfSize = rectSize * 0.5;
    vec2 center = pixelCoord - halfSize;

    float maxRadius = min(halfSize.x, halfSize.y);
    vec4 rRadii = min(cornerRadii, vec4(maxRadius));

    float alpha;
    if (cornerRadii.x < 0.0) {
        float innerRadius = -cornerRadii.x;
        float outerRadius = cornerRadii.y;
        float gapRadians = radians(max(cornerRadii.z, 0.0));
        float radius = length(center);
        float angle = atan(center.y, center.x) + 1.57079632679;
        float sectorCount = cornerRadii.w > 0.0 ? cornerRadii.w : 3.0;
        float sector = 6.28318530718 / sectorCount;
        float localAngle = abs(mod(angle + sector * 0.5, sector) - sector * 0.5);
        float angularLimit = sector * 0.5 - gapRadians * 0.5;
        float radialMask = smoothstep(innerRadius - 1.0, innerRadius + 1.0, radius)
                         * (1.0 - smoothstep(outerRadius - 1.0, outerRadius + 1.0, radius));
        float angularMask = 1.0 - smoothstep(angularLimit - 0.012, angularLimit + 0.012, localAngle);
        alpha = radialMask * angularMask;
    } else {
        float dist = roundedBoxSDF(center, halfSize, rRadii);
        float pixelWidth = fwidth(dist);
        float smoothing = max(pixelWidth, 0.5 / guiScale);
        alpha = 1.0 - smoothstep(-smoothing, smoothing, dist);
    }

    if (alpha < 0.01) {
        discard;
    }

    vec4 blurred = gaussianBlur(texCoord, blurRadius);

    bool colorlessBlur = tintColor.r > 0.97 && tintColor.r < 0.99
            && tintColor.g > 0.99 && tintColor.b > 0.99;
    bool neutralBlur = all(greaterThan(tintColor.rgb, vec3(0.99)));
    vec3 finalColor = colorlessBlur
            ? blurred.rgb
            : neutralBlur
                    ? mix(blurred.rgb, vec3(0.0), 0.58)
                    : mix(blurred.rgb, tintColor.rgb, tintColor.a);

    fragColor = vec4(finalColor, alpha);
}
