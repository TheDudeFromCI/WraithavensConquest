package wraithaven.conquest.client.BuildingCreator;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import java.util.ArrayList;
import wraithaven.conquest.client.BuildingCreator.BlockPalette.ChunklessBlock;
import wraithaven.conquest.client.BuildingCreator.BlockPalette.ChunklessBlockHolder;
import wraithaven.conquest.client.GameWorld.Voxel.BlockRotation;
import wraithaven.conquest.client.GameWorld.Voxel.BlockShape;
import wraithaven.conquest.client.GameWorld.Voxel.CubeTextures;
import wraithaven.conquest.client.GameWorld.Voxel.QuadBatch;
import wraithaven.conquest.client.GameWorld.Voxel.Texture;

public class BlockIcon implements ChunklessBlockHolder{
	public static final float BLOCK_PITCH = 30;
	public static final float BLOCK_YAW = 45;
	public static float BLOCK_ZOOM = 30;
	private static final float SPIN_SPEED = 10;
	public static float getX(int id){
		return ((Loop.screenRes.width-GuiHandler.HOTBAR_SLOT)/Loop.screenRes.width-0.5f+(GuiHandler.HOTBAR_SLOT/2f/Loop.screenRes.width))*(BLOCK_ZOOM*Loop.screenRes.width/Loop.screenRes.height)*(id<10?1:-1);
	}
	public static float getY(int id){
		return (float)((((Loop.screenRes.height-GuiHandler.HOTBAR_SLOT*10.0)/2+GuiHandler.HOTBAR_SLOT*(9-id%10))/Loop.screenRes.height-0.5f+(GuiHandler.HOTBAR_SLOT/2f/Loop.screenRes.height))*BLOCK_ZOOM);
	}
	private final ArrayList<QuadBatch> batches = new ArrayList();
	public final ChunklessBlock block;
	public int itemSlot;
	private float yaw;
	public BlockIcon(BlockShape shape, CubeTextures textures, BlockRotation rotation){
		block = new ChunklessBlock(this, shape, textures, rotation);
		for(int i = 0; i<batches.size(); i++)
			batches.get(i).recompileBuffer();
	}
	public void dispose(){
		for(int i = 0; i<batches.size(); i++)
			batches.get(i).cleanUp();
	}
	public QuadBatch getBatch(Texture texture){
		QuadBatch batch;
		for(int i = 0; i<batches.size(); i++){
			batch = batches.get(i);
			if(batch.getTexture()==texture) return batch;
		}
		batch = new QuadBatch(texture, 0, 0, 0);
		batches.add(batch);
		return batch;
	}
	public void render(float x, float y){
		render(x, y, 0, 0, 0);
	}
	public void render(float x, float y, float z, float rx, float ry){
		glPushMatrix();
		glTranslatef(x, y, z);
		glRotatef(BLOCK_PITCH+rx, 1, 0, 0);
		glRotatef(BLOCK_YAW+yaw+ry, 0, 1, 0);
		glTranslatef(-0.5f, -0.5f, -0.5f);
		for(int i = 0; i<batches.size(); i++){
			batches.get(i).getTexture().bind();
			batches.get(i).renderPart();
		}
		glPopMatrix();
	}
	public void update(double time){
		yaw = (float)((time*SPIN_SPEED)%360);
	}
}