package chestviewer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentTranslation;
import org.lwjgl.input.Keyboard;

public class ChestViewerKeyHandler {

	static KeyBinding toggleKeyBinding = new KeyBinding(ChestViewer.modid, Keyboard.KEY_P, ChestViewer.modid);

	public ChestViewerKeyHandler() {
        ClientRegistry.registerKeyBinding(toggleKeyBinding);
	}
    @SubscribeEvent
    public void KeyInputEvent(InputEvent.KeyInputEvent event) {
        if (!FMLClientHandler.instance().isGUIOpen(GuiChat.class)) {
            if(toggleKeyBinding.isPressed()) {
                ChestViewer.instance.enabled = !ChestViewer.instance.enabled;
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("ChestViewer: " + (ChestViewer.instance.enabled ? "ON" : "OFF")));
            }
        }
	}

}
