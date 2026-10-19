#version 150
layout(std140) uniform WaveData { vec4 rect; vec4 screen; vec4 color; };
out vec2 localPos;
out vec2 size;
out float progress;
out float time;
out vec4 waveColor;
void main() {
    vec2 p[6] = vec2[](vec2(0,0),vec2(1,0),vec2(1,1),vec2(0,0),vec2(1,1),vec2(0,1));
    vec2 uv = p[gl_VertexID];
    vec2 pixel = rect.xy + uv * rect.zw;
    gl_Position = vec4((pixel.x / screen.x) * 2.0 - 1.0, 1.0 - (pixel.y / screen.y) * 2.0, 0, 1);
    localPos = uv * rect.zw; size = rect.zw; progress = screen.z; time = screen.w; waveColor = color;
}
