package shyunku.project.together.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import shyunku.project.together.Constants.Global;
import shyunku.project.together.Objects.Chat;
import shyunku.project.together.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private ArrayList<Chat> chatList;

    public static final int SENDER = 0;
    public static final int RECEIVER = 1;

    public ChatAdapter(Context baseContext, ArrayList<Chat> chats) {
        chatList = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = null;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(viewType == SENDER){
            view = inflater.inflate(R.layout.sent_message_item, parent, false);
        }else{
            view = inflater.inflate(R.layout.received_message_item, parent, false);
        }
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        if(chat.sender.equals(Global.getOwner()))
            holder.sender.setText("나");
        else
            holder.sender.setText(chat.sender);
        holder.content.setText(chat.content);
        holder.isRead.setText(chat.isRead?"읽음":"");
        holder.timestamp.setText(chat.timestamp);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = chatList.get(position);
        return chat.sender.equals(Global.getOwner())?SENDER:RECEIVER;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView sender;
        public TextView content;
        public TextView timestamp;
        public TextView isRead;

        public ViewHolder(View v){
            super(v);
            sender = (TextView) v.findViewById(R.id.sender_view);
            content = (TextView) v.findViewById(R.id.content_view);
            timestamp = (TextView) v.findViewById(R.id.timestamp_view);
            isRead = (TextView)v.findViewById(R.id.isread_view);
        }
    }
}
