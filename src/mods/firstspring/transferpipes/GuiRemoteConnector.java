package mods.firstspring.transferpipes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiRemoteConnector extends GuiScreen {
	ItemStack stack;
	boolean isKeep = false, hasPosition = false;
	int x,y,z,dim,type;
	String name = "", strType = "";
	GuiTextField namebox;
	public GuiRemoteConnector(ItemStack itemstack){
		this.stack = itemstack;
		NBTTagCompound nbt = stack.getTagCompound();
		if(nbt == null)
			return;
		this.isKeep = nbt.getBoolean("isKeep");
		this.name = nbt.getString("name");
		this.hasPosition = nbt.getBoolean("flag");
		if(hasPosition){
			this.x = nbt.getInteger("posx");
			this.y = nbt.getInteger("posy");
			this.z = nbt.getInteger("posz");
			this.dim = nbt.getInteger("dim");
			this.type = nbt.getInteger("type");
			if(type == 1){
				strType = "Item";
			}else if(type == 2){
				strType = "Liquids";
			}else if(type == 3){
				strType = "Power";
			}

		}
	}

	@Override
	public void initGui(){
		this.buttonList.clear();
		byte var1 = -16;
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + var1, 200, 20, "Regist"));
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96 + var1, 98, 20, "Keep Position : " + getStatFromBoolean(isKeep)));
		this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height / 4 + 96 + var1, 98, 20, "Clear Position"));
		this.namebox = new GuiTextField(this.fontRenderer, this.width / 2 - 100, 100, 200, 20);
		namebox.setText(name);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, "Remote Connector Setting Menu", this.width / 2, 40, 16777215);
		this.namebox.drawTextBox();
		if(hasPosition){
			this.drawCenteredString(this.fontRenderer, "Position : [" + x + "," + y + "," + z + "]", this.width / 2, 60, 16777215);
			this.drawCenteredString(this.fontRenderer, "Dim : " + dim, this.width / 2, 70, 16777215);
			this.drawCenteredString(this.fontRenderer, "Type : " + strType, this.width / 2, 80, 16777215);
		}else{

		}
		super.drawScreen(par1, par2, par3);
	}

	@Override
	public void keyTyped(char par1, int par2)
	{
		if(!this.namebox.textboxKeyTyped(par1, par2)){
			if (par2 == 1 || par2 == this.mc.gameSettings.keyBindInventory.keyCode)
				this.mc.thePlayer.closeScreen();
		}
	}

	@Override
	public void mouseClicked(int par1, int par2, int par3)
	{
		super.mouseClicked(par1, par2, par3);
		this.namebox.mouseClicked(par1, par2, par3);
	}

	@Override
	public void actionPerformed(GuiButton button){
		if(button.id == 0){
			PacketDispatcher.sendPacketToServer(createPacket(0));
		}else if(button.id == 1){
			isKeep = !isKeep;
			button.displayString = "Keep Position : " + getStatFromBoolean(isKeep);
			PacketDispatcher.sendPacketToServer(createPacket(1));
		}else if(button.id == 2){
			hasPosition = false;
			PacketDispatcher.sendPacketToServer(createPacket(2));

		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	public String getStatFromBoolean(boolean f){
		return f ? "ON" : "OFF";
	}

	public Packet createPacket(int type){
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(b);
		try{
			data.writeInt(type);
			if(type == 0)
				data.writeUTF(this.namebox.getText().trim());
			if(type == 1)
				data.writeBoolean(isKeep);
		}catch(Exception e){

		}
		//Packet packet = 
				return new Packet250CustomPayload("transferpipes", b.toByteArray());
	}


}
