#version 150

layout(std140) uniform BubbleData { vec4 data; };
uniform sampler2D SceneSampler;

in vec2 bubbleCoord;
in vec4 bubbleColor;
out vec4 fragColor;

void main() {
    float age = 1.0 - bubbleColor.a;
    float fade = smoothstep(0.0, 0.065, age) * (1.0 - smoothstep(0.35, 1.0, age));
    float r = length(bubbleCoord);
    float angle = atan(bubbleCoord.y, bubbleCoord.x);
    float ripple = sin(angle * 5.0 + age * 8.0) * 0.012
                 + sin(angle * 9.0 - age * 11.0) * 0.006;
    float edgeRadius = 0.79 + ripple * (1.0 - age);
    float edge = r - edgeRadius;
    float band = exp(-edge * edge / 0.008);
    float rim = exp(-edge * edge / 0.00065);
    float innerRim = exp(-(edge + 0.09) * (edge + 0.09) / 0.0015);
    float glass = 1.0 - smoothstep(edgeRadius - 0.035, edgeRadius + 0.075, r);
    float coverage = (1.0 - smoothstep(0.93, 1.0, r)) * max(band, glass * 0.86) * fade;
    vec2 radial = bubbleCoord / max(r, 0.001);
    vec2 coordDx = dFdx(bubbleCoord);
    vec2 coordDy = dFdy(bubbleCoord);
    vec2 gradient = vec2(dot(radial, coordDx), dot(radial, coordDy));
    vec2 screenDirection = gradient / max(length(gradient), 0.00001);
    vec2 flow = vec2(
        sin(bubbleCoord.y * 5.0 + age * 8.0) + sin((bubbleCoord.x + bubbleCoord.y) * 3.0 - age * 6.0) * 0.5,
        cos(bubbleCoord.x * 4.0 - age * 7.0) + sin((bubbleCoord.x - bubbleCoord.y) * 4.0 + age * 5.0) * 0.5
    ) * 0.22;
    vec2 flowGradient = vec2(dot(flow, coordDx), dot(flow, coordDy));
    vec2 flowDirection = flowGradient / max(length(flowGradient), 0.00001);
    if (coverage < 0.002) discard;
    float bulge = -edge * 27.0 * band + r * 0.85 * glass;
    vec2 texel = 1.0 / data.xy;
    vec2 uv = gl_FragCoord.xy * texel;
    vec2 offset = (screenDirection * bulge + flowDirection * length(flow) * glass * 1.4)
                * 10.0 * data.z * fade * texel;
    vec2 chroma = screenDirection * (band * 1.4 + glass * 0.22) * data.z * fade * texel;
    vec2 lower = texel * 0.5;
    vec2 upper = vec2(1.0) - lower;
    vec3 refracted = vec3(
        texture(SceneSampler, clamp(uv + offset + chroma, lower, upper)).r,
        texture(SceneSampler, clamp(uv + offset, lower, upper)).g,
        texture(SceneSampler, clamp(uv + offset - chroma, lower, upper)).b);

    float light = pow(max(0.0, dot(radial, normalize(vec2(-0.55, 0.83)))), 8.0);
    float opposite = pow(max(0.0, dot(radial, normalize(vec2(0.65, -0.76)))), 12.0);
    vec3 tint = mix(vec3(1.0), bubbleColor.rgb, 0.28);
    vec3 reflection = tint * (rim * (0.12 + light * 0.9 + opposite * 0.32)
                           + innerRim * light * 0.16);
    fragColor = vec4(refracted * (1.0 - band * 0.065) + reflection, coverage);
}
