#version 150

// Shader module (post effects) — full-screen triangle and the sun position on screen.
layout(std140) uniform Uniforms {
    mat4 uInvProjection;
    mat4 uProjection;
    mat4 uViewRot;
    vec4 uCameraPos;
    vec4 uScreen;       // xy: size, zw: sun direction
    vec4 uParams;       // preset, bloom, time, exposure
    vec4 uExtraParams;  // vignette, sharpen, saturation, contrast
    vec4 uFxParams;     // sun rays, wetness, haze, aberration
    vec4 uTint;         // rgb: tint, a: sky strength
};

out vec2 vUV;
flat out vec4 vSun;

void main() {
    vec2 uv = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));
    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);
    vUV = uv;
    vSun = vec4(0.0);
    vec3 sunDirection = normalize(vec3(uScreen.z, uScreen.w, 0.0) + vec3(0.0, 0.00001, 0.0));
    vec3 viewDirection = transpose(mat3(uViewRot)) * sunDirection;
    vec4 projected = uProjection * vec4(viewDirection, 0.0);
    if (sunDirection.y > -0.08 && projected.w > 0.0001) {
        vec2 sunUV = projected.xy / projected.w * 0.5 + 0.5;
        float height = clamp(sunDirection.y * 4.0 + 0.3, 0.0, 1.0);
        vSun = vec4(sunUV, height, 1.0);
    }
}
