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
    vec4 uPlayers[16];
};

out vec2 vUV;

void main() {
    vec2 positions[6] = vec2[](
        vec2(-1.0, -1.0),
        vec2( 1.0, -1.0),
        vec2( 1.0,  1.0),
        vec2(-1.0, -1.0),
        vec2( 1.0,  1.0),
        vec2(-1.0,  1.0)
    );

    vec2 uvs[6] = vec2[](
        vec2(0.0, 0.0),
        vec2(1.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 1.0)
    );

    gl_Position = vec4(positions[gl_VertexID], 0.0, 1.0);
    vUV = uvs[gl_VertexID];
}
