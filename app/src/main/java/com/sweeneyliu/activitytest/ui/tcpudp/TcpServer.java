package com.sweeneyliu.activitytest.ui.tcpudp;

import com.sweeneyliu.activitytest.utils.HexStringByeUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TcpServer {
    private static final String TAG = "TcpServer";
    /**
     * 饿汉式单例模式
     */
    private static final TcpServer TcpServerInstance = new TcpServer();
    private TcpServer() {}
    public static TcpServer getInstance() {
        return TcpServerInstance;
    }
    private OnServerListener onServerListener;
    /**
     * OnServerListener作为监听接口供外部调用
     */
    public interface OnServerListener {
        void onStart(int port); // 服务器启动监听时回调
        void onNewClient(String clientIp, int clientPort, int count);// 有新的客户端连接时回调
        void onError(Throwable e);// 服务器启动失败时回调
        void onReceiveMessage(String messageContent, String ip, int port);// 服务器接收到客户端发送的消息时回调
        void onSendMessage(String messageContent, String ip, int port);// 服务器发送消息时回调
        void onAutoReplyMessage(String ip, String message);// 服务器自动回复客户端发送的消息时回调
        void onClientDisConnect(String ip);// 客户端断开连接时回调
        void onConnectTimeOut(String ip);// 客户端连接超时时回调
        void onDisconnect(String... selectedIps);// 服务器主动与指定客户端断开连接时回调
    }
    /**
     * TcpServer类对外提供的设置监听接口的方法
     */
    public void setServerOnListener(OnServerListener onServerListener) {
        this.onServerListener = onServerListener;
    }
    /**
     * 服务器发送消息时是否以16进制发送,true为16进制发送，false为字符串发送
     */
    private boolean sendRadixHex = false;
    public void setSendRadixHex(boolean sendRadixHex) {
        this.sendRadixHex = sendRadixHex;
    }
    /**
     * 服务器接收消息时是否以16进制接收,true为16进制接收，false为字符串接收
     */
    private boolean receiveRadixHex = false;
    public void setReceiveRadixHex(boolean receiveRadixHex) {
        this.receiveRadixHex = receiveRadixHex;
    }

    /**
     * 服务器监听线程
     */
    private ServerSocketThread serverSocketThread;
    /**
     * 服务器监听端口
     */
    private int serverListeningPort;
    /**
     * 服务器待发送的消息
     */
    private String serverMessageToSend;
    /**
     * 已连接的客户端列表
     */
    private List<Socket> connectedClientSocketList = new ArrayList<>();
    /**
     * 已连接的客户端对应的接收处理线程列表
     */
    private List<ConnectedClientHandler> connectedClientHandlerList = new ArrayList<>();
    /**
     * 设置服务器监听端口
     */
    public void setListeningPort(int port) {
        serverListeningPort = port;
    }

    /**
     * 启动TCP服务器
     */
    public void start() {
        if (onServerListener == null) {
            throw new RuntimeException("请设置OnListener");
        }
        if (serverListeningPort == 0) {
            onServerListener.onError(new RuntimeException("请设置port"));
            return;
        }
        if (serverSocketThread == null) {
            serverSocketThread = new ServerSocketThread();
            new Thread(serverSocketThread).start();
        } else onServerListener.onError(new RuntimeException("服务端已经启动过了"));
    }
    /**
     * 关闭TCP服务器
     */
    public void stop() {
        if (onServerListener == null) {
            throw new RuntimeException("请设置OnListener");
        }
        if (serverSocketThread != null) {
            serverSocketThread.close();
            serverSocketThread = null;
        } else {
            onServerListener.onError(new RuntimeException("服务端已经关闭"));
        }
    }
    /**
     * 给选定的已连接客户端IP地址发送消息
     */
    public void sendMessage(String message, String... selectedIPs) {
        if (onServerListener == null) {
            throw new RuntimeException("请设置OnListener");
        }
        serverMessageToSend = message;
        if (!check()) {
            return;
        }
        if (selectedIPs == null || selectedIPs.length == 0) {
            // 若没有选定的IP，给所有已连接客户端发送消息
            sendMessageToAll();
        } else {
            new Thread(() -> {
                for (Socket s : connectedClientSocketList) {
                    for (String ip : selectedIPs) {
                        if (ip.equals(s.getInetAddress().getHostAddress())) {
                            if (sendRadixHex) {
                                // 16进制发送
                                try {
                                    s.getOutputStream().write(HexStringByeUtil.
                                            hexStringToByteArray(serverMessageToSend));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else { // 字符串发送
                                PrintWriter out;
                                try {
                                    // 使用autoFlush=true，不需要手动flush()即会自动刷新缓冲区
                                    out = new PrintWriter(new BufferedWriter(
                                            new OutputStreamWriter(s.getOutputStream())), true);
                                    // 使用println()方法发送消息，不需要手动添加换行符
                                    out.println(serverMessageToSend);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                // 通知监听器服务器发送的消息内容以及服务器自身IP地址和发送的端口
                onServerListener.onSendMessage(serverMessageToSend,
                        connectedClientSocketList.get(0).getLocalAddress().getHostAddress(),
                        connectedClientSocketList.get(0).getLocalPort());
            }).start();
        }
    }

    /**
     * 给所有已连接的客户端发送消息
     */
    private void sendMessageToAll() {
        if (!check()) {
            return;
        }
        new Thread(()->{
            for (int i = 0, size = connectedClientSocketList.size(); i < size; i++) {
                Socket socket = connectedClientSocketList.get(i);
                if (sendRadixHex) {
                    // 16进制发送
                    try {
                        socket.getOutputStream().write(HexStringByeUtil.
                                hexStringToByteArray(serverMessageToSend));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else { // 字符串发送
                    PrintWriter out;
                    try {
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                socket.getOutputStream())), true);
                        out.println(serverMessageToSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 通知监听器服务器发送的消息内容以及服务器自身IP地址和发送的端口
            onServerListener.onSendMessage(serverMessageToSend,
                    connectedClientSocketList.get(0).getLocalAddress().getHostAddress(),
                    connectedClientSocketList.get(0).getLocalPort());
        }).start();
    }
    /**
     * 断开与指定的客户端的连接
     */
    public void disconnect(String... selectedIps){
        if (onServerListener == null) {
            throw new RuntimeException("请设置OnListener");
        }
        if (selectedIps == null || selectedIps.length == 0) {
            // 若没有选定的IP，将所有已连接客户端断开连接
            disconnectAll();
        }
        else{
            for (Socket s : connectedClientSocketList) {
                for (String ip : selectedIps) {
                    if (ip.equals(s.getInetAddress().getHostAddress())) {
                        try {
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            connectedClientHandlerList.clear();
            connectedClientSocketList.clear();
            // 通知监听器服务器主动与指定客户端断开连接
            onServerListener.onDisconnect(selectedIps);
        }
    }
    /**
     * 断开所有已连接的客户端
     */
    private void disconnectAll() {
        if (!check()) {
            return;
        }
        for (int i = 0, size = connectedClientSocketList.size(); i < size; i++) {
            Socket socket = connectedClientSocketList.get(i);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        connectedClientSocketList.clear();
        connectedClientHandlerList.clear();
        // 通知监听器服务器主动与所有已连接的客户端断开连接
        onServerListener.onDisconnect();
    }

    /**
     * ServerSocketThread类用于监听客户端，实现多客户端连接并管理
    */
    class ServerSocketThread implements Runnable {
        private ServerSocket serverSocket;
        // 服务器是否监听，默认处于监听状态
        private boolean isListening = true;
        public void close() {
            try {
                isListening = false;
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(serverListeningPort);
                if (onServerListener != null) {
                    onServerListener.onStart(serverListeningPort);
                }
                while (isListening) {
                    // accept()方法阻塞等待客户端连接
                    Socket socket = serverSocket.accept();
                    boolean isIncludedAlready = false;
                    int index = 0;
                    for (Socket s : connectedClientSocketList) {
                        if (s.getInetAddress().equals(socket.getInetAddress())) {
                            isIncludedAlready = true;
                            break;
                        }
                        index++;
                    }
                    // 如果该Socket已经存在，关闭对应的接收处理线程并从已连接列表中移除旧的Socket
                    if (isIncludedAlready) {
                        connectedClientHandlerList.remove(index).close();
                        connectedClientSocketList.remove(index).close();
                    }
                    // 将新的Socket添加到已连接的客户端列表中
                    connectedClientSocketList.add(socket);
                    // 通知监听器有新的客户端连接
                    if (onServerListener != null) {
                        onServerListener.onNewClient(socket.getInetAddress().getHostAddress(),
                                socket.getPort(), connectedClientSocketList.size());
                    }
                    // 启动新的接收处理线程处理接收到的Socket
                    ConnectedClientHandler clientHandler = new ConnectedClientHandler(socket);
                    new Thread(clientHandler).start();
                    // 将新的接收处理线程添加到已连接的客户端对应的接收处理线程列表中
                    connectedClientHandlerList.add(clientHandler);
                }
            } catch (Exception e) {
                if (onServerListener != null) {
                    onServerListener.onError(new RuntimeException("服务端已经启动"));
                }
            }
        }
    }

    /**
     * ConnectedClientHandler类用于接收客户端消息、生成提示信息、自动回复消息
     */
    class ConnectedClientHandler implements Runnable {
        private String localAddress;// 服务器IP地址
        private String clientAddress;// 客户端IP地址
        private boolean isHandling = true;
        private Socket socket;
        private BufferedReader reader;

        public void close() {
            try {
                isHandling = false;
                socket.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ConnectedClientHandler(Socket socket) {
            this.socket = socket;
            localAddress = socket.getLocalAddress().getHostAddress();
            clientAddress = socket.getInetAddress().getHostAddress();
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // 与新的客户端建立连接后，向所有已连接的客户端发送消息告知当前客户端数量
                serverMessageToSend = "当前客户端数：" + connectedClientSocketList.size();
                sendMessageToAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (isHandling) {
                try {
                    String receivedMessage;
                    if(receiveRadixHex){//16进制接收
                        byte[] buffer = new byte[1024];
                        int len = socket.getInputStream().read(buffer);
                        byte[] data = new byte[len];
                        System.arraycopy(buffer, 0, data, 0, len);
                        receivedMessage = HexStringByeUtil.byteArrayToHexString(data);
                    }
                    else {//字符串接收
                        receivedMessage = reader.readLine();
                    }
                    if (receivedMessage != null) {
                        // 通知监听器服务器接收到新的客户端发送的消息
                        if (onServerListener != null) {
                            onServerListener.onReceiveMessage(receivedMessage, clientAddress, socket.getPort());
                        }
                        if (receivedMessage.equalsIgnoreCase("close")) {
                            // 当客户端发送close消息通知服务器断开连接时，关闭对应的接收处理线程并从已连接列表中移除Socket
                            int size = connectedClientSocketList.size() - 1;
                            // 断开连接后，向所有已连接的客户端发送消息告知退出的客户端IP地址和当前客户端数量
                            serverMessageToSend = "客户端：" + socket.getInetAddress()
                                    + " 退出，当前客户端数：" + size;
                            sendMessageToAll();
                            connectedClientSocketList.remove(socket);
                            reader.close();
                            socket.close();
                            isHandling = false;
                            if (onServerListener != null) {
                                onServerListener.onClientDisConnect(clientAddress);
                            }
                        } else {
                            //服务端发送消息，给单个客户端自动回复
                            serverMessageToSend = receivedMessage + "（服务器自动回复）";
                            if (onServerListener != null) {
                                onServerListener.onAutoReplyMessage(clientAddress, serverMessageToSend);
                            }
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            out.println(serverMessageToSend);
                        }
                    } else {
                        //客户端断开连接
                        isHandling = false;
                        // 通知监听器客户端断开连接
                        if (onServerListener != null) {
                            onServerListener.onClientDisConnect(clientAddress);
                        }
                    }
                } catch (IOException e) {
                    isHandling = false;
                    e.printStackTrace();
                    // 通知监听器客户端连接超时
                    if (onServerListener != null) {
                        onServerListener.onConnectTimeOut(clientAddress);
                    }
                }
            }
            // 停止处理后关闭socket和reader释放资源
            try {
                if (reader != null) {
                    reader.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean check() {
        if (serverMessageToSend == null || serverMessageToSend.length() == 0) {
            if (onServerListener != null) {
                onServerListener.onError(new RuntimeException("请输入内容"));
            }
            return false;
        }
        if (connectedClientSocketList == null || connectedClientSocketList.size() == 0) {
            if (onServerListener != null) {
                onServerListener.onError(new RuntimeException("没有连接中的客户端"));
            }
            return false;
        }
        return true;
    }


}
