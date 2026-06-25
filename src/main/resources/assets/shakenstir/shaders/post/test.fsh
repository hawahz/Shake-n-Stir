#version 330
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

float random(float x)
{
    return fract(sin(x) * 43758.5453123);
}

void main(){
    float v = 0.0;
    if (texCoord.x < mod(GameTime*1200.0, 1.0)) {
        v = 1.0;
    }
    fragColor = vec4(v, 0.0, 0.0, 1.0);
}
