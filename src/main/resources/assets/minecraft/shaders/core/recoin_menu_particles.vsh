#version 150

layout(std140) uniform MenuParticlesData {
    vec4 rect;
    vec4 screen;
    vec4 grid;
    vec4 reserved;
};

out vec2 texCoord;
out vec2 sourcePosition;
out float particleAlpha;

float random(float seed) {
    return fract(sin(seed * 127.1 + 311.7) * 43758.5453);
}

void main() {
    vec2 corners[6] = vec2[](
        vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0),
        vec2(0.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 1.0)
    );
    int index = gl_VertexID / 6;
    float seed = float(index) + 1.0;
    vec2 cell = vec2(mod(float(index), grid.x), floor(float(index) / grid.x));
    vec2 cellSize = rect.zw / grid.xy;
    vec2 corner = corners[gl_VertexID % 6];
    sourcePosition = (cell + corner) * cellSize;
    vec2 source = rect.xy + sourcePosition;
    texCoord = vec2(source.x / screen.x, 1.0 - source.y / screen.y);

    float progress = screen.w;
    float scatter = clamp(progress, 0.0, 1.0);
    float travel = scatter * scatter;
    vec2 center = (cell + 0.5) * cellSize - rect.zw * 0.5;
    vec2 direction = center / rect.zw + vec2(random(seed + 7.0) - 0.5, random(seed + 19.0) - 0.5);
    center += direction * rect.zw * travel * 0.52;

    vec2 local = (corner - 0.5) * cellSize * (1.0 - smoothstep(0.15, 1.0, scatter));
    float angle = (random(seed + 31.0) - 0.5) * scatter * 8.0;
    float tilt = (random(seed + 43.0) - 0.5) * scatter * 6.0;
    local.x *= cos(tilt);
    local = mat2(cos(angle), -sin(angle), sin(angle), cos(angle)) * local;
    float depth = travel * (0.12 + random(seed + 53.0) * 0.65);
    vec2 position = rect.xy + rect.zw * 0.5 + (center + local) / (1.0 + depth);
    gl_Position = vec4(position.x / screen.x * 2.0 - 1.0, 1.0 - position.y / screen.y * 2.0, 0.0, 1.0);
    particleAlpha = 1.0 - smoothstep(0.3, 1.0, scatter);
}
