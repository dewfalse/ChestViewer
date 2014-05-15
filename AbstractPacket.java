package chestviewer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

public abstract class AbstractPacket
{
    /*
     * encodeInto     : byte配列にデータを書き込むメソッド
     * decodeInto    : byte配列からデータを読む込むメソッド
     * handleClientSide : クライアント側のパケット処理
     * handleServerSide : サーバー側のパケット処理
     */
    public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer);
    public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer);
    public abstract void handleClientSide(EntityPlayer player);
    public abstract void handleServerSide(EntityPlayer player);
}
