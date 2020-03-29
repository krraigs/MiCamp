package com.kraigs.android.micamp.UserProfile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.MainActivity;
import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity {

    private String currentUserID;
    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRef;

    private ProgressDialog loadingBar;

    private TextView nameTv;
    private TextView branchTv;
    private TextView yearTv;
    private TextView courseTv;
    private TextView qualityTv;
    private TextView summaryTv;

    private CircleImageView profilePic;
    private Button connectBt,cancelBt;

    private EditText specialityEt;
    private EditText skillsEt;
    private EditText achievementsEt;
    private EditText experienceEt;
    private EditText contactEt;
    private EditText personalDetailsEt;

    private ImageView editBasicInfo;
    String purpose = "";
    CollectionReference userCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userCol = FirebaseFirestore.getInstance().collection("User");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        initializeFields();
        retrieveUserInfo();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(EditProfileActivity.this);
            }
        });

        purpose = getIntent().getStringExtra("purpose");
        if (("edit").equals(purpose)){
            connectBt.setVisibility(View.GONE);
        }

        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editBasicInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, EditUserBasicInfoActivity.class);
                intent.putExtra("parentActivity","editProfile");
                startActivity(intent);
            }
        });
    }

    private void initializeFields() {
        editBasicInfo = (ImageView) findViewById(R.id.userEditBasicInfo);
        specialityEt = (EditText) findViewById(R.id.userSpecialityEt);
        skillsEt = (EditText) findViewById(R.id.userSkillsEt);
        achievementsEt = (EditText) findViewById(R.id.userAchievementsEt);
        experienceEt = (EditText) findViewById(R.id.userExperienceEt);
        personalDetailsEt = (EditText) findViewById(R.id.userPersonalDetailsEt);
        contactEt = (EditText) findViewById(R.id.userContactsEt);
        loadingBar = new ProgressDialog(this);

        nameTv = (TextView) findViewById(R.id.userNameTv);
        branchTv = (TextView) findViewById(R.id.userBranchTv);
        yearTv = (TextView) findViewById(R.id.userYearTv);
        courseTv = (TextView) findViewById(R.id.userCourseTv);
        qualityTv = (TextView) findViewById(R.id.userQualityTv);
        summaryTv = (TextView) findViewById(R.id.userSummaryTv);

        connectBt = (Button) findViewById(R.id.messageMentorBt);
        cancelBt = findViewById(R.id.mentorConnectBt);
        cancelBt.setVisibility(View.GONE);

        profilePic = (CircleImageView) findViewById(R.id.userProfilePic);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);

        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.edit_profile_save) {
            saveProfile();
        }
        return true;
    }

    private void saveProfile() {

        loadingBar.setTitle("Updating Info");
        loadingBar.setMessage("Just wait for your information to be updated.");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        String speciality = specialityEt.getText().toString();
        String skills = skillsEt.getText().toString();
        String achievements = achievementsEt.getText().toString();
        String experience = experienceEt.getText().toString();
        String contact = contactEt.getText().toString();
        String personal = personalDetailsEt.getText().toString();

        HashMap<String, Object> profileMap = new HashMap<>();

        profileMap.put("speciality", speciality);
        profileMap.put("skills", skills);
        profileMap.put("achievements", achievements);
        profileMap.put("experience", experience);
        profileMap.put("contact", contact);
        profileMap.put("personal", personal);

        userCol.document(currentUserID).update(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(EditProfileActivity.this, "Basic Information Updated Successfully.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(EditProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void retrieveUserInfo() {
        userCol.document(currentUserID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot dataSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(dataSnapshot!=null && dataSnapshot.exists()){

                            String retrieveName = dataSnapshot.get("name").toString();
                            String retrieveBranch = dataSnapshot.get("branch").toString();
                            String retrieveYear = dataSnapshot.get("year").toString();
                            String retrieveCourse = dataSnapshot.get("course").toString();
                            String retrieveQuality = dataSnapshot.get("quality").toString();
                            String retrieveSummary = dataSnapshot.get("summary").toString();

                            nameTv.setText(retrieveName);
                            branchTv.setText(retrieveBranch);
                            yearTv.setText(retrieveYear);
                            courseTv.setText(retrieveCourse);
                            qualityTv.setText(retrieveQuality);
                            summaryTv.setText(retrieveSummary);

                            if(dataSnapshot.contains("speciality")){
                                String retrieveSpeciality = dataSnapshot.get("speciality").toString();
                                specialityEt.setText(retrieveSpeciality);
                            }
                            if(dataSnapshot.contains("skills")){
                                String retrieveSkills = dataSnapshot.get("skills").toString();
                                skillsEt.setText(retrieveSkills);
                            }
                            if(dataSnapshot.contains("achievements")){
                                String retrieveAchievements = dataSnapshot.get("achievements").toString();
                                achievementsEt.setText(retrieveAchievements);
                            }
                            if(dataSnapshot.contains("experience")){
                                String retrieveExperience = dataSnapshot.get("experience").toString();
                                experienceEt.setText(retrieveExperience);
                            }
                            if(dataSnapshot.contains("contact")){
                                String retrieveContact = dataSnapshot.get("contact").toString();
                                contactEt.setText(retrieveContact);
                            }
                            if(dataSnapshot.contains("personal")){
                                String retrievePersonal = dataSnapshot.get("personal").toString();
                                personalDetailsEt.setText(retrievePersonal);
                            }
                            if (dataSnapshot.contains("image")){
                                String retrieveProfleImage = dataSnapshot.get("image").toString();
                                Picasso.get().load(retrieveProfleImage)
                                        .into(profilePic);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait your profile image is uploading");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                File imageFile = new File(resultUri.getPath());
                Bitmap bitmap = new Compressor(this).setMaxHeight(100).setMaxHeight(100).setQuality(60).compressToBitmap(imageFile);

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
                                                    Toast.makeText(EditProfileActivity.this, "Image saved Successfully to database", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(EditProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(EditProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
        }
    }
}
