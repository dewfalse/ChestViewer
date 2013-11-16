package chestviewer;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathPoint;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = ChestViewer.modid, name = ChestViewer.modid, version = "1.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = true, channels = { ChestViewer.modid }, packetHandler = PacketHandler.class, connectionHandler = ConnectionHandler.class, versionBounds = "[1.0]")
public class ChestViewer {
	public static final String modid = "chestviewer";

	@SidedProxy(clientSide = "chestviewer.ClientProxy", serverSide = "chestviewer.CommonProxy")
	public static CommonProxy proxy;

	Map<PathPoint, ItemStack[]> chestMap = new LinkedHashMap();

	public boolean enabled = true;

	@Instance("ChestViewer")
	public static ChestViewer instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		instance = this;
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
	}
}
