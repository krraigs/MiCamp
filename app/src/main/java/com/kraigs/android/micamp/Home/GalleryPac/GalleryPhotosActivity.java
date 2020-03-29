package com.kraigs.android.micamp.Home.GalleryPac;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.extras.SharedPrefs;

public class GalleryPhotosActivity extends AppCompatActivity {

    private GalleryPhotoAdapter adapterPhotos;
    private String eventName;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_photos);
        getSupportActionBar().setTitle("Gallery");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        eventName = getIntent().getStringExtra("event");

        setUpRecyclerPhotoAdapter();

    }

    private void setUpRecyclerPhotoAdapter() {
        Query query = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Gallery")
                .collection(eventName);
        FirestoreRecyclerOptions<Gallery> options = new FirestoreRecyclerOptions.Builder<Gallery>()
                .setQuery(query,Gallery.class)
                .build();
        adapterPhotos = new GalleryPhotoAdapter(options);
        RecyclerView rv = findViewById(R.id.galleryPhotoRv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new GridLayoutManager(this,3));
        adapterPhotos.notifyDataSetChanged();
        rv.setAdapter(adapterPhotos);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterPhotos.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterPhotos.stopListening();
    }
}
