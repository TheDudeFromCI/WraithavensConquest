uniform sampler2D texture;
uniform vec2 shift;
uniform vec2 size;
out vec3 normal;

void main(){
	vec4 tex = texture(texture, fract(gl_Vertex.xy/size));
	gl_Position = gl_ModelViewProjectionMatrix*vec4(gl_Vertex.x+shift.x, tex.w, gl_Vertex.y+shift.y, 1.0f);
	normal = tex.xyz;
}