package com.kraigs.android.micamp.LostAndFoundActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kraigs.android.micamp.MainActivity;
import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class AddLostFoundActivity extends AppCompatActivity {

    ImageButton imageBt;
    TextInputEditText placeEt,nameEt;
    Button saveBt;
    FirebaseAuth mAuth;
    String currentUserID;
    String type;
    private static Uri uri = null;
    private ProgressDialog loadingBar;
    StorageReference mStorageRef;
    Bitmap bitmap;
    DocumentReference lostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lost_found);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveBt = findViewById(R.id.lost_item_bt);

        placeEt = findViewById(R.id.add_lost_location);
        nameEt = findViewById(R.id.add_lost_name);

        imageBt = findViewById(R.id.ad_lost_image);
        loadingBar = new ProgressDialog(this);

        imageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddLostFoundActivity.this);
            }
        });

        type = getIntent().getStringExtra("missing_type");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        lostRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("LostFound").collection(type).document();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String place = placeEt.getText().toString();
                String name = nameEt.getText().toString();

                if (!TextUtils.isEmpty(place) && !TextUtils.isEmpty(name)){

                    if (bitmap!=null){

                        loadingBar.setTitle("Uploading..");
                        loadingBar.setMessage("Please wait, we are uploading your item!");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        final StorageReference filePath = mStorageRef.child("Buy").child(uri.getLastPathSegment());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,40,baos);
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

                                    HashMap<String ,Object> missingMap = new HashMap<>();
                                    missingMap.put("place",place);
                                    missingMap.put("name",name);
                                    missingMap.put("uid",currentUserID);
                                    missingMap.put("image",uri.toString());
                                    missingMap.put("timestamp", FieldValue.serverTimestamp());

                                    lostRef.set(missingMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                loadingBar.dismiss();
                                                Toast.makeText(AddLostFoundActivity.this, "Your item is loaded successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(AddLostFoundActivity.this, MainActivity.class);
                                                intent.putExtra("type","explore");
                                                startActivity(intent);
                                                finish();

                                            } else{

                                                String message = task.getException().toString();
                                                Toast.makeText(AddLostFoundActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                                }else{
                                    String message = task.getException().toString();
                                    Toast.makeText(AddLostFoundActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }else{

                        Toast.makeText(AddLostFoundActivity.this, "Please upload image!", Toast.LENGTH_SHORT).show();

                    }
                } else{
                    Toast.makeText(AddLostFoundActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();

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
                File imageFile = new File(uri.getPath());
                bitmap = new Compressor(this).setMaxHeight(100).setMaxHeight(100).setQuality(60).compressToBitmap(imageFile);

                imageBt.setImageBitmap(bitmap);
            }}
    }
}
