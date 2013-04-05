package mods.firstspring.transferpipes;

import net.minecraftforge.common.ForgeDirection;

public class PipeLiquidsDimTransfer extends PipeLiquidsTransfer {

	public PipeLiquidsDimTransfer(int itemID) {
		super(itemID);
		dimpipe = true;
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		return super.getIconIndex(direction) + 10;
	}
}
