package mods.firstspring.transferpipes;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.transport.TransportProxyClient;

public class ClientProxy extends CommonProxy {

	@Override
	void loadTexture() {
		MinecraftForgeClient.preloadTexture("/sprite/telepipes.png");
	}

	@Override
	void openGui(ItemStack itemstack) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiRemoteConnector(itemstack));
	}
	
	@Override
	boolean isServer(){
		return false;
	}

	@Override
	void registerPipeRender(int itemid) {
		MinecraftForgeClient.registerItemRenderer(itemid, TransportProxyClient.pipeItemRenderer);
	}


}
