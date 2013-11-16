package chestviewer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ShortBuffer;
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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler {
	protected static RenderItem itemRenderer = new RenderItem();

	private static ShortBuffer slotVertex;

	static{
		slotVertex = getBoxVertexBuffer(16, 16);
	}

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

			if(ChestViewer.instance.enabled == false) {
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

		int columnNum = 9;
		int rowNum = itemStacks.length / 9;
		int var8 = columnNum * 18 + 16;
		int var9 = (rowNum + 1) * 18;
		int var10 = (width - var8) / 2;
		int var11 = (height - (rowNum + 2) * 18) / 2;

		GL11.glPushMatrix();
		GL11.glTranslatef((float)(var10 + 8), (float)(var11 + 18), 0.0F);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		this.renderBoxes(columnNum, itemStacks.length / columnNum);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glPopMatrix();

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

	private static void renderBoxes(int[] ... var0)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		int[][] var1 = var0;
		int var2 = var0.length;

		for (int var3 = 0; var3 < var2; ++var3)
		{
			int[] var4 = var1[var3];
			renderBox(slotVertex, var4[0], var4[1]);
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private static void renderBoxes(int var0, int var1)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);

		for (int var2 = 0; var2 < var0; ++var2)
		{
			for (int var3 = 0; var3 < var1; ++var3)
			{
				renderBox(slotVertex, var2 * 18, var3 * 18);
			}
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private static void renderBox(ShortBuffer var0, int var1, int var2)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)var1, (float)var2, 0.0F);
		GL11.glLineWidth(1.0F);
		GL11.glColor4f(0.5F, 1.0F, 1.0F, 1.0F);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glVertexPointer(2, 0, var0);
		GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, GL11.GL_TRIANGLES);
		GL11.glPopMatrix();
	}

	private static ShortBuffer getBoxVertexBuffer(int var0, int var1)
	{
		boolean var2 = true;
		short[] var3 = new short[] {(short)0, (short)0, (short)0, (short)var1, (short)var0, (short)var1, (short)var0, (short)0};
		int var4 = 0;
		ShortBuffer var5 = BufferUtils.createShortBuffer(var3.length * 2);

		for (short var6 = 0; var6 < var3.length; var6 = (short)(var6 + 2))
		{
			short var7 = var3[var6];
			short var8 = var3[var6 + 1];
			var5.put(var7);
			var5.put(var8);
			var4 += 2;
		}

		var5.flip();
		return var5;
	}

}
