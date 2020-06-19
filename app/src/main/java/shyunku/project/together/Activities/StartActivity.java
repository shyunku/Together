package shyunku.project.together.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Engines.LogEngine;
import shyunku.project.together.Objects.User;
import shyunku.project.together.Objects.UserDump;
import shyunku.project.together.R;

public class StartActivity extends AppCompatActivity {
    TextView processMessageView;
    Button createPartyBtn;
    Button joinPartyBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);

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
        new LogEngine().sendLog("DEVICE_ID = "+Global.curDeviceID);

        deviceIdView.setText(String.format("Your Device ID : %s", Global.curDeviceID));

        setCurrentMessage("Try to Fetching User Info...");
        FirebaseManageEngine.getUserDumpListRef().child(Global.curDeviceID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Exist
                    setCurrentMessage("Checking for User Info...");
                    UserDump me = dataSnapshot.getValue(UserDump.class);
                    usernameView.setText(String.format("Your Username : %s", me.username));

                    // Check SubParty
                    FirebaseManageEngine.getPartiesRef().child(me.subordinatedParty).child(me.deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            if(dataSnapshot2.exists()){
                                // Party Exists & Member Exists
                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
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
                joinDialog.setCancelable(false);
                joinDialog.setCanceledOnTouchOutside(false);
                joinDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positiveBtn = joinDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String joinCodeValue = joinCode.getText().toString();
                                if(joinCodeValue.isEmpty()){

                                }

                                // Check Party Existance
                                FirebaseManageEngine.getPartiesRef().child(joinCodeValue)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(!dataSnapshot.exists()){
                                                    // not available - no such data
                                                    Toast.makeText(StartActivity.this,"입력 코드의 파티는 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                                }else if(dataSnapshot.getChildrenCount() < 2){
                                                    // available
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
