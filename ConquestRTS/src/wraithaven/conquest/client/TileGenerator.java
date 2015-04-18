package wraithaven.conquest.client;

import wraith.library.RandomGeneration.LinearInterpolation;
import wraith.library.RandomGeneration.NoiseGenerator;
import wraith.library.RandomGeneration.RandomInterpolation;
import wraith.library.WorldManagement.TileGrid.Chipset;
import wraith.library.WorldManagement.TileGrid.Tile;
import wraith.library.WorldManagement.TileGrid.TileMaterial;
import wraith.library.WorldManagement.TileGrid.WorldPopulator;

public class TileGenerator implements WorldPopulator{
	private Chipset chipset;
	private int[] near = new int[8];
	private final long[] seeds;
	private final MapHeightScaler mapHeightScaler;
	private static final int deepWaterMaterial = 16;
	private static final int shallowWaterMaterial = 19;
	private static final int beachMaterial = 13;
	private static final int lowGrassMaterial = 1;
	private static final int highGrassMaterial = 4;
	private static final int lowMountainMaterial = 10;
	private static final int highMountainMaterial = 7;
	public TileGenerator(Chipset chipset, long[] seeds, MapHeightScaler mapHeightScaler){
		this.seeds=seeds;
		this.mapHeightScaler=mapHeightScaler;
		this.chipset=chipset;
	}
	public void generate(Tile[][][] tiles){
		NoiseGenerator noise = new NoiseGenerator(seeds[0], 50, 2);
		RandomInterpolation lerp = new RandomInterpolation(new LinearInterpolation(), 0.1f, seeds[1]);
		lerp.setDirection(RandomInterpolation.TOWARDS_HIGHER);
		noise.setFunction(lerp);
		int[][] mountains = new int[tiles.length+2][tiles[0][0].length+2];
		int x, y, z;
		float n;
		for(x=0; x<mountains.length; x++){
			for(z=0; z<mountains[x].length; z++){
				n=mapHeightScaler.scale(noise.noise(x, z));
				if(n<0.26)mountains[x][z]=0;       //Deep water
				else if(n<0.3)mountains[x][z]=1;   //Shallow water
				else if(n<0.33)mountains[x][z]=2;  //Beach
				else if(n<0.5)mountains[x][z]=3;   //Low grass
				else if(n<0.65)mountains[x][z]=4;  //High grass
				else if(n<0.85)mountains[x][z]=5;  //Low mountain
				else mountains[x][z]=6;            //High mountain
			}
		}
		TileMaterial tileMaterial;
		for(x=0; x<tiles.length; x++){
			for(y=0; y<tiles[x].length; y++){
				for(z=0; z<tiles[x][y].length; z++){
					tileMaterial=getMaterial(mountains, x, y, z);
					if(tileMaterial!=null)tiles[x][y][z]=new Tile(tileMaterial, x, y, z);
				}
			}
		}
	}
	private TileMaterial getMaterial(int[][] mountains, int x, int y, int z){
		x++;
		z++;
		if(mountains[x][z]==0)return runHeight(mountains, x, y, z, 0, deepWaterMaterial);
		else if(mountains[x][z]==1)return runHeight(mountains, x, y, z, 1, shallowWaterMaterial);
		else if(mountains[x][z]==2)return runHeight(mountains, x, y, z, 2, beachMaterial);
		else if(mountains[x][z]==3)return runHeight(mountains, x, y, z, 3, lowGrassMaterial);
		else if(mountains[x][z]==4)return runHeight(mountains, x, y, z, 4, highGrassMaterial);
		else if(mountains[x][z]==5)return runHeight(mountains, x, y, z, 5, lowMountainMaterial);
		return runHeight(mountains, x, y, z, 6, highMountainMaterial);
	}
	private TileMaterial runHeight(int[][] mountains, int x, int y, int z, int layer, int tileMaterialX){
		near[0]=sign(mountains[x][z-1], layer);
		near[1]=sign(mountains[x-1][z], layer);
		near[2]=sign(mountains[x][z+1], layer);
		near[3]=sign(mountains[x+1][z], layer);
		near[4]=sign(mountains[x-1][z-1], layer);
		near[5]=sign(mountains[x+1][z-1], layer);
		near[6]=sign(mountains[x-1][z+1], layer);
		near[7]=sign(mountains[x+1][z+1], layer);
		return breakDown(y, tileMaterialX);
	}
	private TileMaterial breakDown(int y, int tileMaterialX){
		if(y>1)return null;
		int up = near[0];
		int left = near[1];
		int down = near[2];
		int right = near[3];
		int upLeft = near[4];
		int upRight = near[5];
		int downLeft = near[6];
		int downRight = near[7];
		if(up>0){
			if(upRight<=0&&upLeft<=0){
				if(down<0){
					if(left<0&&right<0)return y!=0?null:chipset.getTileMaterial(24, 0);
					if(left<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 20);
					if(right<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 20);
					return y!=0?null:chipset.getTileMaterial(tileMaterialX, 20);
				}
				if(down==0&&left==0&&right==0){
					if(downLeft>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 5);
					if(downLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 22);
					if(downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 22);
					return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 22);
				}
				if(left<0&&right<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 21);
				if(left<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 21);
				if(right<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 21);
				return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 5);
			}
			if(upRight<=0){
				if(down<0&&right==0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 17);
				if(down<0)return y!=0?null:chipset.getTileMaterial(23, 0);
				if(down==0&&right==0&&downRight<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 19);
				if(downRight<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 17);
				if(upLeft==0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 17);
				if(right<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 17);
				return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 3);
			}
			if(upLeft<=0){
				if(down<0&&left==0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 19);
				if(down<0)return y!=0?null:chipset.getTileMaterial(21, 0);
				if(down==0&&left==0&&downLeft<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 19);
				if(downLeft<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 17);
				if(upRight==0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 17);
				if(left<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 17);
				return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 3);
			}
			if(down<0)return y!=0?null:chipset.getTileMaterial(22, 0);
			if(downLeft<0&&downRight<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 18);
			if(downLeft<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 18);
			if(downRight<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 18);
			return y!=0?null:chipset.getTileMaterial(tileMaterialX, 3);
		}
		if(up<0&&left<0&&down>=0&&right>=0&&downRight>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX-1, 0);
		if(up<0&&right<0&&down>=0&&left>=0&&downLeft>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX+1, 0);
		if(up<0&&right>=0&&down>=0&&left>=0&&downLeft>=0&&downRight>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX, 0);
		if(up<0&&left>=0&&down>=0&&right>=0&&downRight>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX+1, 11);
		if(up<0&&left>=0&&down>=0&&right>=0&&downLeft>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX+1, 12);
		if(up<0&&left<0&&right<0&&down<0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX-1, 6);
		if(down>=0&&up<0&&left<0&&right<0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX-1, 9);
		if(up<0&&down<0&&left<0&&right>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX, 9);
		if(right>=0&&down>=0&&up<0&&left<0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX-1, 10);
		if(up<0&&right<0&&down<0&&left>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX+1, 9);
		if(up<0&&right<0&&left>=0&&down>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX, 10);
		if(up<0&&down<0&&left>=0&&right>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX+1, 6);
		if(up<0&&left>=0&&right>=0&&down>=0)return y==0?chipset.getTileMaterial(getMaterialBelow(tileMaterialX), 1):chipset.getTileMaterial(tileMaterialX+1, 10);
		if(up>=0&&left<0&&down<0&&right<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 4);
		if(up>=0&&left<0&&right<0&&down>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 6);
		if(up>=0&&right>=0&&down<0&&left<0&&upRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 2);
		if(up>=0&&right>=0&&down<0&&left<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 11);
		if(up>=0&&right>=0&&down>=0&&left<0&&upRight>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 1);
		if(up>=0&&right>=0&&down>=0&&left<0&&upRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 12);
		if(up>=0&&right>=0&&down>=0&&left<0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 12);
		if(up>=0&&right>=0&&down>=0&&left<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 13);
		if(up>=0&&left>=0&&down<0&&right<0&&upLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 2);
		if(up>=0&&left>=0&&down<0&&right<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 11);
		if(up>=0&&left>=0&&down>=0&&right<0&&upLeft>=0&&downLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 1);
		if(up>=0&&left>=0&&down>=0&&right<0&&upLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 13);
		if(up>=0&&left>=0&&down>=0&&right<0&&downLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 13);
		if(up>=0&&left>=0&&down>=0&&right<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 14);
		if(up>=0&&left>=0&&right>=0&&down<0&&upLeft>=0&&upRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 2);
		if(up>=0&&left>=0&&right>=0&&down<0&&upLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 14);
		if(up>=0&&left>=0&&right>=0&&down<0&&upRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 14);
		if(up>=0&&left>=0&&right>=0&&down<0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 15);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0&&upRight>=0&&downLeft>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 1);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0&&upRight>=0&&downLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 5);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0&&upRight>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 5);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0&&downLeft>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 4);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upRight>=0&&downLeft>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 4);
		if(up>=0&&left>=0&&right>=0&&down>=0&&downLeft>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 7);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0&&upRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 8);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upRight>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 8);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0&&downLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 7);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upRight>=0&&downLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 8);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 7);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 15);
		if(up>=0&&left>=0&&right>=0&&down>=0&&upRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 15);
		if(up>=0&&left>=0&&right>=0&&down>=0&&downLeft>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX, 16);
		if(up>=0&&left>=0&&right>=0&&down>=0&&downRight>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX-1, 16);
		if(up>=0&&left>=0&&right>=0&&down>=0)return y!=0?null:chipset.getTileMaterial(tileMaterialX+1, 16);
		return null;
	}
	private static int getMaterialBelow(int material){
		if(material==shallowWaterMaterial)return deepWaterMaterial;
		if(material==beachMaterial)return shallowWaterMaterial;
		if(material==lowGrassMaterial)return beachMaterial;
		if(material==highGrassMaterial)return lowGrassMaterial;
		if(material==lowMountainMaterial)return highGrassMaterial;
		if(material==highMountainMaterial)return lowMountainMaterial;
		return 0;
	}
	private static int sign(int a, int b){ return a==b?0:a>b?1:-1; }
}