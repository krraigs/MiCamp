package com.kraigs.android.micamp.UserProfile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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

import id.zelory.compressor.Compressor;

public class ComplaintActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private static Uri uri = null;
    private ImageButton feedbackImageBt;
    private TextInputEditText subjectEt;
    private String mCurrentUserId;
    private EditText contentEt;
    private Button sendFeedback;
    private ProgressDialog loadingBar;
    DocumentReference docRef;
    Bitmap bitmap;
    DocumentReference userRef;
    DocumentReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        docRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Hostel");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseFirestore.getInstance().collection("User").document(mCurrentUserId);

        initializeFields();

        feedbackImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ComplaintActivity.this);
            }
        });

        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String feedbackSubject = subjectEt.getText().toString();
                final String feedbackContent = contentEt.getText().toString();

                if (!TextUtils.isEmpty(feedbackSubject) && !TextUtils.isEmpty(feedbackContent)) {

                    loadingBar.setTitle("Uploading..");
                    loadingBar.setMessage("Please wait");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if(snapshot.exists()){
                                if(snapshot.contains("hostelVerified")){

                                    String verified = snapshot.get("hostelVerified").toString();
                                    if(verified.equals("true")){

                                        String roomNo = snapshot.get("roomNo").toString();
                                        String rollNo = snapshot.get("rollNo").toString();
                                        String name = snapshot.get("name").toString();
                                        String hostel = snapshot.get("hostel").toString();

                                        myRef = docRef.collection(hostel).document("Complaint").collection("Latest").document();

                                        if (bitmap != null) {
                                            final StorageReference filePath = mStorageRef.child("Hostels").child(hostel).child(feedbackSubject + ".jpg");

                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG,60,baos);
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
                                                        String downloadUri = task.getResult().toString();

                                                        HashMap<String, Object> feedbackMap = new HashMap<>();
                                                        feedbackMap.put("user", mCurrentUserId);
                                                        feedbackMap.put("subject", feedbackSubject);
                                                        feedbackMap.put("content", feedbackContent);
                                                        feedbackMap.put("imageUrl",downloadUri);
                                                        feedbackMap.put("roomNo",roomNo);
                                                        feedbackMap.put("rollNo",rollNo);
                                                        feedbackMap.put("name",name);
                                                        feedbackMap.put("timestamp", FieldValue.serverTimestamp());

                                                        myRef.set(feedbackMap)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            loadingBar.dismiss();
                                                                            Toast.makeText(ComplaintActivity.this, "Feedback sent Successfully...", Toast.LENGTH_SHORT).show();

                                                                        }
                                                                        else{
                                                                            String message = task.getException().toString();
                                                                            Toast.makeText(ComplaintActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                                            loadingBar.dismiss();
                                                                        }
                                                                    }
                                                                });

                                                    }
                                                }
                                            });
                                        }

                                        else{

                                            HashMap<String, Object> feedbackMap = new HashMap<>();
                                            feedbackMap.put("user", mCurrentUserId);
                                            feedbackMap.put("subject", feedbackSubject);
                                            feedbackMap.put("content", feedbackContent);
                                            feedbackMap.put("roomNo",roomNo);
                                            feedbackMap.put("rollNo",rollNo);
                                            feedbackMap.put("name",name);
                                            feedbackMap.put("timestamp", FieldValue.serverTimestamp());

                                            myRef.set(feedbackMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){

                                                                loadingBar.dismiss();
                                                                Toast.makeText(ComplaintActivity.this, "Submitted successfully...", Toast.LENGTH_SHORT).show();

                                                            }
                                                            else{
                                                                String message = task.getException().toString();
                                                                Toast.makeText(ComplaintActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                                loadingBar.dismiss();
                                                            }
                                                        }
                                                    });
                                        }

                                    } else{
                                        loadingBar.dismiss();
                                        Toast.makeText(ComplaintActivity.this, "Please match your account details as entered in hostel office!", Toast.LENGTH_SHORT).show();
                                    }

                                } else{
                                    loadingBar.dismiss();
                                    Toast.makeText(ComplaintActivity.this, "Please verify your account first!", Toast.LENGTH_SHORT).show();
                                }

                            } else{
                                loadingBar.dismiss();
                                Toast.makeText(ComplaintActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(ComplaintActivity.this, "Please fill all details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initializeFields() {

        feedbackImageBt = (ImageButton) findViewById(R.id.feedbackImageButton);
        subjectEt =  findViewById(R.id.feedback_subject_et);
        contentEt = (EditText) findViewById(R.id.feedback_content);
        sendFeedback = (Button) findViewById(R.id.send_feedback);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadingBar = new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();

                File imageFile = new File(uri.getPath());
                bitmap = new Compressor(this).setMaxHeight(200).setMaxHeight(200).setQuality(60).compressToBitmap(imageFile);
                feedbackImageBt.setImageBitmap(bitmap);
            }}
    }
}
