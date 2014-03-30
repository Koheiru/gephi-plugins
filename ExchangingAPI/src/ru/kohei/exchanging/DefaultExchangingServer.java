/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.openide.util.lookup.ServiceProvider;
import ru.kohei.exchanging.api.ExchangingServer;
import ru.kohei.exchanging.api.Message;

/**
 *
 * @author Prostov Yury
 */
@ServiceProvider(service=ExchangingServer.class)
public class DefaultExchangingServer extends DefaultExchangingPoint implements ExchangingServer {
    private Channel m_serverChannel = null;
    private ChannelGroup m_clientChannelGroup = null;
    private ServerBootstrap m_bootstrap = null;
    
    private class DefaultMessageHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRegistered(ChannelHandlerContext context) throws Exception {
            m_clientChannelGroup.add(context.channel());
            super.channelRegistered(context);
        }
        
        @Override
        public void channelUnregistered(ChannelHandlerContext context) throws Exception {
            m_clientChannelGroup.remove(context.channel());
            super.channelUnregistered(context);
        }
        
        @Override
        public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
            boolean isExpectedType = (object instanceof Message);
            if (!isExpectedType) {
                return;
            }
            
            Message message = (Message)object;
            handleReceivedMessage(message);
            
            super.channelRead(context, object);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
    
    private class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast("codec", new DefaultMessageCodec());
            pipeline.addLast("handler", new DefaultMessageHandler());
        }
    }
    
    public DefaultExchangingServer() {
    }
    
    @Override
    public void finalize() throws Throwable {
        closeChannel();
        super.finalize();
    }
    
    @Override
    public void open(int port) {
        open(new InetSocketAddress(port));
    }

    @Override
    public void open(String address, int port) {
        open(new InetSocketAddress(address, port));
    }

    private void open(SocketAddress address) {
        State state = state();
        if ((state != State.CLOSED) && (state != State.ERROR)) {
            return;
        }
        
        setState(State.OPENING);
        
        if (m_bootstrap == null) {
            m_bootstrap = new ServerBootstrap()
                    .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new DefaultChannelInitializer());
        }
        
        m_clientChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        
        ChannelFuture future = m_bootstrap.bind(address);
        m_serverChannel = (ServerChannel)future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boolean isOpened = m_serverChannel.isOpen();
                if (isOpened) {
                    State state = state();
                    if (state == State.CLOSING) {
                        closeChannel();
                    }
                    else {
                        setState(State.OPENED);
                    }
                }
                else {
                    m_serverChannel = null;
                    m_clientChannelGroup = null;
                    setState(State.ERROR);
                }
            }
        });
    }
    
    @Override
    public void close() {
        State state = state();
        if (state != State.OPENED) {
            if (state == State.OPENING) {
                setState(State.CLOSING);
            }
            return;
        }
        setState(State.CLOSING);
        closeChannel();
    }
    
    private void closeChannel() {
        m_clientChannelGroup.add(m_serverChannel);
        m_serverChannel = null;
        
        ChannelGroupFuture future = m_clientChannelGroup.close();
        future.addListener(new ChannelGroupFutureListener() {
            @Override
            public void operationComplete(ChannelGroupFuture future) throws Exception {
                m_clientChannelGroup = null;
                boolean wasClosed = future.isDone();
                setState(wasClosed ? State.CLOSED : State.ERROR);
            }
        });
    }
    
    @Override
    public void onMessageSent(Message message) {
        if (state() == State.OPENED) {
            m_clientChannelGroup.writeAndFlush(message);
        }
    }
}
