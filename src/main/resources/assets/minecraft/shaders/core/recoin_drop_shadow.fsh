#version 150

in vec2 vLocalPx;
in vec2 vSize;
in vec2 vSourceSize;
in vec2 vShadowParams;
in vec4 vRadii;
in vec4 vColor;
out vec4 fragColor;

float cornerRadius(vec2 p, vec4 radii) {
    float right = step(0.0, p.x);
    float bottom = step(0.0, p.y);
    float top = mix(radii.x, radii.y, right);
    float bottomRadius = mix(radii.w, radii.z, right);
    return mix(top, bottomRadius, bottom);
}

float sdRoundBox(vec2 p, vec2 halfSize, vec4 radii) {
    vec4 safeRadii = max(radii, vec4(0.0));
    float minHalf = min(halfSize.x, halfSize.y);
    float minR = min(min(safeRadii.x, safeRadii.y), min(safeRadii.z, safeRadii.w));
    if (minR >= minHalf - 0.5) {
        vec2 offset = max(halfSize - vec2(minHalf), vec2(0.0));
        return length(max(abs(p) - offset, vec2(0.0))) - minHalf;
    }
    vec4 clampedRadii = min(safeRadii, vec4(minHalf));
    float radius = cornerRadius(p, clampedRadii);
    vec2 inset = max(halfSize - vec2(radius), vec2(0.0));
    vec2 q = abs(p) - inset;
    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 resolvedInner = vec2(
        vSourceSize.x > 0.0 ? vSourceSize.x : vSize.x,
        vSourceSize.y > 0.0 ? vSourceSize.y : vSize.y
    );
    vec2 innerHalf = max(resolvedInner * 0.5, vec2(0.0));
    vec2 pad = 0.5 * (vSize - resolvedInner);
    vec2 center = pad + innerHalf;
    vec2 local = vLocalPx - center;

    float distInner = sdRoundBox(local, innerHalf, max(vRadii, vec4(0.0)));
    float aaInner = max(fwidth(distInner), 1e-4);
    float innerMask = clamp((distInner + aaInner) / aaInner, 0.0, 1.0);
    if (innerMask <= 0.0) discard;

    float blur = max(vShadowParams.x, 1e-3);
    float spread = max(vShadowParams.y, 0.0);
    float distFromSpread = max(distInner - spread, 0.0);
    float norm = distFromSpread / blur;
    float gaussian = exp(-0.5 * norm * norm);

    float limit = blur * 3.0;
    float outerMask = 1.0;
    if (limit > 0.0) {
        float aaOuter = max(fwidth(distFromSpread), 1e-4);
        outerMask = 1.0 - smoothstep(limit - aaOuter, limit + aaOuter, distFromSpread);
    }

    float alpha = vColor.a * gaussian * innerMask * outerMask;
    if (alpha <= 0.001) discard;
    fragColor = vec4(vColor.rgb * alpha, alpha);
}
