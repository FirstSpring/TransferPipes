package mods.firstspring.transferpipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
    @Override
    public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
    {
    	int type;
    	boolean isKeep,hasPosition;
    	String name;
        ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);
        type = dat.readInt();
        EntityPlayer entityplayer = (EntityPlayerMP)player;
        ItemStack is = entityplayer.getCurrentEquippedItem();
        NBTTagCompound nbt = is.getTagCompound();
        if(nbt == null){
        	nbt = new NBTTagCompound();
        	is.setTagCompound(nbt);
        }
        if(type == 0){
        	name = dat.readUTF();
        	nbt.setString("name", name);
        }else if(type == 1){
        	isKeep = dat.readBoolean();
        	nbt.setBoolean("isKeep", isKeep);
        }else if(type == 2){
        	nbt.setBoolean("flag", false);
        }
    }

    public static Packet getPacket()
    {
        return null;
    }
}