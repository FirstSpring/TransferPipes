package mods.firstspring.transferpipes;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.ItemBuildCraft;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRemoteConnector extends ItemBuildCraft implements IToolWrench {
	private int posx,posy,posz,dim,type;
	public int currentDim;
	private boolean hasPosition,isKeep;
	public String name;
	public ItemRemoteConnector(int itemid)
	{
		super(itemid);
		this.maxStackSize = 1;
	}

	//ゲートGUI回避のためIToolWrenchを実装
	@Override
	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z){
		return false;
	}

	@Override
	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z){}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer){
		CommonProxy.proxy.openGui(itemstack);
		return itemstack;
	}

	@Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int s, float i, float j, float k)
    {
		currentDim = entityplayer.dimension;
		if(!world.isRemote){
			readNBT(itemstack);
			PipeTransfer pipe;
			TileGenericPipe tile = getTile(world,x,y,z);
			if(tile != null){
				pipe = getPipe(tile);
				if(pipe != null){
					if(pipe.remoteMode){
						entityplayer.addChatMessage("Pipe Unconnecting");
						disconnectPipe(world, x, y, z);
						return true;
					}
					if(hasPosition && !(x == posx && y == posy && z == posz)){
						if(connectPipe(world, x, y, z, dim)){
							entityplayer.addChatMessage("Connection Successfully");
							if(!isKeep)
								hasPosition=false;
							writeNBT(itemstack);
							return true;
						}
						entityplayer.addChatMessage("Connection Refused");
						if(!isKeep)
							hasPosition=false;
						writeNBT(itemstack);
						return true;
					}
					else{
						entityplayer.addChatMessage("Position Saved");
						posx=x;posy=y;posz=z;
						hasPosition=true;
						dim=currentDim;
						if(pipe instanceof PipeItemsTransfer)
							type = 1;
						if(pipe instanceof PipeLiquidsTransfer)
							type = 2;
						if(pipe instanceof PipePowerTransfer)
							type = 3;
						writeNBT(itemstack);
						return true;
					}
				}
			}
		}
		if(getTile(world,x,y,z) == null)
			return false;
		return true;
    }

	public TileGenericPipe getTile(World world, int x, int y, int z){
		return WorldHelper.getTile(world, x, y, z);
	}

	public PipeTransfer getPipe(TileGenericPipe tile){
		return WorldHelper.getPipe(tile);
	}

	private boolean connectPipe(World world, int x, int y, int z, int dim)
	{
		TileGenericPipe tile1,tile2;
		tile1 = getTile(world,x,y,z);
		World toWorld = getWorldForDimension(dim);
		tile2 = getTile(toWorld,posx,posy,posz);
		boolean a = ((PipeTransfer)tile1.pipe).dimpipe;
		if((currentDim != dim) && !(((PipeTransfer)tile1.pipe).dimpipe && ((PipeTransfer)tile2.pipe).dimpipe))
			return false;
		if(tile1 != null && tile2 != null){
			if(tile1.pipe instanceof PipeItemsTransfer && !(tile2.pipe instanceof PipeItemsTransfer))
				return false;
			if(tile1.pipe instanceof PipeLiquidsTransfer && !(tile2.pipe instanceof PipeLiquidsTransfer))
				return false;
			if(tile1.pipe instanceof PipePowerTransfer && !(tile2.pipe instanceof PipePowerTransfer))
				return false;
			setPipeRemote((PipeTransfer)tile1.pipe,posx,posy,posz,dim);
			tile1.scheduleRenderUpdate();
			tile1.scheduleNeighborChange();
			setPipeRemote((PipeTransfer)tile2.pipe,x,y,z,currentDim);
			tile2.scheduleRenderUpdate();
			tile2.scheduleNeighborChange();
			return true;
		}
		return false;
	}
	private void disconnectPipe(World world, int x, int y, int z)
	{
		TileGenericPipe tile1,tile2;
		PipeTransfer pipe1 = null,pipe2 = null;
		tile1 = getTile(world,x,y,z);
		if(tile1 != null)
			pipe1 = getPipe(tile1);
		if(pipe1 != null){
			pipe1.remoteMode = false;
			pipe1.broadcastSignal = new boolean[]{false,false,false,false};
			tile1.scheduleRenderUpdate();
			tile1.scheduleNeighborChange();
			World toWorld = getWorldForDimension(pipe1.toDim);
			tile2 = getTile(toWorld, pipe1.toPosX, pipe1.toPosY, pipe1.toPosZ);
			if(tile2 != null)
				pipe2 = getPipe(tile2);
				if(pipe2 != null){
					if((x == pipe2.toPosX) && (y == pipe2.toPosY) && (z == pipe2.toPosZ)){
						pipe2.broadcastSignal = new boolean[]{false,false,false,false};
						pipe2.remoteMode = false;
						tile2.scheduleRenderUpdate();
						tile2.scheduleNeighborChange();
					}
				}
		}
	}
	private void setPipeRemote(PipeTransfer pipe, int x, int y, int z, int dim)
	{
		pipe.remoteMode = true;
		pipe.toPosX = x;
		pipe.toPosY = y;
		pipe.toPosZ = z;
		pipe.broadcastSignal = new boolean[]{false,false,false,false};
		pipe.toDim = dim;
	}

	private void readNBT(ItemStack itemstack){
		NBTTagCompound nbt = itemstack.getTagCompound();
		if(nbt != null){
			hasPosition = nbt.getBoolean("flag");
			isKeep = nbt.getBoolean("isKeep");
			posx = nbt.getInteger("posx");
			posy = nbt.getInteger("posy");
			posz = nbt.getInteger("posz");
			dim = nbt.getInteger("dim");
			name = nbt.getString("name");
			type = nbt.getInteger("type");
			return;
		}
		hasPosition = false;
		isKeep = false;
		name = "";
	}

	private void writeNBT(ItemStack itemstack){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("flag", hasPosition);
		nbt.setBoolean("isKeep", isKeep);
		nbt.setInteger("posx", posx);
		nbt.setInteger("posy", posy);
		nbt.setInteger("posz", posz);
		nbt.setInteger("dim", dim);
		nbt.setString("name", name);
		nbt.setInteger("type", type);
		itemstack.setTagCompound(nbt);
	}

	public World getWorldForDimension(int dim)
	{
		return WorldHelper.getWorldForDimension(dim);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean f){
		readNBT(itemstack);
		list.add(name);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void updateIcons(IconRegister ir){
		iconIndex = ir.registerIcon("firstspring/transferpipes:remote_connector");
	}

}
