package com.kraigs.android.micamp.extras;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kraigs.android.micamp.Login.LoginActivity;
import com.kraigs.android.micamp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.registerGym)
    TextView registerGym;
    @BindView(R.id.logOutTv)
    TextView logOutTv;
    @BindView(R.id.settingLl)
    LinearLayout settingLl;
    @BindView(R.id.fbTv)
    TextView fbTv;
    @BindView(R.id.instaTv)
    TextView instaTv;
    @BindView(R.id.linkedInTv)
    TextView linkedInTv;
    @BindView(R.id.shareLl)
    LinearLayout shareLl;
    @BindView(R.id.tncTv)
    TextView tncTv;
    @BindView(R.id.pvTv)
    TextView pvTv;
    @BindView(R.id.contactTv)
    TextView contactTv;
    @BindView(R.id.supportLl)
    LinearLayout supportLl;
    @BindView(R.id.asFit)
    ImageView asFit;
    @BindView(R.id.versionTv)
    TextView versionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);




        logOutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
    }


    private void logOut() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
        alertDialog.setTitle("Logout").setMessage("Do you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient;
                mGoogleSignInClient = GoogleSignIn.getClient(SettingsActivity.this, gso);

                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                        Intent i2 = new Intent(SettingsActivity.this, LoginActivity.class);
                        startActivity(i2);
                        finish();
                    }
                });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();

    }


}
