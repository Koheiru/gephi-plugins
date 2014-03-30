/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.CharsetUtil;
import java.util.Arrays;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.kohei.exchanging.api.Message;

/**
 *
 * @author Prostov Yury
 */
public class DefaultMessageCodec extends ByteToMessageCodec<Message> {
    private String m_content = new String();

    @Override
    protected void encode(ChannelHandlerContext context, Message message, ByteBuf out) throws Exception {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("source", message.source());
        jsonMessage.put("action", message.action());
        
        Object messageData = message.data();
        if (messageData instanceof Object[]) {
            JSONArray paramsArray = new JSONArray();
            paramsArray.addAll(Arrays.asList((Object[])messageData));
            jsonMessage.put("data", paramsArray);
        } else {
            jsonMessage.put("data", messageData);
        }
        
        String data = jsonMessage.toJSONString();
        byte[] rawData = data.getBytes(CharsetUtil.UTF_8);
        out.writeBytes(rawData);
    }
    
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
        m_content = m_content + in.toString(CharsetUtil.UTF_8);
        in.readerIndex(in.writerIndex());

        JSONParser parser = new JSONParser();

        int begin = 0;
        int depth = 0;
        for (int i = 0; i < m_content.length(); ++i) {
            char ch = m_content.charAt(i);
            if (ch == '{') {
                if (depth == 0) {
                    begin = i;
                }
                ++depth;
            } else if (ch == '}') {
                if (depth == 1) {
                    String objectData = m_content.substring(begin, i + 1);
                    JSONObject jsonMessage = (JSONObject)parser.parse(objectData);
                    String source = (String)jsonMessage.get("source");
                    String action = (String)jsonMessage.get("action");
                    Object data = jsonMessage.get("data");
                    
                    if (data instanceof JSONArray) {
                        data = ((JSONArray)data).toArray();
                    }
                    
                    Message message = new Message(source, action, data);
                    out.add(message);
                }
                --depth;
            }
        }

        if (depth == 0) {
            m_content = new String();
        }
        else {
            m_content = m_content.substring(begin, m_content.length());
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    
}
