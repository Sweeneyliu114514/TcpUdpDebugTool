package com.sweeneyliu.activitytest.ui.tcpudp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sweeneyliu.activitytest.R;

import java.util.List;

public class TcpUdpMsgAdapter extends RecyclerView.Adapter<TcpUdpMsgAdapter.ViewHolder> {
    List<TcpUdpMsg> msgList;
    // constructor
    public TcpUdpMsgAdapter(List<TcpUdpMsg> msgList) {
        this.msgList = msgList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.layout_msg_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TcpUdpMsgAdapter.ViewHolder holder, int position) {
        TcpUdpMsg msg = msgList.get(position);
        if(msg.getMessageType()) {
            // remote message
            holder.layout_msg_received.setVisibility(View.VISIBLE);
            holder.layout_msg_sent.setVisibility(View.GONE);
            holder.msg_received_description.setText(msg.getMessageDescription());
            holder.msg_received_content.setText(msg.getMessageContent());
        } else {
            // local message
            holder.layout_msg_received.setVisibility(View.GONE);
            holder.layout_msg_sent.setVisibility(View.VISIBLE);
            holder.msg_sent_description.setText(msg.getMessageDescription());
            holder.msg_sent_content.setText(msg.getMessageContent());
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout_msg_received;
        private LinearLayout layout_msg_sent;
        private TextView msg_received_description;
        private TextView msg_received_content;
        private TextView msg_sent_description;
        private TextView msg_sent_content;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_msg_received = itemView.findViewById(R.id.layout_msg_item_received);
            layout_msg_sent = itemView.findViewById(R.id.layout_msg_item_sent);
            msg_received_description = itemView.findViewById(R.id.msg_received_description);
            msg_received_content = itemView.findViewById(R.id.msg_received_content);
            msg_sent_description = itemView.findViewById(R.id.msg_sent_description);
            msg_sent_content = itemView.findViewById(R.id.msg_sent_content);

        }
    }
}
