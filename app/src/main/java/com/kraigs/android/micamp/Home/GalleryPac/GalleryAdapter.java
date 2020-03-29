package com.kraigs.android.micamp.Home.GalleryPac;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

public class GalleryAdapter extends FirestoreRecyclerAdapter<Gallery,GalleryAdapter.GalleryHolder> {

    public GalleryAdapter(@NonNull FirestoreRecyclerOptions<Gallery> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final GalleryHolder holder, int position, @NonNull final Gallery model) {
        Picasso.get()
                .load(model.getePhoto())
                .into(holder.eventPhoto);
        holder.eventName.setText(model.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),GalleryPhotosActivity.class);
                intent.putExtra("event",model.getName());
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
    public GalleryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gallery_events,viewGroup,false);

        return new GalleryHolder(v);
    }

    public class GalleryHolder extends RecyclerView.ViewHolder {
        ImageView eventPhoto;
        private TextView eventName;

        public GalleryHolder(@NonNull View itemView) {
            super(itemView);
            eventPhoto = itemView.findViewById(R.id.gallery_event_photo);
            eventName = itemView.findViewById(R.id.gallery_event_name);
        }
    }
}
