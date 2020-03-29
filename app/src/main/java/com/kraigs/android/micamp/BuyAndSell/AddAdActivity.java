package com.kraigs.android.micamp.BuyAndSell;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class AddAdActivity extends AppCompatActivity {

    ImageButton adImageBt;
    TextInputEditText priceEt, itemNameEt, locationEt, detailEt,contactEt;
    Button nextBt;
    FirebaseAuth mAuth;
    String currentUserID, price, itemName, saveCurrentDate;
    private static Uri uri = null;
    DocumentReference productRef,userAds;
    private ProgressDialog loadingBar;
    private StorageReference mStorageRef;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ad);

        getSupportActionBar().setTitle("Add Product");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeFields();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        productRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("BuynSell").collection("Buy").document();
        userAds = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("BuynSell").collection("UserAds").document("Ads").collection(currentUserID).document();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM , yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        adImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AddAdActivity.this);
            }
        });

        nextBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                price = priceEt.getText().toString();
                itemName = itemNameEt.getText().toString();
                String detail = detailEt.getText().toString().trim();
                String location = locationEt.getText().toString().trim();
                String contact = contactEt.getText().toString().trim();
                if (!TextUtils.isEmpty(price) && !TextUtils.isEmpty(itemName) && !TextUtils.isEmpty(detail) && !TextUtils.isEmpty(location) && !TextUtils.isEmpty(contact) ){

                    if (bitmap != null){

                        loadingBar.setTitle("Uploading..");
                        loadingBar.setMessage("Please wait, we are posting your ad shortly!");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        final StorageReference filePath = mStorageRef.child("Buy").child(SharedPrefs.getCollege()).child(currentUserID + uri.getLastPathSegment() + ".jpg");
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

                                    HashMap<String, Object> itemMap = new HashMap<>();
                                    itemMap.put("detail", detail);
                                    itemMap.put("location", location);
                                    itemMap.put("itemName", itemName);
                                    itemMap.put("price", price);
                                    itemMap.put("sellerUid", currentUserID);
                                    itemMap.put("date", saveCurrentDate);
                                    itemMap.put("image", task.getResult().toString());
                                    itemMap.put("contact",contact);
                                    itemMap.put("key",productRef.getId());
                                    itemMap.put("timestamp", FieldValue.serverTimestamp());

                                    productRef.set(itemMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                userAds.set(itemMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            loadingBar.dismiss();
                                                            Toast.makeText(AddAdActivity.this, "Your product is added successfully!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(AddAdActivity.this, MainActivity.class);
                                                            intent.putExtra("type","explore");
                                                            startActivity(intent);
                                                            finish();
                                                        }else{
                                                            String message = task.getException().toString();
                                                            Toast.makeText(AddAdActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });

                                            } else{
                                                String message = task.getException().toString();
                                                Toast.makeText(AddAdActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                                }else{
                                    String message = task.getException().toString();
                                    Toast.makeText(AddAdActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

                    } else{
                        Toast.makeText(AddAdActivity.this, "Please upload image of your product!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AddAdActivity.this, "Please fill all details to continue!", Toast.LENGTH_SHORT).show();
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
                bitmap = new Compressor(this).setMaxHeight(100).setMaxWidth(100).setQuality(60).compressToBitmap(imageFile);

                adImageBt.setImageBitmap(bitmap);
            }}
    }

    private void initializeFields() {
        priceEt = findViewById(R.id.ad_price);
        itemNameEt = findViewById(R.id.ad_product);
        locationEt = findViewById(R.id.ad_location);
        detailEt = findViewById(R.id.ad_detail);
        loadingBar = new ProgressDialog(this);
        contactEt = findViewById(R.id.ad_contact);

        nextBt = findViewById(R.id.ad_next);

        adImageBt = findViewById(R.id.adImageBt);
    }
}
