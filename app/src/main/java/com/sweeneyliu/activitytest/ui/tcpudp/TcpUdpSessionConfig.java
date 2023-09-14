package com.sweeneyliu.activitytest.ui.tcpudp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class TcpUdpSessionConfig implements Parcelable{
    //该类用于存储TCP/UDP初始会话配置，包括会话模式（TCP/UDP）、本地IP地址、本地端口、远程IP地址、远程端口
    private final String mode;
    private final String localIp;
    private final int localPort;
    @Nullable
    private final String remoteIp;
    private final int remotePort;

    protected TcpUdpSessionConfig(Parcel in) {
        mode = in.readString();
        localIp = in.readString();
        localPort = in.readInt();
        remoteIp = in.readString();
        remotePort = in.readInt();
    }

    public TcpUdpSessionConfig(String mode,String localIp, int localPort, @Nullable String remoteIp, int remotePort) {
        this.mode = mode;
        this.localIp = localIp;
        this.localPort = localPort;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
    }
    // getter方法
    public String getMode() {
        return mode;
    }
    public String getLocalIp() {
        return localIp;
    }
    public int getLocalPort() {
        return localPort;
    }
    @Nullable
    public String getRemoteIp() {
        return remoteIp;
    }
    public int getRemotePort() {
        return remotePort;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mode);
        dest.writeString(localIp);
        dest.writeInt(localPort);
        dest.writeString(remoteIp);
        dest.writeInt(remotePort);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TcpUdpSessionConfig> CREATOR = new Creator<TcpUdpSessionConfig>() {
        @Override
        public TcpUdpSessionConfig createFromParcel(Parcel in) {
            return new TcpUdpSessionConfig(in);
        }

        @Override
        public TcpUdpSessionConfig[] newArray(int size) {
            return new TcpUdpSessionConfig[size];
        }
    };
}
