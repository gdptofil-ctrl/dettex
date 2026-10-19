#version 150

uniform sampler2D MaskSampler;
out vec4 fragColor;

void main() {
    ivec2 origin = ivec2(gl_FragCoord.xy) * 16;
    ivec2 limit = textureSize(MaskSampler, 0) - ivec2(1);
    for (int y = 0; y < 16; y++) {
        for (int x = 0; x < 16; x++) {
            vec4 sampleValue = texelFetch(MaskSampler, min(origin + ivec2(x, y), limit), 0);
#ifdef MASK_RED
            float value = sampleValue.r;
#else
            float value = sampleValue.a;
#endif
            if (value > 0.0) {
                fragColor = vec4(1.0);
                return;
            }
        }
    }
    fragColor = vec4(0.0);
}
