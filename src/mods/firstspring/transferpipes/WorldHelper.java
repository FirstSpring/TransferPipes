package mods.firstspring.transferpipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.transport.TileGenericPipe;



public class WorldHelper {
	public static List<TileEntity> getNeighborTileEntityList(World world, int x, int y, int z){
		ArrayList<TileEntity> list = new ArrayList();
		if(world == null)
			return list;
		TileEntity[] tile = new TileEntity[6];
		tile[0] = world.getBlockTileEntity(x-1, y, z);
		tile[1] = world.getBlockTileEntity(x+1, y, z);
		tile[2] = world.getBlockTileEntity(x, y-1, z);
		tile[3] = world.getBlockTileEntity(x, y+1, z);
		tile[4] = world.getBlockTileEntity(x, y, z+1);
		tile[5] = world.getBlockTileEntity(x, y, z-1);
		for(TileEntity tileBuf:tile)
			if(tileBuf != null)
				list.add(tileBuf);
		return list;
	}
	
	public static List<TileGenericPipe> getNeighborTileGenericPipeList(World world, int x, int y, int z){
		ArrayList<TileGenericPipe> list = new ArrayList();
		if(world == null)
			return list;
		for(TileEntity tile : getNeighborTileEntityList(world, x, y, z)){
			if(tile instanceof TileGenericPipe)
				list.add((TileGenericPipe)tile);
		}
		return list;
	}
	
	public static List<PipeTransfer> getNeighborPipeTransferList(World world, int x, int y, int z){
		ArrayList<PipeTransfer> list = new ArrayList();
		if(world == null)
			return list;
		for(TileGenericPipe tile : getNeighborTileGenericPipeList(world, x, y, z)){
			if(getPipe(tile) != null)
				list.add(getPipe(tile));
		}
		return list;
	}
	
	public static World getWorldForDimension(int dim)
	{
		MinecraftServer server = MinecraftServer.getServer();
		if(server == null)
			return null;
		return server.worldServerForDimension(dim);
	}
	
	public static TileGenericPipe getTile(World world, int x, int y, int z)
	{
		if(world == null)
			return null;
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(tile instanceof TileGenericPipe)
			return (TileGenericPipe)tile;
		return null;
	}
	
	public static PipeTransfer getPipe(TileGenericPipe tile){
		if(tile.pipe instanceof PipeTransfer)
			return (PipeTransfer)tile.pipe;
		return null;
	}
	
	public static PipeTransfer getPipe(int x, int y,int z, int dim){
    	World world = getWorldForDimension(dim);
    	TileGenericPipe tile = getTile(world, x, y, z);
    	if(tile == null)
    		return null;
    	return getPipe(tile);
    }

}
