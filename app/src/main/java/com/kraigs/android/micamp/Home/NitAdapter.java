package com.kraigs.android.micamp.Home;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kraigs.android.micamp.Chat.ImageViewrActivity;
import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

public class NitAdapter extends FirestoreRecyclerAdapter<Nit,NitAdapter.NitHolder> {

    private Context mContext;
    public NitAdapter(@NonNull FirestoreRecyclerOptions<Nit> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final NitHolder holder, int position, @NonNull final Nit model) {

        holder.descriptionCardHome.setText(model.getmCardDescription());
        Picasso.get()
                .load(model.getmCardPhotoUrl())
                .into(holder.cardHome);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ImageViewrActivity.class);
                intent.putExtra("url",model.getmCardPhotoUrl());
                intent.putExtra("description",model.getmCardDescription());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public NitHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_cards,
                parent,false);
        return new NitHolder(v);
    }

    class NitHolder extends RecyclerView.ViewHolder {
        TextView descriptionCardHome;
        ImageView cardHome;
        public NitHolder(@NonNull View itemView) {
            super(itemView);
            descriptionCardHome = itemView.findViewById(R.id.card_description);
            cardHome = itemView.findViewById(R.id.card_image);
        }
    }
}
