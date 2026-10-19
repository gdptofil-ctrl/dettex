#version 150

uniform sampler2D MainSampler;
uniform sampler2D BlurSampler;
uniform sampler2D DepthSampler;
layout(std140) uniform FogBlurData {
    mat4 InverseProjection;
    vec4 Params;
};
in vec2 texCoord;
out vec4 fragColor;

float viewDistance(vec2 uv) {
    float depth = texture(DepthSampler, uv).r;
    if (depth >= 0.999999) return Params.x * 2.0;
    vec4 view = InverseProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return length(view.xyz / view.w);
}

void main() {
    vec4 original = texture(MainSampler, texCoord);
    float centerDistance = viewDistance(texCoord);
    float fog = smoothstep(2.0, max(3.0, Params.x), centerDistance);
    if (fog < 0.001) {
        fragColor = original;
        return;
    }
    vec2 halfTexel = 0.5 / vec2(textureSize(BlurSampler, 0));
    vec4 blurred = texture(BlurSampler, clamp(texCoord, halfTexel, vec2(1) - halfTexel));
    vec3 color = blurred.a > 0.01 ? blurred.rgb / blurred.a : original.rgb;
    fragColor = vec4(mix(original.rgb, color, fog), original.a);
}
