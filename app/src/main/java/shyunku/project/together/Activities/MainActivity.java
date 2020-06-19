package shyunku.project.together.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shyunku.project.together.Constants.Global;
import shyunku.project.together.CustomViews.SquareProgressBarView;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Engines.Lgm;
import shyunku.project.together.Objects.User;
import shyunku.project.together.Objects.UserDump;
import shyunku.project.together.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    UserDump meDump;
    boolean isOppExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        meDump = (UserDump) intent.getSerializableExtra("UserDump");

        initialSetting();
        listenToUserInfo();
    }

    private void listenToUserInfo(){
        final DatabaseReference partyRef = FirebaseManageEngine.getPartyRef(meDump.subordinatedParty);
        final DatabaseReference userRef = partyRef.child("users");

        final TextView myStatusView = findViewById(R.id.my_status);
        final TextView myStatusDescription = findViewById(R.id.my_status_message);
        final TextView myHappinessView = findViewById(R.id.my_happiness);
        final ProgressBar myHappinessBar = findViewById(R.id.my_happiness_bar);

        // Listen My Info
        userRef.child(Global.curDeviceID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User me = dataSnapshot.getValue(User.class);
                    Global.OwnerName = me.name;

                    myStatusView.setText(me.status);
                    myStatusDescription.setText(me.getStatusDescription(MainActivity.this));
                    myHappinessView.setText(me.happiness+"");
                    myHappinessBar.setProgress(me.happiness);
                    //myStatusBG.setBackgroundResource(me.getStatusBackgroundColorTag(MainActivity.this));

                    final TextView deviceIDt = findViewById(R.id.device_id);
                    deviceIDt.setText("Device ID : "+Global.curDeviceID);
                    final TextView Ver = findViewById(R.id.version);
                    Ver.setText(Global.version +" |  "+Global.getOwner()+" 전용");

                    registerFCMKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final TextView oppStatusView = findViewById(R.id.opp_status);
        final TextView oppStatusDescription = findViewById(R.id.opp_status_message);
        final TextView oppHappinessView = findViewById(R.id.opp_happiness);
        final ProgressBar oppHappinessBar = findViewById(R.id.opp_happiness_bar);

        // Listen Opp Info
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String iteratingDeviceID = (String) snapshot.child("deviceID").getValue();
                    if(!iteratingDeviceID.equals(Global.curDeviceID)){
                        User opp = snapshot.getValue(User.class);
                        Global.OpperName = opp.name;

                        oppStatusView.setText(opp.status);
                        oppStatusDescription.setText(opp.getStatusDescription(MainActivity.this));
                        oppHappinessView.setText(opp.happiness+"");
                        oppHappinessBar.setProgress(opp.happiness);
                        //oppStatusBG.setBackgroundResource(opp.getStatusBackgroundColorTag(MainActivity.this));

                        Global.setOppFCMkey(opp.FCMtoken);
                        //new LogEngine().sendLog("opp FCM_KEY = "+opp.FCMtoken);

                        final TextView statusTitle = findViewById(R.id.opp_status_title);
                        statusTitle.setText(String.format("%s의 프로필", Global.getOpper()));

                        isOppExist = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initialSetting(){
        final Button goTogetherTalkButton = findViewById(R.id.go_together_talk);
        final Button requestButton = findViewById(R.id.request_button);
        final SquareProgressBarView myHappinessProgressBar = findViewById(R.id.my_happiness_bar);
        final TextView partyCodeView = findViewById(R.id.party_code);
        final DatabaseReference myRef = FirebaseManageEngine.getPartyRef(meDump.subordinatedParty).child("users").child(Global.curDeviceID);

        myHappinessProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View viewGroup = inflater.inflate(R.layout.happiness_update, (ViewGroup)findViewById(R.id.happiness_update_layout));
                builder.setTitle("기분 지수 업데이트");
                builder.setView(viewGroup );

                final SeekBar seekBar = (SeekBar)viewGroup.findViewById(R.id.happiness_seekBar);
                final TextView seekBarValue = (TextView)viewGroup.findViewById(R.id.happiness_monitor);

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            // Already Exists
                            User me = dataSnapshot.getValue(User.class);
                            seekBarValue.setText(me.happiness+"");
                            seekBar.setMax(100);
                            seekBar.setMin(1);
                            seekBar.setProgress(me.happiness);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

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
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef.child("happiness").setValue(seekBar.getProgress());
                    }
                });

                builder.show();
            }
        });

        goTogetherTalkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TogetherTalkActivity.class);
                startActivity(intent);
            }
        });


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
                        String statusMessage = "";

                        switch(menuItem.getItemId()){
                            case R.id.status_boring:
                                statusMessage = getResources().getString(R.string.status_boring_message);
                                break;
                            case R.id.status_hungry:
                                statusMessage = getResources().getString(R.string.status_hungry_message);
                                break;
                            case R.id.status_sleeping:
                                statusMessage = getResources().getString(R.string.status_sleeping_message);
                                break;
                            case R.id.status_sleepy:
                                statusMessage = getResources().getString(R.string.status_sleepy_message);
                                break;
                            case R.id.status_out:
                                statusMessage = getResources().getString(R.string.status_out_message);
                                break;
                            case R.id.status_private:
                                statusMessage = getResources().getString(R.string.status_private_message);
                                break;
                            case R.id.status_public:
                                statusMessage = getResources().getString(R.string.status_public_message);
                                break;
                            case R.id.status_inclass:
                                statusMessage = getResources().getString(R.string.status_inclass_message);
                                break;
                        }
                        myRef.child("status").setValue(statusMessage);
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

        partyCodeView.setText(String.format("Party Code: %s", meDump.subordinatedParty));
        // click to copy party code
        partyCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // assert that sdk level > 11 (honeycomb)
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Together Party Code", meDump.subordinatedParty);
                clipboardManager.setPrimaryClip(clipData);

                Global.makeToast(MainActivity.this, "참가 코드가 클립보드에 복사되었습니다.");
            }
        });
    }

    public void registerFCMKey(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Lgm.g("cannot gain token");
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        //new LogEngine().sendLog("FCM_KEY = "+me.FCMtoken);


                        final DatabaseReference myRef = FirebaseManageEngine.getPartyRef(meDump.subordinatedParty).child("users").child(Global.curDeviceID);
                        myRef.child("token").setValue(token);
                        // Log and toast
                    }
                });
    }
}
