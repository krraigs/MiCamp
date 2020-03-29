package com.kraigs.android.micamp.BuyAndSell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdsHistoryActivity extends AppCompatActivity {

    RecyclerView adHistoryRv;
    CollectionReference adHistoryRef;
    FirebaseAuth mAuth;
    String currentUserId;
    int count;
    ArrayList<Buy> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_history);
        getSupportActionBar().setTitle("Your Ads");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        adHistoryRv = findViewById(R.id.adHistory);
        adHistoryRv.setLayoutManager(new GridLayoutManager(this,2));


        adHistoryRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("BuynSell").collection("UserAds").document("Ads").collection(currentUserId);

    }

    @Override
    protected void onStart() {
        super.onStart();

        adHistoryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    list = new ArrayList<>();

                    for(QueryDocumentSnapshot ds : task.getResult()){
                        Buy buy = ds.toObject(Buy.class);
                        if(currentUserId.equals(buy.getSellerUid())){
                            list.add(ds.toObject(Buy.class));
                        }
                    }

                    if (list.size() == 0){
                        findViewById(R.id.emptyRl).setVisibility(View.VISIBLE);
                        adHistoryRv.setVisibility(View.GONE);
                    } else{
                        findViewById(R.id.emptyRl).setVisibility(View.GONE);
                        adHistoryRv.setVisibility(View.VISIBLE);
                    }

                    AdsAdapter adsAdapter = new AdsAdapter(list);
                    adHistoryRv.setAdapter(adsAdapter);
                }
            }
        });
    }

    class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.AdsHolder>{

        ArrayList<Buy> list;
        AdsAdapter(ArrayList<Buy> list){
            this.list = list;
        }

        @NonNull
        @Override
        public AdsAdapter.AdsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_buy_item, parent, false);
            return new AdsHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull AdsAdapter.AdsHolder holder, int position) {
            Buy model = list.get(position);
            holder.itemView.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
            Picasso.get().load(model.getImage()).into(holder.image);
            holder.priceTv.setText("Rs. " + model.getPrice());
            holder.itemNameTv.setText(model.getItemName());
            String curretAd = model.getKey();

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdsHistoryActivity.this);
                    alertDialog.setTitle("Delete").setMessage("Do you want to delete this ad?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adHistoryRef.document(curretAd).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        list.remove(position);
                                        notifyItemRemoved(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(AdsHistoryActivity.this, "Removed successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        String message = task.getException().toString();
                                        Toast.makeText(AdsHistoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }
            });

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdsHistoryActivity.this, ItemDescActivity.class);
                    intent.putExtra("adId", curretAd);
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class AdsHolder extends RecyclerView.ViewHolder {
            ImageView image,delete;
            TextView priceTv, itemNameTv;
            public AdsHolder(@NonNull View itemView) {
                super(itemView);

                image = itemView.findViewById(R.id.item_image);
                priceTv = itemView.findViewById(R.id.item_price);
                itemNameTv = itemView.findViewById(R.id.item_info);
                delete = itemView.findViewById(R.id.deleteAd);
            }
        }
    }
}
