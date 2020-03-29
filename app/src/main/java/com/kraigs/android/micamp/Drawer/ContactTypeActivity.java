package com.kraigs.android.micamp.Drawer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kraigs.android.micamp.extras.SharedPrefs;

public class ContactTypeActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ContactAdapter adapter;
    private RecyclerView contactTypeRv;
    private static String type;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_type);
        getSupportActionBar().setTitle("Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        type = getIntent().getStringExtra("type");

        contactTypeRv = (RecyclerView) findViewById(R.id.contactTypeRv);
        contactTypeRv.setHasFixedSize(true);
        contactTypeRv.setLayoutManager(new LinearLayoutManager(this));

        setUpRecyclerAdapter(SharedPrefs.getCollege());

    }

    private void setUpRecyclerAdapter(String college) {

        Query query = db.collection(college).document("Contacts").collection(type).orderBy("timestamp");
        Log.d("Contacts","in setUpRecycler");
        FirestoreRecyclerOptions<Contact> options = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(query, Contact.class)
                .build();
        adapter = new ContactAdapter(options);
        adapter.notifyDataSetChanged();
        contactTypeRv.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_spinner,menu);

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner)item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.array_college,R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        if (SharedPrefs.getCollege().equals("Nit"))
            spinner.setSelection(0);
        else
            spinner.setSelection(1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d("Contacts",position + " clicked");
                if (position == 0){
                    setUpRecyclerAdapter(SharedPrefs.getCollege());
                } else if (position == 1)
                    setUpRecyclerAdapter("Nit");
                else
                    setUpRecyclerAdapter((String) parent.getItemAtPosition(position));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return true;
    }

}
