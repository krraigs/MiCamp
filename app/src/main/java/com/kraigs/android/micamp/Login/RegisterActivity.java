package com.kraigs.android.micamp.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kraigs.android.micamp.UserProfile.EditUserBasicInfoActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private TextInputEditText userEmail,userPassword,userRepeatPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    CollectionReference userCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        userCol = FirebaseFirestore.getInstance().collection("User");
        getSupportActionBar().hide();
        initializeMethods();

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email  = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String repeatPassword = userRepeatPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(repeatPassword)){
            Toast.makeText(this, "Please enter password again", Toast.LENGTH_SHORT).show();
        }
        else if(password.equals(repeatPassword)){
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait,while we are creating new Account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUserId = mAuth.getCurrentUser().getUid();

                                String email1 = mAuth.getCurrentUser().getEmail();

                                HashMap<String,Object> userMap = new HashMap<>();
                                userMap.put("device_token",deviceToken);
                                userMap.put("userID",email1);
                                userMap.put("name"," ");
                                userMap.put("summary", " ");
                                userMap.put("branch", " ");
                                userMap.put("year", " ");
                                userMap.put("course", " ");
                                userMap.put("quality", " ");
                                userMap.put("key",currentUserId);
                                userMap.put("password",password);

                                userCol.document(currentUserId)
                                        .set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            SendUserToMainActivity();
                                            Toast.makeText(RegisterActivity.this, "Account created Successfully", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        } else{
                                            loadingBar.dismiss();
                                            Toast.makeText(RegisterActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }

                    });
        }
        else {
            Toast.makeText(this, "Password does not match. Please Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeMethods() {
        createAccountButton = (Button) findViewById(R.id.bt_go);
        userEmail =  findViewById(R.id.et_username);
        userPassword =  findViewById(R.id.et_password);
        userRepeatPassword =  findViewById(R.id.et_repeatpassword);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, EditUserBasicInfoActivity.class);
        mainIntent.putExtra("parentActivity","main");
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

}