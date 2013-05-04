/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package mods.firstspring.transferpipes;

import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipedItem;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Utils;
import buildcraft.energy.TileEngine;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsTransfer extends PipeTransfer implements IPipeTransportItemsHook,IPowerReceptor {

	private IPowerProvider powerProvider;
	public boolean receive = false;
	public PipeItemsTransfer(int itemID) {
		super(new PipeTransportItems(), itemID);
		((PipeTransportItems) transport).allowBouncing = true;
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 1, 64, 1, 64);
		powerProvider.configurePowerPerdition(64, 1);
	}
	
	@Override
	public int getIconIndex(ForgeDirection direction) {
		if(remoteMode)
			return TransferPipes.iconProvider.itemR;
		if(antiConnect)
			return TransferPipes.iconProvider.itemA;
		else
			return TransferPipes.iconProvider.item;
	}
	
	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}


	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos,
			IPipedItem item) {
		LinkedList<ForgeDirection> f = new LinkedList();
		for(int i = 0; i < 6; i++){
			Position p = new Position(pos);
			p.orientation = ForgeDirection.getOrientation(i);
			p.moveForwards(1);
			TileEntity tile = worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
			if(tile instanceof TileEngine)
				return possibleOrientations;
			if(tile instanceof TileGenericPipe)
				continue;
			if(((PipeTransportItems) transport).canReceivePipeObjects(ForgeDirection.getOrientation(i), item)){
				f.add(ForgeDirection.getOrientation(i));
			}
		}
		if(!f.isEmpty())
			return f;
		return possibleOrientations;
	}
	
	//木パイプよりコピペ
	@Override
	public void doWork() {
		if (powerProvider.getEnergyStored() <= 0)
			return;

		World w = worldObj;

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (meta > 5)
			return;

		Position pos = new Position(xCoord, yCoord, zCoord, ForgeDirection.getOrientation(meta));
		pos.moveForwards(1);
		TileEntity tile = w.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

		if (tile instanceof IInventory) {
			if (!PipeManager.canExtractItems(this, w, (int) pos.x, (int) pos.y, (int) pos.z))
				return;

			IInventory inventory = (IInventory) tile;

			ItemStack[] extracted = checkExtract(inventory, true, pos.orientation.getOpposite());
			if (extracted == null)
				return;

			for (ItemStack stack : extracted) {
				if (stack == null || stack.stackSize == 0) {
					powerProvider.useEnergy(1, 1, false);
					continue;
				}

				Position entityPos = new Position(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, pos.orientation.getOpposite());

				entityPos.moveForwards(0.6);

				IPipedItem entity = new EntityPassiveItem(w, entityPos.x, entityPos.y, entityPos.z, stack);

				((PipeTransportItems) transport).entityEntering(entity, entityPos.orientation);
			}
		}
	}
	
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {

		// / ISPECIALINVENTORY
		if (inventory instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) inventory).extractItem(doRemove, from, (int) powerProvider.getEnergyStored());
			if (stacks != null && doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						powerProvider.useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
			}
			return stacks;
		}

		if (inventory instanceof ISidedInventory) {
			ISidedInventory sidedInv = (ISidedInventory) inventory;

			int first = sidedInv.getStartInventorySide(from);
			int last = first + sidedInv.getSizeInventorySide(from) - 1;

			IInventory inv = Utils.getInventory(inventory);

			ItemStack result = checkExtractGeneric(inv, doRemove, from, first, last);

			if (result != null)
				return new ItemStack[] { result };
		} else if (inventory.getSizeInventory() == 2) {
			// This is an input-output inventory

			int slotIndex = 0;

			if (from == ForgeDirection.DOWN || from == ForgeDirection.UP) {
				slotIndex = 0;
			} else {
				slotIndex = 1;
			}

			ItemStack slot = inventory.getStackInSlot(slotIndex);

			if (slot != null && slot.stackSize > 0) {
				if (doRemove)
					return new ItemStack[] { inventory.decrStackSize(slotIndex, (int) powerProvider.useEnergy(1, slot.stackSize, true)) };
				else
					return new ItemStack[] { slot };
			}
		} else if (inventory.getSizeInventory() == 3) {
			// This is a furnace-like inventory

			int slotIndex = 0;

			if (from == ForgeDirection.UP) {
				slotIndex = 0;
			} else if (from == ForgeDirection.DOWN) {
				slotIndex = 1;
			} else {
				slotIndex = 2;
			}

			ItemStack slot = inventory.getStackInSlot(slotIndex);

			if (slot != null && slot.stackSize > 0) {
				if (doRemove)
					return new ItemStack[] { inventory.decrStackSize(slotIndex, (int) powerProvider.useEnergy(1, slot.stackSize, true)) };
				else
					return new ItemStack[] { slot };
			}
		} else {
			// This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);

			ItemStack result = checkExtractGeneric(inv, doRemove, from, 0, inv.getSizeInventory() - 1);

			if (result != null)
				return new ItemStack[] { result };
		}

		return null;
	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, ForgeDirection from, int start, int stop) {
		for (int k = start; k <= stop; ++k)
			if (inventory.getStackInSlot(k) != null && inventory.getStackInSlot(k).stackSize > 0) {

				ItemStack slot = inventory.getStackInSlot(k);

				if (slot != null && slot.stackSize > 0)
					if (doRemove)
						return inventory.decrStackSize(k, (int) powerProvider.useEnergy(1, slot.stackSize, true));
					else
						return slot;
			}

		return null;
	}

	@Override
	public void entityEntered(IPipedItem item, ForgeDirection orientation) {
		item.setSpeed(Utils.pipeNormalSpeed * 30F);
		if(worldObj.isRemote)
			return;
		World toWorld = getWorldForDimension(toDim);
		TileGenericPipe tile = getTile(toWorld, toPosX, toPosY, toPosZ);
		if(tile != null && !this.receive){
			ForgeDirection orient = tile.pipe.getOpenOrientation().getOpposite();
			PipeTransfer pipeto = null;
			pipeto = getPipe(tile);
			if (orient != ForgeDirection.UNKNOWN && (pipeto != null)) {
				boolean illegal = (toDim != pipeto.toDim) && !(dimpipe && pipeto.dimpipe);
				if(remoteMode && pipeto.remoteMode && !illegal){
					item.setWorld(toWorld);
					item.setPosition(toPosX + 0.5, toPosY + Utils.getPipeFloorOf(null),toPosZ + 0.5);
					//IPipedItem passive = new EntityPassiveItem(worldObj, toPosX + 0.5, toPosY + Utils.getPipeFloorOf(stack),toPosZ + 0.5, stack);
					//item.setSpeed(Utils.pipeNormalSpeed * 30F);
					//相手のパイプを受信モードに
					((PipeItemsTransfer)pipeto).receive = true;
					tile.pipe.transport.entityEntering(item, orient);
					TransferPipes.println("The item is teleported");
				}
			}
		}
		//受信モード解除
		this.receive = false;
		TransferPipes.println("passive transfer pipe");
	}

	@Override
	public void readjustSpeed(IPipedItem item) {
		((PipeTransportItems) transport).defaultReajustSpeed(item);
	}

	@Override
	public int powerRequest(ForgeDirection from) {
		return getPowerProvider().getMaxEnergyReceived();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return TransferPipes.iconProvider;
	}

}
