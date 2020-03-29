package com.kraigs.android.micamp.Order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class FoodItemsActivity extends AppCompatActivity {

    final int UPI_PAYMENT = 0;
    Button orderBt;
    String note,upiId;
    long amount;

    RecyclerView itemsRv;
    CollectionReference colRef;
    String currentUserId;
    TextView billTv,itemsCountTv;
    CollectionReference pendingRef;
    DocumentReference userorderRef,userRef;
    CollectionReference rootRef;
    ProgressDialog loadingBar;
    DatabaseReference notifyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_items);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initializeFields();
        rootRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege());
        colRef = rootRef.document("Order").collection("Snakkers").document("Orders").collection(currentUserId);
        userorderRef = rootRef.document("Order").collection("Snakkers").document("UserOrders").collection(currentUserId).document();
        pendingRef = rootRef.document("Order").collection("Snakkers").document("OrderCategory").collection("PendingOrders");
        userRef = FirebaseFirestore.getInstance().collection("User").document(currentUserId);
        notifyRef = FirebaseDatabase.getInstance().getReference().child("NotificationsSnakkers");

        loadingBar = new ProgressDialog(this);
        setuUpRv();
        upiId = "praveen4030@upi";
        note = "Order at Snakkers";

        orderBt = findViewById(R.id.orderBt);

        findViewById(R.id.orderMoreBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FoodItemsActivity.this,ItemsListActivity.class);
                startActivity(intent);
            }
        });

        colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (!snapshots.isEmpty()){
                    long amt = 0,count = 0;
                    ArrayList<ItemList> list = new ArrayList<>();

                    for(QueryDocumentSnapshot snap : snapshots){

                        if (snap.contains("qty") && snap.contains("qtyFull")){

                            long qty = (long)snap.get("qty");
                            long qtyFull = (long)snap.get("qtyFull");
                            long price = Long.parseLong(snap.get("price").toString());
                            long priceFull = Long.parseLong(snap.get("fullPrice").toString());

                            ItemList itemHalf = new ItemList(snap.get("name").toString(),price + "",qty);
                            ItemList itemFull = new ItemList(snap.get("nameFull").toString(),priceFull + "",qtyFull);

                            list.add(itemHalf);
                            list.add(itemFull);

                            amt = amt + qty*price + qtyFull*priceFull;
                            count = count + qty + qtyFull;

                        } else if (snap.contains("qty") && !snap.contains("qtyFull")){
                            long qty = (long)snap.get("qty");
                            long price = Long.parseLong(snap.get("price").toString());

                            count = count + qty;
                            list.add(snap.toObject(ItemList.class));
                            amt = amt + qty*price;

                        }  else if (!snap.contains("qty") && snap.contains("qtyFull")){
                            long qtyFull = (long)snap.get("qtyFull");
                            long priceFull = Long.parseLong(snap.get("fullPrice").toString());

                            ItemList item = new ItemList(snap.get("nameFull").toString(),priceFull+ "",qtyFull);
                            count = count + qtyFull;
                            list.add(item);

                            amt = amt + qtyFull*priceFull;

                        }
                    }

                    billTv.setText("Rs. " + amt + "");
                    itemsCountTv.setText(count  + " Items");

                    long finalAmt = amt;

                    orderBt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String userOrderKey = userorderRef.getId();
                            addressDialog(finalAmt,list,userOrderKey);
                        }
                    });

                }
            }
        });
    }

    private void addressDialog(long amt,ArrayList<ItemList> list,String userOrderKey) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.zlayout_address);

        RelativeLayout selfRl = dialog.findViewById(R.id.selfRl);
        RelativeLayout homeRl = dialog.findViewById(R.id.homeRl);
        RelativeLayout addressRl = dialog.findViewById(R.id.addressRl);
        RelativeLayout addAddressRl = dialog.findViewById(R.id.enterAddressRl);
        TextView homeAddressTv = dialog.findViewById(R.id.homeAddressTv);
        TextInputEditText addressEt = dialog.findViewById(R.id.addressEt);
        Button saveBt = dialog.findViewById(R.id.saveBt);
        addAddressRl.setVisibility(View.GONE);

        TextInputEditText callEt = dialog.findViewById(R.id.callEt);
        TextInputLayout callL = dialog.findViewById(R.id.callL);

        DocumentReference docRef = pendingRef.document();

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable DocumentSnapshot snapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()){
                    String name = snapshot.get("name").toString();

                    if (snapshot.contains("address") && snapshot.contains("number")){
                        String address = snapshot.get("address").toString();
                        String number = snapshot.get("number").toString();
                        homeAddressTv.setText(address);

                        homeRl.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                dialog.dismiss();
                                loadingBar.setTitle("Order");
                                loadingBar.setMessage("Please wait");
                                loadingBar.show();

                                HashMap<String,Object> map1 = new HashMap<>();
                                map1.put("order",list);
                                map1.put("timestamp",FieldValue.serverTimestamp());
                                map1.put("userOrderKey",userOrderKey);
                                map1.put("user",currentUserId);
                                map1.put("price", amt + 20);
                                map1.put("name",name);
                                map1.put("address",address);
                                map1.put("number",number);

                                HashMap<String,Object> map2 = new HashMap<>();
                                map2.put("order",list);
                                map2.put("timestamp",FieldValue.serverTimestamp());
                                map2.put("price",amt + 20);
                                map2.put("name",name);
                                map2.put("status","Pending");
                                map2.put("orderKey",docRef.getId());

                                docRef.set(map1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            userorderRef.set(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){
                                                        HashMap<String,Object> map3 = new HashMap<>();
                                                        map3.put("type","NewOrder");

                                                        notifyRef.push().setValue(map3).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    loadingBar.dismiss();
                                                                    Toast.makeText(FoodItemsActivity.this, "Your order has been placed ,now procees with payment.", Toast.LENGTH_SHORT).show();
                                                                    payUsingUpi((amt + 20) + "", upiId, "Order payment", note);
                                                                } else{
                                                                    loadingBar.dismiss();
                                                                    Toast.makeText(FoodItemsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                    }else{
                                                        loadingBar.dismiss();
                                                        Toast.makeText(FoodItemsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });
                                        }else{
                                            loadingBar.dismiss();
                                            Toast.makeText(FoodItemsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                    } else{
                        homeRl.setVisibility(View.GONE);
                    }

                    selfRl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            dialog.dismiss();
                            loadingBar.setTitle("Order");
                            loadingBar.setMessage("Please wait");
                            loadingBar.show();

                            HashMap<String,Object> map1 = new HashMap<>();
                            map1.put("order",list);
                            map1.put("timestamp",FieldValue.serverTimestamp());
                            map1.put("userOrderKey",userOrderKey);
                            map1.put("user",currentUserId);
                            map1.put("price", amt);
                            map1.put("name",name);
                            map1.put("address","Snackers");

                            if (snapshot.contains("number")){
                                String number = snapshot.get("number").toString();
                                map1.put("number",number);
                            }

                            HashMap<String,Object> map2 = new HashMap<>();
                            map2.put("order",list);
                            map2.put("timestamp",FieldValue.serverTimestamp());
                            map2.put("price",amt);
                            map2.put("name",name);
                            map2.put("status","Pending");
                            map2.put("orderKey",docRef.getId());

                            docRef.set(map1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        userorderRef.set(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    HashMap<String,Object> map3 = new HashMap<>();
                                                    map3.put("type","NewOrder");

                                                    notifyRef.push().setValue(map3).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                loadingBar.dismiss();
                                                                Toast.makeText(FoodItemsActivity.this, "Your order has been placed ,now procees with payment.", Toast.LENGTH_SHORT).show();
                                                                payUsingUpi(amt + "", upiId, "Order payment", note);
                                                            } else{
                                                                loadingBar.dismiss();
                                                                Toast.makeText(FoodItemsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    loadingBar.dismiss();
                                                    Toast.makeText(FoodItemsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else{
                                        loadingBar.dismiss();
                                        Toast.makeText(FoodItemsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        addressRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selfRl.setVisibility(View.GONE);
                homeRl.setVisibility(View.GONE);
                addAddressRl.setVisibility(View.VISIBLE);
            }
        });

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newAddress = addressEt.getText().toString().trim();
                String number = callEt.getText().toString().trim();
                if (!TextUtils.isEmpty(newAddress) && !TextUtils.isEmpty(number)){
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("address",newAddress);
                    map.put("number",number);

                    userRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                selfRl.setVisibility(View.VISIBLE);
                                homeRl.setVisibility(View.VISIBLE);
                                addAddressRl.setVisibility(View.GONE);

                            }
                        }
                    });
                } else{
                    Toast.makeText(FoodItemsActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();

    }

    private void setuUpRv() {

        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(colRef,Item.class)
                .build();

        FirestoreRecyclerAdapter<Item,ItemHolder> adapter = new FirestoreRecyclerAdapter<Item, ItemHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemHolder itemHolder, int i, @NonNull Item item) {
                String key = getSnapshots().getSnapshot(i).getId();

                itemHolder.nameTv.setText(item.getName());

                if (item.getPrice() != null && item.getFullPrice() != null){

                    long price = item.getQty()*Integer.parseInt(item.getPrice());
                    long priceFull = item.getQtyFull()*Integer.parseInt(item.getFullPrice());

                    itemHolder.priceTv.setText("Rs. " + (price + priceFull));

                } else if (item.getPrice() == null && item.getFullPrice() != null){
                    long priceFull = item.getQtyFull()*Integer.parseInt(item.getFullPrice());
                    itemHolder.priceTv.setText("Rs. " + priceFull);
                } else if (item.getPrice() != null && item.getFullPrice() == null){
                    long price = item.getQty()*Integer.parseInt(item.getPrice());
                    itemHolder.priceTv.setText("Rs. " + price);

                }

                colRef.document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (snapshot.exists()){

                            if (item.getHalfAvailable() != null){
                                if (item.getHalfAvailable().equals("true")){

                                    if (snapshot.contains("qty") && snapshot.contains("qtyFull")){
                                        long qty = (long)snapshot.get("qty");
                                        long qtyFull = (long)snapshot.get("qtyFull");
                                        itemHolder.qtyTv.setText(qty + qtyFull + "");
                                    } else if (snapshot.contains("qty") && !snapshot.contains("qtyFull")){
                                        long qty = (long)snapshot.get("qty");
                                        itemHolder.qtyTv.setText(qty + "");
                                    }  else if (!snapshot.contains("qty") && snapshot.contains("qtyFull")){
                                        long qtyFull = (long)snapshot.get("qtyFull");
                                        itemHolder.qtyTv.setText(qtyFull + "");
                                    }

                                } else{

                                    long qty = (long)snapshot.get("qty");
                                    itemHolder.qtyTv.setText(qty + "");

                                }
                            } else{

                                long qty = (long)snapshot.get("qty");
                                itemHolder.qtyTv.setText(qty + "");
                            }
                        }
                    }
                });

                itemHolder.subBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (item.getHalfAvailable()!=null){

                            if (item.getHalfAvailable().equals("true")){

                                halfDialog(itemHolder,item,key);

                            } else{
                                long qty = Long.parseLong(itemHolder.qtyTv.getText().toString());
                                if (qty >1){

                                    HashMap<String,Object> map = new HashMap<>();
                                    map.put("qty", FieldValue.increment(-1));

                                    colRef.document(key).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                            }
                                        }
                                    });
                                } else if(qty == 1){
                                    colRef.document(key).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                notifyItemRemoved(i);
                                                notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }
                        } else{

                            long qty = Long.parseLong(itemHolder.qtyTv.getText().toString());
                            if (qty >1){

                                HashMap<String,Object> map = new HashMap<>();
                                map.put("qty", FieldValue.increment(-1));

                                colRef.document(key).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                        }
                                    }
                                });

                            } else if(qty == 1){
                                colRef.document(key).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            notifyItemRemoved(i);
                                            notifyDataSetChanged();
                                        }
                                    }
                                });
                            }

                        }
                    }
                });

                itemHolder.addQtyBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (item.getHalfAvailable()!=null){

                            if (item.getHalfAvailable().equals("true")){

                                halfDialog(itemHolder,item,key);

                            } else{

                                HashMap<String,Object> map = new HashMap<>();
                                map.put("qty", FieldValue.increment(1));
                                colRef.document(key).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                        }
                                    }
                                });
                            }
                        } else{


                            HashMap<String,Object> map = new HashMap<>();
                            map.put("qty", FieldValue.increment(1));
                            colRef.document(key).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                    }
                                }
                            });
                        }


                    }
                });

            }

            @NonNull
            @Override
            public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_food_item,parent,false);
                return  new ItemHolder(v);
            }
        };

        itemsRv.setAdapter(adapter);
        adapter.startListening();

    }

    private void halfDialog(ItemHolder holder,Item item,String key) {

        BottomSheetDialog dialog = new BottomSheetDialog(holder.itemView.getContext());
        dialog.setContentView(R.layout.zlayout_halffull);

        TextView itemNameTv = dialog.findViewById(R.id.itemTv);
        TextView halfPriceTv = dialog.findViewById(R.id.halfPriceTv);
        TextView fullPriceTv = dialog.findViewById(R.id.fullPriceTv);

        halfPriceTv.setText("Rs. " + item.getPrice());
        fullPriceTv.setText("Rs. " + item.getFullPrice());

        if (item.getName() != null){
            itemNameTv.setText(item.getName());
        } else{
            itemNameTv.setText(item.getNameFull());
        }

        TextView qtyTv,qtyFullTv;
        Button addBt,addFullBt;
        RelativeLayout qtyRl,qtyFullRl;
        ImageView addQtyBt,subBt,addQtyFullBt,subFullBt;

        addBt = dialog.findViewById(R.id.addItemBt);
        addQtyBt = dialog.findViewById(R.id.addQtyBt);
        subBt = dialog.findViewById(R.id.subBt);
        qtyTv = dialog.findViewById(R.id.qtyTv);
        qtyRl = dialog.findViewById(R.id.qtyRl);

        addFullBt = dialog.findViewById(R.id.addFullBt);
        addQtyFullBt = dialog.findViewById(R.id.addQtyFullBt);
        subFullBt = dialog.findViewById(R.id.subFullBt);
        qtyFullTv = dialog.findViewById(R.id.qtyFullTv);
        qtyFullRl = dialog.findViewById(R.id.qtyFullRl);

        addBt.setVisibility(View.GONE);
        addFullBt.setVisibility(View.GONE);

        if (item.getQtyFull() ==0){
            dialog.findViewById(R.id.fullRl).setVisibility(View.GONE);
        } else if (item.getQty() == 0){
            dialog.findViewById(R.id.halfRl).setVisibility(View.GONE);
        }

        colRef.document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()){

                    if (snapshot.contains("qty")){
                        long qty = (long)snapshot.get("qty");
                        qtyTv.setText(qty + "");
                    }

                    if (snapshot.contains("qtyFull")) {
                        long qty = (long)snapshot.get("qtyFull");
                        qtyFullTv.setText(qty + "");
                    }

                }
            }
        });

        subBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long qty = Long.parseLong(qtyTv.getText().toString());
                if (qty >1){

                    HashMap<String,Object> map = new HashMap<>();
                    map.put("qty", FieldValue.increment(-1));

                    colRef.document(key).update(map);

                } else if(qty == 1){

                    colRef.document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains("qtyFull")){

                                    HashMap<String,Object> map2 = new HashMap<>();
                                    map2.put("qty",FieldValue.delete());
                                    map2.put("price",FieldValue.delete());
                                    map2.put("name", FieldValue.delete());

                                    colRef.document(key).update(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                dialog.dismiss();
                                            }
                                        }
                                    });

                                } else{
                                    colRef.document(key).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                dialog.dismiss();
                                                dialog.findViewById(R.id.fullRl).setVisibility(View.GONE);

                                            }
                                        }
                                    });

                                }
                            }
                        }
                    });
                }
            }
        });

        addQtyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String,Object> map = new HashMap<>();
                map.put("qty", FieldValue.increment(1));

                colRef.document(key).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                        }
                    }
                });
            }
        });

        subFullBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long qty = Long.parseLong(qtyFullTv.getText().toString());
                if (qty >1){

                    HashMap<String,Object> map = new HashMap<>();
                    map.put("qtyFull", FieldValue.increment(-1));

                    colRef.document(key).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                            }
                        }
                    });
                } else if(qty == 1){

                    colRef.document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains("qty")){

                                    HashMap<String,Object> map2 = new HashMap<>();
                                    map2.put("qtyFull",FieldValue.delete());
                                    map2.put("fullPrice",FieldValue.delete());
                                    map2.put("nameFull", FieldValue.delete());

                                    colRef.document(key).update(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                dialog.dismiss();
                                            }
                                        }
                                    });

                                } else{
                                    colRef.document(key).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                dialog.dismiss();
                                            }
                                        }
                                    });

                                }
                            }
                        }
                    });
                }
            }
        });

        addQtyFullBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String,Object> map = new HashMap<>();
                map.put("qtyFull", FieldValue.increment(1));

                colRef.document(key).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                        }
                    }
                });
            }
        });

        dialog.show();

    }


    private void initializeFields() {
        itemsRv = findViewById(R.id.itemsRv);
        itemsRv.setLayoutManager(new LinearLayoutManager(this));
        billTv = findViewById(R.id.billTv);
        itemsCountTv = findViewById(R.id.itemsCountTv);
    }

    void payUsingUpi(String amount, String upiId, String name, String note) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText( FoodItemsActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(FoodItemsActivity.this)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(FoodItemsActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+approvalRefNo);
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(FoodItemsActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(FoodItemsActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(FoodItemsActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        public TextView nameTv,priceTv,qtyTv;
        RelativeLayout qtyRl;
        ImageView addQtyBt,subBt;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.list_item_name);
            priceTv = itemView.findViewById(R.id.priceTv);
            addQtyBt = itemView.findViewById(R.id.addQtyBt);
            subBt = itemView.findViewById(R.id.subBt);
            qtyTv = itemView.findViewById(R.id.qtyTv);
            qtyRl = itemView.findViewById(R.id.qtyRl);
        }
    }
}
