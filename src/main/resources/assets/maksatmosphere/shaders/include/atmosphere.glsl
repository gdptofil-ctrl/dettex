// Screen-space sunlight and distance fog; no extra full-resolution render target.
vec3 atmosphere(vec3 color, float depth) {
    bool sky=depth>=0.999999;
    vec3 view=positionAt(texCoord,sky?0.9999:depth);
    vec3 ray=normalize(mat3(InverseView)*normalize(view));
    vec3 world=(InverseView*vec4(view,1.0)).xyz+CameraTime.xyz;
    float distance=sky?Environment.y*3.0:length(view);

    if(Environment.w>3.5 && Environment.w<4.5) {
        float strength=Climate.x;
        vec3 gold=vec3(1.0,0.73,0.32);
        float horizon=exp(-abs(ray.y)*24.0);
        if(sky) {
            vec3 upper=mix(vec3(0.15,0.19,0.28),vec3(0.32,0.39,0.49),strength);
            float streak=noise(vec2(ray.x*8.0+ray.z*4.0,ray.y*3.0)+CameraTime.w*0.002);
            return mix(upper,gold,horizon*(0.65+0.35*strength))
                +vec3(0.15,0.12,0.07)*pow(streak,3.0)*exp(-abs(ray.y)*3.0)*strength;
        }
        vec3 lit=1.0-exp(-color*(1.3+strength));
        float edge=smoothstep(Environment.y*0.40,Environment.y*0.95,distance);
        return mix(mix(color,lit,strength),gold,edge*strength*0.72);
    }

    float elevation=mix(0.025,0.22,Climate.z)+sin(CameraTime.w*0.008)*0.008;
    vec3 sun=vec3(-cos(elevation),sin(elevation),0.0);
    float toward=max(dot(ray,sun),0.0);
    float strength=Atmosphere.x;
    vec3 warm=vec3(1.0,0.63,0.27);
    if(sky) {
        float height=smoothstep(-0.04,0.65,ray.y);
        vec3 dawn=mix(vec3(0.97,0.62,0.39),vec3(0.34,0.60,0.85),height);
        float clouds=noise(vec2(ray.x*5.0,ray.y*17.0)+vec2(CameraTime.w*0.003,0));
        dawn=mix(dawn,vec3(0.93,0.81,0.69),smoothstep(0.55,0.82,clouds)*0.28);
        float disc=smoothstep(0.99948,0.99966,toward);
        float halo=exp((toward-1.0)*110.0);
        color=mix(color,dawn,0.85);
        color+=warm*halo*strength*0.42;
        color=mix(color,vec3(1.0,0.96,0.79),disc*(0.6+0.4*strength));
    } else {
        vec3 lit=1.0-exp(-color*(1.4+strength));
        lit=lit*vec3(1.06,1.0,0.91)+vec3(0.045,0.028,0.01)*strength;
        color=mix(color,lit,strength);
        float veil=(1.0-exp(-distance*0.004))*strength;
        color=mix(color,vec3(0.96,0.77,0.53),veil*0.32);

    }

    vec4 clip=Projection*View*vec4(sun,0.0);
    if(clip.w>0.0 && Atmosphere.y>0.0) {
        vec2 sunUv=clip.xy/clip.w*0.5+0.5;
        if(all(greaterThan(sunUv,vec2(0.01))) && all(lessThan(sunUv,vec2(0.99)))) {
            float visibility=0.0;
            for(int i=0;i<5;i++) {
                float a=float(i)*6.28318/5.0;
                vec2 u=sunUv+vec2(cos(a),sin(a))*0.005;
                visibility+=step(0.99999,texture(SceneDepth,u).r)*0.2;
            }
            float shaft=0.0;
            for(int i=1;i<=8;i++) {
                vec2 u=mix(texCoord,sunUv,float(i)/9.0);
                shaft+=step(0.99999,texture(SceneDepth,u).r)/8.0;
            }
            float glare=pow(toward,14.0)*visibility*Atmosphere.y*strength;
            // Forward scattering into the eye, without a radial star painted around the sun.
            float facing=pow(max(dot(normalize(mat3(InverseView)*vec3(0,0,-1)),sun),0.0),8.0);
            float forward=exp((toward-1.0)*22.0);
            color+=warm*visibility*Atmosphere.y*strength*facing*forward*(0.30+shaft*0.65);
            vec2 aspect=vec2(Screen.x/Screen.y,1.0);
            for(int i=0;i<3;i++) {
                vec2 ghost=mix(sunUv,vec2(0.5),1.15+float(i)*0.34);
                float r=length((texCoord-ghost)*aspect);
                float disc=1.0-smoothstep(0.008,0.016+float(i)*0.004,r);
                color+=vec3(0.75,0.82,0.55)*disc*glare*0.045;
            }
        }
    }
    return clamp(color,0.0,1.0);
}

