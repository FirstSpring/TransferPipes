package mods.firstspring.transferpipes;

import net.minecraftforge.common.ForgeDirection;

public class PipeItemsDimTransfer extends PipeItemsTransfer {
	int tick = 0;

	public PipeItemsDimTransfer(int itemID) {
		super(itemID);
		dimpipe = true;
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		return super.getIconIndex(direction) + 10;
	}
	
	@Override
	public void updateEntity(){
		super.updateEntity();
		if(worldObj.isRemote)
			return;
		if(tick++ < 20)
			return;
		if (broadcastRedstone || worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)){
			this.getPowerProvider().receiveEnergy(1.0F, ForgeDirection.getOrientation(0));
			tick = 0;
		}
	}

}
