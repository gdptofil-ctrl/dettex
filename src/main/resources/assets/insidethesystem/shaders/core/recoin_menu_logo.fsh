#version 150

uniform sampler2D Sampler0;

in vec2 glowCoord;
in vec4 glowColor;
out vec4 fragColor;

void main() {
    vec2 packedDistance = texture(Sampler0, glowCoord).rg;
    float distance = (dot(packedDistance, vec2(65280.0, 255.0)) - 32768.0) / 256.0;
#ifdef LOGO_GLOW
    float outside = max(-distance, 0.0);
    float nearGlow = exp(-0.5 * outside * outside / (28.0 * 28.0));
    float farGlow = exp(-0.5 * outside * outside / (58.0 * 58.0));
    float opacity = (nearGlow * 0.18 + farGlow * 0.11) * (1.0 - smoothstep(96.0, 126.0, outside));
#else
    float antialias = max(fwidth(distance) * 0.65, 0.0001);
    float opacity = smoothstep(-antialias, antialias, distance);
#endif
    fragColor = vec4(glowColor.rgb, glowColor.a * opacity);
}
