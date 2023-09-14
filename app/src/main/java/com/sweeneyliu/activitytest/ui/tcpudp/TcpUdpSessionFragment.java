package com.sweeneyliu.activitytest.ui.tcpudp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sweeneyliu.activitytest.R;
import com.sweeneyliu.activitytest.databinding.FragmentTcpUdpSessionBinding;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TcpUdpSessionFragment extends Fragment {

    private TcpUdpSessionViewModel mViewModel;
    private TcpUdpMsgAdapter msgAdapter;
    private List<TcpUdpMsg> MsgList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FragmentTcpUdpSessionBinding binding;
    private Handler msgHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            TcpUdpMsg msg1 = (TcpUdpMsg) msg.obj;
            msgAdapter.addMsg(msg1);
        }
    };

    public static TcpUdpSessionFragment newInstance() {
        return new TcpUdpSessionFragment();
    }
    TcpServer tcpServer;
    TcpClient tcpClient;
    UdpConnection udpConnection;
    private boolean sendRadixHex = false;
    private boolean receiveRadixHex = false;
    private List<String> targetHosts = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTcpUdpSessionBinding.inflate(inflater, container, false);
        initMsgList();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        targetHosts.clear();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.dropdown_menu_tcp_udp, targetHosts);
        binding.targetHost.setAdapter(adapter);
        TcpUdpSessionConfig config = TcpUdpSessionFragmentArgs.fromBundle(
                getArguments()).getInitialSessionConfig();
        // 每点击一次按钮，发送进制在16进制和字符串之间切换
        binding.btnSetupSendRadix.setOnClickListener(v -> {
            sendRadixHex = !sendRadixHex;
            switch (config.getMode()) {
                case "TCP服务器":
                    tcpServer.setSendRadixHex(sendRadixHex);
                    break;
                case "TCP客户端":
                    tcpClient.setSendRadixHex(sendRadixHex);
                    break;
                case "UDP":
                    break;
            }
            binding.btnSetupSendRadix.setText(sendRadixHex ? "16进制发送" : "字符串发送");
        });
        // 每点击一次按钮，接收进制在16进制和字符串之间切换
        binding.btnSetupReceiveRadix.setOnClickListener(v -> {
            receiveRadixHex = !receiveRadixHex;
            switch (config.getMode()) {
                case "TCP服务器":
                    tcpServer.setReceiveRadixHex(receiveRadixHex);
                    break;
                case "TCP客户端":
                    tcpClient.setReceiveRadixHex(receiveRadixHex);
                    break;
                case "UDP":
                    break;
            }
            binding.btnSetupReceiveRadix.setText(receiveRadixHex ? "16进制接收" : "字符串接收");
        });
        switch (config.getMode()) {
            case "TCP服务器":
                tcpServer = TcpServer.getInstance();
                tcpServer.setListeningPort(config.getLocalPort());
                binding.targetHostWrapper.setEnabled(true);
                tcpServer.setServerOnListener(new TcpServer.OnServerListener() {
                    @Override
                    public void onStart(int port) {
                        // 在提示信息中显示服务器启动信息
                        TcpUdpMsg msg = new TcpUdpMsg("服务器已启动，监听端口: " + port,
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onNewClient(String clientIp, int clientPort, int count) {
                        // 在提示信息中显示新的客户端连接信息
                        TcpUdpMsg msg = new TcpUdpMsg("新的客户端连接: " + clientIp + ":"
                                + clientPort + "，当前客户端数量: " + count, System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                        // 将新的客户端IP和端口放入targetHosts数组
                        targetHosts.add(clientIp + ":" + clientPort);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // 在提示信息中显示服务器异常信息
                        TcpUdpMsg msg = new TcpUdpMsg("服务器异常: " + e.getMessage(),
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onReceiveMessage(String messageContent, String ip, int port) {
                        // 将收到的客户端消息放入一个新的聊天气泡
                        TcpUdpMsg msg = new TcpUdpMsg(messageContent, ip, port,
                                System.currentTimeMillis(), true);
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onSendMessage(String messageContent, String ip, int port) {
                        // 将发送的消息放入一个新的聊天气泡
                        TcpUdpMsg msg = new TcpUdpMsg(messageContent, ip, port,
                                System.currentTimeMillis(), false);
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onAutoReplyMessage(String ip, String message) {
                        //
                    }

                    @Override
                    public void onClientDisConnect(String ip) {
                        // 在提示信息中显示客户端断开连接信息
                        TcpUdpMsg msg = new TcpUdpMsg("客户端" + ip + "断开连接",
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onConnectTimeOut(String ip) {
                        // 在提示信息中显示客户端连接超时信息
                        TcpUdpMsg msg = new TcpUdpMsg("客户端" + ip + "连接超时",
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onDisconnect(String... selectedIps) {
                        // 在提示信息中显示服务器断开连接信息
                        if(selectedIps == null || selectedIps.length == 0) {
                            TcpUdpMsg msg = new TcpUdpMsg("服务器断开了所有客户端连接",
                                    System.currentTimeMillis());
                            Message message = new Message();
                            message.obj = msg;
                            msgHandler.sendMessage(message);
                            // 将所有客户端从targetHosts数组中移除
                            targetHosts.clear();
                        } else {
                            for(String ip : selectedIps) {
                                TcpUdpMsg msg = new TcpUdpMsg("服务器断开了与" + ip + "的连接",
                                        System.currentTimeMillis());
                                Message message = new Message();
                                message.obj = msg;
                                msgHandler.sendMessage(message);
                                // 将断开连接的客户端从targetHosts数组中移除
                                targetHosts.removeIf(target -> target.startsWith(ip));
                            }
                        }
                    }
                });
                tcpServer.start();
                binding.btnSendMsg.setOnClickListener(v -> {
                    Editable text = binding.msgInput.getText();
                    if (text != null) {
                        if (binding.targetHost.getText() != null) {
                            // 向指定客户端发送消息
                            String[] split = binding.targetHost.getText().toString().split(":");
                            tcpServer.sendMessage(text.toString(), split[0]);
                        } else {
                            // 向所有客户端发送消息
                            tcpServer.sendMessage(text.toString());
                        }
                    }
                });
                binding.btnDisconnectHost.setOnClickListener(v -> {
                    if (binding.targetHost.getText() != null) {
                        // 断开指定客户端连接
                        String[] split = binding.targetHost.getText().toString().split(":");
                        tcpServer.disconnect(split[0]);
                    }
                    else {
                        tcpServer.disconnect(); // 断开所有客户端连接
                    }
                });
                break;
            case "TCP客户端":
                tcpClient = TcpClient.getInstance();
                // 客户端会话禁用目标主机下拉框
                binding.targetHostWrapper.setEnabled(false);
                tcpClient.setTargetIp(config.getRemoteIp());
                tcpClient.setTargetPort(config.getRemotePort());
                tcpClient.setOnClientListener(new TcpClient.OnClientListener() {

                    @Override
                    public void onConnectSuccess(String remoteAddress, String localAddress) {
                        // 在提示信息中显示客户端连接成功信息
                        TcpUdpMsg msg = new TcpUdpMsg("连接成功，服务器地址: " + remoteAddress
                                + "，本地地址: " + localAddress, System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onConnectFail(Throwable e) {
                        // 在提示信息中显示客户端连接失败信息
                        TcpUdpMsg msg = new TcpUdpMsg("连接失败: " + e.getMessage(),
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // 在提示信息中显示异常信息
                        TcpUdpMsg msg = new TcpUdpMsg(e.getMessage(),
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onReceiveMessage(String messageContent, String ip, int port) {
                        // 将收到的服务器消息放入一个新的聊天气泡
                        TcpUdpMsg msg = new TcpUdpMsg(messageContent, ip, port,
                                System.currentTimeMillis(), true);
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onSendMessage(String messageContent, String ip, int port) {
                        // 将发送的消息放入一个新的聊天气泡
                        TcpUdpMsg msg = new TcpUdpMsg(messageContent, ip, port,
                                System.currentTimeMillis(), false);
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }
                });
                tcpClient.connect();
                binding.btnSendMsg.setOnClickListener(v -> {
                    Editable text = binding.msgInput.getText();
                    if (text != null) {
                        tcpClient.sendMessage(text.toString());
                    }
                });
                break;
            case "UDP":
                udpConnection = UdpConnection.getInstance();
                binding.targetHostWrapper.setEnabled(true);
                udpConnection.setReceivePort(config.getLocalPort());
                udpConnection.setTarget(config.getRemoteIp(), config.getRemotePort());
                udpConnection.setOnUdpListener(new UdpConnection.OnUdpListener() {
                    @Override
                    public void onStart(int port) {
                        TcpUdpMsg msg = new TcpUdpMsg("UDP已启动，监听端口: " + port,
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }
                    @Override
                    public void onError(Throwable e) {
                        TcpUdpMsg msg = new TcpUdpMsg("UDP异常: " + e.getMessage(),
                                System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }

                    @Override
                    public void onReceiveMessage(String messageContent, String ip, int port) {
                        TcpUdpMsg msg = new TcpUdpMsg(messageContent, ip, port,
                                System.currentTimeMillis(), true);
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }
                    @Override
                    public void onSendMessage(String messageContent, String ip, int port) {
                        TcpUdpMsg msg = new TcpUdpMsg(messageContent, ip, port,
                                System.currentTimeMillis(), false);
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                    }
                    @Override
                    public void onNewClient(String ip, int port) {
                        TcpUdpMsg msg = new TcpUdpMsg("新的客户端连接: " + ip + ":"
                                + port, System.currentTimeMillis());
                        Message message = new Message();
                        message.obj = msg;
                        msgHandler.sendMessage(message);
                        targetHosts.add(ip + ":" + port);
                    }
                });
                udpConnection.start();
                binding.btnSendMsg.setOnClickListener(v -> {
                    Editable text = binding.msgInput.getText();
                    if (text != null) {
                        if(binding.targetHost.getText() != null) {
                            // 若选择了目标主机，则向目标主机发送消息
                            String[] split = binding.targetHost.getText().toString().split(":");
                            InetSocketAddress target = new InetSocketAddress(split[0],
                                    Integer.parseInt(split[1]));
                            udpConnection.sendMessage(text.toString(),target);
                        } else {
                            // 若不选择目标主机，则向局域网内广播消息
                            udpConnection.sendMessageToAll(text.toString());
                        }
                    }
                });
                break;
        }

        binding.btnClearMsgRecord.setOnClickListener(v -> msgAdapter.clearMsg());



    }

    private void initRecyclerView() {
        recyclerView = binding.recyclerviewTcpudp;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        msgAdapter = new TcpUdpMsgAdapter(MsgList);
        recyclerView.setAdapter(msgAdapter);
    }

    /**
     * 初始化消息列表，在右侧显示Hello，在左侧显示World
     */
    private void initMsgList() {
        /*TcpUdpMsg msg1 = new TcpUdpMsg("Hello", "114.51.4.1",
                1234, System.currentTimeMillis(), false);
        MsgList.add(msg1);
        TcpUdpMsg msg2 = new TcpUdpMsg("服务器向客户端发送了Hello",
                System.currentTimeMillis() + 2000);
        MsgList.add(msg2);
        TcpUdpMsg msg3 = new TcpUdpMsg("World", "114.51.4.2",
                5678, System.currentTimeMillis() + 4000, true);
        MsgList.add(msg3);*/
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        tcpServer.stop();
    }

}