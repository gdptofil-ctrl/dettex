#version 150

layout(std140) uniform MotionBlurData {
    mat4 MvInverse;
    mat4 ProjInverse;
    mat4 PrevModelView;
    mat4 PrevProjection;
    vec4 CameraPosVec;
    vec4 PrevCameraPosVec;
    vec4 Params;
};

out vec2 texCoord;

void main() {
    vec2 positions[6] = vec2[](
        vec2(0.0, 0.0),
        vec2(1.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 1.0)
    );

    vec2 pos = positions[gl_VertexID];
    gl_Position = vec4(pos * 2.0 - 1.0, 0.0, 1.0);
    texCoord = pos;
}
