#version 330
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D FrozenSampler;

layout(std140) uniform BlitConfig {
    float lerp;
};

in vec2 texCoord;

out vec4 fragColor;

float random(float x)
{
    return fract(sin(x) * 43758.5453123);
}

void main(){
    vec4 frozenColor = texture(FrozenSampler, texCoord);
    vec4 screenColor = texture(InSampler, texCoord);
    vec4 test = texture(InSampler, vec2(0.5, 0.5));
    float gt = GameTime * 120;
    float ranVal = texCoord.x * 10 + texCoord.y;
    if (frozenColor.r <= 0.1) {
        fragColor = texture(InSampler, texCoord);
    } else if (test.r >= 0) {
        fragColor = mix(screenColor, frozenColor, test.r * (1.0 - lerp));
    } else {
        if (random(gt + ranVal) < 0.01) {
            fragColor = mix(frozenColor, screenColor, 1.0);
        } else {
            fragColor = mix(frozenColor, screenColor, lerp);
        }
    }

}
