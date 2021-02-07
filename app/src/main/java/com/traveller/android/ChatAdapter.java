package com.traveller.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private ArrayList<ChatPojo> pojos;
    private Context context;

    public ChatAdapter(ArrayList<ChatPojo> pojos, Context context) {
        this.pojos = pojos;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_chat, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder viewHolder, int i) {
        ChatPojo pojo = pojos.get(i);
        if (pojo != null) {
            viewHolder.msgInchat.setText(pojo.getChatText());
            if (pojo.isRobo()) {
               // Log.d("ChatAdap", "onBindViewHolder() "+pojo.isRobo() + pojo.getChatText());
                //other side
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 0, 40, 40);
                viewHolder.llMainDiscussion.setLayoutParams(params);
                viewHolder.llMainDiscussion.setBackground(context.getResources().getDrawable(R.drawable.frame_chat_other));
                viewHolder.msgInchat.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                viewHolder.roboIcon.setVisibility(View.VISIBLE);
            } else {
                //my chats
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(40, 0, 0, 40);
              //  params.addRule(RelativeLayout.ALIGN_RIGHT);
                viewHolder.llMainDiscussion.setLayoutParams(params);
                viewHolder.llMainDiscussion.setBackground(context.getResources().getDrawable(R.drawable.frame_chat_me));
                viewHolder.msgInchat.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                viewHolder.roboIcon.setVisibility(View.GONE);
               // Log.d("ChatAdap", "onBindViewHolder() not robo "+pojo.isRobo() + pojo.getChatText());
            }
        }
    }

    @Override
    public int getItemCount() {
       // Log.d("TAG", "getItemCount() called");
        return pojos.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView roboIcon;
        private RelativeLayout llMainDiscussion;
        private TextView msgInchat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            msgInchat = itemView.findViewById(R.id.msg_inchat);
            llMainDiscussion = itemView.findViewById(R.id.ll_main_discussion);
            roboIcon = itemView.findViewById(R.id.robo_icon);
        }
    }
}
