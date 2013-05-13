package mods.firstspring.transferpipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeLogicWood;



public class PipeLogicTransfer extends PipeLogicWood {

	public boolean isPipeConnected(TileEntity tile) {
		PipeTransfer pipefrom = (PipeTransfer)this.container.pipe;
		PipeTransfer pipeto = null;
		if(tile instanceof TileGenericPipe){
			pipeto = WorldHelper.getPipe((TileGenericPipe)tile);
			if(pipeto != null && pipeto.isConnectUpdate){
				((TileGenericPipe) tile).scheduleNeighborChange();
				pipefrom.isConnectUpdate = false;
			}
		}
		if(pipeto == null)
			return true;
		return !pipefrom.antiConnect || !pipeto.antiConnect;
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return isPipeConnected(tile);
	}

}
