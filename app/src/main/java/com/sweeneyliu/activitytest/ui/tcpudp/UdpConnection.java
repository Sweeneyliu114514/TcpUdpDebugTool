package com.sweeneyliu.activitytest.ui.tcpudp;

import com.sweeneyliu.activitytest.utils.HexStringByeUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * UDP模式下不作服务器和客户端的区分，因此接收三个外部参数
 * 即接收端口、发送目标IP、发送目标端口
 */
public class UdpConnection {
    private static final String TAG = "UdpConnection";
    /**
     * 饿汉式单例模式
     */
    private static final UdpConnection udpConnectionInstance = new UdpConnection();
    private UdpConnection(){}
    public static UdpConnection getInstance() {
        return udpConnectionInstance;
    }
    private OnUdpListener onUdpListener;
    /**
     * OnServerListener作为监听接口供外部回调使用
     */
    public interface OnUdpListener {
        void onStart(int port); // 开始监听时回调
        void onError(Throwable e); // 发生错误时回调
        void onReceiveMessage(String messageContent,String ip, int port); // 接收到消息时回调
        void onSendMessage(String messageContent, String ip, int port); // 发送消息时回调
        void onNewClient(String ip, int port); // 新客户端连接时回调
    }
    public void setOnUdpListener(OnUdpListener onUdpListener) {
        this.onUdpListener = onUdpListener;
    }
    private boolean sendRadixHex = false;
    public void setSendRadixHex(boolean sendRadixHex) {
        this.sendRadixHex = sendRadixHex;
    }
    private boolean receiveRadixHex = false;
    public void setReceiveRadixHex(boolean receiveRadixHex) {
        this.receiveRadixHex = receiveRadixHex;
    }

    private ReceiveThread receiveThread;
    /**
     * 保存目标客户端记录，包括第一次传入的目标客户端和后续接收到的客户端
     */
    private List<InetSocketAddress> historyClients = new ArrayList<>();
    private String targetIp;
    private int targetPort;
    private int receivePort;
    public void setTarget(String ip, int port) {
        targetIp = ip;
        targetPort = port;
    }
    public void setReceivePort(int port) {
        receivePort = port;
    }
    public void start() {
        if (onUdpListener == null) {
            throw new RuntimeException("请先设置OnUdpListener");
        }
        if(receivePort == 0) {
            onUdpListener.onError(new RuntimeException("请先设置接收端口"));
            return;
        }
        if (receiveThread == null) {
            // 将初始目标客户端加入到客户端列表中
            historyClients.add(new InetSocketAddress(targetIp, targetPort));
            receiveThread = new ReceiveThread();
            receiveThread.start();
        }
    }
    public void stop() {
        if (onUdpListener == null) {
            throw new RuntimeException("请先设置OnUdpListener");
        }
        if (receiveThread != null) {
            receiveThread.close();
            receiveThread = null;
        }
    }
    private String messageToSend;
    /**
     * 给选定的目标主机发送消息
     */
    public void sendMessage(String message, InetSocketAddress... targets) {
        if (onUdpListener == null) {
            throw new RuntimeException("请先设置OnUdpListener");
        }
        if (targetIp == null || targetPort == 0) {
            onUdpListener.onError(new RuntimeException("请先设置目标客户端"));
            return;
        }
        messageToSend = message;
        if (targets.length == 0) {
            // 如果没有传入目标客户端，则默认发送给初始目标客户端
            targets = new InetSocketAddress[]{new InetSocketAddress(targetIp, targetPort)};
        }
        InetSocketAddress[] finalTargets = targets;
        new Thread(() -> {
            // 在已有的客户端列表中查找目标客户端
            for(InetSocketAddress target : finalTargets){
                try {
                    DatagramSocket sendSocket = new DatagramSocket();
                    byte[] sendData;
                    if(sendRadixHex){
                        // 16进制发送
                        sendData = HexStringByeUtil.hexStringToByteArray(messageToSend);
                    }
                    else{
                        // 字符串发送
                        sendData = messageToSend.getBytes();
                    }
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                            target.getAddress(), target.getPort());
                    sendSocket.send(sendPacket);
                    if(onUdpListener != null){
                        // 通知监听器发送消息成功
                        onUdpListener.onSendMessage(messageToSend, target.getAddress().getHostAddress(),
                                target.getPort());
                    }
                    sendSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    if(onUdpListener != null){
                        // 通知监听器发送消息失败
                        onUdpListener.onError(e);
                    }
                }
            }

        }).start();

    }
    /**
     * 给局域网内所有主机发送消息（广播）
     */
    public void sendMessageToAll(String message) {
        if (onUdpListener == null) {
            throw new RuntimeException("请先设置OnUdpListener");
        }
        if (targetIp == null || targetPort == 0) {
            onUdpListener.onError(new RuntimeException("请先设置目标客户端"));
            return;
        }
        // 向整个局域网内的所有主机发送消息
        setTarget("255.255.255.255", targetPort);
        messageToSend = message;
        sendMessage(message, new InetSocketAddress(targetIp, targetPort));


    }
    /**
     * 接收线程
     */
    class ReceiveThread extends Thread {
        DatagramSocket receiveSocket = null;
        boolean isListening = true;
        public void close(){
            try {
                isListening = false;
                receiveSocket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try{
                receiveSocket = new DatagramSocket(receivePort);
                if(onUdpListener != null){
                    onUdpListener.onStart(receivePort);
                }
                while(isListening){
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        String receiveMessage;
                        receiveSocket.receive(receivePacket);
                        if(receiveRadixHex){
                            // 16进制接收
                            receiveMessage = HexStringByeUtil.byteArrayToHexString(receivePacket.getData());
                        }
                        else{
                            // 字符串接收
                            receiveMessage = new String(receivePacket.getData(),
                                    receivePacket.getOffset(), receivePacket.getLength());
                        }
                        if (!receiveMessage.isEmpty()){
                            if(onUdpListener!= null) {
                                // 通知监听器接收到新消息
                                onUdpListener.onReceiveMessage(receiveMessage, receivePacket.getAddress().
                                        getHostAddress(), receivePacket.getPort());
                                // 将接收到新消息的客户端加入到客户端列表中
                                historyClients.add(new InetSocketAddress(receivePacket.getAddress().
                                        getHostAddress(), receivePacket.getPort()));
                                onUdpListener.onNewClient(receivePacket.getAddress().getHostAddress(),
                                        receivePacket.getPort());
                            }
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }catch (SocketException e){
                e.printStackTrace();
            }
        }
    }



    
}
