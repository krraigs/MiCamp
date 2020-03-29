package com.kraigs.android.micamp.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.UserProfile.EditUserBasicInfoActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class MobileLoginActivity extends AppCompatActivity implements View.OnClickListener, OnOtpCompletionListener {

    private Button sendCodeButton;
    private TextInputEditText inputPhoneNo ;
    TextInputLayout phoneInputLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;

    private FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    private Button validateButton;
    private OtpView otpView;

    @BindView(R.id.otpLl)
    LinearLayout otpLl;

    @BindView(R.id.loginRl)
    RelativeLayout loginRl;
    TextView mobileTv;
    String phoneNumber;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference userCol;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);
        ButterKnife.bind(this);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.parseColor("#4D7AED"));
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        initializeFields();
        setListeners();

        userCol = db.collection("User");

        mobileSignIn();
    }

    private void mobileSignIn() {
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = inputPhoneNo.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(MobileLoginActivity.this, "Please enter Phone number to continue.", Toast.LENGTH_SHORT).show();
                }

                else{

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait we are authenticating your phone.");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91" + phoneNumber,
                            60, TimeUnit.SECONDS,
                            MobileLoginActivity.this,
                            mCallbacks);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(MobileLoginActivity.this, "Invalid Phone Number ,Please retry", Toast.LENGTH_SHORT).show();

                loginRl.setVisibility(View.VISIBLE);
                otpLl.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();

                Toast.makeText(MobileLoginActivity.this, "Code Sent,Please check and verify.", Toast.LENGTH_SHORT).show();

                loginRl.setVisibility(View.GONE);
                otpLl.setVisibility(View.VISIBLE);
                mobileTv.setText("Enter the OTP send to - " + phoneNumber);

            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String phoneNumber = mAuth.getCurrentUser().getPhoneNumber();

                            userCol.document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen failed.", e);
                                        return;
                                    }

                                    if (snapshot != null && snapshot.exists()) {
                                        Log.d(TAG, "Current data: " + snapshot.getData());

                                        HashMap<String,Object> userMap = new HashMap<>();
                                        userMap.put("device_token",deviceToken);
                                        userMap.put("userID",phoneNumber);
                                        userMap.put("key",currentUserId);

                                        userCol.document(currentUserId)
                                                .update(userMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        loadingBar.dismiss();
                                                        Toast.makeText(MobileLoginActivity.this, "You have logged in Successfully", Toast.LENGTH_SHORT).show();
                                                        SendUserToDetailActivity();
                                                    }
                                                });

                                    } else {
                                        Log.d(TAG, "Current data: null user not exist");

                                        HashMap<String,Object> userMap = new HashMap<>();
                                        userMap.put("device_token",deviceToken);
                                        userMap.put("userID",phoneNumber);
                                        userMap.put("key",currentUserId);
                                        userMap.put("number",phoneNumber);
                                        userMap.put("name"," ");
                                        userMap.put("summary", " ");
                                        userMap.put("branch", " ");
                                        userMap.put("year", " ");
                                        userMap.put("course", " ");
                                        userMap.put("quality", " ");

                                        userCol.document(currentUserId)
                                                .set(userMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        loadingBar.dismiss();
                                                        Toast.makeText(MobileLoginActivity.this, "You have logged in Successfully", Toast.LENGTH_SHORT).show();
                                                        SendUserToDetailActivity();
                                                    }
                                                });

                                    }

                                }
                            });

                        }

                        else{
                            String message = task.getException().toString();
                            Toast.makeText(MobileLoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void initializeFields() {

        loadingBar = new ProgressDialog(this);
        mobileTv = findViewById(R.id.mobileTv);

        sendCodeButton = findViewById(R.id.bt_send_code);
        inputPhoneNo = findViewById(R.id.phone_number_input);
        phoneInputLayout = findViewById(R.id.phoneLayout);
        loadingBar = new ProgressDialog(this);

        otpView = findViewById(R.id.otp_view);
        validateButton = findViewById(R.id.validate_button);

    }

    private void SendUserToDetailActivity() {
        Intent mainIntent = new Intent(MobileLoginActivity.this, EditUserBasicInfoActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY);
        mainIntent.putExtra("parentActivity","main");
        startActivity(mainIntent);
        finish();
    }


    private void setListeners() {
        validateButton.setOnClickListener(this);
        otpView.setOtpCompletionListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.validate_button) {
            loginRl.setVisibility(View.GONE);

            String verificationCode = otpView.getText().toString();
            if (TextUtils.isEmpty(verificationCode)){
                Toast.makeText(MobileLoginActivity.this, "Please enter Verification Code.", Toast.LENGTH_SHORT).show();
            }
            else{
                loadingBar.setTitle("Verification Code");
                loadingBar.setMessage("Please wait we are verifying your verification Code.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                signInWithPhoneAuthCredential(credential);
            }

        }
    }

    @Override
    public void onOtpCompleted(String otp) {
        loginRl.setVisibility(View.GONE);

        String verificationCode = otpView.getText().toString();
        if (TextUtils.isEmpty(verificationCode)){
            Toast.makeText(MobileLoginActivity.this, "Please enter Verification Code.", Toast.LENGTH_SHORT).show();
        }

        else{
            loadingBar.setTitle("Verification Code");
            loadingBar.setMessage("Please wait we are verifying your verification Code.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
            signInWithPhoneAuthCredential(credential);
        }
    }
}
