package mods.firstspring.transferpipes;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.SidedProxy;

public class CommonProxy {
	@SidedProxy(clientSide = "mods.firstspring.transferpipes.ClientProxy", serverSide = "mods.firstspring.transferpipes.CommonProxy")
	public static CommonProxy proxy;
	
	void loadTexture(){
		
	}
	
	void openGui(ItemStack itemstack){
		
	}
	
	boolean isServer(){
		return true;
	}
	
	void registerPipeRender(int itemid){
		
	}
}
