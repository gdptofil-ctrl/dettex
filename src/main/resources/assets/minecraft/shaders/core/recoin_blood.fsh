#version 150

layout(std140) uniform BloodScene {
    mat4 ViewProjection;
    vec4 SunLight;
    vec4 CameraModulo;
};

in vec3 vPosition;
in vec3 vNormal;
in vec4 vColor;
in vec2 vMaterial;

out vec4 fragColor;

float noise(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

void main() {
    float distanceFade = 1.0 - smoothstep(48.0, CameraModulo.w, length(vPosition));
    float alpha = vColor.a * distanceFade;
    if (alpha < 0.004) discard;
    float wet = clamp(vMaterial.x, 0.0, 1.0);
    float ambient = clamp(vMaterial.y, 0.08, 1.0);
    vec3 position = mod(vPosition + CameraModulo.xyz, 64.0);
    float grain = noise(floor(position * 190.0));
    vec3 N = normalize(vNormal);
    vec3 V = normalize(-vPosition + vec3(0.00001));
    vec3 L = normalize(SunLight.xyz);
    vec3 H = normalize(V + L);
    float nl = max(dot(N, L), 0.0);
    float nv = max(dot(N, V), 0.001);
    float nh = max(dot(N, H), 0.0);
    float vh = max(dot(V, H), 0.0);
    float roughness = mix(0.72, 0.22, wet);
    float a = roughness * roughness;
    float a2 = a * a;
    float denominator = nh * nh * (a2 - 1.0) + 1.0;
    float distribution = a2 / max(3.14159265 * denominator * denominator, 0.00001);
    float k = (roughness + 1.0) * (roughness + 1.0) / 8.0;
    float geometry = nv / (nv * (1.0 - k) + k) * nl / (nl * (1.0 - k) + k + 0.00001);
    float fresnel = 0.028 + 0.972 * pow(1.0 - vh, 5.0);
    float specular = min(2.5, distribution * geometry * fresnel / max(4.0 * nv * nl, 0.0001));
    vec3 base = mix(vColor.rgb * vec3(0.40, 0.29, 0.25), vColor.rgb, wet);
    base *= mix(0.90 + grain * 0.10, 1.0, wet);
    vec3 diffuse = base * ambient * (0.48 + 0.52 * nl * SunLight.w);
    vec3 reflection = vec3(1.0, 0.94, 0.88) * specular * nl * SunLight.w * ambient;
    vec3 reflected = reflect(-V, N);
    float sky = smoothstep(-0.25, 0.8, reflected.y);
    float filmReflection = (0.025 + 0.12 * pow(1.0 - nv, 5.0)) * sky * wet * ambient;
    reflection += vec3(0.50, 0.59, 0.72) * filmReflection;
    float grazing = pow(1.0 - nv, 4.0) * wet * 0.045 * ambient;
    fragColor = vec4(diffuse + reflection + vec3(grazing), alpha);
}
