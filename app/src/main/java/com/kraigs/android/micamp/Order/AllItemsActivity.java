package com.kraigs.android.micamp.Order;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.annotation.Nullable;

public class AllItemsActivity extends AppCompatActivity {

    RecyclerView itemsRv,recommendedRv;
    CollectionReference colRef;
    String currentUserID;
    String item;
    ArrayList<Item> itemsList;
    MaterialSearchView searchView;

    DocumentReference docRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("Items");
    CollectionReference categoryRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("AllItems").collection("Category");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_items);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemsRv = findViewById(R.id.allItemsRv);
        recommendedRv = findViewById(R.id.recommendedRv);
        searchView = findViewById(R.id.search_view);
        recommendedRv.setLayoutManager(new GridLayoutManager(this,2));
        itemsRv.setVisibility(View.GONE);
        itemsRv.setLayoutManager(new LinearLayoutManager(this));

        item = getIntent().getStringExtra("item");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        colRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("Orders").collection(currentUserID);

        fetchData();
        search(item);

        setUpRecommendedRv();

    }

    private void setUpRecommendedRv(){

        Query query = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("AllItems").collection("Recommended");

        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query,Item.class)
                .build();

        FirestoreRecyclerAdapter<Item,ItemRecHolder> adapter = new FirestoreRecyclerAdapter<Item, ItemRecHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemRecHolder holder, int i, @NonNull Item item) {

                holder.itemNameTv.setText(item.getName());
                holder.priceTv.setText("Rs. " + item.getPrice());

                Picasso.get().load(item.getImage()).into(holder.itemImage);

                holder.addBt.setVisibility(View.GONE);
                holder.qtyRl.setVisibility(View.GONE);

                colRef.document(item.getKey()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (snapshot.exists()){

                            if (item.getHalfAvailable() != null){
                                if (item.getHalfAvailable().equals("true")){

                                    holder.addBt.setVisibility(View.GONE);
                                    holder.qtyRl.setVisibility(View.VISIBLE);

                                    if (snapshot.contains("qty") && snapshot.contains("qtyFull")){
                                        long qty = (long)snapshot.get("qty");
                                        long qtyFull = (long)snapshot.get("qtyFull");
                                        holder.qtyTv.setText(qty + qtyFull + "");
                                    } else if (snapshot.contains("qty") && !snapshot.contains("qtyFull")){
                                        long qty = (long)snapshot.get("qty");
                                        holder.qtyTv.setText(qty + "");
                                    }  else if (!snapshot.contains("qty") && snapshot.contains("qtyFull")){
                                        long qtyFull = (long)snapshot.get("qtyFull");
                                        holder.qtyTv.setText(qtyFull + "");
                                    }

                                } else{

                                    holder.addBt.setVisibility(View.GONE);
                                    holder.qtyRl.setVisibility(View.VISIBLE);

                                    long qty = (long)snapshot.get("qty");
                                    holder.qtyTv.setText(qty + "");

                                }
                            } else{

                                holder.addBt.setVisibility(View.GONE);
                                holder.qtyRl.setVisibility(View.VISIBLE);

                                long qty = (long)snapshot.get("qty");
                                holder.qtyTv.setText(qty + "");
                            }


                        } else{
                            holder.addBt.setVisibility(View.VISIBLE);
                            holder.qtyRl.setVisibility(View.GONE);
                        }
                    }
                });

                holder.subBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (item.getHalfAvailable()!=null){

                            if (item.getHalfAvailable().equals("true")){

                                halfDialog(holder,item);

                            } else{
                                long qty = Long.parseLong(holder.qtyTv.getText().toString());
                                if (qty >1){
                                    HashMap<String,Object> map = new HashMap<>();
                                    map.put("qty", FieldValue.increment(-1));

                                    colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                            }
                                        }
                                    });
                                } else if(qty == 1){
                                    colRef.document(item.getKey()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){ }
                                        }
                                    });
                                }
                            }
                        } else{

                            long qty = Long.parseLong(holder.qtyTv.getText().toString());
                            if (qty >1){
                                HashMap<String,Object> map = new HashMap<>();
                                map.put("qty", FieldValue.increment(-1));

                                colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                        }
                                    }
                                });
                            } else if(qty == 1){
                                colRef.document(item.getKey()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){ }
                                    }
                                });
                            }

                        }



                    }
                });

                holder.addQtyBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (item.getHalfAvailable()!=null){

                            if (item.getHalfAvailable().equals("true")){

                                halfDialog(holder,item);

                            } else{

                                HashMap<String,Object> map = new HashMap<>();
                                map.put("qty", FieldValue.increment(1));
                                colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                    }
                                }
                            });
                        }

                    }
                });

                holder.addBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (item.getHalfAvailable() != null){
                            if (item.getHalfAvailable().equals("true")){

                                halfDialog(holder,item);

                            } else{

                                HashMap<String,Object> map = new HashMap<>();
                                map.put("qty",1);
                                map.put("name",item.getName());
                                map.put("price",item.getPrice());

                                colRef.document(item.getKey()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                        }
                                    }
                                });
                            }

                        } else{

                            HashMap<String,Object> map = new HashMap<>();
                            map.put("qty",1);
                            map.put("name",item.getName());
                            map.put("price",item.getPrice());

                            colRef.document(item.getKey()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
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
            public ItemRecHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_itemimage,parent,false);
                return  new ItemRecHolder(v);
            }
        };

        recommendedRv.setAdapter(adapter);
        adapter.startListening();

    }

    private void halfDialog(ItemRecHolder holder,Item item) {
        BottomSheetDialog dialog = new BottomSheetDialog(holder.itemView.getContext());
        dialog.setContentView(R.layout.zlayout_halffull);

        TextView itemNameTv = dialog.findViewById(R.id.itemTv);
        TextView halfPriceTv = dialog.findViewById(R.id.halfPriceTv);
        TextView fullPriceTv = dialog.findViewById(R.id.fullPriceTv);

        halfPriceTv.setText("Rs. " + item.getPrice());
        fullPriceTv.setText("Rs. " + item.getFullPrice());
        itemNameTv.setText(item.getName());

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
        qtyRl.setVisibility(View.GONE);

        colRef.document(item.getKey()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()){

                    if (snapshot.contains("qty")){
                        addBt.setVisibility(View.GONE);
                        qtyRl.setVisibility(View.VISIBLE);

                        long qty = (long)snapshot.get("qty");
                        qtyTv.setText(qty + "");
                    } else{
                        addBt.setVisibility(View.VISIBLE);
                        qtyRl.setVisibility(View.GONE);
                    }

                    if (snapshot.contains("qtyFull")) {

                        addFullBt.setVisibility(View.GONE);
                        qtyFullRl.setVisibility(View.VISIBLE);

                        long qty = (long)snapshot.get("qtyFull");
                        qtyFullTv.setText(qty + "");

                    } else {

                        addFullBt.setVisibility(View.VISIBLE);
                        qtyFullRl.setVisibility(View.GONE);
                    }


                } else{

                    addBt.setVisibility(View.VISIBLE);
                    qtyRl.setVisibility(View.GONE);
                    addFullBt.setVisibility(View.VISIBLE);
                    qtyFullRl.setVisibility(View.GONE);
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

                    colRef.document(item.getKey()).update(map);

                } else if(qty == 1){

                    colRef.document(item.getKey()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains("qtyFull")){

                                    HashMap<String,Object> map2 = new HashMap<>();
                                    map2.put("qty",FieldValue.delete());
                                    map2.put("price",FieldValue.delete());
                                    map2.put("name", FieldValue.delete());

                                    colRef.document(item.getKey()).update(map2);

                                } else{
                                    colRef.document(item.getKey()).delete();

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

                colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                        }
                    }
                });
            }
        });

        addBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String,Object> map = new HashMap<>();
                map.put("qty",1);
                map.put("name",item.getName() + " Half");
                map.put("price",item.getPrice());
                map.put("halfAvailable","true");

                colRef.document(item.getKey()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            if (task.getResult().exists()){

                                colRef.document(item.getKey()).update(map);

                            } else{

                                colRef.document(item.getKey()).set(map);

                            }

                        }
                    }
                });



            }
        });

        addFullBt.setVisibility(View.GONE);
        qtyFullRl.setVisibility(View.GONE);

        subFullBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long qty = Long.parseLong(qtyFullTv.getText().toString());
                if (qty >1){

                    HashMap<String,Object> map = new HashMap<>();
                    map.put("qtyFull", FieldValue.increment(-1));

                    colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                            }
                        }
                    });
                } else if(qty == 1){

                    colRef.document(item.getKey()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().contains("qty")){

                                    HashMap<String,Object> map2 = new HashMap<>();
                                    map2.put("qtyFull",FieldValue.delete());
                                    map2.put("fullPrice",FieldValue.delete());
                                    map2.put("nameFull", FieldValue.delete());

                                    colRef.document(item.getKey()).update(map2);

                                } else{
                                    colRef.document(item.getKey()).delete();

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

                colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                        }
                    }
                });
            }
        });

        addFullBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String,Object> map = new HashMap<>();
                map.put("qtyFull",1);
                map.put("nameFull",item.getName() + " Full");
                map.put("fullPrice",item.getFullPrice());
                map.put("halfAvailable","true");

                colRef.document(item.getKey()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().exists()){

                                colRef.document(item.getKey()).update(map);

                            } else{

                                colRef.document(item.getKey()).set(map);


                            }
                        }
                    }
                });

            }
        });

        dialog.show();
    }

    private void fetchData() {

        itemsList = new ArrayList<>();

        categoryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {


                    for (QueryDocumentSnapshot snap : task.getResult()){

                        Log.d("Snackers",snap.getId() + " :       ");

                        docRef.collection(snap.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    Log.d("Snackers",task.getResult().size() + " :       ");

                                    for (QueryDocumentSnapshot snap2 : task.getResult()){
                                        Item item= snap2.toObject(Item.class);
                                        Log.d("Snacker", item.getName());

                                        itemsList.add(item);
                                    }

                                    ItemsAdapter adapter = new ItemsAdapter(itemsList);
                                    itemsRv.setAdapter(adapter);

                                } else{
                                    Log.d("Snackers","Error : " + task.getException().toString());
                                }
                            }
                        });
                    }

                    Collections.sort(itemsList, new SortBtName());

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                recommendedRv.setVisibility(View.GONE);
                itemsRv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                recommendedRv.setVisibility(View.VISIBLE);
                itemsRv.setVisibility(View.GONE);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                search(s);
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_user_search,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;

    }

    private void search(String newText) {
        ArrayList<Item> myList = new ArrayList<>();
        for (Item object : itemsList){
            String searchSt = object.getName().toLowerCase();
            if (searchSt.contains(newText.toLowerCase())){
                myList.add(object);
            }
        }

        itemsRv.setLayoutManager(new LinearLayoutManager(this));
        ItemsAdapter adapter = new ItemsAdapter(myList);
        itemsRv.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemsHolder>{

        ArrayList<Item> itemsList;
        public ItemsAdapter(ArrayList<Item> list){
            this.itemsList = list;
        }

        @NonNull
        @Override
        public ItemsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_food_items,parent,false);
            return new ItemsHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemsHolder holder, int position) {

            final Item item = itemsList.get(position);
            if (position == 0){

            }
            holder.nameTv.setText(item.getName());
            holder.priceTv.setText("Rs. " + item.getPrice());

            holder.addBt.setVisibility(View.GONE);
            holder.qtyRl.setVisibility(View.GONE);

            colRef.document(item.getKey()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (snapshot.exists()){
                        holder.addBt.setVisibility(View.GONE);
                        holder.qtyRl.setVisibility(View.VISIBLE);

                        long qty = (long)snapshot.get("qty");
                        holder.qtyTv.setText(qty + "");

                    } else{
                        holder.addBt.setVisibility(View.VISIBLE);
                        holder.qtyRl.setVisibility(View.GONE);
                    }
                }
            });

            holder.subBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long qty = Long.parseLong(holder.qtyTv.getText().toString());
                    if (qty >1){
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("qty", FieldValue.increment(-1));

                        colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                }
                            }
                        });
                    } else if(qty == 1){
                        colRef.document(item.getKey()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){ }
                            }
                        });
                    }
                }
            });

            holder.addQtyBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("qty", FieldValue.increment(1));
                    colRef.document(item.getKey()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                            }
                        }
                    });
                }
            });

            holder.addBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    HashMap<String,Object> map = new HashMap<>();
                    map.put("qty",1);
                    map.put("name",item.getName());
                    map.put("price",item.getPrice());

                    colRef.document(item.getKey()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                            }
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            Log.d("Snackers",itemsList.size() + " ");
            return itemsList.size();
        }

        public class ItemsHolder extends RecyclerView.ViewHolder {

            public TextView nameTv,priceTv,qtyTv;
            Button addBt;
            RelativeLayout qtyRl;
            ImageView addQtyBt,subBt;

            public ItemsHolder(@NonNull View itemView) {
                super(itemView);

                nameTv = (TextView) itemView.findViewById(R.id.list_item_name);
                priceTv = itemView.findViewById(R.id.priceTv);
                addBt = itemView.findViewById(R.id.addItemBt);
                addQtyBt = itemView.findViewById(R.id.addQtyBt);
                subBt = itemView.findViewById(R.id.subBt);
                qtyTv = itemView.findViewById(R.id.qtyTv);
                qtyRl = itemView.findViewById(R.id.qtyRl);
            }
        }
    }

    class SortBtName implements Comparator<Item> {
        public int compare(Item a, Item b){
            return a.getName().compareTo(b.getName());
        }
    }

    private class ItemRecHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        TextView priceTv,itemNameTv;
        TextView qtyTv;
        Button addBt;
        RelativeLayout qtyRl;
        ImageView addQtyBt,subBt;

        public ItemRecHolder(@NonNull View itemView) {
            super(itemView);

            itemImage = itemView.findViewById(R.id.imageItem);
            itemNameTv = itemView.findViewById(R.id.itemName);
            priceTv = itemView.findViewById(R.id.priceTv);
            addBt = itemView.findViewById(R.id.addItemBt);
            addQtyBt = itemView.findViewById(R.id.addQtyBt);
            subBt = itemView.findViewById(R.id.subBt);
            qtyTv = itemView.findViewById(R.id.qtyTv);
            qtyRl = itemView.findViewById(R.id.qtyRl);
        }
    }
}
