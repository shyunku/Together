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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Engines.LogEngine;
import shyunku.project.together.Objects.User;
import shyunku.project.together.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    User me = new User(), opp = new User();
    int CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, CODE);
            }
        }
        String id = Global.sha256(tm.getLine1Number());
        Global.setCurrentDeviceID(id);

        new LogEngine().sendLog("DEVICE_ID = "+Global.curDeviceID);
        initialSetting();

        // FCM KEY SETTING
        //registerFCMKey();

        listenToUserInfo();
    }

    private void listenToUserInfo(){
        final DatabaseReference ref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users");

        final TextView myStatusView = findViewById(R.id.my_status);
        final TextView myStatusDescription = findViewById(R.id.my_status_message);
        final TextView myHappinessView = findViewById(R.id.my_happiness);
        final ProgressBar myHappinessBar = findViewById(R.id.my_happiness_bar);

        // Listen My Info
        ref.child(Global.curDeviceID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Already Exists
                    me = dataSnapshot.getValue(User.class);
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

        // Listen Opp Info and create me if I doesn't exist
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> users = new ArrayList<>();
                boolean foundMe = false;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String iteratingDeviceID = (String) snapshot.child("deviceID").getValue();
                    String username = (String) snapshot.child("name").getValue();
                    users.add(username);

                    if(iteratingDeviceID.equals(Global.curDeviceID)){
                        foundMe = true;
                    }else{
                        opp = snapshot.getValue(User.class);
                        Global.OpperName = opp.name;

                        oppStatusView.setText(opp.status);
                        oppStatusDescription.setText(opp.getStatusDescription(MainActivity.this));
                        oppHappinessView.setText(opp.happiness+"");
                        oppHappinessBar.setProgress(opp.happiness);
                        //oppStatusBG.setBackgroundResource(opp.getStatusBackgroundColorTag(MainActivity.this));

                        Global.setOppFCMkey(opp.FCMtoken);
                        //new LogEngine().sendLog("opp FCM_KEY = "+opp.FCMtoken);

                        final TextView statusTitle = findViewById(R.id.opp_status_title);
                        statusTitle.setText(Global.getOpper()+"의 프로필");
                    }
                }

                // Check existance of my device
                if(!foundMe){
                    registerDevice(users);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void registerDevice(final List<String> usernameList){
        final DatabaseReference ref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users");

        // There is no me. - create one
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View viewGroup = inflater.inflate(R.layout.register_new_user_dialog, (ViewGroup) findViewById(R.id.register_new_user_layout));

        final EditText username = viewGroup.findViewById(R.id.input_new_username);

        Toast.makeText(MainActivity.this,"등록되지 않은 기기입니다. 등록이 필요합니다.", Toast.LENGTH_SHORT).show();

        builder.setTitle("새로운 유저로 등록");
        builder.setView(viewGroup);

        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("등록", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newUsername = username.getText().toString();

                if(newUsername.length() < 2){
                    Toast.makeText(MainActivity.this, "이름은 2자 이상 입력해주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if(usernameList.size() == 2){
                    ref.child(usernameList.get(0)).removeValue();
                }


                me = new User(newUsername, Global.curDeviceID);
                Map<String, Object> postVal = me.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(Global.rootName + "/users/" + Global.curDeviceID, postVal);
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);                // Prevent Back key
        dialog.setCanceledOnTouchOutside(false);    // Prevent outter touch to cancel
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dia) {
                Button negativeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder warningBuilder = new AlertDialog.Builder(MainActivity.this);

                        warningBuilder
                                .setTitle("기기 미등록 경고")
                                .setMessage("해당 기기는 미등록 상태입니다. 등록하지 않으면 앱을 사용할 수 없습니다.")
                                .setNegativeButton("돌아가기", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // resume register
                                    }
                                })
                                .setPositiveButton("등록 안함", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // cancel register
                                    }
                                });

                        final AlertDialog warningDialog = warningBuilder.create();
                        warningDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dia2) {
                                Button cancelAll = warningDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                cancelAll.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(MainActivity.this, "다시 등록하려면 애플리케이션을 재시작 해야합니다.", Toast.LENGTH_LONG).show();
                                        warningDialog.dismiss();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                        warningDialog.show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void initialSetting(){
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
                        me.happiness = seekBar.getProgress();

                        Map<String, Object> postVal = me.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Global.rootName+"/users/"+Global.curDeviceID, postVal);

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
                        childUpdates.put(Global.rootName+"/users/"+Global.curDeviceID, postVal);

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
                        //new LogEngine().sendLog("FCM_KEY = "+me.FCMtoken);

                        me.FCMtoken = token;
                        Map<String, Object> postVal = me.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Global.rootName+"/users/"+Global.curDeviceID, postVal);

                        FirebaseManageEngine.getFreshLocalDBref().updateChildren(childUpdates);
                        // Log and toast
                    }
                });
    }
}
