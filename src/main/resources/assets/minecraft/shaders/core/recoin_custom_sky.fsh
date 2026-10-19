#version 150

layout(std140) uniform SkyData {
    vec4 screen; // resolution.xy, time, mode
    vec4 camera; // yaw, pitch
    vec4 fog;    // rgb, enabled
    vec4 tint;   // configurable/theme sky tint
    vec4 atmosphere; // wind speed/direction, fog density, horizon dissolve
    vec4 atmosphere2; // sky lift, edge softness
};

in vec3 skyDirection;
out vec4 fragColor;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;

float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float hash31(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float recoinNoise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash31(i), hash31(i + vec3(1,0,0)), f.x),
                   mix(hash31(i + vec3(0,1,0)), hash31(i + vec3(1,1,0)), f.x), f.y),
               mix(mix(hash31(i + vec3(0,0,1)), hash31(i + vec3(1,0,1)), f.x),
                   mix(hash31(i + vec3(0,1,1)), hash31(i + vec3(1,1,1)), f.x), f.y), f.z);
}

float fbm3(vec3 p) {
    float value = 0.0;
    float amplitude = 0.52;
    for (int i = 0; i < 5; i++) {
        value += recoinNoise3(p) * amplitude;
        p = p * 2.03 + vec3(3.17, 1.91, 4.73);
        amplitude *= 0.49;
    }
    return value;
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1.0, 0.0)), f.x),
               mix(hash21(i + vec2(0.0, 1.0)), hash21(i + vec2(1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    mat2 rotation = mat2(1.58, 1.18, -1.18, 1.58);
    for (int i = 0; i < 6; i++) {
        value += noise(p) * amplitude;
        p = rotation * p;
        amplitude *= 0.5;
    }
    return value;
}

vec3 viewDirection(vec2 screenUv) {
    vec2 p = screenUv * 2.0 - 1.0;
    float aspect = screen.x / max(screen.y, 1.0);
    // Custom sky presets use a fixed celestial lens. Gameplay zoom must magnify
    // the world, not rescale or push procedural objects away on the sky sphere.
    float tanV = tan(radians(70.0) * 0.5);
    vec3 direction = normalize(vec3(p.x * tanV * aspect, p.y * tanV, 1.0));
    float yaw = radians(-camera.x);
    float pitch = radians(camera.y);
    float cx = cos(pitch), sx = sin(pitch);
    float cy = cos(yaw), sy = sin(yaw);
    mat3 rotX = mat3(1.0, 0.0, 0.0,
                     0.0, cx, sx,
                     0.0, -sx, cx);
    mat3 rotY = mat3(cy, 0.0, sy,
                     0.0, 1.0, 0.0,
                     -sy, 0.0, cy);
    return normalize(rotY * rotX * direction);
}

vec2 sphericalUv(vec3 direction) {
    return vec2(atan(direction.z, direction.x) / TAU + 0.5,
                asin(clamp(direction.y, -1.0, 1.0)) / PI + 0.5);
}

float starField(vec2 p, float density, float threshold) {
    vec2 grid = p * density;
    vec2 cell = floor(grid);
    vec2 local = fract(grid) - 0.5;
    float seed = hash21(cell);
    vec2 offset = vec2(hash21(cell + 2.13), hash21(cell + 8.71)) - 0.5;
    float core = smoothstep(0.055, 0.0, length(local - offset * 0.55));
    float twinkle = 0.72 + 0.28 * sin(screen.z * 1.25 + seed * 38.0);
    return core * step(threshold, seed) * twinkle;
}

float directionStars(vec3 direction, float density, float threshold) {
    vec3 grid = direction * density;
    vec3 cell = floor(grid);
    vec3 local = fract(grid) - 0.5;
    float seed = hash31(cell + 19.73);
    vec3 offset = vec3(hash31(cell + 2.13), hash31(cell + 8.71), hash31(cell + 15.37)) - 0.5;
    float core = smoothstep(0.075, 0.0, length(local - offset * 0.54));
    float twinkle = 0.72 + 0.28 * sin(screen.z * 1.25 + seed * 38.0);
    return core * step(threshold, seed) * twinkle;
}

vec2 wrappedDelta(vec2 point, vec2 center) {
    vec2 delta = point - center;
    delta.x = fract(delta.x + 0.5) - 0.5;
    delta.x *= 1.85;
    return delta;
}

vec3 blackHole(vec2 skyUv) {
    vec2 p = wrappedDelta(skyUv, vec2(0.16, 0.59));
    // The spherical map is wider than it is tall. Correcting it here keeps the
    // event horizon circular at every aspect ratio and camera angle.
    p.x *= 0.88;
    float radius = length(p);
    float angle = atan(p.y, p.x);
    // Separate clocks keep the disk visibly alive without making the whole
    // object pulse or change size. This also keeps zoom from looking like a
    // scale animation.
    float time = screen.z * 0.19;
    float slowTime = screen.z * 0.065;

    // Gravitational lensing bends the star field around the photon sphere.
    float bend = 0.020 / max(radius * radius, 0.0025);
    vec2 tangent = vec2(-p.y, p.x) / max(radius, 0.001);
    vec2 radial = p / max(radius, 0.001);
    vec2 lensedUv = skyUv + radial * bend * 0.018
                  + tangent * (bend * 0.004 + sin(slowTime + radius * 34.0) * 0.00045);
    float stars = starField(lensedUv * vec2(1.0, 1.9), 150.0, 0.966)
                + starField(lensedUv * vec2(1.73, 2.55) + 7.1, 82.0, 0.944) * 0.52;
    vec3 color = vec3(0.0015, 0.0028, 0.012) + stars * vec3(0.70, 0.84, 1.0);

    float horizonRadius = 0.066;
    float photonRing = exp(-pow((radius - 0.079) / 0.0048, 2.0));
    float lensHalo = exp(-pow((radius - 0.088) / 0.027, 2.0));
    float farHalo = exp(-pow((radius - 0.108) / 0.070, 2.0));

    // Thin tilted accretion disk with rotating turbulent filaments. The near side
    // is brighter (Doppler beaming), while a second arc simulates the lensed back.
    float tiltY = p.y + p.x * 0.16;
    float diskRadius = abs(p.x) + abs(tiltY) * 0.22;
    float diskMask = smoothstep(0.235, 0.072, diskRadius) * smoothstep(0.068, 0.082, radius);
    float thinDisk = exp(-pow(tiltY / 0.0105, 2.0)) * diskMask;
    float backArc = exp(-pow((radius - 0.091) / 0.010, 2.0))
                  * smoothstep(-0.015, 0.016, -tiltY) * 0.72;
    float spiralWarp = sin(angle * 7.0 - time * 5.2 + radius * 94.0) * 0.035;
    float turbulence = fbm(vec2(angle * 5.4 - time * 2.4 + spiralWarp, radius * 138.0 + time * 0.7));
    float streamA = 0.5 + 0.5 * sin(angle * 18.0 - time * 7.0 + turbulence * 5.0);
    float streamB = 0.5 + 0.5 * sin(angle * 31.0 - time * 10.5 + radius * 210.0);
    float filaments = 0.36 + 0.64 * smoothstep(0.22, 0.84, turbulence);
    filaments *= 0.62 + 0.25 * streamA + 0.13 * streamB;
    float beam = 0.58 + 0.42 * smoothstep(-0.20, 0.18, p.x);
    vec3 innerHot = vec3(2.25, 1.18, 0.34);
    vec3 outerCold = vec3(0.48, 0.10, 1.38);
    vec3 diskColor = mix(innerHot, outerCold, smoothstep(0.075, 0.225, diskRadius));

    color += farHalo * vec3(0.055, 0.018, 0.17);
    color += lensHalo * vec3(0.24, 0.07, 0.62)
           * (0.94 + 0.06 * sin(slowTime * 2.0 + angle * 3.0));
    color += photonRing * vec3(1.95, 1.15, 0.48) * 1.45;
    color += (thinDisk * beam + backArc) * filaments * diskColor * 2.15;

    // Animated plasma knots orbit inside the disk while two faint relativistic
    // jets escape from the poles. They are direction-space effects, so neither
    // zoom nor aspect-ratio changes can move or resize the object.
    float knotSeed = 0.5 + 0.5 * sin(angle * 23.0 - time * 13.0 + turbulence * 8.0);
    float knots = pow(knotSeed, 9.0) * exp(-pow(tiltY / 0.018, 2.0))
                * smoothstep(0.20, 0.075, radius) * smoothstep(0.071, 0.082, radius);
    color += knots * mix(vec3(2.8, 1.25, 0.28), vec3(0.72, 0.22, 2.2), turbulence) * 1.35;
    float jetAxis = abs(p.x + p.y * 0.16);
    float jetLength = abs(p.y - p.x * 0.16);
    float jet = exp(-jetAxis * 185.0) * smoothstep(0.30, 0.07, jetLength)
              * smoothstep(0.075, 0.105, jetLength);
    float jetPulse = 0.58 + 0.42 * fbm(vec2(jetLength * 82.0 - time * 4.0, angle * 2.0));
    color += jet * jetPulse * vec3(0.16, 0.52, 1.75) * 0.72;

    // Perfectly dark event horizon with a soft anti-aliased edge.
    float horizon = 1.0 - smoothstep(horizonRadius - 0.004, horizonRadius + 0.003, radius);
    color = mix(color, vec3(0.00002, 0.00003, 0.00008), horizon);
    return color;
}

vec3 aurora(vec2 skyUv, vec3 direction) {
    float longitude = atan(direction.z, direction.x);
    float latitude = asin(clamp(direction.y, -1.0, 1.0));
    vec2 p = vec2(longitude, latitude * 2.25);
    float time = screen.z * 0.07;
    float baseStars = directionStars(direction, 92.0, 0.958);
    vec3 color = mix(vec3(0.003, 0.009, 0.035), vec3(0.01, 0.035, 0.075), max(direction.y, 0.0));
    color += baseStars * vec3(0.72, 0.86, 1.0);

    float directionalNoise = fbm3(direction * 2.4 + vec3(0.0, time * 0.18, 0.0));
    float waveA = sin(p.x * 2.0 + time + directionalNoise * 5.0);
    float waveB = sin(p.x * 4.0 - time * 0.7 + directionalNoise * 3.4);
    float curtainCenter = 0.28 + waveA * 0.12 + waveB * 0.055;
    float curtain = exp(-pow((p.y - curtainCenter) / 0.34, 2.0));
    float folds = pow(0.35 + 0.65 * abs(sin(p.x * 9.0 + directionalNoise * 5.5)), 2.2);
    float wisps = fbm3(direction * 4.3 + vec3(time * 0.11, -time * 0.05, 0.0));
    float altitudeMask = smoothstep(-0.12, 0.16, direction.y) * smoothstep(0.92, 0.15, direction.y);
    float intensity = curtain * (0.32 + folds * 0.95) * (0.5 + wisps * 0.7) * altitudeMask;

    vec3 auroraColor = mix(vec3(0.02, 1.35, 0.62), vec3(0.36, 0.28, 1.55),
                           0.5 + 0.5 * sin(p.x * 2.0 + time));
    color += auroraColor * intensity;
    color += vec3(0.16, 0.85, 1.0) * exp(-pow((p.y - curtainCenter + 0.18) / 0.62, 2.0)) * 0.11;
    return color;
}

vec3 nebula(vec2 skyUv, vec3 direction) {
    vec3 p = direction * 4.8;
    float n = fbm3(p * 1.35 + vec3(screen.z * 0.005, 0.0, 0.0));
    float veins = pow(max(0.0, 1.0 - abs(fbm3(p * 2.2 + 6.0) - 0.52) * 3.5), 2.2);
    vec3 color = mix(vec3(0.008, 0.004, 0.042), vec3(0.48, 0.035, 0.64), n);
    color += veins * vec3(0.06, 0.38, 0.9);
    color += directionStars(direction, 88.0, 0.96) * vec3(0.92, 0.82, 1.0);
    return color;
}

vec3 deepSpace(vec2 skyUv, vec3 direction) {
    vec2 p = vec2(skyUv.x * TAU, (skyUv.y - 0.5) * PI);
    float band = exp(-pow((p.y + 0.15 * sin(p.x * 2.0)) / 0.22, 2.0));
    float dust = fbm3(direction * 5.2);
    vec3 color = vec3(0.0015, 0.006, 0.021);
    color += band * dust * vec3(0.018, 0.26, 0.42);
    color += directionStars(direction, 108.0, 0.965) * vec3(0.7, 0.92, 1.0);
    return color;
}

vec3 ambientPlane(vec2 p, float time, int mode) {
    p *= 5.2;
    float n = fbm(p * 0.62 + vec2(time * 0.035, -time * 0.024));
    float d = fbm(p * 1.48 + vec2(-time * 0.021, time * 0.029) + n * 1.7);
    if (mode == 4) {
        vec2 q = p + vec2(sin(p.y + time), cos(p.x - time)) * (0.38 + n * 0.28);
        float caustic = pow(max(0.0, sin(q.x * 3.2 + d * 5.0) * cos(q.y * 3.7 - d * 4.0)), 2.0);
        return mix(vec3(0.005, 0.12, 0.32), vec3(0.18, 1.15, 1.55), caustic + d * 0.35);
    }
    if (mode == 5) {
        float a = pow(max(0.0, sin((p.x + p.y * 0.42) * 4.4 + time * 0.55 + n * 4.0)), 3.0);
        float b = pow(max(0.0, sin((p.x * 0.38 - p.y) * 6.8 - time * 0.44 + d * 3.2)), 4.0);
        return vec3(0.20, 0.40, 0.72) * n + vec3(0.64, 0.92, 1.35) * a + vec3(0.75, 0.35, 1.25) * b;
    }
    if (mode == 6) {
        float cloud = smoothstep(0.25, 0.86, n * 0.76 + d * 0.58);
        float veins = pow(max(0.0, 1.0 - abs(d - 0.52) * 5.0), 2.8);
        return mix(vec3(0.012, 0.018, 0.085), vec3(0.48, 0.06, 0.76), cloud) + veins * vec3(0.08, 0.62, 1.10);
    }
    if (mode == 7) {
        float folds = pow(max(0.0, sin((p.x * 0.7 + p.y * 0.45 + d * 3.2) * 3.9 + time * 0.2)), 2.2);
        return mix(vec3(0.15, 0.18, 0.30), vec3(0.88, 0.91, 1.08), smoothstep(0.2, 0.88, n + d * 0.45)) + folds * 0.22;
    }
    if (mode == 8) {
        vec2 q = p + vec2(sin(p.y + time * 0.7), cos(p.x - time * 0.65)) * (0.55 + n * 0.4);
        float cells = 0.5 + 0.5 * sin(q.x * 1.7 + sin(q.y * 1.9 + time) + d * 5.0);
        float veins = pow(max(0.0, sin(length(q) * 4.4 - time * 1.7 + n * 5.0)), 4.0);
        return mix(vec3(0.08, 0.38, 1.30), vec3(1.15, 0.12, 1.42), cells) + veins * vec3(1.3, 0.8, 0.2);
    }
    float shardA = pow(max(0.0, 1.0 - abs(sin((p.x + p.y * 0.38 + n * 2.2) * 2.8))), 5.0);
    float shardB = pow(max(0.0, 1.0 - abs(sin((p.x * 0.44 - p.y + d * 2.8) * 3.6))), 6.0);
    return vec3(0.07, 0.15, 0.30) + vec3(0.65, 0.94, 1.25) * (shardA + shardB * 0.8 + n * 0.22);
}

vec3 ambientSky(vec3 direction, int mode) {
    vec3 d = normalize(direction);
    vec3 w = pow(max(abs(d), vec3(0.001)), vec3(4.0));
    w /= w.x + w.y + w.z;
    float t = screen.z * 0.18;
    vec3 color = ambientPlane(d.yz, t, mode) * w.x
               + ambientPlane(d.xz + 2.7, t, mode) * w.y
               + ambientPlane(d.xy + 5.4, t, mode) * w.z;
    color += directionStars(direction, 96.0, 0.968) * vec3(0.75, 0.88, 1.0);
    return color;
}

vec3 stardustAtmosphere(vec3 direction) {
    float windAngle = radians(atmosphere.y);
    vec2 wind = vec2(cos(windAngle), sin(windAngle));
    float horizon = pow(clamp(1.0 - abs(direction.y) * 1.12, 0.0, 1.0), 1.36);
    float lowSky = clamp(1.0 - direction.y * 2.2, 0.0, 1.0);
    vec3 deepBlue = tint.rgb * vec3(0.50, 0.58, 0.86) + vec3(0.012, 0.016, 0.030);
    vec3 fogBlue = tint.rgb * vec3(0.78, 0.84, 1.05) + vec3(0.018, 0.018, 0.030);
    vec3 color = mix(deepBlue, fogBlue, smoothstep(0.0, 0.70, horizon));
    color = mix(color, vec3(0.68, 0.56, 0.84), horizon * 0.32);
    color = mix(color, vec3(0.98, 0.62, 0.72), horizon * lowSky * 0.48);
    float lift = atmosphere2.x;
    color = mix(color, tint.rgb * vec3(0.92, 1.02, 1.18), lift * horizon);
    float drift = screen.z * (0.020 + atmosphere.x * 0.060);
    vec2 p = sphericalUv(direction) * 22.0;
    float ribbon = sin(dot(p, wind) * 1.8 - drift * 3.4 + fbm(p * 0.34) * 4.0);
    ribbon = pow(max(0.0, ribbon), 6.0) * atmosphere.x * smoothstep(-0.2, 0.55, direction.y);
    color += ribbon * mix(tint.rgb, vec3(0.82, 0.93, 1.0), 0.70) * 0.22;
    float fogPower = clamp(atmosphere.z * 7.2 * (0.18 + horizon * 0.74 + lowSky * 0.16), 0.0, 1.0);
    color = mix(color, fog.rgb, fogPower * (0.55 + atmosphere.w * 0.35));
    color += directionStars(direction, 96.0, 0.968) * vec3(0.75, 0.88, 1.0) * (1.0 - fogPower);
    return color;
}

vec3 crimson(vec2 skyUv, vec3 direction);
vec3 cosmicNebula(vec2 skyUv, vec3 direction);

vec3 crimson(vec2 skyUv, vec3 direction) {
    float clouds = fbm3(direction * 5.4 + vec3(screen.z * 0.007, 0.0, 0.0));
    vec2 sunDelta = wrappedDelta(skyUv, vec2(0.72, 0.62));
    float flare = exp(-length(sunDelta) * 11.0);
    vec3 color = mix(vec3(0.018, 0.001, 0.006), vec3(0.58, 0.018, 0.03), clouds) * 0.78;
    color += flare * vec3(1.7, 0.22, 0.045);
    color += directionStars(direction, 82.0, 0.966) * vec3(1.0, 0.48, 0.4);
    return color;
}

vec3 cosmicNebula(vec2 skyUv, vec3 direction) {
    vec3 drift = vec3(screen.z * 0.0035, -screen.z * 0.0018, screen.z * 0.0012);
    float broad = fbm3(direction * 3.15 + drift);
    float detail = fbm3(direction * 8.6 - drift * 1.7);
    float filament = pow(max(0.0, 1.0 - abs(detail - 0.50) * 4.6), 2.8);
    float voids = smoothstep(0.22, 0.72, broad);
    vec3 cold = vec3(0.006, 0.018, 0.085);
    vec3 violet = vec3(0.34, 0.035, 0.72);
    vec3 cyan = vec3(0.02, 0.62, 0.92);
    vec3 color = mix(cold, violet, voids * 0.82);
    color += cyan * filament * (0.22 + broad * 0.82);
    color += vec3(0.95, 0.55, 1.0) * pow(filament, 3.0) * 0.52;
    color += directionStars(direction, 102.0, 0.965) * vec3(0.76, 0.9, 1.0);
    return color;
}

vec3 realisticNebula(vec3 direction) {
    // Photographic emission nebula in direction space: broad hydrogen clouds,
    // dark molecular dust lanes and a thin blue oxygen front. No screen-space UV
    // is used, so the structure remains seamless and fixed in the sky sphere.
    vec3 drift = vec3(screen.z * 0.00022, -screen.z * 0.00009, screen.z * 0.00013);
    vec3 p = direction * 3.05;
    float domainA = fbm3(p * 0.92 + vec3(2.7, 7.1, 4.3) + drift);
    float domainB = fbm3(p * 1.85 + vec3(domainA * 2.4, -domainA * 1.7, domainA * 1.25));
    vec3 warped = p + vec3(domainB - 0.5, domainA - 0.5, domainB - domainA) * 1.65;
    float gas = fbm3(warped * 1.38 - drift * 0.7);
    float fine = fbm3(warped * 4.9 + vec3(9.2, 3.8, 12.6));
    float cloud = smoothstep(0.35, 0.76, gas * 0.76 + domainA * 0.24);
    float denseCloud = smoothstep(0.57, 0.86, gas * 0.68 + fine * 0.32);
    float laneNoise = fbm3(warped * 2.25 + vec3(18.4, 5.6, 1.9));
    float dustLane = smoothstep(0.50, 0.72, laneNoise) * smoothstep(0.32, 0.66, cloud);
    float shellNoise = fbm3(warped * 7.5 + vec3(domainA * 3.7));
    float ionFront = pow(max(0.0, 1.0 - abs(shellNoise - 0.51) * 6.2), 3.4) * cloud;

    vec3 color = vec3(0.0012, 0.0021, 0.0075);
    color += vec3(0.055, 0.012, 0.075) * cloud * 0.72;
    color += vec3(0.72, 0.075, 0.16) * denseCloud * 0.92;
    color += vec3(1.05, 0.31, 0.24) * denseCloud * denseCloud * 0.38;
    color += vec3(0.08, 0.48, 0.82) * ionFront * 0.64;
    color += vec3(0.62, 0.82, 1.10) * pow(ionFront, 2.3) * 0.28;
    color *= 1.0 - dustLane * 0.82;
    color += vec3(0.015, 0.006, 0.012) * dustLane;

    float fineStars = directionStars(direction, 126.0, 0.972);
    float brightStars = directionStars(direction, 63.0, 0.982);
    color += fineStars * vec3(0.68, 0.79, 1.0) * 0.72;
    color += brightStars * vec3(1.0, 0.82, 0.62) * 1.35;
    return color;
}

void main() {
    // The same world-space dome direction used by Aurora/Stardust. It follows
    // the game's projection naturally and never slides away under zoom.
    vec3 direction = normalize(skyDirection);
    vec2 skyUv = sphericalUv(direction);
    int mode = int(screen.w + 0.5);

    vec3 color;
    if (mode == 10) color = stardustAtmosphere(direction);
    else if (mode == 1) color = aurora(skyUv, direction);
    else if (mode == 3) color = deepSpace(skyUv, direction);
    else if (mode >= 4) color = ambientSky(direction, mode);
    else color = blackHole(skyUv);

    float tintPeak = max(max(tint.r, tint.g), max(tint.b, 0.001));
    vec3 normalizedTint = tint.rgb / tintPeak;
    color *= mix(vec3(1.0), vec3(0.30) + normalizedTint * 0.92, 0.72);
    if (fog.w > 0.5) {
        float horizon = 1.0 - smoothstep(0.015, 0.28, abs(direction.y));
        color = mix(color, fog.rgb, horizon * 0.78);
    }
    color = 1.0 - exp(-color * 1.22);
    fragColor = vec4(pow(max(color, 0.0), vec3(1.0 / 2.2)), 1.0);
}
