package com.kraigs.android.micamp.BuyAndSell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.Chat.ChatActivity;
import com.kraigs.android.micamp.Mentor.MentorProfileActivity;
import com.kraigs.android.micamp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class ItemDescActivity extends AppCompatActivity {

    String adID,sellerImage,userName;
    TextView priceTv,itemNameTv,dateTv,locationTv,detailtv,userNameTv;
    ImageView itemImage;
    CircleImageView userProfilePic;
    DocumentReference itemRef;
    CollectionReference userCol;
    RelativeLayout sellerProfileRl;
    Button chat,call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_desc);

        adID = getIntent().getStringExtra("adId");

        initializeFields();

        itemRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("BuynSell").collection("Buy").document(adID);
        userCol = FirebaseFirestore.getInstance().collection("User");

        itemRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    String image = task.getResult().get("image").toString();
                    String price = task.getResult().get("price").toString();
                    String itemName = task.getResult().get("itemName").toString();
                    String location = task.getResult().get("location").toString();
                    String date = task.getResult().get("date").toString();
                    String detail = task.getResult().get("detail").toString();
                    String uid = task.getResult().get("sellerUid").toString();
                    String contact = task.getResult().get("contact").toString();

                    itemNameTv.setText(itemName);
                    locationTv.setText(location);
                    dateTv.setText(date);
                    detailtv.setText(detail);
                    priceTv.setText("Rs. " + price);
                    Picasso.get().load(image).into(itemImage);

                    sellerProfileRl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ItemDescActivity.this, MentorProfileActivity.class);
                            intent.putExtra("mentor_id", uid);
                            startActivity(intent);
                        }
                    });

                    call.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Uri u = Uri.parse("tel:" + contact);
                            Intent i = new Intent(Intent.ACTION_DIAL, u);
                            startActivity(i);
                        }
                    });

                    userCol.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot dataSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (dataSnapshot.exists()) {
                                userName = dataSnapshot.get("name").toString();
                                userNameTv.setText(userName);
                                if (dataSnapshot.contains("image")) {
                                    sellerImage = dataSnapshot.get("image").toString();
                                    Picasso.get().load(sellerImage).into(userProfilePic);
                                }
                            }
                        }
                    });

                    chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ItemDescActivity.this, ChatActivity.class);
                            intent.putExtra("visit_user_id", uid);
                            intent.putExtra("visit_user_name", userName);
                            if (sellerImage != null) {
                                intent.putExtra("visit_user_image", sellerImage);
                            } else {
                                intent.putExtra("visit_user_image", "default_image");
                            }
                            startActivity(intent);
                        }
                    });
                }
            }
        });

    }

    private void initializeFields() {

        priceTv = findViewById(R.id.itemPriceTv);
        itemNameTv = findViewById(R.id.itemInfoTv);
        locationTv = findViewById(R.id.userLocationTv);
        dateTv = findViewById(R.id.adPostedDate);
        detailtv = findViewById(R.id.itemDetailTv);
        userNameTv = findViewById(R.id.sellerName);
        itemImage = findViewById(R.id.itemImage);
        userProfilePic = findViewById(R.id.sellerProfilePic);
        sellerProfileRl = findViewById(R.id.sellerProfileRl);
        chat = findViewById(R.id.sellerChatBt);
        call = findViewById(R.id.sellerCallBt);
    }
}
