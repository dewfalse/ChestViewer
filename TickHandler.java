package chestviewer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumMovingObjectType;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler {
	protected static RenderItem itemRenderer = new RenderItem();

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		Minecraft mc = Minecraft.getMinecraft();
		if(mc == null) {
			return;
		}

		if(mc.objectMouseOver == null) {
			return;
		}

		if(mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			int x = mc.objectMouseOver.blockX;
			int y = mc.objectMouseOver.blockY;
			int z = mc.objectMouseOver.blockZ;
			TileEntity tileEntity = mc.theWorld.getBlockTileEntity(x, y, z);
			if(tileEntity == null) {
				return;
			}
			if(tileEntity instanceof TileEntityChest) {
				sendPacket(x, y, z);
			}
			int blockID = mc.theWorld.getBlockId(x, y, z);
			if(blockID != Block.chest.blockID) {
				return;
			}

			if(ChestViewer.instance == null) {
				return;
			}

			if(ChestViewer.instance.chestMap == null) {
				return;
			}

			ItemStack[] itemStacks = ChestViewer.instance.chestMap.get(new PathPoint(x, y, z));
			if(itemStacks != null) {
				if(mc.currentScreen == null && mc.ingameGUI.getChatGUI().getChatOpen() == false) {
					render(itemStacks);
				}
			}
		}

	}

	private void render(ItemStack[] itemStacks) {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution scaledresolution = new ScaledResolution(
				mc.gameSettings, mc.displayWidth, mc.displayHeight);
		int width = scaledresolution.getScaledWidth();
		int height = scaledresolution.getScaledHeight();
		for(int i = 0; i < itemStacks.length; ++i) {
			if(itemStacks[i] == null) {
				continue;
			}
			int w = (int) (((i % 9) - 4.5) * 18);
			int h = (int) ((i / 9 - (itemStacks.length / 9 / 2.0)) * 18);
			drawItemStack(itemStacks[i], width / 2 + w, height / 2 + h);
		}

	}

	private void drawItemStack(ItemStack par1ItemStack, int par2, int par3) {
		Minecraft mc = Minecraft.getMinecraft();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glPushMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		short short1 = 240;
		short short2 = 240;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) short1 / 1.0F, (float) short2 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		itemRenderer.zLevel = 200.0F;
		itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, par1ItemStack, par2, par3);
		itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, par1ItemStack, par2, par3);
		itemRenderer.zLevel = 0.0F;
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
	}

	private void sendPacket(int x, int y, int z) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try {
			stream.writeUTF("REQUEST");
			stream.writeInt(x);
			stream.writeInt(y);
			stream.writeInt(z);

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = ChestViewer.modid;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;
			Minecraft mc = Minecraft.getMinecraft();
			mc.thePlayer.sendQueue.addToSendQueue(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return null;
	}

}
