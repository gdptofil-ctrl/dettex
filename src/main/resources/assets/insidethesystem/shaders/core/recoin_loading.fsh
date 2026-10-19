#version 330

in vec2 uv;
in float time;
in float opacity;
flat in vec3 accent;
out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(cell), hash21(cell + vec2(1.0, 0.0)), f.x),
               mix(hash21(cell + vec2(0.0, 1.0)), hash21(cell + 1.0), f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    mat2 turn = mat2(0.8, -0.6, 0.6, 0.8);
    for (int i = 0; i < 4; i++) {
        value += noise(p) * amplitude;
        p = turn * p * 2.03 + 7.1;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    float aspect = abs(dFdy(uv.y)) / max(abs(dFdx(uv.x)), 0.000001);
    vec2 p = (uv - 0.5) * vec2(aspect, 1.0);
    float t = time * 0.06;
    vec2 flow = vec2(fbm(p * 2.3 + vec2(t, -t * 0.4)),
                     fbm(p * 2.1 + vec2(4.7, t * 0.6)));
    float cloud = fbm(p * 3.0 + flow * 2.0 + vec2(-t * 0.3, t * 0.2));
    float ribbon = exp(-abs(p.y + sin(p.x * 2.4 + t) * 0.18 + (cloud - 0.5) * 0.5) * 7.0);
    vec3 theme = clamp(accent, 0.0, 1.0);
    vec3 lightTheme = mix(theme, vec3(0.82, 0.87, 1.0), 0.30);
    vec3 color = mix(vec3(0.012, 0.014, 0.024) + theme * 0.025,
                     vec3(0.025, 0.020, 0.045) + theme * 0.16, cloud);
    color += mix(theme * 0.52, lightTheme * 0.72, flow.x) * ribbon * 0.8;
    color += theme * exp(-dot(p, p) * 5.0) * (0.24 + 0.04 * sin(time * 0.8));
    vec2 starCell = floor(p * 190.0);
    vec2 starPosition = fract(p * 190.0) - 0.5;
    float seed = hash21(starCell);
    float star = exp(-dot(starPosition, starPosition) * 100.0) * step(0.992, seed);
    color += lightTheme * star * (0.12 + 0.12 * sin(time * 0.55 + seed * 60.0));
    color *= 1.0 - smoothstep(0.3, 1.2, length(p)) * 0.45;
    fragColor = vec4(color, opacity);
}
