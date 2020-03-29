package com.kraigs.android.micamp.Mentor;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.UserProfile.RequestsFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MentorsFragment extends Fragment {

   View v;
    public MentorsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v =  inflater.inflate(R.layout.fragment_mentors, container, false);

         setUpTabs();

         FloatingActionButton fab  = v.findViewById(R.id.findMentorFab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ConnectMentorActivity.class);
                startActivity(intent);
            }
        });

        return v;

    }

    private void setUpTabs() {

        ViewGroup tab = (ViewGroup) v.findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(getActivity()).inflate(R.layout.zlayout_smart_tab, tab, false));

        SmartTabLayout viewPagerTab = (SmartTabLayout) v.findViewById(R.id.viewpagertab);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);

        FragmentPagerItems pages = new FragmentPagerItems(getActivity());
        pages.add(FragmentPagerItem.of("Android", MentorsCategoryFragment.class));
        pages.add(FragmentPagerItem.of("Web", MentorsCategoryFragment.class));
        pages.add(FragmentPagerItem.of("ML", MentorsCategoryFragment.class));
        pages.add(FragmentPagerItem.of("Requests", RequestsFragment.class));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), pages);

        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);
    }

}
