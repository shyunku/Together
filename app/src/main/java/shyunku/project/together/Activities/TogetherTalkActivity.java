package shyunku.project.together.Activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import shyunku.project.together.Adapters.ChatAdapter;
import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Objects.Chat;
import shyunku.project.together.R;
import shyunku.project.together.Services.FirebaseInstanceService;

public class TogetherTalkActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Chat> chats = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.together_talk);

        initialSetting();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mRecyclerView = (RecyclerView) findViewById(R.id.sendable_chat_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ChatAdapter(getBaseContext(), chats);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    public void initialSetting(){
        final EditText sendableTextField = (EditText)findViewById(R.id.message_content);
        Button  sendButton = (Button)findViewById(R.id.send_message_button);



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                String key = FirebaseManageEngine.getFreshLocalDBref().child(Global.rootName+"/chats").push().getKey();

                Chat chat = new Chat(Global.getOwner(), Global.sdf.format(cal.getTime()), false, sendableTextField.getText().toString(), key);

                sendableTextField.setText("");

                Map<String, Object> postVal = chat.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(Global.rootName+"/chats/"+key, postVal);

                InputMethodManager ipm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //ipm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                FirebaseManageEngine.getFreshLocalDBref().updateChildren(childUpdates);

                FirebaseManageEngine.sendNotificationMessage(chat.content);
            }
        });

        final DatabaseReference ref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/chats");
        ref.addValueEventListener(updateReader);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                chats.add(chat);
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() -1);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if(!chat.isRead)return;
                for(int i=chats.size()-1;i>=0;i--)
                    if(chats.get(i).id.equals(chat.id)){
                        chats.get(i).isRead = true;
                        break;
                    }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ValueEventListener updateReader = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            final DatabaseReference ref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/chats");
            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                Chat chat = snapshot.getValue(Chat.class);
                if(!chat.sender.equals(Global.getOwner())) {
                    ref.child(snapshot.getKey()).child("isRead").setValue(true);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final DatabaseReference ref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/chats");
        ref.removeEventListener(updateReader);
    }
}
