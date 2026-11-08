#version 150

layout(std140) uniform Uniforms {
    mat4 uInvProjection;
    mat4 uProjection;
    mat4 uViewRot;
    vec4 uCameraPos;
    vec4 uScreen;
    vec4 uParams;
    vec4 uExtraParams;
    vec4 uFogParams;
    vec4 uFogColor;
};

out vec2 vUV;
flat out vec4 vSun;

void main() {
    vec2 uv = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));
    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);
    vUV = uv;
    vSun = vec4(0.0);
    if (uFogParams.y > 0.0) {
        // real sun direction from the module (uScreen.zw); no rays once the sun is below the horizon
        vec3 sunDirection = normalize(vec3(uScreen.z, uScreen.w, 0.0) + vec3(0.0, 0.00001, 0.0));
        vec3 viewDirection = transpose(mat3(uViewRot)) * sunDirection;
        vec4 projected = uProjection * vec4(viewDirection, 0.0);
        if (sunDirection.y > -0.05 && projected.w > 0.0001) {
            vec2 sunUV = projected.xy / projected.w * 0.5 + 0.5;
            float visibility = exp(-length(sunUV - vec2(0.5)) * 0.8);
            float strength = visibility * uFogParams.y * 0.006
                    * mix(0.7, 1.1, clamp(uExtraParams.y / 3.0, 0.0, 1.0));
            vSun = vec4(sunUV, strength, 1.0);
        }
    }
}
