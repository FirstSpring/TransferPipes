package mods.firstspring.transferpipes;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.pipes.PipeItemsWood;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsRedstoneWood extends PipeItemsWood {

	public PipeItemsRedstoneWood(int itemID) {
		super(itemID);
		// TODO 自動生成されたコンストラクター・スタブ
	}
	public int baseTexture = 2 * 16 + 0;
	public int plainTexture = 2 * 16 + 1;
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return TransferPipes.iconProvider;
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return baseTexture;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return TransferPipes.iconProvider.rsSol;
			else
				return TransferPipes.iconProvider.rsBase;
		}
	}

	boolean lastPower = false;

	public void updateEntity() {
		super.updateEntity();
		boolean currentPower = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

		if (currentPower != lastPower) {
			if(currentPower)
				getPowerProvider().receiveEnergy(1f, ForgeDirection.UP);
			lastPower = currentPower;
		}
	}
	
	@Override
	public boolean canConnectRedstone() {
		return true;
	}
};
