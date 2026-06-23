#version 330

uniform sampler2D InSampler;

layout(std140) uniform BlitConfig {
    float lerp;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 prevColor = texture(InSampler, texCoord);
    float pr = prevColor.r;
    if (pr >= 0.99) {
        pr = 0.0;
    } else {
        pr = min(pr + 0.003, 1.0);
    }
    fragColor = vec4(pr, 0, 0, 0);
}
