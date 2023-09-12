package com.sweeneyliu.activitytest.ui.tcpudp;

// 封装TCP/UDP通信消息的数据类
// 包括消息内容、消息发送者IP和端口、时间戳以及消息类型（远程发送/本地发送）
public class TcpUdpMsg {
    private final String messageContent;
    private final String messageIp;
    private final int messagePort;
    private final long messageTimestamp;
    private final boolean messageType;
    // true for remote, false for local
    // constructor
    public TcpUdpMsg(String messageContent, String messageIp,
                     int messagePort, long messageTimestamp, boolean messageType) {
        this.messageContent = messageContent;
        this.messageIp = messageIp;
        this.messagePort = messagePort;
        this.messageTimestamp = messageTimestamp;
        this.messageType = messageType;
    }
    // getter
    public String getMessageContent() {
        return messageContent;
    }
    public String getMessageIp() {
        return messageIp;
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

    public String getMessageDescription() {
        return messageIp + ":" + messagePort + " " + messageTimestamp;
    }
}
