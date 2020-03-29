package com.kraigs.android.micamp.Order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class CategoryAdapter extends ExpandableRecyclerViewAdapter<CategoryViewHolder,ItemViewHolder> {

    CollectionReference colRef;
    String currentUserID;

    public CategoryAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public CategoryViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        colRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("Orders").collection(currentUserID);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.zunit_food_category, parent, false);
        return new CategoryViewHolder(view);

    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.zunit_food_items, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ItemViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {

        final Item item = ((Category) group).getItems().get(childIndex);
        holder.nameTv.setText(item.getName());
        holder.priceTv.setText("Rs. " + item.getPrice());

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

    private void halfDialog(ItemViewHolder holder,Item item) {
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

    @Override
    public void onBindGroupViewHolder(CategoryViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        holder.setGenreTitle(group);
    }

}
