package com.ruoyi.system.websocket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 */
@Component
public class CustomerWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerWebSocketHandler.class);
    
    // 存储所有连接的WebSocket会话，key是用户ID
    private static final ConcurrentHashMap<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromUri(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("用户{}建立WebSocket连接，当前在线用户数：{}", userId, SESSIONS.size());
            // 发送连接成功消息
            sendMessage(session, createMessage("connected", "WebSocket连接成功"));
        } else {
            log.error("无法获取用户ID，关闭连接");
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = getUserIdFromUri(session);
        String payload = message.getPayload();
        log.info("收到用户{}的消息：{}", userId, payload);
        
        try {
            JSONObject jsonMessage = JSON.parseObject(payload);
            String type = jsonMessage.getString("type");
            
            switch (type) {
                case "ping":
                    // 处理心跳消息
                    sendMessage(session, createMessage("pong", "heartbeat"));
                    break;
                case "customer_status":
                    // 处理客户状态更新
                    handleCustomerStatusUpdate(session, jsonMessage);
                    break;
                default:
                    // 处理其他类型的消息
                    log.info("收到未知类型消息：{}", type);
                    break;
            }
        } catch (Exception e) {
            log.error("处理消息时发生错误", e);
            sendMessage(session, createMessage("error", "消息处理失败：" + e.getMessage()));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = getUserIdFromUri(session);
        log.error("用户{}的WebSocket连接发生错误：{}", userId, exception.getMessage());
        if (session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromUri(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("用户{}断开WebSocket连接，当前在线用户数：{}", userId, SESSIONS.size());
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
                log.info("成功发送消息给用户{}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送消息给用户{}失败: {}", userId, e.getMessage());
                SESSIONS.remove(userId);
            }
        } else {
            log.warn("用户{}不在线或会话已关闭", userId);
        }
    }

    /**
     * 发送消息给所有业务员
     */
    public void sendMessageToAllSalesReps(String message) {
        SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                    log.info("成功发送消息给用户{}", userId);
                } catch (IOException e) {
                    log.error("发送消息给用户{}失败: {}", userId, e.getMessage());
                    SESSIONS.remove(userId);
                }
            }
        });
    }

    /**
     * 从URI中获取用户ID
     */
    private String getUserIdFromUri(WebSocketSession session) {
        String uri = session.getUri().toString();
        return UriComponentsBuilder.fromUriString(uri)
                .build()
                .getQueryParams()
                .getFirst("userId");
    }

    /**
     * 获取当前在线用户数量
     */
    public int getOnlineCount() {
        return SESSIONS.size();
    }

    /**
     * 处理客户状态更新
     */
    private void handleCustomerStatusUpdate(WebSocketSession session, JSONObject jsonMessage) throws IOException {
        String customerId = jsonMessage.getString("customerId");
        String status = jsonMessage.getString("status");
        log.info("处理客户{}状态更新：{}", customerId, status);
        
        // 发送确认消息
        sendMessage(session, createMessage("customer_status_updated", 
            String.format("客户%s状态已更新为%s", customerId, status)));
    }

    /**
     * 创建消息
     */
    private String createMessage(String type, String content) {
        JSONObject message = new JSONObject();
        message.put("type", type);
        message.put("message", content);
        message.put("timestamp", System.currentTimeMillis());
        return message.toJSONString();
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
} 