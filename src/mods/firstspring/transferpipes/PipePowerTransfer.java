/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package mods.firstspring.transferpipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportPowerHook;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipePowerTransfer extends PipeTransfer implements IPowerReceptor,IPipeTransportPowerHook {

	private IPowerProvider powerProvider;

	protected boolean receive = true;

	public PipePowerTransfer(int itemID) {
		super(new PipeTransportPower(), itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 1, 40000, 1, 40000);
		powerProvider.configurePowerPerdition(0, 100);
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		if(remoteMode)
			return TransferPipes.iconProvider.powR;
		if(antiConnect)
			return TransferPipes.iconProvider.powA;
		else
			return TransferPipes.iconProvider.pow;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return TransferPipes.iconProvider;
	}
	
	@Override
	public void setPowerProvider(IPowerProvider provider) {
		provider = powerProvider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntity() {
		//リモートモードの間エネルギー要求
		if(worldObj.isRemote)
			return;
		if(remoteMode){
			for (int i = 0; i < 6; ++i)
				((PipeTransportPower)this.transport).nextPowerQuery[i] = 40000;
		}
		super.updateEntity();
		if(remoteMode){
			World world = WorldHelper.getWorldForDimension(toDim);
			TileGenericPipe tile = WorldHelper.getTile(world, toPosX, toPosY, toPosZ);
			if(tile != null){
				if(tile.pipe instanceof PipePowerTransfer){
					ForgeDirection orient = tile.pipe.getOpenOrientation();
					if(orient == ForgeDirection.UNKNOWN)
						return;
					PipeTransportPower dest = (PipeTransportPower)tile.pipe.transport;
					float energyToRemove = 0;
					if (powerProvider.getEnergyStored() > 40)
						energyToRemove = powerProvider.getEnergyStored() / 40 + 4;
					else if (powerProvider.getEnergyStored() > 10)
						energyToRemove = powerProvider.getEnergyStored() / 10;
					else
						energyToRemove = 1;
					float teleportEnergy = powerProvider.useEnergy(1, energyToRemove, true);
					//ループ対策
					((PipePowerTransfer)tile.pipe).receive = true;
					dest.receiveEnergy(orient, teleportEnergy);
				}
			}
			return;
		}
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS)
			if (Utils.checkPipesConnections(container, container.getTile(o))) {
				TileEntity tile = container.getTile(o);

				if (tile instanceof TileGenericPipe) {
					if (((TileGenericPipe) tile).pipe == null) {
						continue; // Null pointer protection
					}

					PipeTransportPower pow = (PipeTransportPower) ((TileGenericPipe) tile).pipe.transport;

					float energyToRemove = 0;

					if (powerProvider.getEnergyStored() > 40)
						energyToRemove = powerProvider.getEnergyStored() / 40 + 4;
					else if (powerProvider.getEnergyStored() > 10)
						energyToRemove = powerProvider.getEnergyStored() / 10;
					else
						energyToRemove = 1;

					float energyUsed = powerProvider.useEnergy(1, energyToRemove, true);

					pow.receiveEnergy(o.getOpposite(), energyUsed);

					if (worldObj.isRemote) return;
					((PipeTransportPower) transport).displayPower[o.ordinal()] += energyUsed;
				}

			}
	}
	
	@Override
	public int powerRequest(ForgeDirection from) {
		return getPowerProvider().getMaxEnergyReceived();
	}

	@Override
	public void receiveEnergy(ForgeDirection from, double val) {
		if(worldObj.isRemote)
			return;
		if(remoteMode && !receive)
		{
			if(worldObj.isRemote)
				return;
			TileGenericPipe tile = getTile(getWorldForDimension(toDim), toPosX, toPosY, toPosZ);
			if(tile != null){
				if(tile.pipe instanceof PipePowerTransfer){
					ForgeDirection orient = tile.pipe.getOpenOrientation();
					if((toDim != ((PipeTransfer)tile.pipe).toDim) && !(dimpipe && ((PipeTransfer)tile.pipe).dimpipe))
						return;
					if(orient == ForgeDirection.UNKNOWN)
						return;
					PipeTransportPower dest = (PipeTransportPower)tile.pipe.transport;
					//ループ対策
					((PipePowerTransfer)tile.pipe).receive = true;
					dest.receiveEnergy(orient, val);
				}
			}
			return;
		}
		receive=false;
		PipeTransportPower transportPower = (PipeTransportPower)transport;
		transportPower.internalNextPower[from.ordinal()] += val;
		if(transportPower.internalNextPower[from.ordinal()] > 1000)
		transportPower.internalNextPower[from.ordinal()] = 1000;
		
	}

	@Override
	public void requestEnergy(ForgeDirection from, int i) {
		PipeTransportPower transportPower = (PipeTransportPower)transport;
		transportPower.step();
		transportPower.nextPowerQuery[from.ordinal()] += i;
	}

}
