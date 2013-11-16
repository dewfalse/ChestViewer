package chestviewer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {

	@Override
	public void init() {
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
		KeyBindingRegistry.registerKeyBinding(new ChestViewerKeyHandler());
	}
}
