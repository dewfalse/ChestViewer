package chestviewer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {

		if(packet.channel.equals(ChestViewer.modid) == false) {
			return;
		}
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			String command = stream.readUTF();

			if(command.equals("REQUEST")) {
				int x = stream.readInt();
				int y = stream.readInt();
				int z = stream.readInt();

				EntityPlayerMP entityPlayer = (EntityPlayerMP)player;
				TileEntity tileEntity = entityPlayer.worldObj.getBlockTileEntity(x, y, z);
				if(tileEntity == null) {
					return;
				}

				if(tileEntity instanceof TileEntityChest) {
					TileEntityChest chest = (TileEntityChest)tileEntity;
					ItemStack[] itemStacks = new ItemStack[chest.getSizeInventory()];
					for(int i = 0; i < itemStacks.length; ++i) {
						itemStacks[i] = chest.getStackInSlot(i);
					}

					sendPacket(manager, x, y, z, itemStacks);
				}
			}
			else if(command.equals("RESPONSE")) {
				int x = stream.readInt();
				int y = stream.readInt();
				int z = stream.readInt();

				int invSize = stream.readInt();
				ItemStack[] itemStacks = new ItemStack[invSize];
				for(int i = 0; i < itemStacks.length; ++i) {
					itemStacks[i] = packet.readItemStack(stream);
				}
				if(ChestViewer.instance == null) {
					return;
				}

				if(ChestViewer.instance.chestMap == null) {
					return;
				}

				ChestViewer.instance.chestMap.put(new PathPoint(x, y, z), itemStacks);

			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void sendPacket(INetworkManager manager, int x, int y, int z, ItemStack[] itemStacks) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try {
			stream.writeUTF("RESPONSE");
			stream.writeInt(x);
			stream.writeInt(y);
			stream.writeInt(z);

			stream.writeInt(itemStacks.length);
			for(ItemStack is : itemStacks) {
				Packet.writeItemStack(is, stream);
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = ChestViewer.modid;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;
			Minecraft mc = Minecraft.getMinecraft();
			mc.thePlayer.sendQueue.addToSendQueue(packet);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}


}
