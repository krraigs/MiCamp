package com.kraigs.android.micamp.Hostel;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kraigs.android.micamp.MainActivity;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.NotificationActivity;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class HostelActivity extends AppCompatActivity {

    TextView raw,fromTv,toTv;
    Button submitBt;
    String from,to,location;
    String currentUserId;
    DocumentReference hostelCol;
    TextInputEditText inputEt;
    CollectionReference userCol;
    TextView rawTv;
    ProgressDialog loadingBar;
    DocumentReference userVerifyRef;
    String FLAG_HOSTEL = "Select hostel",FLAG_FROM = "Select time",FLAG_TO = "Select time";
    StorageReference userProfileImageRef;
    LinearLayout timeLl;
    TextInputLayout locationL;
    Spinner fromSp,toSp;
    String type;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userCol = FirebaseFirestore.getInstance().collection("User");
        userVerifyRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Hostel");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        loadingBar = new ProgressDialog(this);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("5C4A2BAD3C2821B48CF46B229445B277")
                .build();
        mAdView.loadAd(adRequest);

        fromTv = findViewById(R.id.fromTv);
        toTv = findViewById(R.id.toTv);
        inputEt = findViewById(R.id.locaionEt);
        rawTv = findViewById(R.id.raw);
        submitBt = findViewById(R.id.submitBt);
        fromSp = findViewById(R.id.fromSp);
        toSp = findViewById(R.id.toSp);

        setUpSpinnerFrom();
        setUpSpinnerTo();

        timeLl = findViewById(R.id.timeLl);
        locationL = findViewById(R.id.locationL);

        hostelCol = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Hostel");

         type = getIntent().getStringExtra("type");
        if(type.equals("mess")){
            rawTv.setText("Mess Off");
            locationL.setVisibility(View.GONE);
            timeLl.setVisibility(View.VISIBLE);

        } else if (type.equals("leave")){
            rawTv.setText("Hostel Leave");
            locationL.setVisibility(View.VISIBLE);
            timeLl.setVisibility(View.GONE);
        }

        fromTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate();
            }
        });

        toTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDateto();
            }
        });

        submitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                location = inputEt.getText().toString().trim();
                from = fromTv.getText().toString();
                to = toTv.getText().toString();

                if(!from.equals("Departure Date") && !to.equals("Return Date") && (!TextUtils.isEmpty(location) || (!FLAG_FROM.equals("Select time") && ! FLAG_TO.equals("Select time"))) ){

                    to = to.substring(5);
                    from = from.substring(7);

                    loadingBar.setTitle("Submit");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.setMessage("Please wait!");
                    loadingBar.show();
                    Log.d("Hostel","loading show");

                    userCol.document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                            if(snapshot.exists()){
                                if(snapshot.contains("hostelVerified")){

                                    String verified = snapshot.get("hostelVerified").toString();
                                    if(verified.equals("true")){

                                        String roomNo = snapshot.get("roomNo").toString();
                                        String rollNo = snapshot.get("rollNo").toString();
                                        String name = snapshot.get("name").toString();
                                        String hostel = snapshot.get("hostel").toString();

                                        HashMap<String,Object> map = new HashMap<>();
                                        map.put("from",from);
                                        map.put("to",to);
                                        map.put("timestamp", FieldValue.serverTimestamp());
                                        map.put("user",currentUserId);
                                        map.put("roomNo",roomNo);
                                        map.put("rollNo",rollNo);
                                        map.put("name",name);

                                        if(type.equals("leave")){
                                            map.put("location",location);
                                            hostelCol = hostelCol.collection(hostel).document("Leave").collection("Latest").document();
                                        }else if (type.equals("mess")){
                                            map.put("fromTime",FLAG_FROM);
                                            map.put("toTime",FLAG_TO);
                                            hostelCol = hostelCol.collection(hostel).document("MessOff").collection("Latest").document();
                                        }

                                        hostelCol.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    loadingBar.dismiss();
                                                    Toast.makeText(HostelActivity.this, "Details saved successfully!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(HostelActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                }
                                            }
                                        });

                                    } else{
                                        loadingBar.dismiss();
                                        detailsDialog();
                                        Toast.makeText(HostelActivity.this, "Please match your account details as entered in hostel office!", Toast.LENGTH_SHORT).show();
                                    }

                                } else{
                                    loadingBar.dismiss();
                                    detailsDialog();
                                    Toast.makeText(HostelActivity.this, "Please verify your account first!", Toast.LENGTH_SHORT).show();
                                }

                            } else{
                                loadingBar.dismiss();
                                Toast.makeText(HostelActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else{
                    Toast.makeText(HostelActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void detailsDialog(){
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.zlayout_user_hostel);

        TextInputEditText nameEt = dialog.findViewById(R.id.nameEt);
        Spinner hostelSp = dialog.findViewById(R.id.hostelSp);
        TextInputEditText rollEt = dialog.findViewById(R.id.rollNoEt);
        TextInputEditText roomEt = dialog.findViewById(R.id.roomEt);
        CircleImageView userPic = dialog.findViewById(R.id.user_profile_pic);
        Button saveBt = dialog.findViewById(R.id.saveBt);

        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(HostelActivity.this);
            }
        });

        setUpSpinnerHostel(hostelSp);

        userCol.document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()){
                    if (snapshot.contains("name")){
                        String name = snapshot.get("name").toString();
                        nameEt.setText(name);
                    }

                    String image = null;

                    if (snapshot.contains("image")){
                        image = snapshot.get("image").toString();
                        Picasso.get().load(image).into(userPic);
                    }

                    if (snapshot.contains("hostel")){
                        String selection = snapshot.get("hostel").toString();
                        if (selection.equals("Select hostel")) {
                            hostelSp.setSelection(0);
                        } else if (selection.equals("MBHF")) {
                            hostelSp.setSelection(1);
                        } else if (selection.equals("MBHB")) {
                            hostelSp.setSelection(2);
                        } else if (selection.equals("MBHA")) {
                            hostelSp.setSelection(3);
                        }
                    }

                    if (snapshot.contains("roomNo")){
                        String name = snapshot.get("roomNo").toString();
                        roomEt.setText(name);
                    }

                    if (snapshot.contains("rollNo")){
                        String name = snapshot.get("rollNo").toString();
                        rollEt.setText(name);
                    }

                    saveBt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String name = nameEt.getText().toString();
                            String rollNo = rollEt.getText().toString();
                            String roomNo = roomEt.getText().toString();

                            if (snapshot.contains("image")){
                                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(rollNo) && !TextUtils.isEmpty(roomNo) && !FLAG_HOSTEL.equals("Select hostel")){
                                    String imageF = snapshot.get("image").toString();
                                    loadingBar.setTitle("Submit");
                                    loadingBar.setCanceledOnTouchOutside(false);
                                    loadingBar.setMessage("Please wait!");
                                    loadingBar.show();

                                    HashMap<String,Object> map = new HashMap<>();
                                    map.put("name",name);
                                    map.put("rollNo",rollNo);
                                    map.put("roomNo",roomNo);
                                    map.put("hostel",FLAG_HOSTEL);
                                    map.put("hostelVerified","false");
                                    map.put("key",currentUserId);
                                    map.put("image",imageF);

                                    userVerifyRef.collection(FLAG_HOSTEL).document("HostelVerify").collection("Users").document(currentUserId).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                loadingBar.dismiss();
                                                Toast.makeText(HostelActivity.this, "Details updated successfully!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            } else{
                                                loadingBar.dismiss();
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                } else{
                                    Toast.makeText(HostelActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                                }
                            } else{
                                Toast.makeText(HostelActivity.this, "Please upload your profile pic.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });


        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_noti, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.ic_noti) {
            Intent intent = new Intent(HostelActivity.this, NotificationActivity.class);
            intent.putExtra("type","hostel");
            startActivity(intent);
        }

        return true;
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
                Bitmap bitmap = new Compressor(this).setMaxHeight(100).setMaxHeight(100).setQuality(60).compressToBitmap(imageFile);

                if (bitmap!=null){
                    final StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");

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

                                userCol.document(currentUserId).update(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(HostelActivity.this, "Image saved Successfully to database", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(HostelActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(HostelActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
        }
    }

    private void setUpSpinnerHostel(Spinner spinner) {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_hostel, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(categorySpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("Select hostel")) {
                        FLAG_HOSTEL = "Select hostel";
                    } else if (selection.equals("MBH-F")) {
                        FLAG_HOSTEL = "MBHF";
                    } else if (selection.equals("MBH-B")) {
                        FLAG_HOSTEL = "MBHB";
                    }
                    else if (selection.equals("MBH-A")) {
                        FLAG_HOSTEL = "MBHA";
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSpinnerFrom() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.mess, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        fromSp.setAdapter(categorySpinnerAdapter);
        fromSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("Select time")) {
                        FLAG_FROM = "Select time";
                    } else if (selection.equals("Breakfast")) {
                        FLAG_FROM = "Breakfast";
                    } else if (selection.equals("Lunch")) {
                        FLAG_FROM = "Lunch";
                    }
                    else if (selection.equals("Dinner")) {
                        FLAG_FROM = "Dinner";
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void setUpSpinnerTo() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.mess, android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        toSp.setAdapter(categorySpinnerAdapter);
        toSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("Select time")) {
                        FLAG_TO = "Select time";
                    } else if (selection.equals("Breakfast")) {
                        FLAG_TO = "Breakfast";
                    } else if (selection.equals("Lunch")) {
                        FLAG_TO = "Lunch";
                    }
                    else if (selection.equals("Dinner")) {
                        FLAG_TO = "Dinner";
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void pickDate(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.date_picker, null, false);

        // the time picker on the alert dialog, getActivity() is how to get the value
        final DatePicker myDatePicker = (DatePicker) view.findViewById(R.id.myDatePicker);

        // the alert dialog
        new AlertDialog.Builder(HostelActivity.this).setView(view)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(DialogInterface dialog, int id) {

                        int month = myDatePicker.getMonth() + 1;
                        int day = myDatePicker.getDayOfMonth();
                        int year = myDatePicker.getYear();

                        from= String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + year;
                        Log.d("HostelDate",from + "  ");

                        if (type.equals("mess")){
                            try {
                                Date dateSelected = new SimpleDateFormat("dd/MM/yyyy").parse(from);

                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.HOUR,24);
                                Date dateCurrent = new SimpleDateFormat("dd/MM/yyyy").parse(new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()));

                                long diff  = dateSelected.getTime() - dateCurrent.getTime();

                                if (diff >= 0){
                                    fromTv.setText("From - " + from);
                                    dialog.cancel();
                                } else{
                                    Toast.makeText(HostelActivity.this, "Please select valid date. Turn your mess off before 24 hours!", Toast.LENGTH_SHORT).show();
                                }


                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.d("HostelDate", "in catch: " + e.getMessage());
                            }
                        } else if(type.equals("leave")){
                            fromTv.setText("From - " + from);
                            dialog.cancel();
                        }


                    }

                }).show();

    }

    public void pickDateto(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.date_picker, null, false);

        // the time picker on the alert dialog, getActivity() is how to get the value
        final DatePicker myDatePicker = (DatePicker) view.findViewById(R.id.myDatePicker);

        // the alert dialog
        new AlertDialog.Builder(HostelActivity.this).setView(view)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(DialogInterface dialog, int id) {

                        int month = myDatePicker.getMonth() + 1;
                        int day = myDatePicker.getDayOfMonth();
                        int year = myDatePicker.getYear();

                        to = String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + year;
                        String from = fromTv.getText().toString();

                        if (type.equals("mess")){

                            if (!from.equals("Departure Date")){

                                from = from.substring(7);

                                try {

                                    Date dateSelectedFrom = new SimpleDateFormat("dd/MM/yyyy").parse(from);
                                    Date dateSelectedTo = new SimpleDateFormat("dd/MM/yyyy").parse(to);

                                    Log.d("HostelDate",dateSelectedFrom.getTime() + "    " + dateSelectedTo.getTime());
                                    Log.d("HostelDate",dateSelectedFrom.toString() + "   " + dateSelectedTo.toString());

                                    long diff  = dateSelectedTo.getTime() - dateSelectedFrom.getTime();

                                    Log.d("HostelDate",diff + "");

                                    if (diff >= 172800000){

                                        toTv.setText("To - " + to);
                                        dialog.cancel();

                                    } else{
                                        Toast.makeText(HostelActivity.this, "Please select valid date. Turn your mess off for at least 6 meals!", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    Log.d("HostelDate", "in catch: " + e.getMessage());
                                }


                            } else {
                                Toast.makeText(HostelActivity.this, "Plese select departure date first!", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }

                        } else if (type.equals("leave")){

                            toTv.setText("To - " + to);
                            dialog.cancel();
                        }

                    }

                }).show();

    }

}
