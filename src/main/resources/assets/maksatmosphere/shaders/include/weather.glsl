layout(std140) uniform Weather {
    mat4 Projection;
    mat4 InverseProjection;
    mat4 View;
    mat4 InverseView;
    vec4 CameraTime;
    vec4 Effects;
    vec4 Screen;
    vec4 Environment;
    vec4 MeshOrigin;
    vec4 Performance;
    vec4 Atmosphere;
    vec4 Climate;
    // density, size, ground lifetime in seconds, wind
    vec4 SakuraMotion;
    // hue, saturation, pink screen tint, flutter
    vec4 SakuraAppearance;
    // darkness, corona, stars, cosmic dust
    vec4 EclipseSettings;
    // meteor rate, brightness, trail length, stars
    vec4 MeteorSettings;
    // debris density, meteor size, size variation, proximity
    vec4 MeteorDetails;
    // moon size, lunar glow, sky redness, cloud amount
    vec4 BloodSky;
    // red mist, ground tint, ember density, moon height
    vec4 BloodWorld;
    // fog strength, fog proximity, sky glow, nebula density
    vec4 MeteorAtmosphere;
};
