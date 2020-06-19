package shyunku.project.together.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Engines.Lgm;
import shyunku.project.together.Objects.User;
import shyunku.project.together.Objects.UserDump;
import shyunku.project.together.R;

public class StartActivity extends AppCompatActivity {
    TextView processMessageView;
    Button createPartyBtn;
    Button joinPartyBtn;

    UserDump meDump;

    final int CODE = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, CODE);
            }
        }

        // Views
        final TextView deviceIdView = findViewById(R.id.display_device_id);
        final TextView usernameView = findViewById(R.id.display_username);
        processMessageView = findViewById(R.id.process_message);
        createPartyBtn = findViewById(R.id.create_new_party);
        joinPartyBtn = findViewById(R.id.participate_party);
        final TextView versionView = findViewById(R.id.version);

        versionView.setText(Global.version);

        setCurrentMessage("Fetching Current Device ID...");
        // Not Immutable (app signature based)
        @SuppressLint("HardwareIds") final String deviceId = Global.sha256(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));

        Global.setCurrentDeviceID(deviceId);
        Lgm.g("DEVICE_ID = "+Global.curDeviceID);

        deviceIdView.setText(String.format("Your Device ID : %s", Global.curDeviceID));

        setCurrentMessage("Try to Fetching User Info...");
        FirebaseManageEngine.getUserDumpListRef().child(Global.curDeviceID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Exist
                    setCurrentMessage("Checking for User Info...");
                    meDump = dataSnapshot.getValue(UserDump.class);
                    usernameView.setText(String.format("Your Username : %s", meDump.username));

                    // Check SubParty
                    Lgm.g("party: "+meDump.subordinatedParty+", user: "+Global.curDeviceID);
                    FirebaseManageEngine.getPartiesRef().child(meDump.subordinatedParty).child("users").child(Global.curDeviceID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                    if(dataSnapshot2.exists()){
                                        // Party Exists & Member Exists
                                        setCurrentMessage("Fetching party Info...");
                                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                        intent.putExtra("UserDump", meDump);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }else{
                                        // No Party Subordinated
                                        Toast.makeText(StartActivity.this,"어느 파티에도 속해 있지 않습니다. 파티를 생성하거나 참가해주세요.", Toast.LENGTH_SHORT).show();
                                        setCurrentMessage("Waiting for User Selection...");
                                        createPartyBtn.setVisibility(View.VISIBLE);
                                        joinPartyBtn.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }else{
                    // Doesn't Exist
                    setCurrentMessage("Register as New User...");
                    registerDevice();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        createPartyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Party 생성 및 user push
                DatabaseReference partyRef = FirebaseManageEngine.getPartiesRef();
                String partyKey = partyRef.push().getKey();
                Lgm.g("partykey: "+partyKey);
                User me = new User(meDump.username, meDump.deviceID);

                assert partyKey != null;
                FirebaseManageEngine.pushSomething(partyRef.child(partyKey).child("users"), me.deviceID, me.toMap());

                // UserDump에 기록
                FirebaseManageEngine.registerJoinedPartyCode(partyKey);
            }
        });

        joinPartyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                View viewGroup = inflater.inflate(R.layout.join_party_dialog, (ViewGroup) findViewById(R.id.register_new_user_layout));

                builder.setTitle("기존 파티에 참가");
                builder.setView(viewGroup);
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // dismiss
                    }
                });
                builder.setPositiveButton("참가", null);

                final EditText joinCode = viewGroup.findViewById(R.id.join_code_view);

                final AlertDialog joinDialog = builder.create();
                joinDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positiveBtn = joinDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String joinCodeValue = joinCode.getText().toString();
                                if(joinCodeValue.isEmpty()){
                                    Toast.makeText(StartActivity.this,"입력 코드를 입력해주세요!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Check Party Existance
                                FirebaseManageEngine.getPartiesRef().child(joinCodeValue)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(!dataSnapshot.exists()){
                                                    // not available - no such data
                                                    Toast.makeText(StartActivity.this,"입력 코드의 파티는 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                                }else if(dataSnapshot.child("users").getChildrenCount() < 2){
                                                    // available (추후에 참가 요청 보내기로 변경)
                                                    DatabaseReference partyRef = FirebaseManageEngine.getPartiesRef();
                                                    User me = new User(meDump.username, meDump.deviceID);
                                                    FirebaseManageEngine.pushSomething(partyRef.child(joinCodeValue).child("users"), me.deviceID, me.toMap());

                                                    FirebaseManageEngine.registerJoinedPartyCode(joinCodeValue);

                                                    joinDialog.dismiss();
                                                }else{
                                                    // not available - 2 people
                                                    Toast.makeText(StartActivity.this,"해당 파티는 이미 정원이 찼습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        });
                    }
                });

                joinDialog.show();
            }
        });
    }

    private void registerDevice(){
        final DatabaseReference ref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users");

        // There is no me. - create one
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        View viewGroup = inflater.inflate(R.layout.register_new_user_dialog, (ViewGroup) findViewById(R.id.register_new_user_layout));

        final EditText username = viewGroup.findViewById(R.id.input_new_username);

        Toast.makeText(StartActivity.this,"등록되지 않은 기기입니다. 등록이 필요합니다.", Toast.LENGTH_SHORT).show();

        builder.setTitle("새로운 유저로 등록");
        builder.setView(viewGroup);

        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("등록", null);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);                // Prevent Back key
        dialog.setCanceledOnTouchOutside(false);    // Prevent outter touch to cancel
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dia) {
                Button negativeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                negativeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder warningBuilder = new AlertDialog.Builder(StartActivity.this);

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
                                        finish();
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
                                        Toast.makeText(StartActivity.this, "다시 등록하려면 애플리케이션을 재시작 해야합니다.", Toast.LENGTH_LONG).show();
                                        warningDialog.dismiss();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                        warningDialog.show();
                    }
                });

                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String newUsername = username.getText().toString();

                        if(newUsername.length() < 2){
                            Toast.makeText(StartActivity.this, "이름은 2자 이상 입력해주세요.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        final UserDump me = new UserDump(newUsername, Global.curDeviceID);
                        FirebaseManageEngine.pushUserDump(me);
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    private void setCurrentMessage(String message){
        processMessageView.setText(message);
    }
}
