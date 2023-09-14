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
        if (msg.getIsPrompt()) {
            // 提示信息，将左右聊天气泡隐藏
            holder.layout_msg_received.setVisibility(View.GONE);
            holder.layout_msg_sent.setVisibility(View.GONE);
            holder.layout_msg_prompt_text.setVisibility(View.VISIBLE);
            holder.msg_prompt_text.setText(msg.getPromptText());
        }
        else {
            if (msg.getMessageType()) {
                // 接收到的消息
                holder.layout_msg_received.setVisibility(View.VISIBLE);
                holder.layout_msg_sent.setVisibility(View.GONE);
                holder.msg_received_description.setText(msg.getMessageDescription());
                holder.msg_received_content.setText(msg.getMessageContent());
            } else {
                // 发送的消息
                holder.layout_msg_received.setVisibility(View.GONE);
                holder.layout_msg_sent.setVisibility(View.VISIBLE);
                holder.msg_sent_description.setText(msg.getMessageDescription());
                holder.msg_sent_content.setText(msg.getMessageContent());
            }
        }
    }
    /**
     * 添加消息到消息列表并刷新
     */
    public void addMsg(TcpUdpMsg msg) {
        msgList.add(msg);
        notifyItemInserted(msgList.size() - 1);
    }
    /**
     * 清空消息列表并刷新
     */
    public void clearMsg() {
        msgList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }
    /**
     * 重写以下两个方法，使得每个item都有不同的viewType，防止item复用时出现错乱
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout layout_msg_received;
        private final LinearLayout layout_msg_sent;
        private final LinearLayout layout_msg_prompt_text;
        private final TextView msg_received_description;
        private final TextView msg_received_content;
        private final TextView msg_sent_description;
        private final TextView msg_sent_content;
        private final TextView msg_prompt_text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_msg_received = itemView.findViewById(R.id.layout_msg_item_received);
            layout_msg_sent = itemView.findViewById(R.id.layout_msg_item_sent);
            layout_msg_prompt_text = itemView.findViewById(R.id.layout_msg_prompt_text);
            msg_received_description = itemView.findViewById(R.id.msg_received_description);
            msg_received_content = itemView.findViewById(R.id.msg_received_content);
            msg_sent_description = itemView.findViewById(R.id.msg_sent_description);
            msg_sent_content = itemView.findViewById(R.id.msg_sent_content);
            msg_prompt_text = itemView.findViewById(R.id.msg_prompt_text);
        }
    }
}
