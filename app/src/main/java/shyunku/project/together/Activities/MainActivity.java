package shyunku.project.together.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
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
import shyunku.project.together.Objects.User;
import shyunku.project.together.R;
import shyunku.project.together.Services.FirebaseInstanceService;

public class MainActivity extends AppCompatActivity {
    User me = new User(), opp = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialSetting();

        //my info
        DatabaseReference myref = FirebaseManageEngine.getFreshLocalDB().getReference(Global.rootName+"/users");
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    me = snapshot.getValue(User.class);
                    String gainedName = snapshot.child("name").getValue().toString();
                    if(gainedName.equals(Global.getOwner())){
                        final TextView statusView = findViewById(R.id.my_status);
                        final TextView statusMessage = findViewById(R.id.my_status_message);
                        final TextView happinessView = findViewById(R.id.my_happiness);

                        statusView.setText(me.status);
                        happinessView.setText(me.happiness+"/100");
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
                        final TextView statusMessage = findViewById(R.id.opp_status_message);
                        final TextView happinessView = findViewById(R.id.opp_happiness);


                        statusView.setText(opp.status);
                        happinessView.setText(opp.happiness+"/100");
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
        statusTitle.setText(Global.getOpper()+"의 프로필");

        final TextView Ver = findViewById(R.id.version);
        Ver.setText(Global.version +" -  "+Global.getOwner()+" 전용 APP");
        final Button updateHappinessBtn = findViewById(R.id.update_happiness_button);
        updateHappinessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final EditText editText = new EditText(MainActivity.this);
                final ConstraintLayout container = new ConstraintLayout(MainActivity.this);
                final ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.alert_dialog_internal_margin);
                params.rightMargin =getResources().getDimensionPixelSize(R.dimen.alert_dialog_internal_margin);

                editText.setLayoutParams(params);
                editText.setHint("기분 지수를 입력해주세요. (1~100)");
                container.addView(editText);

                builder.setTitle("기분 지수 업데이트");
                builder.setView(container);

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        me.happiness = Integer.parseInt(editText.getText().toString());

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
                        new LogEngine().sendLog("token = "+token);
                        // Log and toast
                    }
                });
    }
}
