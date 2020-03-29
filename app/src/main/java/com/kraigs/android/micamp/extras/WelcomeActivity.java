package com.kraigs.android.micamp.extras;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kraigs.android.micamp.Login.LoginActivity;
import com.kraigs.android.micamp.MainActivity;
import com.kraigs.android.micamp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kraigs.android.micamp.UserProfile.EditUserBasicInfoActivity;

import java.util.HashMap;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    CollectionReference userCol;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        rootRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        userCol = FirebaseFirestore.getInstance().collection("User");

        if (mAuth.getCurrentUser() != null){
            updateUserStatus(mAuth.getCurrentUser().getUid());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String college = preferences.getString("college", "");
            SharedPrefs prefs = new SharedPrefs(college);

            if (college!=null && !TextUtils.isEmpty(college)){
                Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }else{

                Intent intent = new Intent(WelcomeActivity.this, EditUserBasicInfoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }

        } else{
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void updateUserStatus(String uid) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("online", "true");

        rootRef.child("OfflineUsers").child(uid).updateChildren(map);

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("timestamp",ServerValue.TIMESTAMP);
        onlineStateMap.put("online", "false");
        rootRef.child("OfflineUsers").child(uid).onDisconnect().setValue(onlineStateMap);

    }

}
