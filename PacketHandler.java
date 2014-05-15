package chestviewer;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;

import java.io.*;

public class PacketHandler extends AbstractPacket {

    EnumCommand command;
    int x;
    int y;
    int z;
    ItemStack[] itemStacks = null;

    public PacketHandler() {

    }
    public PacketHandler(EnumCommand command, int x, int y, int z, ItemStack[] itemStacks) {
        this.command = command;
        this.x = x;
        this.y = y;
        this.z = z;
        this.itemStacks = itemStacks;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(command.ordinal());
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        if(itemStacks == null) {
            buffer.writeInt(0);
        }
        else {
            buffer.writeInt(itemStacks.length);
            for(ItemStack is : itemStacks) {
                ByteBufUtils.writeItemStack(buffer, is);
            }
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        command = EnumCommand.values()[buffer.readInt()];
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        int size = buffer.readInt();
        if(size == 0) {
            itemStacks = null;
        }
        else {
            itemStacks = new ItemStack[size];
            for(int i = 0; i < size; ++i) {
                itemStacks[i] = ByteBufUtils.readItemStack(buffer);
            }
        }
    }
    @Override
    public void handleClientSide(EntityPlayer player) {
        if(ChestViewer.instance == null) {
            return;
        }

        if(ChestViewer.instance.chestMap == null) {
            return;
        }

        ChestViewer.instance.chestMap.put(new PathPoint(x, y, z), itemStacks);
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

        EntityPlayerMP entityPlayer = (EntityPlayerMP)player;
        TileEntity tileEntity = entityPlayer.worldObj.getTileEntity(x, y, z);
        if(tileEntity == null) {
            return;
        }

        if(tileEntity instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) tileEntity;
            TileEntityChest[] chests = {chest.adjacentChestXNeg, chest.adjacentChestZNeg, chest, chest.adjacentChestXPos, chest.adjacentChestZPos};

            int invSize = 0;
            for (TileEntityChest c : chests) {
                if (c == null) {
                    continue;
                }
                invSize += c.getSizeInventory();
            }
            ItemStack[] itemStacks = new ItemStack[invSize];

            int index = 0;
            for (TileEntityChest c : chests) {
                if (c == null) {
                    continue;
                }

                for (int i = 0; i < c.getSizeInventory(); ++i) {
                    itemStacks[index++] = c.getStackInSlot(i);
                }
            }

            sendPacket(x, y, z, itemStacks);
        }
    }

	private void sendPacket(int x, int y, int z, ItemStack[] itemStacks) {
        ChestViewer.packetPipeline.sendPacketToAllPlayer(new PacketHandler(EnumCommand.RESPONSE, x, y, z, itemStacks));
	}


}
