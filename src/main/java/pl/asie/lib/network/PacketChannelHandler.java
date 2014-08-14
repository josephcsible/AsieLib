package pl.asie.lib.network;

import java.lang.ref.WeakReference;
import java.util.List;

import pl.asie.lib.AsieLibMod;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.AttributeKey;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class PacketChannelHandler extends MessageToMessageCodec<FMLProxyPacket, Packet> {
	private final MessageHandlerBase handlerClient, handlerServer;
	
    public PacketChannelHandler(MessageHandlerBase client, MessageHandlerBase server) {
    	this.handlerClient = client;
    	this.handlerServer = server;
    }

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg,
			List<Object> out) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        msg.toBytes(buffer);
        FMLProxyPacket proxy = new FMLProxyPacket(buffer, ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
        out.add(proxy);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg,
			List<Object> out) throws Exception {
        Packet newMsg = new Packet();
        newMsg.fromBytes(msg.payload());
        INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        AsieLibMod.proxy.handlePacket(handlerClient, handlerServer, newMsg, netHandler);
	}
}
