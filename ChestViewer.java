package chestviewer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathPoint;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod(modid = ChestViewer.modid, name = ChestViewer.modid, version = "1.0")
public class ChestViewer {
	public static final String modid = "chestviewer";

	@SidedProxy(clientSide = "chestviewer.ClientProxy", serverSide = "chestviewer.CommonProxy")
	public static CommonProxy proxy;

	Map<PathPoint, ItemStack[]> chestMap = new LinkedHashMap();

	public boolean enabled = true;

    public static final PacketPipeline packetPipeline = new PacketPipeline();

	@Instance("ChestViewer")
	public static ChestViewer instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		instance = this;
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
        packetPipeline.init(ChestViewer.modid);
        packetPipeline.registerPacket(PacketHandler.class);
	}

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        packetPipeline.postInit();
    }
}
