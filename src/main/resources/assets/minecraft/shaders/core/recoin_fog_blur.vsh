#version 150

out vec2 texCoord;

void main() {
    vec2 positions[6] = vec2[](vec2(0, 0), vec2(1, 0), vec2(1, 1),
                              vec2(0, 0), vec2(1, 1), vec2(0, 1));
    texCoord = positions[gl_VertexID];
    gl_Position = vec4(texCoord * 2.0 - 1.0, 0.0, 1.0);
}
