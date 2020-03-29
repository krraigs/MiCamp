package com.kraigs.android.micamp.Order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ItemsListActivity extends AppCompatActivity {

    public CategoryAdapter adapter;
    Button cartBt;
    DocumentReference docRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("Items");
    CollectionReference colRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("AllItems").collection("Category");
    TextView priceTv,itemsNoTv;
    String currentUserId;
    CollectionReference rootRef,orderRef;
    RecyclerView recyclerView;
    RelativeLayout searchRl;
    Switch vegSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege());
        orderRef = rootRef.document("Order").collection("Snakkers").document("Orders").collection(currentUserId);

        priceTv = findViewById(R.id.priceTv);
        itemsNoTv = findViewById(R.id.itemsNoTv);
        vegSwitch = findViewById(R.id.vegSwitch);

        orderRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (!snapshots.isEmpty()){

                    long amt = 0;
                    long count = 0;

                    ArrayList<ItemList> list = new ArrayList<>();
                    for(QueryDocumentSnapshot snap : snapshots){

                        if (snap.contains("qty") && snap.contains("qtyFull")){
                            long qty = (long)snap.get("qty");
                            long qtyFull = (long)snap.get("qtyFull");
                            long price = Long.parseLong(snap.get("price").toString());
                            long priceFull = Long.parseLong(snap.get("fullPrice").toString());

                            count = count + qty+ qtyFull;
                            list.add(snap.toObject(ItemList.class));
                            amt = amt + qty*price + qtyFull*priceFull;

                        } else if (snap.contains("qty") && !snap.contains("qtyFull")){
                            long qty = (long)snap.get("qty");
                            long price = Long.parseLong(snap.get("price").toString());

                            count = count + qty;
                            list.add(snap.toObject(ItemList.class));
                            amt = amt + qty*price;

                        }  else if (!snap.contains("qty") && snap.contains("qtyFull")){
                            long qtyFull = (long)snap.get("qtyFull");
                            long priceFull = Long.parseLong(snap.get("fullPrice").toString());

                            count = count + qtyFull;
                            list.add(snap.toObject(ItemList.class));
                            amt = amt + qtyFull*priceFull;

                        }

                    }

                    itemsNoTv.setText(count + " Items");
                    priceTv.setText("Total = Rs. " + amt);

                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.itemsRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ItemsListActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        cartBt = findViewById(R.id.cartBt);

        cartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemsListActivity.this,FoodItemsActivity.class);
                intent.putExtra("item","Paneer");
                startActivity(intent);
            }
        });

        findViewById(R.id.searchRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemsListActivity.this,AllItemsActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        vegSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    ArrayList<Category> vegList = new ArrayList<>();

                    colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){

                                int i =0;
                                for (QueryDocumentSnapshot snap : task.getResult()){
                                    i++;
                                    if (snap.contains("type")){
                                        String type = snap.get("type").toString();

                                        Category item = new Category(snap.getId(),addData(snap.getId()));

                                        if (type.equals("Veg")){
                                            vegList.add(item);
                                        }
                                    }
                                }

                                CategoryAdapter adapter1 = new CategoryAdapter(vegList);
                                recyclerView.setAdapter(adapter1);

                            }
                        }
                    });

                } else{
                    colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){

                                ArrayList<Category> list = new ArrayList<>();

                                for (QueryDocumentSnapshot snap : task.getResult()) {
                                    Category item = new Category(snap.getId(), addData(snap.getId()));
                                    list.add(item);
                                }

                                adapter = new CategoryAdapter(list);
                                recyclerView.setAdapter(adapter);

                            }
                        }
                    });
                }
            }
        });

        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    ArrayList<Category> list = new ArrayList<>();

                    for (QueryDocumentSnapshot snap : task.getResult()) {
                        Category item = new Category(snap.getId(), addData(snap.getId()));
                        list.add(item);
                    }

                    adapter = new CategoryAdapter(list);
                    recyclerView.setAdapter(adapter);

                }
            }
        });
    }

    public List<Item> addData(String category) {

        ArrayList<Item> list = new ArrayList<>();

        docRef.collection(category).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    list.addAll(task.getResult().toObjects(Item.class));
                }
            }
        });

        return  list;
    }

}
