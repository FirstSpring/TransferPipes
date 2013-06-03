package mods.firstspring.transferpipes;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.TileGenericPipe;

public abstract class PipeTransfer extends Pipe {
	
	public boolean remoteMode = false;
	protected boolean dimpipe = false, antiConnect = false, isConnectUpdate = false;
	public boolean signalBuffer[] = {false, false, false, false};
	protected int toPosX = 0,toPosY = 0,toPosZ = 0,toDim = 0;
	public PipeTransfer(PipeTransport transport, int itemID) {
		super(transport, new PipeLogicTransfer(), itemID);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(remoteMode)
			teleportSignal();
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer){
		ItemStack is = entityplayer.getCurrentEquippedItem();
		if(is == null)
			return false;
		Item item = is.getItem();
		if (item instanceof IToolWrench && ((IToolWrench) item).canWrench(entityplayer, i, j, k)) {
			if(world.isRemote)
				return true;
			TileGenericPipe tile = getTile(getWorldForDimension(toDim), toPosX, toPosY, toPosZ);
			PipeTransfer pipe = null;
			if(tile != null)
				pipe = getPipe(tile);
			if(pipe != null && remoteMode){
				boolean buf[] = new boolean[]{false, false, false, false};
				broadcastSignal = buf;
				pipe.broadcastSignal = buf;
				signalBuffer = buf;
				pipe.signalBuffer = buf;
				entityplayer.addChatMessage("Initialize Signal State");
				this.container.scheduleNeighborChange();
				tile.scheduleNeighborChange();
			}
			this.antiConnect = !antiConnect;
			List<PipeTransfer> pipeList = WorldHelper.getNeighborPipeTransferList(worldObj, xCoord, yCoord, zCoord);
			for(PipeTransfer pipeBuf : pipeList)
				pipeBuf.isConnectUpdate = true;
			this.container.scheduleNeighborChange();
			return logic.blockActivated(entityplayer);
		}
		return false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		toPosX = nbt.getInteger("X");
		toPosY = nbt.getInteger("Y");
		toPosZ = nbt.getInteger("Z");
		remoteMode = nbt.getBoolean("remoteMode");
		toDim = nbt.getInteger("dim");
		antiConnect = nbt.getBoolean("antiConnect");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("X", toPosX);
		nbt.setInteger("Y", toPosY);
		nbt.setInteger("Z", toPosZ);
		nbt.setBoolean("remoteMode", remoteMode);
		nbt.setInteger("dim", toDim);
		nbt.setBoolean("antiConnect", antiConnect);
	}
	
	protected void teleportSignal(){
		if(worldObj.isRemote)
			return;
		TileGenericPipe tile = getTile(getWorldForDimension(toDim), toPosX, toPosY, toPosZ);
		PipeTransfer pipe = null;
		if(tile !=null)
			pipe = getPipe(tile);
		if(pipe != null){
			for(int i = 0;i <= 3;i++){
				if(signalStrength[i] != 0 && !signalBuffer[i]){
					pipe.broadcastSignal[i] = true;
					if(!pipe.signalBuffer[i]){
						tile.scheduleNeighborChange();
						TransferPipes.println("ON");
					}
					pipe.signalBuffer[i] = true;
					tile.scheduleNeighborChange();
				}
				if(signalStrength[i] == 0 && !signalBuffer[i]){
					pipe.broadcastSignal[i] = false;
					if(pipe.signalBuffer[i]){
						tile.scheduleNeighborChange();
						TransferPipes.println("OFF");
					}
					pipe.signalBuffer[i] = false;
				}
			}
		}
	}
	
	@Override
	public void onBlockRemoval()
	{
		super.onBlockRemoval();
		PipeTransfer pipe = null;
		TileGenericPipe tile = getTile(getWorldForDimension(toDim), toPosX, toPosY, toPosZ);
		if(tile !=null)
			pipe = getPipe(tile);
		if(pipe != null){
			if((xCoord == pipe.toPosX) && (yCoord == pipe.toPosY) && (zCoord == pipe.toPosZ)){
				pipe.remoteMode = false;
				tile.scheduleRenderUpdate();
			}
		}
	}
	
	@Override
	public String toString(){
		String str1 = "";
		if(this instanceof PipeItemsTransfer)
			str1 = "Item : ";
		if(this instanceof PipeLiquidsTransfer)
			str1 = "Liquids : ";
		if(this instanceof PipePowerTransfer)
			str1 = "Power : ";
		return str1 + "[" + toPosX + "," + toPosY + "," + toPosZ + "]";
	}
	
	public TileGenericPipe getTile(World world, int x,int y,int z)
	{
		return WorldHelper.getTile(world, x, y, z);
	}
	
	public PipeTransfer getPipe(TileGenericPipe tile){
		return WorldHelper.getPipe(tile);
	}
	
	public World getWorldForDimension(int dim)
	{
		return WorldHelper.getWorldForDimension(dim);
	}
	
	public List<TileEntity> getNeighborTileEntityList(){
		return WorldHelper.getNeighborTileEntityList(worldObj, xCoord, yCoord, zCoord);
	}
	
}
