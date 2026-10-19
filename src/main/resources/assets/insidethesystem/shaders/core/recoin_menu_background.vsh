#version 150
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;

out vec2 uv;
out float time;
out vec3 pointer;

void main() {
    vec4 clip = ProjMat * ModelViewMat * vec4(Position, 1.0);
    gl_Position = clip;
    uv = clip.xy / clip.w * 0.5 + 0.5;

    vec4 encoded = floor(Color * 255.0 + 0.5);
    float timeCode = encoded.r * 256.0 + encoded.g;
    time = timeCode / 65535.0 * 60.0;
    pointer = vec3(encoded.b / 255.0, (encoded.a - 1.0) / 254.0, 1.0);
}
