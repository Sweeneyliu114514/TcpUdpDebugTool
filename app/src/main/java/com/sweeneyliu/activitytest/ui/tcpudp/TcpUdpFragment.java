package com.sweeneyliu.activitytest.ui.tcpudp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.sweeneyliu.activitytest.R;
import com.sweeneyliu.activitytest.databinding.FragmentTcpUdpBinding;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;

public class TcpUdpFragment extends Fragment {

    private FragmentTcpUdpBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTcpUdpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TcpUdpViewModel tcpUdpViewModel =
                new ViewModelProvider(this).get(TcpUdpViewModel.class);


        binding.promptText.setText(getLocalIPAddress().equals("")
                ? "无法获取本机IP地址，请检查网络连接" : "本地IP地址：" + getLocalIPAddress());
        binding.btnRefreshIp.setOnClickListener(v -> binding.promptText.setText(getLocalIPAddress().equals("")
                ? "无法获取本机IP地址，请检查网络连接" : "本地IP地址：" + getLocalIPAddress()));
        // 表单的文本观察者，当表单文本发生变化时，调用ViewModel中的tcpUdpFormDataChanged方法更新状态
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tcpUdpViewModel.tcpUdpFormDataChanged(
                        binding.tcpudpMode.getText().toString(),
                        binding.localPort.getText().toString(),
                        binding.remoteIP.getText().toString(),
                        binding.remotePort.getText().toString()
                );
            }
        };
        binding.tcpudpMode.addTextChangedListener(afterTextChangedListener);
        binding.localPort.addTextChangedListener(afterTextChangedListener);
        binding.remoteIP.addTextChangedListener(afterTextChangedListener);
        binding.remotePort.addTextChangedListener(afterTextChangedListener);

        // 给ViewModel中的tcpUdpFormState设置观察者
        tcpUdpViewModel.getTcpUdpFormState().observe(getViewLifecycleOwner(), tcpUdpFormState -> {
            if (tcpUdpFormState == null) {
                return;
            }
            // 如果表单状态不正确，禁用创建TCP/UDP对话的按钮
            binding.btnNewTcpudpSession.setEnabled(tcpUdpFormState.isDataValid());
            // 如果表单状态不正确，显示错误信息
            if (tcpUdpFormState.getSelectedMode() != null) {
                switch (tcpUdpFormState.getSelectedMode()) {
                    case "TCP服务器":
                        binding.localPortWrapper.setEnabled(true);
                        binding.localPortWrapper.setError(tcpUdpFormState.getLocalPortError() == null ? null :
                                getString(tcpUdpFormState.getLocalPortError()));
                        //若选择了TCP服务器模式，则禁用远程IP和远程端口的输入
                        binding.remoteIPWrapper.setEnabled(false);
                        binding.remotePortWrapper.setEnabled(false);
                        break;
                    case "TCP客户端":
                        binding.remoteIPWrapper.setEnabled(true);
                        binding.remotePortWrapper.setEnabled(true);
                        binding.remoteIPWrapper.setError(tcpUdpFormState.getRemoteIpError() == null ? null :
                                getString(tcpUdpFormState.getRemoteIpError()));
                        binding.remotePortWrapper.setError(tcpUdpFormState.getRemotePortError() == null ? null :
                                getString(tcpUdpFormState.getRemotePortError()));
                        //若选择了TCP客户端模式，则禁用本地端口的输入
                        binding.localPortWrapper.setEnabled(false);
                        break;
                    case "UDP":
                        binding.localPortWrapper.setEnabled(true);
                        binding.remoteIPWrapper.setEnabled(true);
                        binding.remotePortWrapper.setEnabled(true);
                        binding.localPortWrapper.setError(tcpUdpFormState.getLocalPortError() == null ? null :
                                getString(tcpUdpFormState.getLocalPortError()));
                        binding.remoteIPWrapper.setError(tcpUdpFormState.getRemoteIpError() == null ? null :
                                getString(tcpUdpFormState.getRemoteIpError()));
                        binding.remotePortWrapper.setError(tcpUdpFormState.getRemotePortError() == null ? null :
                                getString(tcpUdpFormState.getRemotePortError()));
                        break;
                }
            }
        });
        NavController navController = Navigation.findNavController(view);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.tcpUdpSessionFragment) {
                destination.setLabel(binding.tcpudpMode.getText().toString() + "会话");
            }
        });

        // 将TCP/UDP模式、本地端口、远程IP和远程端口的值打包为一个TcpUdpSessionConfig对象
        // 将该对象作为SafeArgs的参数传递给TcpUdpSessionFragment
        binding.btnNewTcpudpSession.setOnClickListener(v -> {
            String mode = binding.tcpudpMode.getText().toString();
            String localIp = getLocalIPAddress();
            String remoteIp = binding.remoteIP.getText().toString().equals("") ?
                    "0" : Objects.requireNonNull(binding.remoteIP.getText()).toString();
            int localPort = Integer.parseInt(binding.localPort.getText().toString().equals("") ?
                    "0" : Objects.requireNonNull(binding.localPort.getText()).toString());
            int remotePort = Integer.parseInt(binding.remotePort.getText().toString().equals("") ?
                    "0" : Objects.requireNonNull(binding.remotePort.getText()).toString());
            TcpUdpSessionConfig config = new TcpUdpSessionConfig
                    (mode, localIp, localPort, remoteIp, remotePort);
            TcpUdpFragmentDirections.CreateTcpUdpSessionAction action =
                    TcpUdpFragmentDirections.createTcpUdpSessionAction(config);
            navController.navigate(action);
            // 将模式选择框的值重置为默认值
            binding.tcpudpMode.clearListSelection();
        });



    }
    @Override
    // material库中关于autoCompleteTextView的bug，在跳转到其它fragment后只能显示下拉菜单中的一行
    // 将setAdapter的相关代码放入onResume可以解决这个问题，详情见：
    // https://github.com/material-components/material-components-android/issues/2012
    public void onResume() {
        super.onResume();
        String[] mode_list = getActivity().getResources().getStringArray(R.array.tcpudp_mode_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu_tcp_udp, mode_list);
        binding.tcpudpMode.setAdapter(adapter);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getLocalIPAddress() {
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            return inetAddr.getHostAddress();
                        }
                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案
            return candidateAddress == null ?
                    "" : candidateAddress.getHostAddress();
        } catch (SocketException e) {
            e.printStackTrace();
            Log.w("getLocalHostExactAddress", "无法找到合适的网卡接口");
        }
        return "";
    }
}