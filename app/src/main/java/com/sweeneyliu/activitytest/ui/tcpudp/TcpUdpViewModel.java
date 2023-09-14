package com.sweeneyliu.activitytest.ui.tcpudp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sweeneyliu.activitytest.R;

public class TcpUdpViewModel extends ViewModel {
    // TcpUdpFragment中下拉菜单中选择的TCP/UDP模式
    private final MutableLiveData<TcpUdpFormState> tcpUdpFormState = new MutableLiveData<>();
    LiveData<TcpUdpFormState> getTcpUdpFormState() {
        return tcpUdpFormState;
    }
    // TCP/UDP表单数据发生变化时，更新表单状态
    public void tcpUdpFormDataChanged(String selectedMode, String localPort, String remoteIp, String remotePort) {
        switch (selectedMode) {
            case "TCP服务器":
                if (!isPortValid(localPort)) {
                    tcpUdpFormState.setValue(new TcpUdpFormState(selectedMode, R.string.invalid_local_port));
                } else {
                    tcpUdpFormState.setValue(new TcpUdpFormState(selectedMode,true));
                }
                break;
            case "TCP客户端":
                if (!isIpValid(remoteIp) || !isPortValid(remotePort)) {
                    tcpUdpFormState.setValue(new TcpUdpFormState(selectedMode,
                        isIpValid(remoteIp) ? null : R.string.invalid_remote_ip,
                        isPortValid(remotePort) ? null : R.string.invalid_remote_port));
                } else {
                    tcpUdpFormState.setValue(new TcpUdpFormState(selectedMode,true));
                }
                break;
            case "UDP":
                if (!isPortValid(localPort) || !isIpValid(remoteIp) || !isPortValid(remotePort)) {
                    tcpUdpFormState.setValue(new TcpUdpFormState(selectedMode,
                        isPortValid(localPort) ? null : R.string.invalid_local_port,
                        isIpValid(remoteIp) ? null : R.string.invalid_remote_ip,
                        isPortValid(remotePort) ? null : R.string.invalid_remote_port));
                } else {
                    tcpUdpFormState.setValue(new TcpUdpFormState(selectedMode,true));
                }
                break;
        }
    }


    // 检查端口号是否合法
    private boolean isPortValid(String port) {
        if (port == null) {
            return false;
        }
        try {
            int portNum = Integer.parseInt(port);
            return portNum > 0 && portNum < 65536;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    // 检查IP地址是否合法，这里只检查IPv4地址
    private boolean isIpValid(String ip) {
        if (ip == null) {
            return false;
        }
        String[] ipParts = ip.split("\\.");
        if (ipParts.length != 4 || ip.endsWith(".")) {
            return false;
        }
        for (String ipPart : ipParts) {
            try {
                int ipPartNum = Integer.parseInt(ipPart);
                if (ipPartNum < 0 || ipPartNum > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }




}