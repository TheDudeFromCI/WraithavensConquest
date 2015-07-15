package com.wraithavens.conquest.SinglePlayer;

import com.wraithavens.conquest.SinglePlayer.BlockPopulators.Block;

public class ChunkXQuadCounter implements QuadCounter{
	private static final float[] QUAD_POINTS       = new float[12];
	private static final float[] COLORS = new float[3];
	private QuadListener listener;
	private int x, side, startX, startY, startZ;
	private final boolean full;
	public ChunkXQuadCounter(boolean full){
		this.full = full;
	}
	public void addQuad(int y, int z, int w, int h){
		float smallX = full?startX+x:x/8f+startX;
		float smallY = full?startY+y:y/8f+startY;
		float smallZ = full?startZ+z:z/8f+startZ;
		float bigX   = full?smallX+1:smallX+1/8f;
		float bigY   = full?smallY+w:smallY+w/8f;
		float bigZ   = full?smallZ+h:smallZ+h/8f;
		if(side==0){
			QUAD_POINTS[ 0] =   bigX;
			QUAD_POINTS[ 1] =   bigY;
			QUAD_POINTS[ 2] =   bigZ;
			QUAD_POINTS[ 3] =   bigX;
			QUAD_POINTS[ 4] = smallY;
			QUAD_POINTS[ 5] =   bigZ;
			QUAD_POINTS[ 6] =   bigX;
			QUAD_POINTS[ 7] = smallY;
			QUAD_POINTS[ 8] = smallZ;
			QUAD_POINTS[ 9] =   bigX;
			QUAD_POINTS[10] =   bigY;
			QUAD_POINTS[11] = smallZ;
		}else{
			QUAD_POINTS[ 0] = smallX;
			QUAD_POINTS[ 1] = smallY;
			QUAD_POINTS[ 2] = smallZ;
			QUAD_POINTS[ 3] = smallX;
			QUAD_POINTS[ 4] = smallY;
			QUAD_POINTS[ 5] =   bigZ;
			QUAD_POINTS[ 6] = smallX;
			QUAD_POINTS[ 7] =   bigY;
			QUAD_POINTS[ 8] =   bigZ;
			QUAD_POINTS[ 9] = smallX;
			QUAD_POINTS[10] =   bigY;
			QUAD_POINTS[11] = smallZ;
		}
		listener.addQuad(new Quad(QUAD_POINTS, COLORS, side));
	}
	public void setup(int startX, int startY, int startZ, int x, int side, QuadListener listener, Block block){
		this.x      =      x;
		this.side   =   side;
		this.listener  =  listener;
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		COLORS[0] = block.red;
		COLORS[1] = block.green;
		COLORS[2] = block.blue;
	}
}