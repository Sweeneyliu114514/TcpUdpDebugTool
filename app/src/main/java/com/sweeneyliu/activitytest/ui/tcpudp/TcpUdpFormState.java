package com.sweeneyliu.activitytest.ui.tcpudp;

public class TcpUdpFormState {
    // 该类用于保存TCP/UDP会话配置表单的状态，包括本地端口、远程IP地址、远程端口以及选择的模式
    private final String selectedMode;
    private final Integer localPortError;
    private final Integer remoteIpError;
    private final Integer remotePortError;

    private final boolean isDataValid;
    // TCP服务器模式表单错误
    public TcpUdpFormState(String selectedMode, Integer localPortError ) {
        this.selectedMode = selectedMode;
        this.localPortError = localPortError;
        this.remoteIpError = null;
        this.remotePortError = null;
        this.isDataValid = false;
    }
    // TCP客户端模式表单错误
    public TcpUdpFormState(String selectedMode, Integer remoteIpError, Integer remotePortError) {
        this.selectedMode = selectedMode;
        this.localPortError = null;
        this.remoteIpError = remoteIpError;
        this.remotePortError = remotePortError;
        this.isDataValid = false;
    }
    // UDP模式表单错误
    public TcpUdpFormState(String selectedMode, Integer localPortError, Integer remoteIpError, Integer remotePortError) {
        this.selectedMode = selectedMode;
        this.localPortError = localPortError;
        this.remoteIpError = remoteIpError;
        this.remotePortError = remotePortError;
        this.isDataValid = false;
    }
    // 表单正确
    public TcpUdpFormState(String selectedMode,boolean isDataValid) {
        this.selectedMode = selectedMode;
        this.localPortError = null;
        this.remoteIpError = null;
        this.remotePortError = null;
        this.isDataValid = isDataValid;
    }
    // getter方法
    public String getSelectedMode() {
        return selectedMode;
    }
    public Integer getLocalPortError() {
        return localPortError;
    }
    public Integer getRemoteIpError() {
        return remoteIpError;
    }
    public Integer getRemotePortError() {
        return remotePortError;
    }
    public boolean isDataValid() {
        return isDataValid;
    }
}
