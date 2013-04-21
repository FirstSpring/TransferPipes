package mods.firstspring.transferpipes;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TransferPipesIconProvider implements IIconProvider {
	
	private boolean registered = false;
	
	public static final int item = 0;
	public static final int itemR = 1;
	public static final int itemA = 2;
	public static final int itemRS = 3;
	public static final int itemDim = 10;
	public static final int itemDimR = 11;
	public static final int itemDimA = 12;
	public static final int itemDimRS = 13;
	public static final int liq = 20;
	public static final int liqR = 21;
	public static final int liqA = 22;
	public static final int liqRS = 23;
	public static final int liqDim = 30;
	public static final int liqDimR = 31;
	public static final int liqDimA = 32;
	public static final int liqDimRS = 33;
	public static final int pow = 40;
	public static final int powR = 41;
	public static final int powA = 42;
	public static final int powDim = 50;
	public static final int powDimA = 0;
	public static final int rsBase = 58;
	public static final int rsSol = 59;
	
	@SideOnly(Side.CLIENT)
	Icon[] icons;

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		Icon ico = icons[iconIndex];
		if(ico == null)return icons[0];
		return icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister ir) {
		//if(registered)return;
		icons = new Icon[60];
		icons[item] = regist(ir, "pipeItemsTransfer");
		icons[itemR] = regist(ir, "pipeItemsTransfer_remote");
		icons[itemA] = regist(ir, "pipeItemsTransfer_anti");
		icons[itemRS] = regist(ir, "pipeItemsTransfer_redstone");
		icons[item + 10] = regist(ir, "pipeItemsDimTransfer");
		icons[itemR + 10] = regist(ir, "pipeItemsDimTransfer_remote");
		icons[itemA + 10] = regist(ir, "pipeItemsDimTransfer_anti");
		icons[itemRS + 10] = regist(ir, "pipeItemsDimTransfer_redstone");
		icons[liq] = regist(ir, "pipeLiquidsTransfer");
		icons[liqR] = regist(ir, "pipeLiquidsTransfer_remote");
		icons[liqA] = regist(ir, "pipeLiquidsTransfer_anti");
		icons[liqRS] = regist(ir, "pipeLiquidsTransfer_redstone");
		icons[liq + 10] = regist(ir, "pipeLiquidsDimTransfer");
		icons[liqR + 10] = regist(ir, "pipeLiquidsDimTransfer_remote");
		icons[liqA + 10] = regist(ir, "pipeLiquidsDimTransfer_anti");
		icons[liqRS + 10] = regist(ir, "pipeLiquidsDimTransfer_redstone");
		icons[pow] = regist(ir, "pipePowerTransfer");
		icons[powR] = regist(ir, "pipePowerTransfer_remote");
		icons[powA] = regist(ir, "pipePowerTransfer_anti");
		icons[pow + 10] = regist(ir, "pipePowerDimTransfer");
		icons[powR + 10] = regist(ir, "pipePowerDimTransfer_remote");
		icons[powA + 10] = regist(ir, "pipePowerDimTransfer_anti");
		icons[rsBase] = regist(ir, "pipeItemsRedstoneWood");
		icons[rsSol] = ir.registerIcon("buildcraft:pipeAllWood_solid");
		
	}
	
	public Icon regist(IconRegister ir, String name){
		return ir.registerIcon("firstspring/transferpipes:" + name);
	}

}
