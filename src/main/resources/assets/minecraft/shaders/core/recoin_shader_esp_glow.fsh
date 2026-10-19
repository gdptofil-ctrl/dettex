#version 150

layout(std140) uniform GlowData { vec4 data; }; // resolution.xy, radius, intensity
uniform sampler2D MaskSampler;
uniform sampler2D CoverageSampler;
in vec2 texCoord;
out vec4 fragColor;

const vec2 SAMPLE_DIRECTIONS[12] = vec2[12](
    vec2(1.0, 0.0),
    vec2(0.8660254, 0.5),
    vec2(0.5, 0.8660254),
    vec2(0.0, 1.0),
    vec2(-0.5, 0.8660254),
    vec2(-0.8660254, 0.5),
    vec2(-1.0, 0.0),
    vec2(-0.8660254, -0.5),
    vec2(-0.5, -0.8660254),
    vec2(0.0, -1.0),
    vec2(0.5, -0.8660254),
    vec2(0.8660254, -0.5)
);

void main() {
    vec2 texel = 1.0 / data.xy;
    vec2 maskSize = vec2(textureSize(MaskSampler, 0));
    vec2 reach = (abs(data.z) * texel) * maskSize + vec2(1.0);
    vec2 pixel = texCoord * maskSize;
    ivec2 lastTile = textureSize(CoverageSampler, 0) - ivec2(1);
    ivec2 first = clamp(ivec2(floor((pixel - reach) / 16.0)), ivec2(0), lastTile);
    ivec2 last = clamp(ivec2(floor((pixel + reach) / 16.0)), ivec2(0), lastTile);
    bool occupied = false;
    for (int y = first.y; y <= last.y && !occupied; y++) {
        for (int x = first.x; x <= last.x; x++) {
            if (texelFetch(CoverageSampler, ivec2(x, y), 0).r > 0.0) {
                occupied = true;
                break;
            }
        }
    }
    if (!occupied) {
        fragColor = vec4(0.0);
        return;
    }
    vec4 center = texture(MaskSampler, texCoord);
    vec3 glowColor = vec3(0.0);
    float halo = 0.0;
    const int DIRECTIONS = 12;
    for (int i = 0; i < DIRECTIONS; i++) {
        vec2 dir = SAMPLE_DIRECTIONS[i];
        vec4 nearSample = texture(MaskSampler, texCoord + dir * texel * data.z * 0.48);
        vec4 farSample = texture(MaskSampler, texCoord + dir * texel * data.z);
        glowColor += nearSample.rgb * nearSample.a * 0.72 + farSample.rgb * farSample.a * 0.28;
        halo += nearSample.a * 0.72 + farSample.a * 0.28;
    }
    glowColor /= float(DIRECTIONS);
    halo = halo / float(DIRECTIONS);
    float outside = 1.0 - smoothstep(0.02, 0.34, center.a);
    float alpha = smoothstep(0.01, 0.56, halo) * outside;
    fragColor = vec4(glowColor * data.w * 1.75, alpha * 0.72);
}
