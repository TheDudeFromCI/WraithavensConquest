package com.wraithavens.conquest.SinglePlayer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import com.wraithavens.conquest.SinglePlayer.BlockPopulators.WorldRenderer;

public class QuadBatch{
	private static final int MAX_SIZE = 500;
	private ArrayList<Quad> quads = new ArrayList();
	private final int vbo;
	private final int ibo;
	private int indexCount;
	public QuadBatch(){
		vbo = GL15.glGenBuffers();
		ibo = GL15.glGenBuffers();
	}
	public void addQuad(Quad q){
		quads.add(q);
	}
	void cleanUp(){
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vbo);
		GL15.glDeleteBuffers(ibo);
		System.out.println("Disposed data buffers: "+vbo+", "+ibo);
	}
	void compileBuffer(){
		int points = quads.size()*4;
		int indices = quads.size()*6;
		FloatBuffer vertexData = BufferUtils.createFloatBuffer(points*7);
		ShortBuffer indexData = BufferUtils.createShortBuffer(indices);
		short elementCount = 0;
		indexCount = 0;
		Quad q;
		for(int i = 0; i<quads.size(); i++){
			q = quads.get(i);
			vertexData.put(q.data.get(0)).put(q.data.get(1)).put(q.data.get(2));
			vertexData.put(q.data.get(12)).put(q.data.get(13)).put(q.data.get(14));
			vertexData.put(q.side==2?1.0f:q.side==3?0.6f:0.8f);
			vertexData.put(q.data.get(3)).put(q.data.get(4)).put(q.data.get(5));
			vertexData.put(q.data.get(12)).put(q.data.get(13)).put(q.data.get(14));
			vertexData.put(q.side==2?1.0f:q.side==3?0.6f:0.8f);
			vertexData.put(q.data.get(6)).put(q.data.get(7)).put(q.data.get(8));
			vertexData.put(q.data.get(12)).put(q.data.get(13)).put(q.data.get(14));
			vertexData.put(q.side==2?1.0f:q.side==3?0.6f:0.8f);
			vertexData.put(q.data.get(9)).put(q.data.get(10)).put(q.data.get(11));
			vertexData.put(q.data.get(12)).put(q.data.get(13)).put(q.data.get(14));
			vertexData.put(q.side==2?1.0f:q.side==3?0.6f:0.8f);
			indexData.put(elementCount).put((short)(elementCount+1)).put((short)(elementCount+2));
			indexData.put(elementCount).put((short)(elementCount+2)).put((short)(elementCount+3));
			elementCount += 4;
			indexCount += 6;
		}
		vertexData.flip();
		indexData.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW);
		quads = null;
	}
	public void renderPart(){
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
		GL11.glColorPointer(3, GL11.GL_FLOAT, 28, 12);
		GL20.glVertexAttribPointer(WorldRenderer.SHADER_LOCATION, 1, GL11.GL_FLOAT, false, 28, 24);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_SHORT, 0);
	}
	boolean isFull(){
		return quads.size()>=QuadBatch.MAX_SIZE;
	}
}