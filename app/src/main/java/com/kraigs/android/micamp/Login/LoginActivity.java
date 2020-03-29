package com.kraigs.android.micamp.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kraigs.android.micamp.UserProfile.EditUserBasicInfoActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kraigs.android.micamp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private CardView phoneLoginButton;
    TextView registerUserButton;
    private static final String TAG = "LoginActivity";
    private TextInputEditText userEmail, userPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCol;
    CardView googleCv;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.parseColor("#4D7AED"));
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        userCol = db.collection("User");

        initialiseFields();

        registerUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MobileLoginActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });

        googleSignIn();

        findViewById(R.id.guestTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                anonymous();
            }
        });

    }

    private void anonymous() {
        loadingBar.setTitle("Sign in");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            userCol.document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        if (task.getResult().exists()){

                                            HashMap<String, Object> userMap = new HashMap<>();
                                            userMap.put("device_token", deviceToken);

                                            userCol.document(currentUserId).update(userMap);
                                            loadingBar.dismiss();
                                            LoginActivity.this.SendUserToMainActivity();
                                            Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();

                                        } else{

                                            HashMap<String, Object> userMap = new HashMap<>();
                                            userMap.put("device_token", deviceToken);
                                            userMap.put("userID", "Anonymous");
                                            userMap.put("name","Anonymous");
                                            userMap.put("summary", "");
                                            userMap.put("branch", "");
                                            userMap.put("year", "");
                                            userMap.put("course", "");
                                            userMap.put("key",currentUserId);
                                            userMap.put("quality","");

                                            userCol.document(currentUserId).set(userMap);
                                            loadingBar.dismiss();
                                            LoginActivity.this.SendUserToMainActivity();
                                            Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    private void allowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password..", Toast.LENGTH_SHORT).show();

        } else {
            loadingBar.setTitle("Sign in");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String email1 = mAuth.getCurrentUser().getEmail();

                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("device_token", deviceToken);
                                userMap.put("userID", email1);

                                userCol.document(currentUserID)
                                        .update(userMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    SendUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    private void googleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        googleCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn();

                Toast.makeText(LoginActivity.this, "Google sign In", Toast.LENGTH_SHORT).show();

            }

        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Toast.makeText(this, "Signing In...", Toast.LENGTH_SHORT).show();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }



    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        loadingBar.setTitle("Sign in");
        loadingBar.setMessage("Please Wait");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNew) {
                                Log.d(TAG,"new google acc");

                                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this.getApplicationContext());

                                String personName = account.getDisplayName();
                                String email = account.getEmail();
                                String image = account.getPhotoUrl().toString();

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUserId = mAuth.getCurrentUser().getUid();

                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("device_token", deviceToken);
                                userMap.put("userID", email);
                                userMap.put("name",personName);
                                userMap.put("summary", "");
                                userMap.put("branch", "");
                                userMap.put("year", "");
                                userMap.put("course", "");
                                userMap.put("key",currentUserId);
                                userMap.put("quality","");

                                if (image!=null && !TextUtils.isEmpty(image)){
                                    userMap.put("image",image);
                                }

                                userCol.document(currentUserId).set(userMap);
                                loadingBar.dismiss();
                                LoginActivity.this.SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();

                            } else {

                                Log.d(TAG,"old google account");
                                String Uid = mAuth.getCurrentUser().getUid();
                                String token = FirebaseInstanceId.getInstance().getToken();
                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("device_token", token);
                                userCol.document(Uid).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingBar.dismiss();
                                        SendUserToMainActivity();
                                    }
                                });
                            }
                        } else {
                            loadingBar.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();


                        }

                    }
                });
    }

    private void initialiseFields() {
        loginButton =  findViewById(R.id.bt_go_login);
        phoneLoginButton =  findViewById(R.id.fab_mobile);
        registerUserButton =  findViewById(R.id.fab_register);
        userEmail = findViewById(R.id.et_username_login);
        userPassword = findViewById(R.id.et_password_login);
        loadingBar = new ProgressDialog(this);
        googleCv = findViewById(R.id.googleCv);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, EditUserBasicInfoActivity.class);
        mainIntent.putExtra("parentActivity","main");
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
