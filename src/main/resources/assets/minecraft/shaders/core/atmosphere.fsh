#version 150

layout(std140) uniform Uniforms {
    mat4 uInvProjection;
    mat4 uProjection;
    mat4 uViewRot;
    vec4 uCameraPos;
    vec4 uScreen;
    vec4 uParams;
    vec4 uExtraParams;
    vec4 uFogParams;
    vec4 uFogColor;
};

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
in vec2 vUV;
flat in vec4 vSun;
out vec4 fragColor;

vec3 viewPosition(vec2 uv, float depth) {
    vec4 position = uInvProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return position.xyz / (abs(position.w) < 0.00001 ? 0.00001 : position.w);
}

float noise(vec2 position) {
    vec2 cell = floor(position);
    vec2 blend = smoothstep(vec2(0.0), vec2(1.0), fract(position));
    vec4 samples = fract(sin(vec4(dot(cell, vec2(127.1, 311.7)),
            dot(cell + vec2(1.0, 0.0), vec2(127.1, 311.7)),
            dot(cell + vec2(0.0, 1.0), vec2(127.1, 311.7)),
            dot(cell + vec2(1.0), vec2(127.1, 311.7)))) * 43758.5453);
    return mix(mix(samples.x, samples.y, blend.x), mix(samples.z, samples.w, blend.x), blend.y);
}

void main() {
    vec4 source = texture(Sampler0, vUV);
    float depth = texture(Sampler1, vUV).r;
    vec3 position = viewPosition(vUV, depth);
    vec3 worldPosition = (uViewRot * vec4(position, 0.0)).xyz + uCameraPos.xyz;
    float sky = step(0.99999, depth);
    float density = uParams.x;
    float amount = uParams.y;
    float time = uParams.z;
    int mode = int(uParams.w + 0.5);
    float softness = uExtraParams.y;
    float grade = clamp(uExtraParams.z * 0.22, 0.0, 0.65);
    vec3 tint = uFogColor.rgb;
    vec3 color = source.rgb;

    if (mode == 5 && amount > 0.0) {
        vec3 normal = normalize(cross(dFdx(worldPosition), dFdy(worldPosition)) + vec3(0.000001));
        if (sky < 0.5) {
            float cover = smoothstep(0.45, 0.85, abs(normal.y));
            if (cover > 0.0) {
                float variation = noise(worldPosition.xz * 0.7);
                float coverage = cover * clamp(amount, 0.0, 1.0) * mix(0.6, 0.9, variation);
                color = mix(color, vec3(0.82, 0.88, 0.94) * (0.62 + dot(source.rgb, vec3(0.2126, 0.7152, 0.0722)) * 0.38), coverage);
            }
        }
    }
    if (mode == 7) {
        float foliage = smoothstep(0.015, 0.13, source.g - max(source.r, source.b));
        color = mix(color, vec3(0.79, 0.37, 0.12) * max(0.2, source.g * 1.8), foliage * clamp(amount * 0.75, 0.0, 1.0));
    }
    vec3 graded = color * mix(vec3(1.0), tint / max(0.0001, max(max(tint.r, tint.g), tint.b)), 0.35);
    color = mix(color, graded, grade);

    if (density > 0.0 && uFogParams.x > 0.0) {
        float haze;
        if (sky > 0.5) {
            haze = clamp(density * uFogParams.x * 0.08, 0.0, 0.35);
        } else {
            float distance = length(position);
            float heightFalloff = exp(-max(worldPosition.y - uExtraParams.w, 0.0) * 0.012);
            haze = (1.0 - exp(-distance * 0.0035 * density * uFogParams.x)) * heightFalloff;
            float flow = noise(worldPosition.xz * 0.015 + vec2(time * 0.012, time * 0.008));
            haze *= mix(0.7, 1.0, flow) * mix(0.7, 1.0, clamp(softness / 3.0, 0.0, 1.0));
            haze = clamp(haze, 0.0, 0.55);
        }
        color = mix(color, tint, haze);
    }

    if (vSun.w > 0.5) {
        vec2 sunUV = vSun.xy;
        vec2 stepUV = (sunUV - vUV) / 12.0;
        vec2 sampleUV = vUV;
        float light = 0.0;
        float weight = 1.0;
        for (int i = 0; i < 12; i++) {
            sampleUV += stepUV;
            if (all(greaterThanEqual(sampleUV, vec2(0.0))) && all(lessThanEqual(sampleUV, vec2(1.0)))) {
                light += step(0.99999, texture(Sampler1, sampleUV).r) * weight;
            }
            weight *= 0.91;
        }
        color += tint * light * vSun.z;
    }
    float edge = smoothstep(0.3, 0.75, length(vUV - vec2(0.5)));
    color *= 1.0 - edge * clamp(uExtraParams.x * 0.13, 0.0, 0.4);
    fragColor = vec4(clamp(color, 0.0, 1.0), source.a);
}
