/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package mods.firstspring.transferpipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.core.network.TileNetworkData;
import buildcraft.transport.IPipeTransportLiquidsHook;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeLiquidsTransfer extends PipeTransfer implements IPipeTransportLiquidsHook {

	public @TileNetworkData
	int liquidToExtract;

	long lastMining = 0;
	boolean lastPower = false;

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return TransferPipes.iconProvider;
	}
	
	public PipeLiquidsTransfer(int itemID) {
		super(new PipeTransportLiquids(), itemID);
		((PipeTransportLiquids) transport).flowRate = 160;
		((PipeTransportLiquids) transport).travelDelay = 1;
	}

	/**
	 * Extracts a random piece of item outside of a nearby chest.
	 */

	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (broadcastRedstone || worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) && !remoteMode)
			liquidToExtract = LiquidContainerRegistry.BUCKET_VOLUME;
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (liquidToExtract > 0 && meta < 6) {
			Position pos = new Position(xCoord, yCoord, zCoord, ForgeDirection.getOrientation(meta));
			pos.moveForwards(1);

			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

			if (tile instanceof ITankContainer) {
				ITankContainer container = (ITankContainer) tile;

				int flowRate = ((PipeTransportLiquids) transport).flowRate;

				LiquidStack extracted = container.drain(pos.orientation.getOpposite(), liquidToExtract > flowRate ? flowRate : liquidToExtract, false);

				int inserted = 0;
				if (extracted != null) {
					inserted = ((PipeTransportLiquids) transport).fill(pos.orientation, extracted, true);

					container.drain(pos.orientation.getOpposite(), inserted, true);
				}

				liquidToExtract -= inserted;
			}
		}
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if(remoteMode)
			return TransferPipes.iconProvider.liqR;
		if (broadcastRedstone || worldObj != null && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			return TransferPipes.iconProvider.liqRS;
		if(antiConnect)
			return TransferPipes.iconProvider.liqA;
		else
			return TransferPipes.iconProvider.liq;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		int filled;
		PipeLiquidsTransfer pipe = this;//(PipeLiquidsTransfer)this.container.pipe;
		//転送処理
		if(worldObj.isRemote)
			return 0;
		if (pipe.remoteMode && (from != ForgeDirection.UNKNOWN)){
			TileGenericPipe tile = getTile(getWorldForDimension(toDim), pipe.toPosX, pipe.toPosY, pipe.toPosZ);
			if(tile != null){
				PipeTransfer pipeto = (PipeTransfer)tile.pipe;
				if(pipeto != null){
					boolean illegal = (toDim != pipeto.toDim) && !(dimpipe && pipeto.dimpipe) || (pipeto.toPosX != xCoord || pipeto.toPosY != yCoord || pipeto.toPosZ != zCoord);
						if(pipeto.remoteMode && !illegal){
							filled = ((PipeTransportLiquids)pipeto.transport).fill(ForgeDirection.UNKNOWN, resource, doFill);
							return filled;
						}
				}
			}
		}
		filled = ((PipeTransportLiquids)this.container.pipe.transport).getTanks(from)[from.ordinal()].fill(resource, doFill);


		/*if (filled > 0 && doFill && tankIndex != Orientations.Unknown.ordinal()){
			((PipeTransportLiquids)this.container.pipe.transport).transferState[tankIndex] = TransferState.Input;
			((PipeTransportLiquids)this.container.pipe.transport)inputTTL[tankIndex] = INPUT_TTL;
		}*/
		return filled;
	}
}
