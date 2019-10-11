package shyunku.project.together.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Engines.LogEngine;
import shyunku.project.together.Fragments.GoogleMapFragment;
import shyunku.project.together.Objects.User;
import shyunku.project.together.R;
import shyunku.project.together.Services.FirebaseInstanceService;

public class MainActivity extends AppCompatActivity {
    User me = new User(), opp = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Global.setCurrentDeviceID(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
        new LogEngine().sendLog("DEVICE_ID = "+Global.curDeviceID);
        Toast.makeText(getApplicationContext(), "ID = "+Global.curDeviceID, Toast.LENGTH_LONG);
        initialSetting();

        //my info
        DatabaseReference myref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users");
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    me = snapshot.getValue(User.class);
                    registerFCMKey();
                    String gainedName = snapshot.child("name").getValue().toString();
                    if(gainedName.equals(Global.getOwner())){
                        final TextView statusView = findViewById(R.id.my_status);
                        final TextView statusDescription = findViewById(R.id.my_status_message);
                        final TextView happinessView = findViewById(R.id.my_happiness);

                        final ConstraintLayout myStatusBG = findViewById(R.id.my_status_color);

                        statusView.setText(me.status);
                        statusDescription.setText(me.getStatusDescription(MainActivity.this));
                        happinessView.setText(me.happiness+"/100");
                        myStatusBG.setBackgroundResource(me.getStatusBackgroundColorTag(MainActivity.this));
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //opp info
        DatabaseReference oppref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users");
        oppref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    opp = snapshot.getValue(User.class);
                    String gainedName = snapshot.child("name").getValue().toString();
                    if(gainedName.equals(Global.getOpper())){
                        final TextView statusView = findViewById(R.id.opp_status);
                        final TextView statusDescription = findViewById(R.id.opp_status_message);
                        final TextView happinessView = findViewById(R.id.opp_happiness);
                        final ConstraintLayout oppStatusBG = findViewById(R.id.opp_status_color);

                        statusView.setText(opp.status);
                        statusDescription.setText(opp.getStatusDescription(MainActivity.this));
                        happinessView.setText(opp.happiness+"/100");
                        oppStatusBG.setBackgroundResource(opp.getStatusBackgroundColorTag(MainActivity.this));

                        Global.setOppFCMkey(snapshot.child("token").getValue().toString());
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void initialSetting(){
        final TextView statusTitle = findViewById(R.id.opp_status_title);
        final TextView deviceIDt = findViewById(R.id.device_id);
        statusTitle.setText(Global.getOpper()+"의 프로필");

        final TextView Ver = findViewById(R.id.version);
        deviceIDt.setText("Device ID : "+Global.curDeviceID);
        Ver.setText(Global.version +" -  "+Global.getOwner()+" 전용 APP - "+(Global.DEBUG_MODE?"Debug":"Release")+" mode");
        final Button updateHappinessBtn = findViewById(R.id.update_happiness_button);
        updateHappinessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View viewGroup = inflater.inflate(R.layout.happiness_update, (ViewGroup)findViewById(R.id.happiness_update_layout));
                final SeekBar seekBar = (SeekBar)viewGroup.findViewById(R.id.happiness_seekBar);
                final TextView seekBarValue = (TextView)viewGroup.findViewById(R.id.happiness_monitor);

                seekBarValue.setText(me.happiness+"");
                seekBar.setMax(100);
                seekBar.setMin(1);
                seekBar.setProgress(me.happiness);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        seekBarValue.setText(i+"");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });



                builder.setTitle("기분 지수 업데이트");
                builder.setView(viewGroup );

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        me.happiness = Integer.parseInt(seekBar.getProgress()+"");

                        Map<String, Object> postVal = me.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Global.rootName+"/users/"+Global.getOwner(), postVal);

                        FirebaseManageEngine.getFreshLocalDBref().updateChildren(childUpdates);
                    }
                });

                builder.show();
            }
        });

        final Button goTogetherTalkButton = findViewById(R.id.go_together_talk);
        goTogetherTalkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TogetherTalkActivity.class);
                startActivity(intent);
            }
        });

        final Button requestButton = findViewById(R.id.request_button);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.request_option, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String sender = Global.getOwner()+"님이 ";
                        switch(menuItem.getItemId()){
                            case R.id.request_call:
                                FirebaseManageEngine.sendNotificationRequestMessage(sender+"당신을 호출했습니다!");
                                break;
                            case R.id.request_db:
                                FirebaseManageEngine.sendNotificationRequestMessage(sender+"담배를 피자고 요청했습니다!");
                                break;
                            case R.id.request_help:
                                FirebaseManageEngine.sendNotificationRequestMessage(sender+"긴급 구조를 요청했습니다!");
                                break;
                            case R.id.request_inner_meal:
                                FirebaseManageEngine.sendNotificationRequestMessage(sender+"먹을 것을 시켜 먹자고 합니다!");
                                break;
                            case R.id.request_out:
                                FirebaseManageEngine.sendNotificationRequestMessage(sender+"나가자고 요청했습니다!");
                                break;
                            case R.id.request_outer_meal:
                                FirebaseManageEngine.sendNotificationRequestMessage(sender+"나가서 뭔가 먹자고 요청했습니다!");
                                break;
                        }
                        Global.makeToast(MainActivity.this, "요청을 보냈습니다!");
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        final Button updateStatusButton = findViewById(R.id.update_status_button);
        updateStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.status_option, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String StatusMessage = "";

                        switch(menuItem.getItemId()){
                            case R.id.status_boring:
                                StatusMessage = getResources().getString(R.string.status_boring_message);
                                break;
                            case R.id.status_hungry:
                                StatusMessage = getResources().getString(R.string.status_hungry_message);
                                break;
                            case R.id.status_sleeping:
                                StatusMessage = getResources().getString(R.string.status_sleeping_message);
                                break;
                            case R.id.status_sleepy:
                                StatusMessage = getResources().getString(R.string.status_sleepy_message);
                                break;
                            case R.id.status_out:
                                StatusMessage = getResources().getString(R.string.status_out_message);
                                break;
                            case R.id.status_private:
                                StatusMessage = getResources().getString(R.string.status_private_message);
                                break;
                            case R.id.status_public:
                                StatusMessage = getResources().getString(R.string.status_public_message);
                                break;
                            case R.id.status_inclass:
                                StatusMessage = getResources().getString(R.string.status_inclass_message);
                                break;
                        }

                        me.status = StatusMessage;

                        Map<String, Object> postVal = me.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Global.rootName+"/users/"+Global.getOwner(), postVal);

                        FirebaseManageEngine.getFreshLocalDBref().updateChildren(childUpdates);
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        Button viewLocation = (Button) findViewById(R.id.view_our_location);
        viewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        final Button goMoneyTransactionPage = (Button)findViewById(R.id.money_management_button);
        goMoneyTransactionPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MoneyTransactionActivity.class);
                startActivity(intent);
            }
        });

        getSupportActionBar().hide();
    }

    public void registerFCMKey(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            new LogEngine().sendLog("cannot gain token");
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        //new LogEngine().sendLog("token = "+token);

                        me.FCMtoken = token;
                        Map<String, Object> postVal = me.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Global.rootName+"/users/"+Global.getOwner(), postVal);

                        FirebaseManageEngine.getFreshLocalDBref().updateChildren(childUpdates);
                        // Log and toast
                    }
                });
    }
}
