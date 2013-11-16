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
					TileEntityChest[] chests = {chest.adjacentChestXNeg, chest.adjacentChestZNeg, chest, chest.adjacentChestXPos, chest.adjacentChestZPosition};

					int invSize = 0;
					for(TileEntityChest c : chests) {
						if(c == null) {
							continue;
						}
						invSize += c.getSizeInventory();
					}
					ItemStack[] itemStacks = new ItemStack[invSize];

					int index = 0;
					for(TileEntityChest c : chests) {
						if(c == null) {
							continue;
						}

						for(int i = 0; i < c.getSizeInventory(); ++i) {
							itemStacks[index++] = c.getStackInSlot(i);
						}
					}

					sendPacket(manager, player, x, y, z, itemStacks);
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

	private void sendPacket(INetworkManager manager, Player player, int x, int y, int z, ItemStack[] itemStacks) {
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

			manager.addToSendQueue(packet);
			//ChestViewer.proxy.sendToPlayer(packet, player);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}


}
