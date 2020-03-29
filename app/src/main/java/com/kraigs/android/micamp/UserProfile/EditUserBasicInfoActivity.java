package com.kraigs.android.micamp.UserProfile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.Intro.IntroActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditUserBasicInfoActivity extends AppCompatActivity {

    private String currentUserID;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private StorageReference userProfileImageRef;

    private Spinner branchSpinner;
    private EditText summaryEt;
    private EditText qualityEt,collegeEt;
    private Spinner courseSpinner;
    private Spinner yearSpinner, qualitySpinner,collegeSpinner;

    private static String FLAG_YEAR = "";
    private static String FLAG_COURSE = "";
    private static String FLAG_BRANCH = "";
    private static String FLAG_QUALITY = "";
    private static String FLAG_COLLEGE = "";

    private String branch;
    private String summary;
    private String quality,college;
    private String retrieveQuality,retrieveCollege;
    private String retrieveSummary;

    private CircleImageView profilePic;
    private EditText userNameEt;

    private String userName;
    CollectionReference userCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_basic_info);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userCol = FirebaseFirestore.getInstance().collection("User");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        initializeFields();
        qualityEt.setVisibility(View.GONE);
        collegeEt.setVisibility(View.GONE);
        //mentor commit

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(EditUserBasicInfoActivity.this);
            }
        });

        getSupportActionBar().setTitle("Edit Basic Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpSpinnerCourse();
        setUpSpinnerYear();
        setUpSpinnerBranch();
        setUpQualitySpinner();
        setUpCollegeSpinner();

        RetrieveUserInfo();

        findViewById(R.id.saveBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void setUpQualitySpinner() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_quality, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(categorySpinnerAdapter);
        qualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FLAG_QUALITY = (String) parent.getItemAtPosition(position);
                Log.d("Contacts",position + " clicked in profile for quality");
                if (FLAG_QUALITY.equals("Other")) {
                    qualityEt.setVisibility(View.VISIBLE);
                } else{
                    qualityEt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpCollegeSpinner() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_college, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collegeSpinner.setAdapter(categorySpinnerAdapter);
        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FLAG_COLLEGE = (String) parent.getItemAtPosition(position);

                if (FLAG_COLLEGE.equals("Other")){
                    collegeEt.setVisibility(View.VISIBLE);
                } else {
                    collegeEt.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSpinnerCourse() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_course, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(categorySpinnerAdapter);
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FLAG_COURSE = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSpinnerYear() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_user_year, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        yearSpinner.setAdapter(categorySpinnerAdapter);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FLAG_YEAR = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSpinnerBranch() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_user_branch, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        branchSpinner.setAdapter(categorySpinnerAdapter);
        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    switch (selection) {
                        case "Select branch":
                            FLAG_BRANCH = "Select branch";
                            break;
                        case "Computer Science":
                            FLAG_BRANCH = "Computer Science Engineering";
                            break;
                        case "Instrumentation and Control":
                            FLAG_BRANCH = "Instrumentation and Control";
                            break;
                        case "Electronics And Comm.":
                            FLAG_BRANCH = "Electronics And Comm.";
                            break;
                        case "Electrical":
                            FLAG_BRANCH = "Electrical Engineering";
                            break;
                        case "Mechanical":
                            FLAG_BRANCH = "Mechanical Engineering";
                            break;
                        case "Information Technology":
                            FLAG_BRANCH = "Information Technology";
                            break;
                        case "Chemical":
                            FLAG_BRANCH = "Chemical Engineering";
                            break;
                        case "Industrial And Production":
                            FLAG_BRANCH = "Industrial And Production";
                            break;
                        case "Civil":
                            FLAG_BRANCH = "Civil Engineering";
                            break;
                        case "Biotechnology":
                            FLAG_BRANCH = "Biotechnology";
                            break;
                        case "Textile":
                            FLAG_BRANCH = "Textile";
                            break;
                        case "Humanities":
                            FLAG_BRANCH = "Humanities";
                            break;
                        case "Chemistry":
                            FLAG_BRANCH = "Chemistry";
                            break;
                        case "Physics ":
                            FLAG_BRANCH = "Physics";
                            break;
                        case "Mathematics":
                            FLAG_BRANCH = "Mathematics";
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeFields() {
        branchSpinner = (Spinner) findViewById(R.id.edit_user_branch);
        collegeSpinner = (Spinner) findViewById(R.id.edit_user_college);
        summaryEt = (EditText) findViewById(R.id.edit_user_profile_summary);
        collegeEt = (EditText) findViewById(R.id.collegeEt);
        qualityEt = (EditText) findViewById(R.id.edit_user_quality);
        courseSpinner = (Spinner) findViewById(R.id.edit_user_course);
        yearSpinner = (Spinner) findViewById(R.id.edit_user_year);
        qualitySpinner = findViewById(R.id.edit_quality_sp);
        profilePic = (CircleImageView) findViewById(R.id.edit_user_profile_image);
        userNameEt = (EditText) findViewById(R.id.edit_user_name);
        loadingBar = new ProgressDialog(this);

    }

    private void saveProfile() {
        summary = summaryEt.getText().toString().trim();
        quality = qualityEt.getText().toString();
        college = collegeEt.getText().toString().trim();
        userName = userNameEt.getText().toString().trim();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        if (!TextUtils.isEmpty(summary) && !TextUtils.isEmpty(userName) && !("Select your quality").equals(FLAG_QUALITY) && !("Select branch").equals(FLAG_BRANCH)&& !("Select course").equals(FLAG_COURSE)
                && !("Select year").equals(FLAG_YEAR) && !("Select college").equals(FLAG_COLLEGE) ) {
            HashMap<String, Object> profileMap = new HashMap<>();

            String collegeToSave = college;
            if (FLAG_QUALITY.equals("Other")) {

                if (!TextUtils.isEmpty(quality)){
                    profileMap.put("quality", quality);
                }
                else{
                    Toast.makeText(this, "Please fill quality column!", Toast.LENGTH_SHORT).show();
                    return;
                }

            } else {
                profileMap.put("quality", FLAG_QUALITY);
            }

            if (FLAG_COLLEGE.equals("Other")) {

                if (!TextUtils.isEmpty(college)){
                    collegeToSave = college;
                    editor.putString("college",college);
                    profileMap.put("college", college);
                }
                else{
                    Toast.makeText(this, "Please fill college column!", Toast.LENGTH_SHORT).show();
                    return;
                }

            } else {
                collegeToSave  = "Nit";
                editor.putString("college","Nit");
                profileMap.put("college", FLAG_COLLEGE);
            }

            editor.apply();
            profileMap.put("summary", summary);
            profileMap.put("name", userName);
            profileMap.put("branch", FLAG_BRANCH);
            profileMap.put("year", FLAG_YEAR);
            profileMap.put("course", FLAG_COURSE);

            String finalCollegeToSave = collegeToSave;
            userCol.document(currentUserID).update(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                String parentActivity = getIntent().getStringExtra("parentActivity");
                                Toast.makeText(EditUserBasicInfoActivity.this, "Basic Information Updated Successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent;
                                if (parentActivity.equals("main")) {
                                    intent = new Intent(EditUserBasicInfoActivity.this, IntroActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    new SharedPrefs(finalCollegeToSave);
                                    startActivity(intent);
                                    finish();
                                } else if (parentActivity.equals("editProfile")) {
                                    intent = new Intent(EditUserBasicInfoActivity.this, EditProfileActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("purpose", "edit");
                                    startActivity(intent);
                                }

                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(EditUserBasicInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(EditUserBasicInfoActivity.this, "Please fill all necessary details.", Toast.LENGTH_SHORT).show();
        }
    }

    private void RetrieveUserInfo() {
        userCol.document(currentUserID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(snapshot!=null && snapshot.exists()){

                                if (snapshot.contains("name")) {
                                    String retrieveUserName = snapshot.get("name").toString();
                                    userNameEt.setText(retrieveUserName);
                                }

                                if(snapshot.contains("quality")){

                                    retrieveQuality = snapshot.get("quality").toString();

                                    switch (retrieveQuality) {
                                        case "Select your quality":
                                            qualitySpinner.setSelection(0);
                                            break;
                                        case "Android Developer":
                                            qualitySpinner.setSelection(1);
                                            break;
                                        case "Website Developer":
                                            qualitySpinner.setSelection(2);
                                            break;
                                        case "Robotics":
                                            qualitySpinner.setSelection(3);
                                            break;
                                        case "Machine Learning":
                                            qualitySpinner.setSelection(4);
                                            break;
                                        case "Data Scientist":
                                            qualitySpinner.setSelection(5);
                                            break;
                                        default:
                                            qualitySpinner.setSelection(6);
                                            qualityEt.setVisibility(View.VISIBLE);
                                            qualityEt.setText(retrieveQuality);
                                            break;

                                    }
                                }

                            if(snapshot.contains("college")){

                                retrieveCollege = snapshot.get("college").toString();

                                switch (retrieveCollege) {
                                    case "Select your college":
                                        collegeSpinner.setSelection(0);
                                        collegeEt.setVisibility(View.GONE);
                                        break;
                                    case "NIT Jalandhar":
                                        collegeSpinner.setSelection(1);
                                        collegeEt.setVisibility(View.GONE);
                                        break;
                                    default:
                                        collegeSpinner.setSelection(2);
                                        collegeEt.setVisibility(View.VISIBLE);
                                        collegeEt.setText(retrieveCollege);
                                        break;
                                }
                            }

                                if (snapshot.contains("summary")) {
                                    retrieveSummary = snapshot.get("summary").toString();
                                    summaryEt.setText(retrieveSummary);
                                }

                                if (snapshot.contains("branch")) {
                                    FLAG_BRANCH = snapshot.get("branch").toString();
                                    switch (FLAG_BRANCH) {
                                        case "Select branch":
                                            branchSpinner.setSelection(0);
                                        case "Computer Science Engineering":
                                            branchSpinner.setSelection(1);
                                            break;
                                        case "Instrumentation and Control":
                                            branchSpinner.setSelection(3);
                                            break;
                                        case "Electronics And Comm.":
                                            branchSpinner.setSelection(2);
                                            break;
                                        case "Electrical Engineering":
                                            branchSpinner.setSelection(4);
                                            break;
                                        case "Mechanical Engineering":
                                            branchSpinner.setSelection(5);
                                            break;
                                        case "Information Technology":
                                            branchSpinner.setSelection(6);
                                            break;
                                        case "Chemical Engineering":
                                            branchSpinner.setSelection(7);
                                            break;
                                        case "Industrial And Production":
                                            branchSpinner.setSelection(8);
                                            break;
                                        case "Civil Engineering":
                                            branchSpinner.setSelection(9);
                                            break;
                                        case "Biotechnology":
                                            branchSpinner.setSelection(10);
                                            break;
                                        case "Textile":
                                            branchSpinner.setSelection(11);
                                            break;
                                        case "Humanities":
                                            branchSpinner.setSelection(12);
                                            break;
                                        case "Physics ":
                                            branchSpinner.setSelection(13);
                                            break;
                                        case "Chemistry":
                                            branchSpinner.setSelection(14);
                                            break;

                                        case "Mathematics":
                                            branchSpinner.setSelection(15);
                                            break;
                                        default:
                                            branchSpinner.setSelection(0);
                                    }
                                }

                                if (snapshot.contains("course")) {
                                    FLAG_COURSE = snapshot.get("course").toString();
                                    if (FLAG_COURSE.equals("Select course")) {
                                        courseSpinner.setSelection(0);
                                    }else if (FLAG_COURSE.equals("B.Tech")) {
                                        courseSpinner.setSelection(1);
                                    } else if (FLAG_COURSE.equals("M.Tech")) {
                                        courseSpinner.setSelection(2);
                                    } else if (FLAG_COURSE.equals("PhD")) {
                                        courseSpinner.setSelection(3);
                                    } else{
                                        courseSpinner.setSelection(0);
                                    }
                                }

                                if (snapshot.contains("year")) {
                                    FLAG_YEAR = snapshot.get("year").toString();
                                    if (FLAG_YEAR.equals("Select year")) {
                                        yearSpinner.setSelection(0);
                                    } else if (FLAG_YEAR.equals("1st Year")) {
                                        yearSpinner.setSelection(1);
                                    } else if (FLAG_YEAR.equals("2nd Year")) {
                                        yearSpinner.setSelection(2);
                                    } else if (FLAG_YEAR.equals("3rd Year")) {
                                        yearSpinner.setSelection(3);
                                    } else if (FLAG_YEAR.equals("4th Year")) {
                                        yearSpinner.setSelection(4);
                                    } else{
                                        yearSpinner.setSelection(0);
                                    }
                                }
                        }
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                                                    Toast.makeText(EditUserBasicInfoActivity.this, "Image saved Successfully to database", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(EditUserBasicInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(EditUserBasicInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
        }
    }


}
