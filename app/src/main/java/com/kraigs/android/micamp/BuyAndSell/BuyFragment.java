package com.kraigs.android.micamp.BuyAndSell;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.BuyAndSell.AddAdActivity;
import com.kraigs.android.micamp.BuyAndSell.Buy;
import com.kraigs.android.micamp.BuyAndSell.ItemDescActivity;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

public class BuyFragment extends Fragment {

    View v;
    RecyclerView buyItemRv;
    GridLayoutManager llm;
    private final List<Buy> buyList = new ArrayList<>();
    Query buyRef;
    RecyclerView.Adapter<ItemHolder> adapter;
    FloatingActionButton addItemFb;

    public BuyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_buy, container, false);

        addItemFb = v.findViewById(R.id.addItemFb);
        addItemFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddAdActivity.class);
                startActivity(intent);
            }
        });

        buyItemRv = v.findViewById(R.id.buySellRv);
        llm = new GridLayoutManager(getActivity(), 2);
        buyItemRv.setLayoutManager(llm);

        buyRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("BuynSell").collection("Buy").orderBy("timestamp", Query.Direction.DESCENDING);

        buyRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    for (QueryDocumentSnapshot postSnapshot : task.getResult()) {
                        Buy buy = postSnapshot.toObject(Buy.class);
                        buyList.add(buy);
                    }

                    buyItemRv.setAdapter(adapter);

                }
            }
        });

        adapter = new RecyclerView.Adapter<ItemHolder>() {
            @NonNull
            @Override
            public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_buy_item, parent, false);
                return new ItemHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull ItemHolder holder, int position) {

                Buy model = buyList.get(position);
                holder.delete.setVisibility(View.GONE);
                Picasso.get().load(model.getImage()).into(holder.image);
                holder.priceTv.setText("Rs. " + model.getPrice());
                holder.itemNameTv.setText(model.getItemName());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ItemDescActivity.class);
                        intent.putExtra("adId", model.getKey());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return buyList.size();
            }
        };

        return v;
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView image,delete;
        TextView priceTv, itemNameTv;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.item_image);
            priceTv = itemView.findViewById(R.id.item_price);
            itemNameTv = itemView.findViewById(R.id.item_info);
            delete = itemView.findViewById(R.id.deleteAd);
        }
    }

}
