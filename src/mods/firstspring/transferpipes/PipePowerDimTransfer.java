package mods.firstspring.transferpipes;

import net.minecraftforge.common.ForgeDirection;

public class PipePowerDimTransfer extends PipePowerTransfer {

	public PipePowerDimTransfer(int itemID) {
		super(itemID);
		dimpipe = true;
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		return super.getIconIndex(direction) + 10;
	}
}
