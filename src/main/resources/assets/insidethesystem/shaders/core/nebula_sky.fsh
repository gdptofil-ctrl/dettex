#version 330

#ifdef NEBULA_MENU
in vec2 uv;
in float time;
in vec3 pointer;
in vec3 menuTheme;

const vec4 uSettings = vec4(1.15, 0.92, 70.0, 0.0);
#else
layout(std140) uniform SkyShaderData {
    vec4 uResolution;   
    vec4 uSettings;     
    vec4 uCameraDir;    
    vec4 uSkyZenith;
    vec4 uSkyHorizon;
    vec4 uNebColor1;    
    vec4 uNebColor2;    
    vec4 uStarColor;    
    vec4 uFogColor;
    vec4 uFogRange;
};
#endif

out vec4 fragColor;


float hash21(vec2 p) {
    p = fract(p * vec2(443.8975, 397.2973));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float smoothNoise2D(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
#ifdef NEBULA_MENU
    f = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
#else
    f = f * f * (3.0 - 2.0 * f);
#endif
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5, freq = 1.0;
#ifdef NEBULA_MENU
    for (int k = 0; k < 3; k++) {
#else
    for (int k = 0; k < 5; k++) {
#endif
        v += a * smoothNoise2D(p * freq);
        freq *= 2.1;
        a *= 0.48;
    }
#ifdef NEBULA_MENU
    return v * 1.095;
#else
    return v;
#endif
}


vec2 warp(vec2 p, float t) {
    vec2 q = vec2(fbm(p + vec2(0.0, 0.0) + t * 0.12),
                  fbm(p + vec2(5.2, 1.3) + t * 0.09));
    vec2 r = vec2(fbm(p + 4.0 * q + vec2(1.7, 9.2) + t * 0.06),
                  fbm(p + 4.0 * q + vec2(8.3, 2.8) + t * 0.07));
    return p + 1.8 * r;
}


vec3 calcRayDir() {
#ifdef NEBULA_MENU
    vec2 sp = uv * 2.0 - 1.0;
    sp += vec2(pointer.x - 0.5, 0.5 - pointer.y) * 0.10;
    float aspect = abs(dFdy(uv.y) / max(abs(dFdx(uv.x)), 0.000001));
    float tanV = tan(radians(uSettings.z) * 0.5);
    vec3 ray = normalize(vec3(sp.x * tanV * aspect, sp.y * tanV, -1.0));
    return vec3(ray.x, ray.y * 0.5 - ray.z * 0.8660254, ray.y * 0.8660254 + ray.z * 0.5);
#else
    vec2 uv = gl_FragCoord.xy / uResolution.xy;
    vec2 sp = uv * 2.0 - 1.0;
    float aspect = uResolution.x / uResolution.y;
    float tanV = tan(radians(uSettings.z) * 0.5);
    vec3 ray = normalize(vec3(sp.x * tanV * aspect, sp.y * tanV, -1.0));

    vec3 qv = uCameraDir.xyz;
    float qw = uCameraDir.w;
    mat3 rot = mat3(
        1.0 - 2.0*(qv.y*qv.y + qv.z*qv.z), 2.0*(qv.x*qv.y + qv.z*qw), 2.0*(qv.x*qv.z - qv.y*qw),
        2.0*(qv.x*qv.y - qv.z*qw), 1.0 - 2.0*(qv.x*qv.x + qv.z*qv.z), 2.0*(qv.y*qv.z + qv.x*qw),
        2.0*(qv.x*qv.z + qv.y*qw), 2.0*(qv.y*qv.z - qv.x*qw), 1.0 - 2.0*(qv.x*qv.x + qv.y*qv.y)
    );
    return rot * ray;
#endif
}

void main() {
    vec3 dir = normalize(calcRayDir());
#ifdef NEBULA_MENU
    float anim = time * 0.35;
    vec4 uNebColor1 = vec4(menuTheme, 1.0);
    vec4 uNebColor2 = vec4(mix(menuTheme, vec3(1.0), 0.12), 1.0);
    vec4 uStarColor = vec4(mix(menuTheme, vec3(1.0), 0.65), 1.0);
#else
    float anim = uResolution.z * uResolution.w;
#endif

    
    float up = clamp(dir.y, 0.0, 1.0);
    float horizonFade = smoothstep(-0.15, 0.05, dir.y);
    if (horizonFade < 0.005) discard;

    float intensity = uSettings.x;
    float alpha = uSettings.y;

    
    vec3 drift = vec3(0.0, anim * 0.025, anim * 0.04);

    
    vec2 p = dir.xz * 1.0 + drift.xz * 0.9;
    p.y += dir.y * 0.2;

    
    float t = anim * 0.045;
    vec2 wp = warp(p, t);

    
    float n1 = fbm(wp * 0.9 + vec2(0.0, 0.0));
    float n2 = fbm(wp * 1.5 + vec2(2.7, 4.3));
    float n3 = fbm(wp * 0.5 + vec2(-1.8, 3.1));
    float n4 = fbm(wp * 2.2 + vec2(-3.0, 1.2));

    
    float juice = (n1 * 0.4 + n2 * 0.25 + n3 * 0.20 + n4 * 0.15);
    juice = smoothstep(0.15, 0.85, juice); 

    
    float heightMask = 0.7 + 0.3 * smoothstep(0.0, 0.6, up);
    
#ifdef NEBULA_MENU
    float flicker = 0.74 + 0.02 * sin(anim * 0.16 + dir.x * 3.0 + dir.z * 2.0);
#else
    float flicker = 0.8 + 0.2 * sin(anim * 0.5 + dir.x * 14.0 + dir.z * 10.0);
    flicker *= 0.9 + 0.1 * sin(anim * 0.9 + dir.y * 18.0);
#endif
    juice = juice * heightMask * flicker;

    
    float glitter = 0.0;
    for (int i = 0; i < 3; i++) {
        float seed = float(i) + 1.0;
        float longitude = seed * 2.39996323;
        float elevation = mix(0.18, 0.88, fract(seed * 0.381966));
        float horizontal = sqrt(1.0 - elevation * elevation);
        vec3 starDir = vec3(cos(longitude) * horizontal, elevation, sin(longitude) * horizontal);
        float d = acos(clamp(dot(dir, starDir), -1.0, 1.0));
        float sz = 0.007 + 0.003 * (0.5 + 0.5 * sin(seed * 3.1 + anim * 0.3));
        glitter += exp(-d * d / (2.0 * sz * sz)) * 0.6;
    }
    glitter *= (0.7 + 0.3 * sin(anim * 0.6 + p.x * 5.0));

    
    
    float mix1 = 0.5 + 0.5 * sin(anim * 0.06 + p.x * 1.2 + p.y * 0.8 + juice * 1.5);
    float mix2 = 0.5 + 0.5 * cos(anim * 0.08 + p.x * 0.9 - p.y * 1.1 + juice * 2.0);
    vec3 color1 = mix(uNebColor1.rgb, uNebColor2.rgb, mix1);
#ifdef NEBULA_MENU
    vec3 color2 = mix(uNebColor2.rgb, menuTheme, 0.3);
#else
    vec3 color2 = mix(uNebColor2.rgb, vec3(1.0, 0.9, 0.8), 0.3);
#endif
    vec3 color = mix(color1, color2, mix2);

    
    float hot = exp(-abs(juice - 0.4) * 6.0) * 0.6;
#ifdef NEBULA_MENU
    color += mix(menuTheme, vec3(1.0), 0.35) * hot;
#else
    color += vec3(1.0, 0.95, 0.7) * hot;
#endif

    
    color += uStarColor.rgb * glitter * 0.8;

    
    float bloom = exp(-juice * 3.0) * 0.4;
#ifdef NEBULA_MENU
    color += mix(menuTheme, vec3(1.0), 0.15) * bloom * 0.5;
#else
    color += vec3(0.9, 0.8, 1.0) * bloom * 0.5;
#endif

    
    float finalIntensity = juice * 1.3 + hot * 0.3 + glitter * 0.4;
    vec3 finalColor = color * finalIntensity * intensity;

    
    float finalAlpha = clamp(finalIntensity * 1.5, 0.0, 1.0) * alpha * horizonFade;
    
    float vignette = 1.0 - length(dir.xz) * 0.15;
    finalAlpha *= clamp(vignette, 0.0, 1.0);

    if (finalAlpha < 0.01) discard;
#ifdef NEBULA_MENU
    fragColor = vec4(mix(menuTheme * 0.085, clamp(finalColor, 0.0, 1.0), finalAlpha), 1.0);
#else
    fragColor = vec4(clamp(finalColor, 0.0, 1.0), finalAlpha);
#endif
}
