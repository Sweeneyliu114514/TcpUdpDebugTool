package com.sweeneyliu.activitytest.ui.tcpudp;

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
    private static TcpServer TcpServerInstance = new TcpServer();
    private TcpServer() {};
    public static TcpServer getInstance() {
        return TcpServerInstance;
    }
    private OnListener onListener;
    /**
     * OnListener作为监听接口供外部调用
     */
    public interface OnListener {
        void onStart();

        void onNewClient(String serverIp, String clientIp, int count);

        void onError(Throwable e, String message);

        void onMessage(String ip, String message);

        void onAutoReplyMessage(String ip, String message);

        void onClientDisConnect(String ip);

        void onConnectTimeOut(String ip);
    }
    /**
     * TcpServer类对外提供的设置监听接口的方法
     */
    public void setOnListener(OnListener onListener) {
        this.onListener = onListener;
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
    private List<ClientHandler> connectedClientHandlerList = new ArrayList<>();
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
        if (onListener == null) {
            throw new RuntimeException("请设置OnListener");
        }
        if (serverListeningPort == 0) {
            onListener.onError(new RuntimeException("请设置port"), "请设置port");
            return;
        }
        if (serverSocketThread == null) {
            serverSocketThread = new ServerSocketThread();
            new Thread(serverSocketThread).start();
        } else {
            onListener.onError(new RuntimeException("服务端已经启动过了"), "服务端已经启动过了");
        }
    }
    /**
     * 关闭TCP服务器
     */
    public void stop() {
        if (onListener == null) {
            throw new RuntimeException("请设置OnListener");
        }
        if (serverSocketThread != null) {
            serverSocketThread.close();
            serverSocketThread = null;
        } else {
            onListener.onError(new RuntimeException("服务端已经关闭"), "服务端已经关闭");
        }
    }
    /**
     * 给选定的已连接客户端IP地址发送消息
     */
    public void sendMessage(String message, String... selectedIPs) {
        if (onListener == null) {
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
            for (Socket s : connectedClientSocketList) {
                for (String ip : selectedIPs) {
                    if (ip.equals(s.getInetAddress().getHostAddress())) {
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
    }

    /**
     * 给所有已连接的客户端发送消息
     */
    private void sendMessageToAll() {
        if (!check()) {
            return;
        }
        for (int i = 0, size = connectedClientSocketList.size(); i < size; i++) {
            Socket socket = connectedClientSocketList.get(i);
            PrintWriter out;
            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(serverMessageToSend);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ServerSocketThread类用于监听客户端的连接
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
                if (onListener != null) {
                    onListener.onStart();
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
                    if (onListener != null) {
                        onListener.onNewClient(socket.getLocalAddress().getHostAddress(),
                                socket.getInetAddress().getHostAddress(), connectedClientSocketList.size());
                    }
                    // 启动新的接收处理线程处理接收到的Socket
                    ClientHandler clientHandler = new ClientHandler(socket);
                    new Thread(clientHandler).start();
                    // 将新的接收处理线程添加到已连接的客户端对应的接收处理线程列表中
                    connectedClientHandlerList.add(clientHandler);
                }
            } catch (Exception e) {
                if (onListener != null) {
                    onListener.onError(new RuntimeException("服务端已经启动"), "服务端已经启动");
                }
            }
        }
    }

    /**
     * ClientHandler类用于接收处理客户端发送的消息
     */
    class ClientHandler implements Runnable {
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

        public ClientHandler(Socket socket) {
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
                    if ((receivedMessage = reader.readLine()) != null) {
                        // 通知监听器服务器接收到新的客户端发送的消息
                        if (onListener != null) {
                            onListener.onMessage(clientAddress, receivedMessage);
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
                            if (onListener != null) {
                                onListener.onClientDisConnect(clientAddress);
                            }
                        } else {
                            //服务端发送消息，给单个客户端自动回复
                            serverMessageToSend = receivedMessage + "（服务器自动回复）";
                            if (onListener != null) {
                                onListener.onAutoReplyMessage(clientAddress, serverMessageToSend);
                            }
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            out.println(serverMessageToSend);
                        }
                    } else {
                        //客户端断开连接
                        isHandling = false;
                        // 通知监听器客户端断开连接
                        if (onListener != null) {
                            onListener.onClientDisConnect(clientAddress);
                        }
                    }
                } catch (IOException e) {
                    isHandling = false;
                    e.printStackTrace();
                    // 通知监听器客户端连接超时
                    if (onListener != null) {
                        onListener.onConnectTimeOut(clientAddress);
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
            if (onListener != null) {
                onListener.onError(new RuntimeException("请输入内容"), "请输入内容");
            }
            return false;
        }
        if (connectedClientSocketList == null || connectedClientSocketList.size() == 0) {
            if (onListener != null) {
                onListener.onError(new RuntimeException("没有连接中的客户端"), "没有连接中的客户端");
            }
            return false;
        }
        return true;
    }


}
