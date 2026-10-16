#version 150
uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;
uniform float Time;
uniform float Strength;
uniform vec2 ScreenSize;
in vec2 texCoord;
out vec4 fragColor;

const float CORE_RADIUS = 2.0 / 2.45;
const float MAX_DISPLACEMENT_1080P = 18.0;

void main() {
    vec4 mark = texture(MaskSampler, texCoord);
    if (mark.b < 0.01) {
        fragColor = texture(SceneSampler, texCoord);
        return;
    }
    vec2 local = (mark.rg - vec2(0.5)) * 2.0;
    float radius = length(local);
    if (radius > 1.0) {
        fragColor = texture(SceneSampler, texCoord);
        return;
    }
    float angle = atan(local.y, local.x);
    vec2 radial = radius > 0.001 ? local / radius : vec2(0.0, 1.0);
    vec2 tangent = vec2(-radial.y, radial.x);
    float radialWave = sin(radius * 30.0 - Time * 2.1 + sin(angle * 5.0 + Time) * 0.7);
    float spiral = sin(angle * 8.0 - Time * 1.7 + radius * 15.0);
    float jitter = sin(Time * 13.0 + local.x * 41.0 + local.y * 29.0) * 0.10;
    float rim = exp(-pow((radius - CORE_RADIUS) * 24.0, 2.0));
    float outerFade = 1.0 - smoothstep(CORE_RADIUS, 1.0, radius);
    float amplitude = Strength * (0.28 + 0.72 * smoothstep(0.35, CORE_RADIUS, radius));
    vec2 shiftPixels = (radial * (radialWave * 0.50 + spiral * 0.27 + jitter + rim * 0.36)
            + tangent * (spiral * 0.42 + rim * sin(angle * 6.0 - Time * 2.0) * 0.45))
            * MAX_DISPLACEMENT_1080P * (ScreenSize.y / 1080.0) * amplitude;
    shiftPixels += radial * outerFade * sin(radius * 17.0 - Time * 1.3) * 3.0 * Strength;
    shiftPixels *= min(1.0, 24.0 * ScreenSize.y / 1080.0 / max(length(shiftPixels), 0.001));
    vec2 sampleAt = clamp(texCoord + shiftPixels / ScreenSize, vec2(0.0), vec2(1.0));
    vec3 scene = texture(SceneSampler, sampleAt).rgb;
    float ringPulse = 0.5 + 0.5 * sin(angle * 10.0 - Time * 2.0 + radialWave * 0.4);
    float breathing = 0.025 * sin(Time * 1.1 + angle * 3.0);
    float ring = exp(-pow((radius - CORE_RADIUS - breathing) * 28.0, 2.0));
    vec3 darkPurple = vec3(0.16, 0.025, 0.31);
    vec3 lightPurple = vec3(0.60, 0.23, 0.83);
    vec3 bluePurple = vec3(0.24, 0.35, 0.92);
    vec3 ringColor = mix(mix(darkPurple, lightPurple, ringPulse), bluePurple,
            0.5 + 0.5 * sin(angle * 5.0 + Time * 1.4));
    vec3 innerTint = vec3(0.06, 0.018, 0.11) * (1.0 - smoothstep(0.1, CORE_RADIUS, radius));
    vec3 glow = ringColor * ring * (0.45 + 0.45 * Strength);
    fragColor = vec4(scene * (1.0 - 0.10 * ring) + innerTint + glow, 1.0);
}
