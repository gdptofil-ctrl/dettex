#version 150

in vec2 uv;
in float time;
in vec3 pointer;
out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

vec2 hash22(vec2 p) {
    vec3 a = fract(vec3(p.x, p.y, p.x) * vec3(123.34, 234.34, 345.65));
    a += dot(a, a + 34.45);
    return fract(vec2(a.x * a.y, a.y * a.z));
}

float noise21(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1.0, 0.0)), f.x),
               mix(hash21(i + vec2(0.0, 1.0)), hash21(i + 1.0), f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    mat2 rotation = mat2(0.80, -0.60, 0.60, 0.80);
    for (int i = 0; i < 5; i++) {
        value += noise21(p) * amplitude;
        p = rotation * p * 2.03 + 17.17;
        amplitude *= 0.5;
    }
    return value;
}

float softGlow(vec2 d, float k) {
    return exp(-dot(d, d) * k);
}

void main() {
    vec2 q = vec2(uv.x, 1.0 - uv.y);
    float aspect = clamp(abs(dFdy(uv.y) / max(abs(dFdx(uv.x)), 0.000001)), 0.5, 3.0);
    vec2 p = (q - 0.5) * vec2(aspect, 1.0);
    vec2 mouse = (pointer.xy - 0.5) * vec2(aspect, 1.0);
    float t = time;

    vec2 wp = p + mouse * 0.05;

    vec3 deep = vec3(0.004, 0.006, 0.022);
    vec3 mid = vec3(0.017, 0.014, 0.055);
    vec3 violet = vec3(0.090, 0.020, 0.140);
    vec3 blue = vec3(0.100, 0.420, 1.000);
    vec3 cyan = vec3(0.180, 0.950, 0.880);
    vec3 magenta = vec3(0.620, 0.240, 1.000);

    vec3 col = mix(deep, mid, smoothstep(-0.70, 0.90, q.y));
    col = mix(col, violet * 0.85, smoothstep(0.42, 1.05, q.y));

    vec2 warp = vec2(fbm(wp * 0.90 + vec2(0.0, t * 0.065)),
                     fbm(wp * 0.90 + vec2(7.3, -t * 0.050)));
    vec2 ap = wp + (warp - 0.5) * 0.85;

    float curtain = fbm(vec2(ap.x * 2.25, ap.y * 1.05 - t * 0.135));
    float veil = smoothstep(0.40, 0.98, curtain);
    float core = pow(smoothstep(0.55, 0.97, curtain), 2.4);

    float topFade = 1.0 - smoothstep(0.55, 1.15, q.y);
    float body = smoothstep(-0.95, 0.35, q.y) * topFade;

    vec3 curtainTint = mix(blue, magenta, smoothstep(-0.75, 0.75, ap.x + 0.25 * sin(t * 0.11)));
    curtainTint = mix(curtainTint, cyan, smoothstep(0.35, 0.95, warp.y) * 0.65);

    col += curtainTint * veil * body * 0.58;
    col += mix(cyan, blue, 0.5) * core * body * 0.46;

    float under = fbm(vec2(ap.x * 1.15 - 4.0, ap.y * 0.75 - t * 0.09));
    col += mix(magenta, blue, smoothstep(0.0, 1.0, under)) * smoothstep(0.52, 1.0, under) * body * 0.20;

    vec2 gp = p - vec2(0.0, 0.06);
    col += mix(blue, magenta, 0.30) * softGlow(gp * vec2(0.72, 1.06), 1.15) * 0.30;

    float rays = 0.5 + 0.5 * sin((p.x * 5.5 + p.y * 1.6) + t * 0.30 + warp.x * 4.0);
    rays = pow(rays, 14.0) * (1.0 - smoothstep(0.05, 0.95, abs(p.x)));
    col += mix(blue, cyan, 0.35) * rays * 0.07;

    vec2 sc = wp * 62.0;
    vec2 sid = floor(sc);
    vec2 sf = fract(sc) - 0.5;
    float chance = hash21(sid);
    if (chance > 0.986) {
        float tw = 0.45 + 0.55 * sin(t * (1.5 + chance * 7.0) + chance * 50.0);
        col += vec3(0.75, 0.85, 1.0) * exp(-dot(sf, sf) * 36.0) * tw * 0.9;
    }

    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        vec2 layer = wp * (1.7 + fi * 1.4);
        layer.y += t * (0.045 + fi * 0.03);
        vec2 cell = floor(layer);
        vec2 f = fract(layer) - 0.5;
        vec2 rnd = hash22(cell + fi * 21.0);
        if (rnd.x > 0.82) {
            float d = length(f - (rnd - 0.5) * 0.55);
            float size = 0.16 + rnd.y * 0.26;
            float mote = 1.0 - smoothstep(0.0, size, d);
            float tw = 0.55 + 0.45 * sin(t * 1.4 + rnd.y * 22.0);
            col += mix(cyan, magenta, rnd.y) * mote * tw * (0.05 + fi * 0.02);
        }
    }

    vec2 fromMouse = p - mouse;
    col += blue * softGlow(fromMouse, 5.0) * 0.20;
    col += mix(cyan, blue, 0.5) * softGlow(fromMouse, 26.0) * 0.10;

    float vig = 1.0 - smoothstep(0.30, 1.30, length(p * vec2(0.62, 1.0)));
    col *= 0.55 + vig * 0.55;

    col = col / (col + vec3(0.90));
    col = pow(max(col, 0.0), vec3(0.85));

    float grain = hash21(gl_FragCoord.xy + fract(t) * 57.0) - 0.5;
    col += grain / 640.0;

    fragColor = vec4(col, 1.0);
}
