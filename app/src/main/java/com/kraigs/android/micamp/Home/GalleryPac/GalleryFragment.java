package com.kraigs.android.micamp.Home.GalleryPac;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.SharedPrefs;

/**
 * A simple {@link Fragment} subclass.
 */

public class GalleryFragment extends Fragment {

    private GalleryAdapter adapter;
    View v;

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_gallery, container, false);

        setUpRecyclerAdapter();

        return v;
    }

    private void setUpRecyclerAdapter() {
        Query query = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Gallery")
                .collection("events");

        FirestoreRecyclerOptions<Gallery> options = new FirestoreRecyclerOptions.Builder<Gallery>()
                .setQuery(query,Gallery.class)
                .build();

        adapter = new GalleryAdapter(options);
        RecyclerView rv = v.findViewById(R.id.galleryEventsRv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new GridLayoutManager(getActivity(),2));
        adapter.notifyDataSetChanged();
        rv.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
