#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;

layout(std140) uniform StardustSky {
    vec4 u_SkyPrimary;
    vec4 u_SkySecondary;
    vec4 u_CameraWeather;
    vec4 u_ResolutionTime;
    vec4 u_SkyParams;
    mat4 u_viewProjectionInverse;
    ivec4 u_ModeFlags;
};

out vec3 vDir;
out vec3 vProjectionDir;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    vec4 clip = ProjMat * view;
    float invW = 1.0 / max(abs(clip.w), 0.00001);
    vec4 worldFar = u_viewProjectionInverse * vec4(clip.xy * invW, 1.0, 1.0);
    float worldW = abs(worldFar.w) < 0.00001 ? 1.0 : worldFar.w;
    vec3 dir = normalize(Position);
    gl_Position = clip;
    vDir = dir;
    vProjectionDir = normalize(worldFar.xyz / worldW);
}

