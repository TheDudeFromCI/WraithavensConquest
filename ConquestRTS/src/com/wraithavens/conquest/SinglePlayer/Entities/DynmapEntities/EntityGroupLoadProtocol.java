package com.wraithavens.conquest.SinglePlayer.Entities.DynmapEntities;

import java.io.File;
import com.wraithavens.conquest.Launcher.MainLoop;
import com.wraithavens.conquest.SinglePlayer.Entities.EntityType;
import com.wraithavens.conquest.SinglePlayer.Noise.Biome;
import com.wraithavens.conquest.SinglePlayer.Noise.WorldNoiseMachine;
import com.wraithavens.conquest.Utility.BinaryFile;

public class EntityGroupLoadProtocol{
	private static double square(double d){
		return d*d;
	}
	// The number of updates between each save.
	private final static int SaveRate = 50;
	private final File file;
	private final EntityGroup group;
	private final GiantEntityDictionary dictionary;
	private final int[] possibleEntityLocations;
	private final float loadOffsetX;
	private final float loadOffsetZ;
	private final float loadRange;
	private final WorldNoiseMachine machine;
	private boolean loadingEntities;
	private int step;
	private int framesSinceSave = 0;
	EntityGroupLoadProtocol(WorldNoiseMachine machine, EntityGroup group, int x, int z, File file, BinaryFile bin){
		System.out.println("Not fully generated, however.");
		this.group = group;
		this.file = file;
		this.machine = machine;
		dictionary = new GiantEntityDictionary();
		possibleEntityLocations = new int[dictionary.getSpawnRate()*2];
		loadRange = 8192-dictionary.getMinDistance();
		loadOffsetX = dictionary.getMinDistance()/2f+x;
		loadOffsetZ = dictionary.getMinDistance()/2f+z;
		if(bin!=null){
			loadingEntities = bin.getBoolean();
			step = bin.getInt();
			for(int i = 0; i<step; i++){
				possibleEntityLocations[i*2] = bin.getInt();
				possibleEntityLocations[i*2+1] = bin.getInt();
			}
		}
	}
	private boolean loadEntityAttempt(){
		int x = possibleEntityLocations[step*2];
		int z = possibleEntityLocations[step*2+1];
		float[] tempOut = new float[3];
		Biome biome = machine.getBiomeAt(x, z, tempOut);
		EntityType type = dictionary.randomEntity(biome);
		if(type!=null){
			MainLoop.endLoopTasks.add(new Runnable(){
				public void run(){
					DynmapEntity e = new DynmapEntity(type);
					float height = machine.scaleHeight(tempOut[0], tempOut[1], tempOut[2], x, z);
					e.moveTo(x, height, z);
					e.scaleTo((float)(Math.random()*0.2f+0.9f));
					e.setYaw((float)(Math.random()*360));
					e.updateAABB();
					group.addEntity(e);
					System.out.println("  Generated entity.");
				}
			});
		}
		step++;
		return step==dictionary.getSpawnRate();
	}
	private boolean loadPointAttempt(){
		int x = (int)(Math.random()*loadRange+loadOffsetX);
		int z = (int)(Math.random()*loadRange+loadOffsetZ);
		double r = dictionary.getMinDistance()*dictionary.getMinDistance();
		for(int i = 0; i<step; i++)
			if(square(x-possibleEntityLocations[i*2])+square(z-possibleEntityLocations[i*2+1])<r)
				return false;
		possibleEntityLocations[step*2] = x;
		possibleEntityLocations[step*2+1] = z;
		step++;
		return step==dictionary.getSpawnRate();
	}
	private void save(boolean done){
		framesSinceSave = 0;
		int totalBytes = group.getEntityCount()*24+5;
		if(!done)
			totalBytes += 5+step*8;
		BinaryFile bin = new BinaryFile(totalBytes);
		group.saveEntities(bin);
		bin.addBoolean(done);
		if(!done){
			bin.addBoolean(loadingEntities);
			bin.addInt(step);
			for(int i = 0; i<step; i++){
				bin.addInt(possibleEntityLocations[i*2]);
				bin.addInt(possibleEntityLocations[i*2+1]);
			}
		}
		bin.compress(true);
		bin.compile(file);
		System.out.println("  Saved progress.");
	}
	void dispose(){
		if(framesSinceSave==0)
			return;
		save(false);
	}
	boolean update(){
		System.out.println("Updating dynmap entity group.");
		{
			if(loadingEntities){
				for(int i = 0; i<10; i++)
					if(loadEntityAttempt()){
						System.out.println("  Fully generated.");
						save(true);
						return true;
					}
				System.out.println("  Generated "+step+" entities.");
			}else{
				for(int i = 0; i<100; i++)
					if(loadPointAttempt()){
						loadingEntities = true;
						step = 0;
						break;
					}
				if(!loadingEntities)
					System.out.println("  Generated "+step+" points.");
				else
					System.out.println("  Generated all points.");
			}
		}
		framesSinceSave++;
		if(framesSinceSave==SaveRate)
			save(false);
		return false;
	}
}
