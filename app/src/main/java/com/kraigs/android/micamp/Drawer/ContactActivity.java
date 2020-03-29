package com.kraigs.android.micamp.Drawer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.kraigs.android.micamp.R;

public class ContactActivity extends AppCompatActivity {

    private CardView administrationType;
    private CardView facultyType;
    private CardView staffType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getSupportActionBar().setTitle("Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        administrationType = (CardView) findViewById(R.id.administration_type);
        facultyType = (CardView) findViewById(R.id.faculty_type);
        staffType = (CardView) findViewById(R.id.staff_type);

        administrationType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactActivity.this,ContactTypeActivity.class);
                intent.putExtra("type","administration");
                startActivity(intent);
            }
        });

        facultyType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactActivity.this,ContactTypeActivity.class);
                intent.putExtra("type","warden");
                startActivity(intent);
            }
        });

        staffType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactActivity.this,ContactTypeActivity.class);
                intent.putExtra("type","staff");
                startActivity(intent);
            }
        });
    }
}
