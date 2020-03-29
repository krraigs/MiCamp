package com.kraigs.android.micamp.Home.GalleryPac;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kraigs.android.micamp.Chat.ImageViewrActivity;
import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

public class GalleryPhotoAdapter extends FirestoreRecyclerAdapter<Gallery,GalleryPhotoAdapter.GalleryPhotoViewHolder> {
    public GalleryPhotoAdapter(@NonNull FirestoreRecyclerOptions<Gallery> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final GalleryPhotoViewHolder holder, int position, @NonNull final Gallery model) {
        Picasso.get().load(model.getPhotos())
                .into(holder.galleryPhoto);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ImageViewrActivity.class);
                intent.putExtra("url",model.getPhotos());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public GalleryPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gallery_photo_list,viewGroup,false);
        return new GalleryPhotoViewHolder(v);    }

    public class GalleryPhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView galleryPhoto;

        public GalleryPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            galleryPhoto = (ImageView) itemView.findViewById(R.id.galleryPhoto);
        }
    }
}
