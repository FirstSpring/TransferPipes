package mods.firstspring.transferpipes;

import net.minecraft.tileentity.TileEntity;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeLogic;



public class PipeLogicTransfer extends PipeLogic {

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		if (BuildCraftTransport.alwaysConnectPipes)
			return super.isPipeConnected(tile);
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

}
