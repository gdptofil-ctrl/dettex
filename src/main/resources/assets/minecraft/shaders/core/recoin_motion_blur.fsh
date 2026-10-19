#version 150

uniform sampler2D MainSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform MotionBlurData {
    mat4 MvInverse;
    mat4 ProjInverse;
    mat4 PrevModelView;
    mat4 PrevProjection;
    vec4 CameraPosVec;
    vec4 PrevCameraPosVec;
    vec4 Params;
};

in vec2 texCoord;
out vec4 fragColor;

vec3 reproject(vec3 screenPos) {
    vec3 ndc = screenPos * 2.0 - 1.0;
    vec4 viewPos = ProjInverse * vec4(ndc, 1.0);
    vec3 worldPos = (MvInverse * vec4(viewPos.xyz / viewPos.w, 1.0)).xyz
                  + (CameraPosVec.xyz - PrevCameraPosVec.xyz);
    vec4 prevPos = PrevProjection * (PrevModelView * vec4(worldPos, 1.0));
    return (prevPos.xyz / prevPos.w) * 0.5 + 0.5;
}

vec2 clampLength(vec2 velocity) {
    float lenSq = dot(velocity, velocity);
    return lenSq > 0.16 ? velocity * (0.4 * inversesqrt(lenSq)) : velocity;
}

float noise(vec2 pos) {
    return fract(52.9829189 * fract(0.06711056 * pos.x + 0.00583715 * pos.y));
}

void main() {
    float strength = Params.x;
    int maxSamples = int(Params.y);
    bool centered = Params.z > 0.5;
    bool useDepth = Params.w > 0.5;

    vec2 res = vec2(textureSize(MainSampler, 0));
    ivec2 texel = ivec2(gl_FragCoord.xy);
    ivec2 maxTexel = ivec2(res) - 1;

    float depth = 1.0;
    if (useDepth) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                depth = min(depth, texelFetch(DepthSampler,
                    clamp(texel + ivec2(x, y), ivec2(0), maxTexel), 0).x);
            }
        }
    }

    if (useDepth && depth < 0.0101) {
        fragColor = vec4(texture(MainSampler, texCoord).rgb, 1.0);
        return;
    }

    vec2 velocity = clampLength(texCoord - reproject(vec3(texCoord, depth)).xy) * strength;

    int count = clamp(int(ceil(length(velocity * res))), 1, maxSamples);
    vec2 stepUv = velocity / float(count);
    float centerOffset = centered ? -(float(count) * 0.5) : 0.0;

    vec3 sum = vec3(0.0);
    for (int i = 0; i < count; i++) {
        float fi = float(i);
        float jitter = noise(gl_FragCoord.xy + vec2(fi, fi * 1.4));
        vec3 c = texture(MainSampler, texCoord + (fi + centerOffset + jitter) * stepUv).rgb;
        sum += c * c;
    }

    fragColor = vec4(sqrt(sum / float(count)), 1.0);
}
