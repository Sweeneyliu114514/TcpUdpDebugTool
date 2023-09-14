package com.sweeneyliu.activitytest.ui.tcpudp;

import java.text.SimpleDateFormat;

/**
 * 封装TCP/UDP通信消息的数据类
 * 包括消息内容、消息发送者IP和端口、时间戳以、消息类型（远程发送/本地发送）
 */
public class TcpUdpMsg {
    private final String messageContent;
    private final String messageIP;
    private final int messagePort;
    private final long messageTimestamp;
    private final String promptText;
    private final boolean messageType;// true for remote, false for local
    private final boolean isPrompt; // true for prompt, false for message

    // constructor
    public TcpUdpMsg(String messageContent, String messageIP,
                     int messagePort, long messageTimestamp, boolean messageType){
        this.messageContent = messageContent;
        this.messageIP = messageIP;
        this.messagePort = messagePort;
        this.messageTimestamp = messageTimestamp;
        this.messageType = messageType;
        this.promptText = null;
        this.isPrompt = false;

    }
    public TcpUdpMsg(String promptText, long messageTimestamp){
        this.messageContent = null;
        this.messageIP = null;
        this.messagePort = 0;
        this.messageTimestamp = messageTimestamp;
        this.messageType = false;
        this.promptText = promptText;
        this.isPrompt = true;
    }
    // getter
    public String getMessageContent() {
        return messageContent;
    }
    public String getMessageIP() {
        return messageIP;
    }
    public int getMessagePort() {
        return messagePort;
    }
    public long getMessageTimestamp() {
        return messageTimestamp;
    }
    public boolean getMessageType() {
        return messageType;
    }
    public boolean getIsPrompt() {
        return isPrompt;
    }

    public String getMessageDescription() {
        return messageIP + ":" + messagePort + " " + getFormattedTime(messageTimestamp);
    }
    public String getPromptText() {
        return getFormattedTime(messageTimestamp) + '\n' + promptText;
    }
    private String getFormattedTime(long messageTimestamp) {
        // 将时间戳转换为可读的小时分钟秒毫秒格式
        return new SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(messageTimestamp);
    }
}
