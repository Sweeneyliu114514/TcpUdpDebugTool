package com.sweeneyliu.activitytest.ui.tcpudp;

import com.sweeneyliu.activitytest.utils.HexStringByeUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {
    private static final String TAG = "TcpClient";
    /**
     * 饿汉式单例模式
     */
    private static final TcpClient tcpClientInstance = new TcpClient();
    private TcpClient(){}
    public static TcpClient getInstance() {
        return tcpClientInstance;
    }
    private OnClientListener onClientListener;
    /**
     * OnServerListener作为监听接口供外部回调使用
     */
    public interface OnClientListener {
        void onConnectSuccess(String remoteAddress, String localAddress); // 连接成功时回调
        void onConnectFail(Throwable e); // 连接失败时回调
        void onError(Throwable e); // 发生错误时回调
        void onReceiveMessage(String messageContent,String ip, int port); // 接收到消息时回调
        void onSendMessage(String messageContent, String ip, int port); // 发送消息时回调
    }

    /**
     * TcpClient类提供的设置OnServerListener的方法
     */
    public void setOnClientListener(OnClientListener onClientListener) {
        this.onClientListener = onClientListener;
    }
    private boolean sendRadixHex = false;
    public void setSendRadixHex(boolean sendRadixHex) {
        this.sendRadixHex = sendRadixHex;
    }
    private boolean receiveRadixHex = false;
    public void setReceiveRadixHex(boolean receiveRadixHex) {
        this.receiveRadixHex = receiveRadixHex;
    }
    /**
     * 客户端连接线程
     */
    private ClientSocketThread clientSocketThread;
    private String targetIp;
    private int targetPort;
    private Socket clientSocket;
    private BufferedReader clientReader;
    private PrintWriter clientWriter;
    private String clientMessageToSend;
    public void setTargetIp(String ip) {
        targetIp = ip;
    }
    public void setTargetPort(int port) {
        targetPort = port;
    }

    /**
     * 连接目标服务器，供外部调用
     */
    public void connect() {
        if (onClientListener == null) {
            throw new RuntimeException("请设置OnClientListener");
        }
        if (targetIp == null || targetIp.length() == 0 || targetPort == 0) {
            onClientListener.onConnectFail(new RuntimeException("请设置ip与port"));
            return;
        }
        if (clientSocketThread == null) {
            clientSocketThread = new ClientSocketThread();
            clientSocketThread.start();
        } else {
            onClientListener.onError(new RuntimeException("已经建立连接"));
        }
    }
    /**
     * 手动断开连接，供外部调用
     */
    public void manualDisconnect() {
        if (onClientListener == null) {
            throw new RuntimeException("请设置OnClientListener");
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
                clientReader.close();
                clientWriter.close();
                clientWriter = null;
                clientReader = null;
                clientSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            onClientListener.onError(new RuntimeException("已经手动断开连接"));
        }
        if (clientSocketThread != null) {
            clientSocketThread = null;
        }
    }

    private void disconnect() {
        if (clientSocket != null) {
            try {
                clientSocket.close();
                clientReader.close();
                clientWriter.close();
                clientWriter = null;
                clientReader = null;
                clientSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clientSocketThread != null) {
            clientSocketThread = null;
        }
    }


    /**
     * ClientSocketThread用于开新线程连接目标服务器并进行管理
     */
    class ClientSocketThread extends Thread {
        @Override
        public void run() {
            try {
                clientSocket = new Socket(targetIp, targetPort);
                clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        clientSocket.getOutputStream())), true);
                ConnectedServerHandler runnable = new ConnectedServerHandler(clientSocket);
                new Thread(runnable).start();
                // 连接成功
                if (onClientListener != null) {
                    onClientListener.onConnectSuccess(clientSocket.getInetAddress().getHostAddress(),
                            clientSocket.getLocalAddress().getHostAddress());
                }
            } catch (IOException e) {
                if (onClientListener != null) {
                    onClientListener.onConnectFail(new RuntimeException("无法连接目标服务器"));
                }
                disconnect();
                e.printStackTrace();
            }
        }
    }
    /**
     * ConnectedServerHandler用于接收服务器消息、生成提示消息
     */
    class ConnectedServerHandler implements Runnable {
        private Socket socket;
        public ConnectedServerHandler(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            boolean isConnected = true;
            while (isConnected) {
                try {
                    if (socket != null && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown()) {
                        String clientReceivedMessage;
                        if(receiveRadixHex){ // 十六进制接收
                            byte[] buffer = new byte[1024];
                            int len = socket.getInputStream().read(buffer);
                            byte[] data = new byte[len];
                            System.arraycopy(buffer, 0, data, 0, len);
                            clientReceivedMessage = HexStringByeUtil.byteArrayToHexString(data);
                        }
                        else{ // 字符串接收
                            clientReceivedMessage = clientReader.readLine();
                        }
                        if (clientReceivedMessage != null) {
                            // 客户端接收到消息
                            if (onClientListener != null) {
                                onClientListener.onReceiveMessage(clientReceivedMessage,
                                        socket.getInetAddress().getHostAddress(),socket.getLocalPort());
                            }
                        } else {
                            //服务端断开连接
                            isConnected = false;
                            if (onClientListener != null) {
                                onClientListener.onError(new RuntimeException("服务器断开连接"));
                            }
                            disconnect();
                        }
                    }
                } catch (Exception e) {
                    isConnected = false;
                    e.printStackTrace();
                    if (onClientListener != null) {
                        onClientListener.onError(e);
                    }
                }
            }
            // 保证在断开与服务器连接后socket、clientReader、clientWriter被关闭
            try {
                if (socket != null) {
                    socket.close();
                }
                if (clientReader != null) {
                    clientReader.close();
                }
                if (clientWriter != null) {
                    clientWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void sendMessage(String message) {
        if (onClientListener == null) {
            throw new RuntimeException("请设置OnClientListener");
        }
        clientMessageToSend = message;
        if (clientMessageToSend == null || clientMessageToSend.length() == 0) {
            onClientListener.onError(new RuntimeException("请输入内容"));
            return;
        }
        if (clientSocket != null && clientSocket.isConnected() && !clientSocket.isOutputShutdown()
                && clientWriter != null) {
            new Thread(() -> {
                if (sendRadixHex) {// 十六进制发送
                    try{
                        clientSocket.getOutputStream().write(HexStringByeUtil.
                                hexStringToByteArray(clientMessageToSend));
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                else { // 字符串发送
                    clientWriter.println(clientMessageToSend);
                }
                // 通知监听器已发送消息的内容和客户端自身IP和端口
                onClientListener.onSendMessage(clientMessageToSend,
                        clientSocket.getLocalAddress().getHostAddress(),clientSocket.getPort());
            }).start();
        } else {
            onClientListener.onError(new RuntimeException("服务器已经断开连接，无法发送消息"));
        }
    }

}