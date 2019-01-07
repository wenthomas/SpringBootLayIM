package com.fyp.im.handler;

import com.fyp.im.utils.LogUtil;
import com.fyp.service.intf.LayIMService;
import com.fyp.utils.jwt.JwtUtil;
import com.fyp.utils.jwt.JwtVertifyResult;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.HttpResponseStatus;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

import java.util.List;


/**
 * WebSocket 核心消息处理
 * */
public class MyMsgHandler implements IWsMsgHandler {

    private LayIMService layIMService;
    /**
     * 握手
     * */
    public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        String token = httpRequest.getParam("access_token");
        JwtVertifyResult result = JwtUtil.verifyToken(token);
        if (result.isVertified()) {
            bindUser(channelContext, result.getUserId());
        } else {
            httpResponse.setStatus(HttpResponseStatus.C401);
        }
        return httpResponse;
    }

    private void bindUser(ChannelContext channelContext, Long userId) {
        Tio.bindUser(channelContext, userId.toString());
        List<Long> groupIds = layIMService.getGroupIds(userId);
        LogUtil.debug("获取到的群组个数为：{}",groupIds.size());
        for (Long id : groupIds) {
            Tio.bindGroup(channelContext, id.toString());
        }
    }

    /**
     * 握手完毕
     * */
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        System.out.println("握手完毕");
    }

    /**
     * 字节传输
     * */
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        return Processor.process(channelContext, bytes);
    }

    /**
     * 关闭
     * */
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        return null;
    }

    /**
     * 文本传输
     * */
    public Object onText(WsRequest wsRequest, String s, ChannelContext channelContext) throws Exception {
        return null;
    }
}
