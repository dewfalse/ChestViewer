package chestviewer;

import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {

	@Override
	public void init() {
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
	}
}
