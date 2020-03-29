package com.kraigs.android.micamp.Home.News;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kraigs.android.micamp.extras.SharedPrefs;

import java.util.HashMap;

public class AddNewsActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText dateEt,titleEt,contentEt;
    Button saveBt;
    Toolbar toolbar;
    DatabaseReference newsRef;
    ProgressDialog loadingBar;
    CollectionReference newsCol;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);
        getSupportActionBar().setTitle("Add View");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleEt = findViewById(R.id.addTitleEt);
        contentEt = findViewById(R.id.contentEt);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

        newsRef = FirebaseDatabase.getInstance().getReference().child("News").push();
        newsCol = db.collection(SharedPrefs.getCollege()).document("Home").collection("News");

        saveBt = findViewById(R.id.saveNewsBt);

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEt.getText().toString();
                String content = contentEt.getText().toString();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)){
                    HashMap<String,Object> newsMap = new HashMap<>();
                    newsMap.put("content",content);
                    newsMap.put("topic",title);
                    newsMap.put("timestamp", FieldValue.serverTimestamp());
                    newsMap.put("by",currentUserID);

                    loadingBar.setTitle("Adding");
                    loadingBar.setMessage("Please wait while we are adding your news");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(false);

                    newsCol.document().set(newsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                loadingBar.dismiss();
                                Toast.makeText(AddNewsActivity.this, "News Added Successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddNewsActivity.this,NewsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else{
                                loadingBar.dismiss();
                            }
                        }
                    });
                }else{
                    Toast.makeText(AddNewsActivity.this, "Please add all details", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
