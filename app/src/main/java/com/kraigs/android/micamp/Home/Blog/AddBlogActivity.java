package com.kraigs.android.micamp.Home.Blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.MainActivity;
import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class AddBlogActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 4;
    private static Uri uri = null;
    EditText contentEt;
    TextInputEditText titleEt;
    Button addImageBt, postBt;
    StorageReference mStorageRef;
    FirebaseAuth mAuth;
    String currentUserId;
    ProgressDialog loadingBar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference docRef = db.collection("Public").document("Collections").collection("Blog");
    DocumentReference myRef;
    String type, key;
    private String TAG = "AddBlogActivity";
    String blog_imageUrl;
    CollectionReference userCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blog);

        getSupportActionBar().setTitle("Add Blog");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        type = getIntent().getStringExtra("type");
        key = getIntent().getStringExtra("key");

        userCol = FirebaseFirestore.getInstance().collection("User");

        contentEt = findViewById(R.id.addBlogContent);
        titleEt = findViewById(R.id.addBlogTitle);
        addImageBt = findViewById(R.id.addImageBt);
        postBt = findViewById(R.id.addBlogBt);
        loadingBar = new ProgressDialog(this);

        if (type.equals("edit")) {

            myRef = docRef.document(key);

            myRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    String blog_title = (String) snapshot.get("mTitle").toString();
                    String blog_content = (String) snapshot.get("mInfo").toString();
                    if(snapshot.contains("mPhotoUrl")){
                        blog_imageUrl = (String) snapshot.get("mPhotoUrl").toString();
                    }

                    contentEt.setText(blog_content);
                    titleEt.setText(blog_title);
                    addImageBt.setText("Change Image");

                } else {
                    Log.d(TAG, "Current data: null");
                }
            });
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        addImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddBlogActivity.this);
            }
        });

        postBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEt.getText().toString().trim();
                String title = titleEt.getText().toString().trim();

                if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(title)) {

                    loadingBar.setTitle("Uploading..");
                    loadingBar.setMessage("Please wait, we are posting your ad shortly!");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    if (uri != null) {

                        final StorageReference filePath = mStorageRef.child("Blog").child(uri.getLastPathSegment());

                        filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            userCol.document(currentUserId)
                                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot dataSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                            if(dataSnapshot.exists()){
                                                                String name = dataSnapshot.get("name").toString();

                                                                HashMap<String, Object> blogMap = new HashMap<>();
                                                                blogMap.put("mTitle", title);
                                                                blogMap.put("mAuthor", name);
                                                                blogMap.put("mInfo", content);
                                                                blogMap.put("mPhotoUrl", uri.toString());
                                                                blogMap.put("uid", currentUserId);
                                                                blogMap.put("timestamp", FieldValue.serverTimestamp());

                                                                if (type.equals("edit")) {
                                                                    docRef.document(key).update(blogMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            loadingBar.dismiss();
                                                                            Toast.makeText(AddBlogActivity.this, "Your blog added successfully!", Toast.LENGTH_SHORT).show();
                                                                            Intent intent = new Intent(AddBlogActivity.this, BloghistoryActivity.class);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    });
                                                                } else if (type.equals("add")) {
                                                                    docRef.document().set(blogMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            loadingBar.dismiss();
                                                                            Toast.makeText(AddBlogActivity.this, "Your blog added successfully!", Toast.LENGTH_SHORT).show();
                                                                            Intent intent = new Intent(AddBlogActivity.this, MainActivity.class);
                                                                            intent.putExtra("type","home");
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }
                                                    });

                                        }
                                    });
                                } else {
                                    String message = task.getException().toString();
                                    Toast.makeText(AddBlogActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

                    } else {
                        if (type.equals("edit")) {

                            userCol.document(currentUserId)
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                            if(snapshot.exists()){
                                                String name = snapshot.get("name").toString();

                                                HashMap<String, Object> blogMap = new HashMap<>();
                                                blogMap.put("mTitle", title);
                                                blogMap.put("mAuthor", name);
                                                blogMap.put("mInfo", content);
                                                blogMap.put("mPhotoUrl", blog_imageUrl);
                                                blogMap.put("uid", currentUserId);
                                                blogMap.put("timestamp", FieldValue.serverTimestamp());

                                                docRef.document(key).update(blogMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        loadingBar.dismiss();
                                                        Toast.makeText(AddBlogActivity.this, "Your blog added successfully!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(AddBlogActivity.this, BloghistoryActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                            }
                                        }
                                    });
                        } else if (type.equals("add")){
                            userCol.document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                    if(snapshot.exists()){
                                        String name = snapshot.get("name").toString();

                                        HashMap<String, Object> blogMap = new HashMap<>();
                                        blogMap.put("mTitle", title);
                                        blogMap.put("mAuthor", name);
                                        blogMap.put("mInfo", content);
                                        blogMap.put("uid", currentUserId);
                                        blogMap.put("timestamp", FieldValue.serverTimestamp());

                                        docRef.document().set(blogMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                loadingBar.dismiss();
                                                Toast.makeText(AddBlogActivity.this, "Your blog added successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(AddBlogActivity.this, MainActivity.class);
                                                intent.putExtra("type","home");
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                                    }
                                }
                            });
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
                uri = result.getUri();
                addImageBt.setText("Image Selected");
            }
        }
    }
}
