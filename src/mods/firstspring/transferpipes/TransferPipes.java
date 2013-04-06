package mods.firstspring.transferpipes;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Localization;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid="TransferPipes", name="TransferPipes", version="Build 9")
//channelsで登録したパケットをpackethandlerで指定したパケットハンドラに送信
@NetworkMod(channels = {"transferpipes"}, clientSideRequired=true, serverSideRequired=true, packetHandler = PacketHandler.class)
public class TransferPipes{
	@Instance("TransferPipes")
	public static TransferPipes instance;
	public static TransferPipesIconProvider iconProvider = new TransferPipesIconProvider();
	
	//public final static PipeItemRenderer pipeItemRenderer = new PipeItemRenderer();
	public static Item pipeItemsTransfer;
	public static int pipeItemsTransferId;
	public static Item pipeLiquidsTransfer;
	public static int pipeLiquidsTransferId;
	public static Item pipePowerTransfer;
	public static int pipePowerTransferId;
	public static Item pipeItemsDimTransfer;
	public static int pipeItemsDimTransferId;
	public static Item pipeLiquidsDimTransfer;
	public static int pipeLiquidsDimTransferId;
	public static Item pipePowerDimTransfer;
	public static int pipePowerDimTransferId;
	public static Item pipeItemsRedstoneWood;
	public static int pipeItemsRedstoneWoodId;
	public static Item remoteConnector;
	public static int remoteConnectorId;
	
	public static boolean balanceMode;
	public static boolean isDebug;
	public static boolean enableInsertion;
	
	
	@PreInit
	public void loadConfiguration(FMLPreInitializationEvent evt){
		Configuration cfg = new Configuration(evt.getSuggestedConfigurationFile());
		cfg.load();
		Property prop;
		prop = cfg.get(Configuration.CATEGORY_ITEM, "pipeItemsTransfer.id", 19300);
		pipeItemsTransferId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_ITEM, "pipeLiquidsTransfer.id", 19301);
		pipeLiquidsTransferId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_ITEM, "pipePowerTransfer.id", 19302);
		pipePowerTransferId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_ITEM, "remoteConnector.id", 19303);
		remoteConnectorId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_ITEM, "pipeItemsRedstoneWood.id", 19304);
		pipeItemsRedstoneWoodId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_ITEM, "pipeItemsDimTransfer.id", 19305);
		pipeItemsDimTransferId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_ITEM, "pipeLiquidsDimTransfer.id", 19306);
		pipeLiquidsDimTransferId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_ITEM, "pipePowerDimTransfer.id", 19307);
		pipePowerDimTransferId = prop.getInt();
		prop = cfg.get(Configuration.CATEGORY_GENERAL, "BalanceRecipe", false);
		balanceMode = prop.getBoolean(true);
		prop = cfg.get(Configuration.CATEGORY_GENERAL, "isDebug", false);
		isDebug = prop.getBoolean(true);
		prop = cfg.get(Configuration.CATEGORY_GENERAL, "Enable_Insertion_Function", false);
		enableInsertion = prop.getBoolean(true);
		cfg.save();
		MinecraftForge.EVENT_BUS.register(this);
		//preInitで登録しないと手持ちのアイテムのテクスチャが化ける
		//内部でIconインスタンスを保持してしまうからだと思われ
		pipeItemsTransfer = registerPipe(pipeItemsTransferId, PipeItemsTransfer.class, "Transfer Transport Pipe");
		pipeLiquidsTransfer = registerPipe(pipeLiquidsTransferId, PipeLiquidsTransfer.class, "Transfer Waterproof Pipe");
		pipePowerTransfer = registerPipe(pipePowerTransferId, PipePowerTransfer.class, "Transfer Conductive Pipe");
		pipeItemsRedstoneWood = registerPipe(pipeItemsRedstoneWoodId, PipeItemsRedstoneWood.class, "Redstone Wood Transport Pipe");
		pipeItemsDimTransfer = registerPipe(pipeItemsDimTransferId, PipeItemsDimTransfer.class, "Dimensional Transfer Transport Pipe");
		pipeLiquidsDimTransfer = registerPipe(pipeLiquidsDimTransferId, PipeLiquidsDimTransfer.class, "Dimensional Transfer Waterproof Pipe");
		pipePowerDimTransfer = registerPipe(pipePowerDimTransferId, PipePowerDimTransfer.class, "Dimensional Transfer Conductive Pipe");
		remoteConnector = new ItemRemoteConnector(remoteConnectorId).setUnlocalizedName("rimouto").setCreativeTab(CreativeTabs.tabTools);
	}
	@Init
	public void initialize(FMLInitializationEvent evt)
	{
		CommonProxy.proxy.loadTexture();
		
		LanguageRegistry.addName(remoteConnector, "Remote Connector");
		Localization.addLocalization("/mods/firstspring/transferpipes/lang/", DefaultProps.DEFAULT_LANGUAGE);
		Object craftbase;
		if(balanceMode){
			craftbase = new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3);
		}
		else{
			craftbase = new ItemStack(Item.dyePowder, 1, 4);
		}
		GameRegistry.addShapelessRecipe(new ItemStack(pipeItemsTransfer), new Object[]{craftbase, BuildCraftTransport.pipeItemsGold});
		GameRegistry.addShapelessRecipe(new ItemStack(pipeLiquidsTransfer), new Object[]{pipeItemsTransfer, BuildCraftTransport.pipeWaterproof});
		GameRegistry.addShapelessRecipe(new ItemStack(pipeLiquidsTransfer), new Object[]{craftbase, BuildCraftTransport.pipeLiquidsGold});
		GameRegistry.addShapelessRecipe(new ItemStack(pipePowerTransfer, 1), new Object[]{pipeItemsTransfer, Item.redstone});
		GameRegistry.addShapelessRecipe(new ItemStack(pipePowerTransfer), new Object[]{craftbase, BuildCraftTransport.pipePowerGold});
		GameRegistry.addShapelessRecipe(new ItemStack(pipeItemsDimTransfer, 1), new Object[]{Item.eyeOfEnder, pipeItemsTransfer});
		GameRegistry.addShapelessRecipe(new ItemStack(pipeLiquidsDimTransfer, 1), new Object[]{Item.eyeOfEnder, pipeLiquidsTransfer});
		GameRegistry.addShapelessRecipe(new ItemStack(pipeLiquidsDimTransfer, 1), new Object[]{pipeItemsDimTransfer, BuildCraftTransport.pipeWaterproof});
		GameRegistry.addShapelessRecipe(new ItemStack(pipePowerDimTransfer, 1), new Object[]{pipeItemsDimTransfer, Item.redstone});
		GameRegistry.addShapelessRecipe(new ItemStack(pipePowerDimTransfer), new Object[]{Item.eyeOfEnder, pipePowerTransfer});
		GameRegistry.addRecipe(new ItemStack(pipeItemsRedstoneWood, 8), new Object[]{" E ", "RGR", 
																					Character.valueOf('E'), new ItemStack(BuildCraftEnergy.engineBlock, 1, 0), 
																					Character.valueOf('R'), Item.redstone, 
																					Character.valueOf('G'), Block.glass});
		GameRegistry.addRecipe(new ItemStack(remoteConnector, 1), new Object[]{"X X", " Y ", " X ", 
																				Character.valueOf('X'), craftbase, 
																				Character.valueOf('Y'), BuildCraftCore.wrenchItem});
		GameRegistry.addShapelessRecipe(new ItemStack(remoteConnector, 1), new Object[]{remoteConnector});
		LogisticsTransferHandler.reflect();
		System.out.println("Transfer Pipes Loaded");
	}
	
	public Item registerPipe(int id, Class<? extends Pipe> clas, String name){
		ItemPipe pipe = BlockGenericPipe.registerPipe(id, clas);
		pipe.setUnlocalizedName(clas.getSimpleName());
		LanguageRegistry.addName(pipe, name);
		CommonProxy.proxy.registerPipeRender(pipe.itemID);
		return pipe;
	}
	
	//テクスチャを登録するイベント
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event){
		if (event.map == Minecraft.getMinecraft().renderEngine.textureMapBlocks) {
			iconProvider.registerIcons(event.map);
		}

	}
	
	public static void println(Object obj){
		if(isDebug)
			System.out.println(obj);
	}
	
}