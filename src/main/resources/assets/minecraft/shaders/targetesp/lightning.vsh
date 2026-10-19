#version 330 core

layout(location = 0) in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 Color;

out vec4 vertexColor;
out float pulseSeed;
out float across;
out float along;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    pulseSeed = Position.x * 7.13 + Position.y * 11.71 + Position.z * 5.39;
    int corner = gl_VertexID % 4;
    across = (corner == 0 || corner == 3) ? -1.0 : 1.0;
    along = (corner == 0 || corner == 1) ? 0.0 : 1.0;
}
