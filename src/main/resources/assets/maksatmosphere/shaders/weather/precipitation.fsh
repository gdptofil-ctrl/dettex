#version 150
#moj_import <maksatmosphere:weather.glsl>
in vec2 dropUv;
in float opacity;
in float snowMode;
in float sparkle;
in float splashMode;
in float petalLight;
out vec4 fragColor;

vec3 hsvToRgb(float hue, float saturation) {
    vec3 channels=clamp(abs(fract(hue+vec3(0.0,0.666667,0.333333))*6.0-3.0)-1.0,0.0,1.0);
    return mix(vec3(1.0),channels,saturation);
}

void main() {
    vec2 p=dropUv*2.0-1.0;
    float alpha;
    vec3 tint;
    if(snowMode>8.5) {
        float radius=length(p);
        float core=exp(-radius*radius*18.0);
        float halo=exp(-radius*radius*3.2);
        alpha=(core*0.56+halo*0.12)*(1.0-smoothstep(0.78,1.0,radius));
        tint=mix(vec3(0.77,0.12,0.08),vec3(1.0,0.48,0.19),sparkle);
    } else if(snowMode>7.5) {
        float taper=smoothstep(-1.0,-0.75,p.y)*(1.0-smoothstep(0.82,1.0,p.y));
        float tail=exp(-p.x*p.x*24.0)*taper;
        float glow=exp(-p.x*p.x*4.5)*taper;
        float head=exp(-p.x*p.x*105.0-pow(p.y-0.66,2.0)*18.0);
        alpha=(tail*0.38+glow*0.10+head*0.92)*(0.58+MeteorSettings.y*0.72);
        tint=mix(vec3(1.0,0.38,0.17),vec3(1.0,0.96,0.77),
                smoothstep(-0.7,0.85,p.y)+head*0.15);
    } else if(snowMode>5.5) {
        float radius=length(p);
        float core=exp(-radius*radius*16.0);
        float halo=exp(-radius*radius*3.0);
        alpha=(core*0.78+halo*0.20)*(1.0-smoothstep(0.73,1.0,radius));
        tint=mix(vec3(1.0,0.65,0.31),vec3(1.0,0.94,0.74),sparkle)
                *(0.66+MeteorSettings.y*0.58);
    } else if(snowMode>3.5) {
        float radius=length(p);
        float core=exp(-radius*radius*18.0);
        float haze=exp(-radius*radius*3.8);
        alpha=(core*0.55+haze*0.17)*(1.0-smoothstep(0.72,1.0,radius));
        tint=mix(vec3(0.36,0.26,0.60),vec3(0.83,0.76,1.0),sparkle);
    } else if(snowMode>2.5) {
        // A broad, slightly asymmetric petal with a split tip and narrow base.
        vec2 q=p;
        q.x+=0.055*(1.0-q.y*q.y)*sin(sparkle*19.0);
        float t=clamp((q.y+0.96)*0.5,0.0,1.0);
        float halfWidth=(0.10+0.91*pow(max(sin(t*3.14159),0.0),0.72))
                *(0.92+0.12*sparkle);
        float notch=0.23*exp(-q.x*q.x*65.0)*smoothstep(0.52,0.91,q.y);
        float edge=min(halfWidth-abs(q.x),min(q.y+0.95,0.96-notch-q.y));
        alpha=smoothstep(-0.035,0.07,edge)*0.88;
        float hue=fract(SakuraAppearance.x+(sparkle-0.5)*0.025);
        vec3 pigment=hsvToRgb(hue,clamp(SakuraAppearance.y*(0.78+0.35*sparkle),0.0,1.0));
        float blush=smoothstep(-0.74,0.86,q.y);
        tint=mix(vec3(1.0,0.975,0.960),pigment,0.35+0.48*blush);
        float vein=exp(-pow(q.x+0.045*q.y,2.0)*175.0)*(1.0-smoothstep(0.60,0.94,q.y));
        tint=mix(tint,pigment*vec3(0.92,0.72,0.77),vein*0.13);
        tint*=petalLight*(0.74+0.26*Environment.x);
        alpha*=0.90+0.10*petalLight;
    } else if(snowMode>1.5) {
        float r2=dot(p,p);
        alpha=exp(-r2*5.8)*(1.0-smoothstep(0.72,1.0,r2))
                *(0.40+0.22*sparkle);
        tint=mix(vec3(0.77,0.69,0.56),vec3(1.0,0.97,0.88),sparkle)
                *(0.60+0.40*Environment.x);
    } else if(snowMode>0.5) {
        float radius=length(p);
        float softFlake=exp(-radius*radius*3.0)*(1.0-smoothstep(0.64,0.97,radius));
        alpha=softFlake*(0.72+sparkle*0.20);
        if(Performance.x>0.5 && sparkle>0.7) {
            float angle=atan(p.y,p.x)+sparkle*6.28;
            float arms=pow(abs(cos(angle*3.0)),14.0);
            alpha=max(alpha,arms*(1.0-smoothstep(0.38,0.88,radius))*0.68);
        }
        tint=mix(vec3(0.69,0.76,0.87),vec3(0.99,0.995,1.0),Environment.x)
                *(0.88+0.12*sparkle);
    } else {
        float taper=smoothstep(-1.0,-0.65,p.y)*(1.0-smoothstep(0.54,1.0,p.y));
        float shaft=exp(-p.x*p.x*11.0)*taper;
        float highlight=exp(-(p.x+0.13)*(p.x+0.13)*85.0)*taper;
        alpha=shaft*(0.34+0.16*sparkle)+highlight*0.10;
        tint=mix(vec3(0.31,0.39,0.47),vec3(0.76,0.84,0.91),Environment.x)
                +vec3(0.11,0.14,0.18)*highlight;
    }
    alpha*=opacity;
    if(splashMode>0.5) {
        float bead=1.0-smoothstep(0.15,0.9,dot(p,p));
        float glint=exp(-dot(p-vec2(-0.18,0.25),p-vec2(-0.18,0.25))*28.0);
        alpha=bead*opacity*0.72;
        tint=mix(vec3(0.27,0.38,0.50),vec3(0.74,0.84,0.93),Environment.x)+glint*0.22;
    }
    if(alpha<0.004) discard;
    fragColor=vec4(tint,alpha);
}
