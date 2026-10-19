#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

layout(std140) uniform StardustSky {
    vec4 u_SkyPrimary;
    vec4 u_SkySecondary;
    vec4 u_CameraWeather;
    vec4 u_ResolutionTime;
    vec4 u_SkyParams;
    mat4 u_viewProjectionInverse;
    ivec4 u_ModeFlags;
};

#define u_cameraDir u_CameraWeather.xyz
#define u_rainGradient u_CameraWeather.w
#define u_resolution u_ResolutionTime.xy
#define u_time u_ResolutionTime.z
#define u_timeOfDay u_ResolutionTime.w
#define u_intensity u_SkyParams.x
#define u_starDensity u_SkyParams.y
#define u_mode u_ModeFlags.x

in vec3 vDir;
in vec3 vProjectionDir;

out vec4 fragColor;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;
const int AURORA = 0;
const int STARDUST = 1;
const int TWILIGHT_RAYLEIGH = 2;
const int QUANTUM_NEBULA = 3;
const int CHRONOS_SINGULARITY = 4;

float sky_saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

vec3 sky_saturate3(vec3 value) {
    return clamp(value, vec3(0.0), vec3(1.0));
}

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float hash31(vec3 p) {
    p = fract(p * vec3(0.1031, 0.11369, 0.13787));
    p += dot(p, p.yzx + 19.19);
    return fract((p.x + p.y) * p.z);
}

float sky_noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float sky_noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);
    float n000 = hash31(i + vec3(0.0, 0.0, 0.0));
    float n100 = hash31(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash31(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash31(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash31(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash31(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash31(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash31(i + vec3(1.0, 1.0, 1.0));
    float nx00 = mix(n000, n100, u.x);
    float nx10 = mix(n010, n110, u.x);
    float nx01 = mix(n001, n101, u.x);
    float nx11 = mix(n011, n111, u.x);
    float nxy0 = mix(nx00, nx10, u.y);
    float nxy1 = mix(nx01, nx11, u.y);
    return mix(nxy0, nxy1, u.z);
}

float fbm3(vec3 p) {
    float value = 0.0;
    float amp = 0.52;
    for (int i = 0; i < 4; i++) {
        value += sky_noise3(p) * amp;
        p = p * 2.031 + vec3(17.17, -11.31, 23.83);
        amp *= 0.52;
    }
    return value;
}

float fbm2(vec2 p) {
    float value = 0.0;
    float amp = 0.55;
    mat2 m = mat2(1.74, -1.08, 1.08, 1.74);
    for (int i = 0; i < 4; i++) {
        value += sky_noise2(p) * amp;
        p = m * p + vec2(9.7, -13.1);
        amp *= 0.50;
    }
    return value;
}

vec3 sky_exposureTone(vec3 color, float exposure) {
    return vec3(1.0) - exp(-max(color, vec3(0.0)) * exposure);
}

float rayleighPhase(float mu) {
    return 0.05968310366 * (1.0 + mu * mu);
}

float miePhase(float mu, float g) {
    float g2 = g * g;
    return 0.11936620732 * ((1.0 - g2) * (1.0 + mu * mu)) / ((2.0 + g2) * pow(max(0.035, 1.0 + g2 - 2.0 * g * mu), 1.5));
}

vec3 sunDirection() {
    float a = u_timeOfDay * TAU - PI * 0.48;
    float low = sin(a) * 0.18 - 0.045;
    return normalize(vec3(cos(a) * 0.78, low, sin(a) * 0.62));
}

float starLayer(vec3 d, float scale, float threshold, float brightness) {
    vec3 g = d * scale;
    vec3 cell = floor(g);
    vec3 local = fract(g) - 0.5;
    float h = hash31(cell);
    float alive = smoothstep(threshold, 1.0, h);
    vec2 p = local.xy + local.z * vec2(0.37, -0.29);
    float r = dot(p, p);
    float core = exp(-r * mix(900.0, 1700.0, h));
    float halo = exp(-r * mix(42.0, 96.0, h));
    float rare = smoothstep(0.996, 1.0, h);
    float spike = (exp(-abs(p.x) * 120.0) * exp(-abs(p.y) * 15.0) + exp(-abs(p.y) * 120.0) * exp(-abs(p.x) * 15.0)) * rare;
    float twinkle = 0.72 + 0.28 * sin(u_time * 9.0 + h * 73.0);
    return (core * 2.2 + halo * 0.34 + spike * 0.56) * alive * twinkle * brightness;
}

vec3 stellarColor(vec3 d) {
    float h = hash31(floor(d * 93.0));
    vec3 cold = vec3(0.62, 0.78, 1.00);
    vec3 warm = vec3(1.00, 0.78, 0.48);
    vec3 pearl = vec3(0.92, 0.96, 1.00);
    return mix(mix(cold, warm, smoothstep(0.16, 0.86, h)), pearl, 0.26);
}

vec3 moonDirection() {
    vec3 s = sunDirection();
    return normalize(vec3(-s.x, max(-s.y, 0.12), -s.z));
}

vec4 moonLayer(vec3 d, vec3 primary, vec3 secondary, out float diskMask) {
    diskMask = 0.0;
    vec3 m = moonDirection();
    float cosA = clamp(dot(d, m), -1.0, 1.0);
    float rr = sqrt(max(2.0 * (1.0 - cosA), 0.0)) / 0.052;
    if (rr >= 5.0) {
        return vec4(0.0);
    }
    vec3 axisRight = normalize(cross(vec3(0.0, 1.0, 0.0), m));
    vec3 axisUp = cross(m, axisRight);
    vec2 mp = vec2(dot(d, axisRight), dot(d, axisUp)) / 0.052;
    float disk = 1.0 - smoothstep(0.90, 1.0, rr);
    diskMask = disk;
    float surf = sky_noise2(mp * 3.1 + 4.7) * 0.6 + sky_noise2(mp * 7.3 - 2.9) * 0.4;
    float shade = 0.74 + 0.26 * smoothstep(0.25, 0.78, surf);
    float limb = 1.0 - 0.34 * smoothstep(0.45, 1.0, rr);
    vec3 moonCol = mix(vec3(0.93, 0.96, 1.04), secondary, 0.16) * shade * limb;
    float halo = exp(-max(rr - 1.0, 0.0) * 1.7);
    vec3 haloCol = mix(vec3(0.72, 0.80, 1.00), primary, 0.30);
    return vec4(moonCol * disk * 1.25 + haloCol * halo * 0.22, disk * 0.92 + halo * 0.20);
}

vec4 auroraSky(vec3 d, float fade) {
    vec3 primary = max(u_SkyPrimary.rgb, vec3(0.02));
    vec3 secondary = max(u_SkySecondary.rgb, vec3(0.02));
    float top = smoothstep(-0.10, 0.95, d.y);
    vec2 dome = d.xz * (2.15 + top * 0.72) + d.y * vec2(0.37, -0.29);
    float drift = u_time * 0.08;
    float n1 = fbm2(dome * vec2(2.4, 1.8) + vec2(drift, -drift * 0.37));
    float n2 = fbm2(dome * vec2(5.2, 3.6) + vec2(-drift * 1.4, drift * 0.82));
    float wave = sin(d.x * 7.8 - d.z * 6.4 + n1 * 5.2 + u_time * 0.55) * 0.5 + 0.5;
    float bandCenter = 0.16 + wave * 0.21 + (n2 - 0.5) * 0.10;
    float bandDist = abs(d.y - bandCenter);
    float curtain = pow(sky_saturate(sin(d.x * 13.0 - d.z * 9.0 + n2 * 8.0 + u_time * 0.72)), 2.0);
    float az = atan(d.z, d.x);
    float striae = sky_noise2(vec2(az * 15.0 + n1 * 2.4, u_time * 0.05));
    striae = 0.42 + 0.58 * smoothstep(0.28, 0.85, striae);
    float mask = smoothstep(-0.035, 0.30, d.y) * (1.0 - smoothstep(0.78, 1.0, d.y)) * fade;
    float body = (1.0 - smoothstep(0.0, 0.17, bandDist)) * (0.32 + curtain * 0.68) * striae * mask;
    float glow = (1.0 - smoothstep(0.10, 0.48, bandDist)) * mask;
    float veil = (1.0 - smoothstep(0.0, 0.46, abs(d.y - bandCenter - 0.20))) * curtain * striae * mask * 0.5;
    float nebula = smoothstep(0.50, 0.86, n1 * 0.64 + n2 * 0.36) * fade;
    vec3 base = mix(vec3(0.004, 0.009, 0.035), vec3(0.020, 0.050, 0.140), top);
    vec3 aurora = mix(primary, secondary, sky_saturate((d.y - bandCenter) * 2.6 + 0.35));
    float moonMask;
    vec4 moon = moonLayer(d, primary, secondary, moonMask);
    float starVis = 1.0 - moonMask;
    float star = starLayer(d, 158.0 + u_starDensity * 45.0, 0.983, smoothstep(0.04, 0.40, d.y)) * starVis;
    float deepStar = starLayer(normalize(d + vec3(0.017, -0.008, 0.023)), 92.0, 0.974, smoothstep(0.08, 0.62, d.y)) * 0.36 * starVis;
    vec3 color = base + primary * nebula * 0.42 + aurora * body * 1.42 + (primary + secondary) * glow * 0.13;
    color += mix(aurora, secondary, 0.5) * veil * 0.9;
    color += stellarColor(d) * star * 2.0 + mix(stellarColor(d), secondary, 0.38) * deepStar;
    color += moon.rgb * fade;
    float alpha = clamp((0.18 + body * 0.40 + glow * 0.16 + veil * 0.18 + nebula * 0.18 + star * 0.48 + deepStar * 0.22 + moon.a * 0.85) * fade, 0.0, 0.97);
    return vec4(color, alpha);
}

vec4 stardustSky(vec3 d, float fade) {
    float top = smoothstep(-0.08, 1.0, d.y);
    vec3 primary = max(u_SkyPrimary.rgb, vec3(0.02));
    vec3 secondary = max(u_SkySecondary.rgb, vec3(0.02));
    vec3 base = mix(vec3(0.002, 0.004, 0.018), vec3(0.012, 0.020, 0.070), top);
    vec3 p = d * 2.1 + u_cameraDir * 0.17;
    float dustA = fbm3(p + vec3(u_time * 0.010, -u_time * 0.006, u_time * 0.008));
    float dustB = fbm3(p * 2.3 + vec3(-u_time * 0.017, u_time * 0.011, -u_time * 0.013) + dustA);
    float lane = smoothstep(0.45, 0.82, dustA * 0.54 + dustB * 0.46) * smoothstep(-0.04, 0.92, d.y) * fade;
    float river = (1.0 - smoothstep(0.03, 0.28, abs(d.x * 0.42 + d.z * 0.28 + (dustB - 0.5) * 0.38))) * smoothstep(0.03, 0.86, d.y) * fade;
    float moonMask;
    vec4 moon = moonLayer(d, primary, secondary, moonMask);
    float starVis = 1.0 - moonMask;
    float star = starLayer(d, 180.0 + u_starDensity * 90.0, 0.978, smoothstep(0.02, 0.42, d.y)) * starVis;
    float micro = starLayer(normalize(d + vec3(0.021, 0.004, -0.018)), 270.0, 0.992, smoothstep(0.02, 0.58, d.y)) * 0.58 * starVis;
    vec3 color = base;
    color += mix(primary, secondary, dustA) * lane * 0.54;
    color += mix(secondary, vec3(0.78, 0.88, 1.00), 0.38) * river * 0.34;
    color += stellarColor(d) * star * 2.45 + mix(vec3(1.0), secondary, 0.35) * micro * 1.45;
    color += moon.rgb * fade;
    float alpha = clamp((0.16 + lane * 0.18 + river * 0.14 + star * 0.62 + micro * 0.32 + moon.a * 0.85) * fade, 0.0, 0.95);
    return vec4(color, alpha);
}

vec4 twilightRayleighSky(vec3 d, float fade) {
    vec3 sun = sunDirection();
    float mu = dot(d, sun);
    float horizon = pow(1.0 - sky_saturate(abs(d.y)), 2.0);
    float opticalR = 0.0;
    float opticalM = 0.0;
    for (int i = 0; i < 6; i++) {
        float t = (float(i) + 0.5) * 0.16666667;
        float h = max(0.0, d.y * 0.58 + t * 0.62);
        float ground = exp(-h * 4.15) * (1.0 + horizon * 1.6);
        opticalR += ground * (0.105 + t * 0.025);
        opticalM += exp(-h * 9.25) * (0.095 + horizon * 0.21) * (1.0 + u_rainGradient * 1.2);
    }
    vec3 lambda = vec3(650.0, 570.0, 475.0);
    vec3 betaR = pow(vec3(475.0) / lambda, vec3(4.0)) * vec3(0.62, 0.92, 1.72);
    vec3 betaM = vec3(1.00, 0.66, 0.38);
    float rp = rayleighPhase(mu);
    float mp = miePhase(mu, 0.78);
    vec3 extinction = exp(-(betaR * opticalR * 0.72 + betaM * opticalM * 0.42));
    vec3 deepZenith = vec3(0.004, 0.014, 0.064) + betaR * 0.018;
    vec3 scatter = betaR * rp * opticalR * vec3(0.46, 0.70, 1.28) + betaM * mp * opticalM * vec3(1.55, 0.88, 0.36);
    float rim = exp(-abs(d.y + 0.015) * 15.0) * smoothstep(-0.12, 0.22, d.y) * (0.48 + 0.52 * smoothstep(0.60, 1.0, mu));
    float solarDisk = smoothstep(0.99905, 0.99988, mu);
    float solarGlare = pow(sky_saturate(mu), 96.0) * smoothstep(-0.12, 0.18, d.y);
    vec3 color = mix(deepZenith, scatter, smoothstep(-0.10, 0.86, d.y)) * (1.0 - extinction * 0.18);
    color += vec3(1.00, 0.34, 0.075) * rim * 1.65;
    color += vec3(1.00, 0.74, 0.28) * solarGlare * 1.35 + vec3(1.0, 0.94, 0.76) * solarDisk * 10.0;
    color += stellarColor(d) * starLayer(d, 135.0, 0.988, smoothstep(0.18, 0.68, d.y)) * 0.62;
    float alpha = clamp((0.36 + rim * 0.34 + solarGlare * 0.32 + solarDisk) * fade, 0.0, 1.0);
    return vec4(color, alpha);
}

vec4 quantumNebulaSky(vec3 d, float fade) {
    vec3 p = normalize(mix(d, normalize(vProjectionDir + d * 0.35), 0.28)) * 2.55 + u_cameraDir * 0.22;
    vec3 t = vec3(u_time * 0.018, -u_time * 0.012, u_time * 0.015);
    vec3 advectA = vec3(fbm3(p + t), fbm3(p.yzx - t * 1.31), fbm3(p.zxy + t * 0.83));
    vec3 advectB = advectA.yzx * 1.28 - advectA.zxy * 0.55 + vec3(advectA.x * advectA.y, advectA.y * advectA.z, advectA.z * advectA.x) * 1.15 - t * 0.74;
    float gas = fbm3(p * 1.28 + advectA * 2.15 + advectB * 0.72);
    float filament = fbm3(p * 3.30 + advectB * 2.40 - t * 1.6);
    float dust = fbm3(p * 2.15 - advectA * 1.45 + vec3(3.7, -8.1, 5.4));
    float lane = smoothstep(0.50, 0.82, dust) * (1.0 - smoothstep(0.83, 1.0, gas));
    float ha = smoothstep(0.40, 0.88, gas * 0.72 + filament * 0.28);
    float oiii = smoothstep(0.48, 0.92, filament * 0.68 + gas * 0.32);
    float cluster = starLayer(d, 115.0 + u_starDensity * 70.0, 0.970, smoothstep(-0.02, 0.96, d.y));
    float micro = starLayer(normalize(d + advectA * 0.018), 260.0, 0.991, smoothstep(0.02, 0.96, d.y));
    vec3 hAlpha = vec3(1.00, 0.055, 0.24);
    vec3 oxygen = vec3(0.04, 0.92, 1.00);
    vec3 base = mix(vec3(0.002, 0.002, 0.014), vec3(0.010, 0.006, 0.038), smoothstep(-0.10, 0.92, d.y));
    vec3 emission = hAlpha * ha * 1.34 + oxygen * oiii * 1.06 + mix(u_SkyPrimary.rgb, u_SkySecondary.rgb, gas) * smoothstep(0.62, 0.96, gas) * 0.46;
    vec3 color = base + emission * (1.0 - lane * 0.78);
    color += stellarColor(d) * cluster * (1.60 - lane * 0.95) + mix(oxygen, vec3(1.0), 0.52) * micro * (0.64 - lane * 0.34);
    color = sky_exposureTone(color, 1.35 + ha * 0.35 + oiii * 0.26);
    float alpha = clamp((0.20 + ha * 0.26 + oiii * 0.22 + cluster * 0.34 + micro * 0.18 - lane * 0.12) * fade, 0.0, 0.96);
    return vec4(color, alpha);
}

vec4 chronosSingularitySky(vec3 d, float fade) {
    vec3 center = normalize(vec3(0.08, 0.20, 0.976));
    vec3 right = normalize(cross(vec3(0.0, 1.0, 0.0), center));
    vec3 up = normalize(cross(center, right));
    vec2 p = vec2(dot(d, right), dot(d, up));
    float r = length(p);
    vec2 bend = p * (0.056 / (r * r + 0.0065));
    vec3 lensed = normalize(d + right * bend.x + up * bend.y);
    float backgroundStars = starLayer(lensed, 170.0 + u_starDensity * 80.0, 0.979, smoothstep(-0.02, 0.94, d.y));
    float farStars = starLayer(normalize(lensed + vec3(0.031, -0.017, 0.011)), 92.0, 0.965, smoothstep(-0.04, 0.96, d.y)) * 0.42;
    vec3 diskNormal = normalize(center * 0.26 + up * 0.90 + right * 0.18);
    float diskPlane = abs(dot(d, diskNormal));
    float ringWindow = smoothstep(0.070, 0.135, r) * (1.0 - smoothstep(0.37, 0.54, r));
    float diskCore = exp(-diskPlane * 72.0) * ringWindow;
    float diskHaze = exp(-diskPlane * 18.0) * ringWindow * 0.34;
    vec3 tangent = normalize(cross(diskNormal, d) + right * 0.0001);
    float approach = dot(tangent, normalize(u_cameraDir));
    float beam = pow(max(0.08, 1.0 + approach * 0.78), 2.35);
    float orbital = sin(atan(p.y, p.x) * 9.0 - u_time * 2.7 + fbm3(d * 5.0) * 3.0) * 0.5 + 0.5;
    vec3 hot = mix(vec3(1.00, 0.24, 0.045), vec3(0.30, 0.58, 1.00), sky_saturate(approach * 0.5 + 0.5));
    vec3 plasma = mix(hot, vec3(1.0, 0.88, 0.46), orbital * 0.34) * beam;
    float horizon = 1.0 - smoothstep(0.058, 0.084, r);
    float photon = exp(-abs(r - 0.086) * 86.0);
    float lensHalo = exp(-abs(r - 0.155) * 18.0) * 0.44;
    vec3 base = mix(vec3(0.001, 0.001, 0.006), vec3(0.010, 0.014, 0.040), smoothstep(-0.05, 0.90, d.y));
    vec3 color = base + stellarColor(lensed) * backgroundStars * (1.0 + lensHalo * 2.8) + mix(stellarColor(lensed), u_SkySecondary.rgb, 0.36) * farStars;
    color += plasma * (diskCore * 2.9 + diskHaze);
    color += vec3(0.64, 0.74, 1.00) * photon * 2.6 + u_SkySecondary.rgb * lensHalo * 0.82;
    color = mix(color, vec3(0.0), horizon);
    color = sky_exposureTone(color, 1.42);
    float alpha = clamp((0.22 + backgroundStars * 0.46 + farStars * 0.24 + diskCore * 0.72 + diskHaze * 0.20 + photon * 0.38 + lensHalo * 0.16) * fade, 0.0, 0.98);
    return vec4(color, alpha);
}

void main() {
    vec3 d = normalize(vDir);
    float fade = smoothstep(-0.20, 0.08, d.y) * (1.0 - smoothstep(0.992, 1.0, d.y) * 0.08);
    if (fade <= 0.001 || u_intensity <= 0.001) {
        fragColor = vec4(0.0);
        return;
    }

    vec4 sky;
    if (u_mode == STARDUST) {
        sky = stardustSky(d, fade);
    } else if (u_mode == TWILIGHT_RAYLEIGH) {
        sky = twilightRayleighSky(d, fade);
    } else if (u_mode == QUANTUM_NEBULA) {
        sky = quantumNebulaSky(d, fade);
    } else if (u_mode == CHRONOS_SINGULARITY) {
        sky = chronosSingularitySky(d, fade);
    } else {
        sky = auroraSky(d, fade);
    }

    float rainDim = mix(1.0, 0.58, sky_saturate(u_rainGradient));
    vec3 color = sky_exposureTone(sky.rgb * (1.06 + u_intensity * 0.18) * rainDim, 1.0);
    color += vec3((hash21(gl_FragCoord.xy) - 0.5) * 0.0045);
    float alpha = clamp(sky.a * u_intensity * mix(1.0, 0.72, sky_saturate(u_rainGradient)), 0.0, 1.0);
    fragColor = vec4(sky_saturate3(color), alpha);
}
