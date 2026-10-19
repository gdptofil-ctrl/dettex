#version 330 core

in vec4 vertexColor;
in float pulseSeed;
in float across;
in float along;

uniform float Time;
uniform float CoreStrength;

out vec4 fragColor;

float hash(float value) {
    return fract(sin(value) * 43758.5453123);
}

void main() {
    // Real soft glow inside the bolt quad: bright electrical core in the centre,
    // then a smooth transparent falloff to the edges.
    float flicker = 0.84 + 0.16 * sin(Time * 22.0 + pulseSeed * 3.0 + hash(pulseSeed) * 6.28318);
    float edge = abs(across);
    float aura = pow(max(0.0, 1.0 - edge), 1.35);
    float core = exp(-edge * edge * 42.0);

    // Small charges race along every zig-zag segment, giving the bolt a living
    // electric flow instead of a static glowing polyline.
    float chargePhase = fract(along * 2.8 - Time * 3.6 + hash(pulseSeed) * 0.83);
    float travellingCharge = exp(-pow(chargePhase - 0.46, 2.0) * 92.0);
    float branchSpark = pow(max(0.0, sin(Time * 13.0 + pulseSeed * 9.0)), 9.0);
    float energy = 1.0 + travellingCharge * 1.65 + branchSpark * 0.45;

    vec3 electric = vertexColor.rgb * (aura * 2.05 + core * 2.8 * CoreStrength) * flicker * energy;
    float alpha = vertexColor.a * (aura * 0.76 + core * 0.90 * CoreStrength) * flicker * (1.0 + travellingCharge * 0.22);
    fragColor = vec4(electric, alpha);
}
