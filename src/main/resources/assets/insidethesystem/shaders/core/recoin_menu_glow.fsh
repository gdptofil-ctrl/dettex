#version 150

in vec2 glowCoord;
in vec4 glowColor;
out vec4 fragColor;

void main() {
    float radius = dot(glowCoord, glowCoord);
    float opacity = exp(-radius * 5.5) * (1.0 - smoothstep(0.65, 1.0, radius));
    fragColor = vec4(glowColor.rgb, glowColor.a * opacity);
}
