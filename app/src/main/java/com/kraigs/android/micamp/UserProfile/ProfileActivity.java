package com.kraigs.android.micamp.UserProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kraigs.android.micamp.Chat.ChatRequestActivity;
import com.kraigs.android.micamp.Mentor.ConnectedMentorsActivity;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.WebViewClass;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RelativeLayout connectionsRl,addEventRl,addPhotoRl,addBookRl,joinRl,reviewRl,shareRl,requestRl;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRef;
    ImageView editProfile;
    TextView countTv;
    private TextView userName, userBranch;
    private CircleImageView userProfilePic;
    private ProgressDialog loadingBar;

    CollectionReference userCol;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userCol = db.collection("User");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        initializeFields();

        RetrieveUserInfo();

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("purpose", "edit");
                startActivity(intent);
            }
        });




        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ProfileActivity.this);
            }
        });

        connectionsRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ConnectedMentorsActivity.class);
                startActivity(intent);
            }
        });

        requestRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ChatRequestActivity.class);
                startActivity(intent);
            }
        });

        addEventRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intentWebView("https://docs.google.com/forms/d/e/1FAIpQLSfrNQyXYbhkQQwsx0ulYaTTMs0O4XiNy7KmVelM6o8MQWWlcA/viewform?usp=sf_link");
            }
        });

        addBookRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intentWebView("https://docs.google.com/forms/d/e/1FAIpQLSfFQ-G_96xpqgpiHgngEW70b0CIfHLkAZ2luoTYDddSMqCccw/viewform?usp=sf_link");
            }
        });

        addPhotoRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intentWebView("https://docs.google.com/forms/d/e/1FAIpQLSfHFEnFiz6YCn1ItOsmFfyWVofnZZZphQZVFiiY68yUQ5zltQ/viewform?usp=sf_link");
            }
        });

        shareRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/*");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Check out MiCamp now to be always updated inside your campus  https://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName());
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "MiCamp");
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

            }
        });


        reviewRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uri = Uri.parse("market://details?id=" + getBaseContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName())));
                }

            }
        });


        joinRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intentWebView("https://docs.google.com/forms/d/e/1FAIpQLSfpDzU56JsUD4wtha8Va0SwLcbs1-Psi242krFwVFgVjIDzUw/viewform?usp=sf_link");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait your profile image is uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                File imageFile = new File(resultUri.getPath());
                bitmap = new Compressor(this).setMaxHeight(100).setMaxHeight(100).setQuality(60).compressToBitmap(imageFile);

                if (bitmap!=null){
                    final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
                    byte[] bytes = baos.toByteArray();

                    UploadTask uploadTask = filePath.putBytes(bytes);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()){
                                throw  task.getException();
                            }

                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){

                                HashMap<String,Object> map = new HashMap<>();
                                map.put("image",task.getResult().toString());

                                userCol.document(currentUserID).update(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(ProfileActivity.this, "Image saved Successfully to database", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
        }
    }

    private void RetrieveUserInfo() {
        userCol.document(currentUserID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(dataSnapshot.exists()){

                            if (dataSnapshot.contains("image")) {
                                String retrieveProfleImage = dataSnapshot.get("image").toString();
                                Picasso.get().load(retrieveProfleImage)
                                        .into(userProfilePic);
                            }

                            if (dataSnapshot.contains("name")) {
                                String retrieveName = dataSnapshot.get("name").toString();
                                userName.setText(retrieveName);
                            }

                            if (dataSnapshot.contains("branch")) {
                                String retrieveBranch = dataSnapshot.get("branch").toString();
                                String year = dataSnapshot.get("year").toString();
                                userBranch.setText(year + "  "  + retrieveBranch);
                            }

                        }
                    }
                });

        userCol.document(currentUserID).collection("Friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    int count = task.getResult().size();
                    countTv.setText(count + "");
                }
            }
        });

    }

    public void intentWebView(String webUrl) {
        Intent intent = new Intent(ProfileActivity.this, WebViewClass.class);
        intent.putExtra("webUrl", webUrl);
        startActivity(intent);
    }

    private void initializeFields() {

        connectionsRl = (RelativeLayout) findViewById(R.id.connectionRl);
        userProfilePic = (CircleImageView) findViewById(R.id.user_profile_pic);
        userName = (TextView) findViewById(R.id.userName);
        userBranch = (TextView) findViewById(R.id.user_branch_name);
        loadingBar = new ProgressDialog(this);

        requestRl = findViewById(R.id.requestRl);
        addBookRl = findViewById(R.id.addBookRl);
        addEventRl = findViewById(R.id.addEventRl);
        addPhotoRl = findViewById(R.id.addPhotosRl);
        shareRl = findViewById(R.id.shareRl);
        reviewRl = findViewById(R.id.reviewRL);
        joinRl = findViewById(R.id.joinRl);
        editProfile = findViewById(R.id.editProfile);
        countTv = findViewById(R.id.countTv);


    }
}
