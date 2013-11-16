package chestviewer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumMovingObjectType;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler {

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
				for(ItemStack is : itemStacks) {
					if(is != null) {
						System.out.println(is.getDisplayName());
					}
				}
			}
		}

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
