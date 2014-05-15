package chestviewer;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

import java.util.*;

/*
 * https://github.com/reginn/Tutorial-Packetの完全なるコピペ
 * 圧倒的感謝・・・っ！
 */

/*
 * http://www.minecraftforge.net/wiki/Netty_Packet_Handlingにあるコードをほぼそのまま使用.
 * コメントは説明用に書き変えてます.
 * AKさんが日本語訳を作ってくれました.
 * http://forum.minecraftuser.jp/viewtopic.php?f=21&t=18255
 */

/*
 * パケットを扱う総合的なクラス.
 * パケットの登録から送受信を担う.
 */
@ChannelHandler.Sharable
public class PacketPipeline extends MessageToMessageCodec<FMLProxyPacket, AbstractPacket>
{
    private EnumMap<Side, FMLEmbeddedChannel> channels;
    private LinkedList<Class<? extends AbstractPacket>> packets           = Lists.newLinkedList();
    private boolean                                     isPostInitialised = false;

    /*
     * パケットを登録するメソッド.
     * AbstractPacketクラスを継承したクラスを登録する.
     */
    public boolean registerPacket(Class<? extends AbstractPacket> clazz)
    {
        if (this.packets.size() > 256 | this.packets.contains(clazz) | this.isPostInitialised)
        {
            return false;
        }

        this.packets.add(clazz);
        return true;
    }

    /*
      * PacketPipelineの初期化を行うメソッド.
      * 具体的にはNetworkRegistryに新しいチャンネルを登録する.
      * 今回は引数をチャンネル名として登録する.
      */
    public void init(String channelName)
    {
        this.channels = NetworkRegistry.INSTANCE.newChannel(channelName, this);
    }

    /*
     * PacketPipelineに登録されたパケットを整理するメソッド.
     * FMLPostInitializationで呼ぶ必要がある.
     */
    public void postInit()
    {
        if (this.isPostInitialised)
        {
            return;
        }

        this.isPostInitialised = true;
        Collections.sort(this.packets, new Comparator<Class<? extends AbstractPacket>>()
        {
            @Override
            public int compare(Class<? extends AbstractPacket> clazz1, Class<? extends AbstractPacket> clazz2)
            {
                int com = String.CASE_INSENSITIVE_ORDER.compare(clazz1.getCanonicalName(), clazz2.getCanonicalName());
                if (com == 0)
                {
                    com = clazz1.getCanonicalName().compareTo(clazz2.getCanonicalName());
                }
                return com;
            }
        });
    }

    /*
     * 送るパケットをバイト列に書きだすメソッド.
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractPacket msg, List<Object> out) throws Exception
    {
        ByteBuf buffer = Unpooled.buffer();
        Class<? extends AbstractPacket> clazz = msg.getClass();
        if (!this.packets.contains(msg.getClass()))
        {
            throw new NullPointerException("No Packet Registered for: " + msg.getClass().getCanonicalName());
        }

        byte discriminator = (byte) this.packets.indexOf(clazz);
        buffer.writeByte(discriminator);
        msg.encodeInto(ctx, buffer);
        FMLProxyPacket proxyPacket = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
        out.add(proxyPacket);
    }

    /*
     * 受け取ったパケットを読み込んで実行するメソッド.
     * ここでパケットのhandleClient/ServerSide()が実行される.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception
    {
        ByteBuf payload = msg.payload();
        byte discriminator = payload.readByte();
        Class<? extends AbstractPacket> clazz = this.packets.get(discriminator);
        if (clazz == null)
        {
            throw new NullPointerException("No packet registered for discriminator: " + discriminator);
        }

        AbstractPacket pkt = clazz.newInstance();
        pkt.decodeInto(ctx, payload.slice());

        EntityPlayer player;
        switch (FMLCommonHandler.instance().getEffectiveSide())
        {
            case CLIENT:
                player = this.getClientPlayer();
                pkt.handleClientSide(player);
                break;

            case SERVER:
                INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
                player = ((NetHandlerPlayServer)netHandler).playerEntity;
                pkt.handleServerSide(player);
                break;

            default:
        }

        out.add(pkt);
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer()
    {
        return Minecraft.getMinecraft().thePlayer;
    }

    /*
     * 全プレイヤーにパケットを送るメソッド.
     */
    public void sendPacketToAllPlayer(AbstractPacket packet)
    {
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        this.channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /*
     * 特定プレイヤーにパケットを送るメソッド.
     */
    public void sendPacketToPlayer(AbstractPacket packet, EntityPlayerMP player)
    {
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        this.channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /*
     * 特定位置周囲にいるプレイヤーにパケットを送るメソッド.
     */
    public void sendPacketToAllAround(AbstractPacket packet, NetworkRegistry.TargetPoint point)
    {
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        this.channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /*
     * 特定ディメンションにいるプレイヤー全てにパケットを送るメソッド.
     */
    public void sendPacketToAllInDimension(AbstractPacket packet, int dimensionId)
    {
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        this.channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /*
     * クライアントからサーバーにパケットを送るメソッド.
     */
    public void sendPacketToServer(AbstractPacket packet)
    {
        this.channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.channels.get(Side.CLIENT).writeAndFlush(packet);
    }
}
