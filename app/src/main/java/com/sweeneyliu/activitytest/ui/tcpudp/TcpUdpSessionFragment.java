package com.sweeneyliu.activitytest.ui.tcpudp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sweeneyliu.activitytest.R;
import com.sweeneyliu.activitytest.databinding.FragmentTcpUdpSessionBinding;

import java.util.Objects;

public class TcpUdpSessionFragment extends Fragment {

    private TcpUdpSessionViewModel mViewModel;
    private FragmentTcpUdpSessionBinding binding;

    public static TcpUdpSessionFragment newInstance() {
        return new TcpUdpSessionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTcpUdpSessionBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TcpUdpSessionConfig config = TcpUdpSessionFragmentArgs.fromBundle(
                getArguments()).getInitialSessionConfig();
        if(config.getMode().equals("TCP服务器")) {
            TcpServer tcpServer = TcpServer.getInstance();
            tcpServer.setListeningPort(config.getLocalPort());
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}