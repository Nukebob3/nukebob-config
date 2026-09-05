#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    bool hitbox = false;
    if (!hitbox) {
        if (color.a == 0.0) {
            discard;
        }
    } else {
        fragColor = color * ColorModulator;
        vec2 penis = texCoord0-0.5;
        if (penis.x*penis.x+(penis.y-0.03)*(penis.y-0.03) < 0.15) fragColor += vec4(0,1,0, 0.5);
        return;
    }

    if (color.rgb == vec3(1.0)) {
        vec2 uv = texCoord0;
        uv-=0.5-1/64.;
        uv.y-=0.03;
        uv.x = round(uv.x*32.)/32.;
        uv.y = round(uv.y*32.)/32.;
        float dist = length(uv);
        float radius = 0.4;

        if (dist > radius) {
            discard;
        }

        vec2 mouse = -(vertexColor.rg * 2.0 - 1.0);

        float z = sqrt(radius * radius - dist * dist);
        vec3 N = normalize(vec3(uv, z));
        bool distAffect = false;

        vec3 L;

        if (distAffect) {
            L = normalize(vec3(mouse * 2.0, 1.0));
        } else {
            vec2 lightDir2D = length(mouse) > 0.0 ? normalize(mouse) : vec2(0.0);
            L = normalize(vec3(lightDir2D, 1.0));
        }

        float diff = max(0.0, dot(N, L));

        float brightness = dot(vec3(0.7)*diff+0.4,vec3(1./3.));
        brightness = round(brightness*10.)/10.;

        fragColor = vec4(vec3(brightness)*vec3(1.,0.5,0.0), 1.0);
    } else {
        fragColor = color * ColorModulator;
    }
}
