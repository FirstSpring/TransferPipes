package mods.firstspring.transferpipes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.ISpecialPipedConnection;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;

public class LogisticsTransferHandler implements ISpecialPipedConnection {

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileGenericPipe tile) {
		return WorldHelper.getPipe(tile) != null;
	}

	@Override
	public List<TileGenericPipe> getConnections(TileGenericPipe tile) {
		List list = new ArrayList();
		PipeTransfer pipe = WorldHelper.getPipe(tile);
		if(pipe == null)
			return list;
		if(!pipe.remoteMode)
			return list;
		if(pipe.getOpenOrientation() == ForgeDirection.UNKNOWN)
			return list;
		World world = WorldHelper.getWorldForDimension(pipe.toDim);
		TileGenericPipe tileto = WorldHelper.getTile(world, pipe.toPosX, pipe.toPosY, pipe.toPosZ);
		if(tileto == null)
			return list;
		list.add(tileto);
		return list;
	}
	
	//リフレクションにより呼び出し
	public static void reflect(){
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> clazz = null;
		try {
			clazz = loader.loadClass("logisticspipes.proxy.SimpleServiceLocator");
		} catch (ClassNotFoundException e) {
			printMessage(e, true);
			return;
		}
		Field f = null;
		try{
			f = clazz.getField("specialconnection");
		}catch(NoSuchFieldException e){
			printMessage(e, false);
			try{
			f = clazz.getField("specialpipeconnection");
			}catch(NoSuchFieldException e2){
				printMessage(e2, true);
				return;
			}
		}
		Class<?> clas = null;
		try {
			clas = loader.loadClass("logisticspipes.proxy.specialconnection.SpecialConnection");
		} catch (ClassNotFoundException e1) {
			printMessage(e1, false);
			try{
				clas = loader.loadClass("logisticspipes.proxy.specialconnection.SpecialPipeConnection");
			}catch(ClassNotFoundException e2){
				printMessage(e2, true);
				return;
			}
		}
		try{
		Method m = clas.getMethod("registerHandler", new Class[]{ISpecialPipedConnection.class});
		m.invoke(f.get(null), new LogisticsTransferHandler());
		}catch(Exception e){
			printMessage(e, true);
			return;
		}
	}
	
	public static void printMessage(Exception e, boolean printMessage){
		if(TransferPipes.isDebug)
			e.printStackTrace();
		if(printMessage)
			System.out.println("Logistics Pipes Not Found");
	}

}
