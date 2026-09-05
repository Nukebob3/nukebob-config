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

    vec2 circleCenter = vec2(vertexColor.r, vertexColor.g);
    vec2 relative = circleCenter-texCoord0;

    float w = 0.45;
    float r =  vertexColor.b;
    if (length(relative) < r && relative.y<abs(relative.x) && (abs(relative.x)>w*r||relative.y<-0.5*r)) {
        color += vec4(0.0, 0.0, 1.0, 0.5);
    }

    fragColor = color * ColorModulator;
}
