package chestviewer;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class ChestViewerKeyHandler extends KeyHandler {

	static KeyBinding toggleKeyBinding = new KeyBinding(ChestViewer.modid, Keyboard.KEY_P);

	public ChestViewerKeyHandler() {
		super(new KeyBinding[] {toggleKeyBinding}, new boolean[] {true});
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		if(tickEnd) {
			Minecraft mc = Minecraft.getMinecraft();
			if(mc.currentScreen == null && mc.ingameGUI.getChatGUI().getChatOpen() == false) {
				ChestViewer.instance.enabled = !ChestViewer.instance.enabled;
				mc.thePlayer.addChatMessage("ChestViewer: " + (ChestViewer.instance.enabled ? "ON" : "OFF"));
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

}
