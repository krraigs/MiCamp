package com.kraigs.android.micamp.Home.Events;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kraigs.android.micamp.R;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Events");

        setUpTabs();

    }

    private void setUpTabs() {

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout viewPagerTab = (TabLayout) findViewById(R.id.tab);

        FragmentPagerItems pages = new FragmentPagerItems(EventsActivity.this);
        pages.add(FragmentPagerItem.of("Upcoming Events", EventsFragment.class));
        pages.add(FragmentPagerItem.of("Featured Events", EventsFragment.class));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), pages);

        viewPager.setAdapter(adapter);
        viewPagerTab.setupWithViewPager(viewPager);
    }
}
