/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import org.openide.util.lookup.ServiceProvider;
import ru.kohei.exchanging.api.ExchengingClient;
import ru.kohei.exchanging.api.Message;

/**
 *
 * @author Prostov Yury
 */
@ServiceProvider(service=ExchengingClient.class)
public class DefaultExchangingClient extends DefaultExchangingPoint implements ExchengingClient {
    private Channel m_channel = null;
    private Bootstrap m_bootstrap = null;
    
    private class DefaultMessageHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRegistered(ChannelHandlerContext context) throws Exception {
            super.channelRegistered(context);
        }
        
        @Override
        public void channelUnregistered(ChannelHandlerContext context) throws Exception {
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
        protected void initChannel(SocketChannel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast("codec", new DefaultMessageCodec());
            pipeline.addLast("handler", new DefaultMessageHandler());
        }
        
    }
    
    public DefaultExchangingClient() {        
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
    
    private void open(InetSocketAddress address) {
        State state = state();
        if ((state != State.CLOSED) && (state != State.ERROR)) {
            return;
        }
        
        setState(State.OPENING);
        
        if (m_bootstrap == null) {
            m_bootstrap = new Bootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new DefaultChannelInitializer());
        }
        
        ChannelFuture future = m_bootstrap.connect(address);
        m_channel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boolean isOpened = m_channel.isOpen();
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
                    m_channel = null;
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
        ChannelFuture future = m_channel.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                m_channel = null;
                boolean wasClosed = future.isDone();
                setState(wasClosed ? State.CLOSED : State.ERROR);
            }
        });
    }

    @Override
    public void onMessageSent(Message message) {
        if (state() == State.OPENED) {
            m_channel.write(message);
        }
    }
    
}
