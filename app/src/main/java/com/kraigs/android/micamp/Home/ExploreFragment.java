package com.kraigs.android.micamp.Home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.kraigs.android.micamp.Home.Blog.BlogsFragment;
import com.kraigs.android.micamp.BuyAndSell.BuyFragment;
import com.kraigs.android.micamp.Home.GalleryPac.GalleryFragment;
import com.kraigs.android.micamp.Home.Vedio.VedioListFragment;
import com.kraigs.android.micamp.LostAndFoundActivity.FoundFragment;
import com.kraigs.android.micamp.LostAndFoundActivity.LostFragment;
import com.kraigs.android.micamp.R;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

/**
 * A simple {@link Fragment} subclass.
 */

public class ExploreFragment extends Fragment {

    View v;
    CollectionReference galleryRef,blogsRef;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_explore, container, false);

        setUpTabs();

//
//        gallery();
//
//        blogs();
//
//        clickAll();
//
//        academics();

        return v;

    }

    private void setUpTabs() {

        ViewGroup tab = (ViewGroup) v.findViewById(R.id.tab);
        tab.addView(LayoutInflater.from(getActivity()).inflate(R.layout.zlayout_smart_tab, tab, false));

        SmartTabLayout viewPagerTab = (SmartTabLayout) v.findViewById(R.id.viewpagertab);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);

        FragmentPagerItems pages = new FragmentPagerItems(getActivity());
        pages.add(FragmentPagerItem.of("Vedios", VedioListFragment.class));
        pages.add(FragmentPagerItem.of("Buy & Sell", BuyFragment.class));
        pages.add(FragmentPagerItem.of("Blogs", BlogsFragment.class));
        pages.add(FragmentPagerItem.of("Lost", LostFragment.class));
        pages.add(FragmentPagerItem.of("Found", FoundFragment.class));
        pages.add(FragmentPagerItem.of("Gallery", GalleryFragment.class));


        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), pages);

        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);
    }
//    private void blogs() {
//
//        TextView authorTv = v.findViewById(R.id.authorTv);
//        TextView authorTv2 = v.findViewById(R.id.authorTv2);
//        TextView authorTv3 = v.findViewById(R.id.authorTv3);
//        TextView authorTv4 = v.findViewById(R.id.authorTv4);
//
//        TextView titleTv = v.findViewById(R.id.topicTv);
//        TextView titleTv2 = v.findViewById(R.id.topicTv2);
//        TextView titleTv3 = v.findViewById(R.id.topicTv3);
//        TextView titleTv4 = v.findViewById(R.id.topicTv4);
//
//        Query blogsRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Home").collection("Blog").orderBy("timestamp", Query.Direction.DESCENDING).limit(4);
//        blogsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()){
//                    if (task.getResult() !=null && !task.getResult().isEmpty()){
//                        String author = task.getResult().getDocuments().get(0).get("mAuthor").toString();
//                        String author2 = task.getResult().getDocuments().get(1).get("mAuthor").toString();
//                        String author3 = task.getResult().getDocuments().get(2).get("mAuthor").toString();
//                        String author4 = task.getResult().getDocuments().get(3).get("mAuthor").toString();
//
//                        String title = task.getResult().getDocuments().get(0).get("mTitle").toString();
//                        String title2 = task.getResult().getDocuments().get(1).get("mTitle").toString();
//                        String title3 = task.getResult().getDocuments().get(2).get("mTitle").toString();
//                        String title4 = task.getResult().getDocuments().get(3).get("mTitle").toString();
//
//                        String key = task.getResult().getDocuments().get(0).getId();
//                        String key2 = task.getResult().getDocuments().get(1).getId();
//                        String key3 = task.getResult().getDocuments().get(2).getId();
//                        String key4 = task.getResult().getDocuments().get(3).getId();
//
//                        authorTv.setText("By " + author);
//                        authorTv2.setText("By " + author2);
//                        authorTv3.setText("By " + author3);
//                        authorTv4.setText("By " + author4);
//
//                        titleTv.setText(title);
//                        titleTv2.setText(title2);
//                        titleTv3.setText(title3);
//                        titleTv4.setText(title4);
//
//                        v.findViewById(R.id.blogcard).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent detailActivity = new Intent(getActivity(), BlogDetailActivity.class);
//                                detailActivity.putExtra("BlogID", key);
//                                startActivity(detailActivity);
//                            }
//                        });
//
//                        v.findViewById(R.id.blogcard2).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent detailActivity = new Intent(getActivity(), BlogDetailActivity.class);
//                                detailActivity.putExtra("BlogID", key2);
//                                startActivity(detailActivity);
//                            }
//                        });
//
//                        v.findViewById(R.id.blogcard3).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent detailActivity = new Intent(getActivity(), BlogDetailActivity.class);
//                                detailActivity.putExtra("BlogID", key3);
//                                startActivity(detailActivity);
//                            }
//                        });
//
//                        v.findViewById(R.id.blogcard4).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent detailActivity = new Intent(getActivity(), BlogDetailActivity.class);
//                                detailActivity.putExtra("BlogID", key4);
//                                startActivity(detailActivity);
//                            }
//                        });
//                    } else{
//                        v.findViewById(R.id.blogsCv).setVisibility(View.GONE);
//                    }
//
//                }
//            }
//        });
//    }
//
//    private void gallery() {
//
//        ImageView imagView = v.findViewById(R.id.gallery_event_photo);
//        ImageView imagView2 = v.findViewById(R.id.gallery_event_photo2);
//        ImageView imagView3 = v.findViewById(R.id.gallery_event_photo3);
//        ImageView imagView4 = v.findViewById(R.id.gallery_event_photo4);
//
//        TextView nameTv = v.findViewById(R.id.gallery_event_name);
//        TextView nameTv2 = v.findViewById(R.id.gallery_event_name2);
//        TextView nameTv3 = v.findViewById(R.id.gallery_event_name3);
//        TextView nameTv4 = v.findViewById(R.id.gallery_event_name4);
//
//        Query galleryRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Gallery").collection("events").limit(4);
//        galleryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()){
//                    if (task.getResult() != null && !task.getResult().isEmpty()){
//                        String image1 = task.getResult().getDocuments().get(0).get("ePhoto").toString();
//                        String image2 = task.getResult().getDocuments().get(1).get("ePhoto").toString();
//                        String image3 = task.getResult().getDocuments().get(2).get("ePhoto").toString();
//                        String image4 = task.getResult().getDocuments().get(3).get("ePhoto").toString();
//
//                        Picasso.get().load(image1).into(imagView);
//                        Picasso.get().load(image2).into(imagView2);
//                        Picasso.get().load(image3).into(imagView3);
//                        Picasso.get().load(image4).into(imagView4);
//
//
//                        String name = task.getResult().getDocuments().get(0).get("name").toString();
//                        String name2 = task.getResult().getDocuments().get(1).get("name").toString();
//                        String name3 = task.getResult().getDocuments().get(2).get("name").toString();
//                        String name4 = task.getResult().getDocuments().get(3).get("name").toString();
//
//                        nameTv.setText(name);
//                        nameTv2.setText(name2);
//                        nameTv3.setText(name3);
//                        nameTv4.setText(name4);
//
//                        v.findViewById(R.id.galleryCv1).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent intent = new Intent(getActivity(), GalleryPhotosActivity.class);
//                                intent.putExtra("event",name);
//                                startActivity(intent);
//                            }
//                        });
//
//                        v.findViewById(R.id.galleryCv2).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent intent = new Intent(getActivity(), GalleryPhotosActivity.class);
//                                intent.putExtra("event",name2);
//                                startActivity(intent);
//                            }
//                        });
//
//                        v.findViewById(R.id.galleryCv3).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent intent = new Intent(getActivity(), GalleryPhotosActivity.class);
//                                intent.putExtra("event",name3);
//                                startActivity(intent);
//                            }
//                        });
//
//                        v.findViewById(R.id.galleryCv4).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent intent = new Intent(getActivity(), GalleryPhotosActivity.class);
//                                intent.putExtra("event",name4);
//                                startActivity(intent);
//                            }
//                        });
//                    } else{
//                        v.findViewById(R.id.galleryCv).setVisibility(View.GONE);
//                    }
//
//                }
//            }
//        });
//    }
//
//    private void clickAll() {
//
//        v.findViewById(R.id.seeAllBlogs).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(getActivity(), BlogActivity.class);
//                startActivity(intent);
//
//            }
//        });
//
//        v.findViewById(R.id.seeAllGallery).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), GalleryEventsActivity.class);
//                startActivity(intent);
//            }
//        });
////
//
//        v.findViewById(R.id.clubsLl).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), ClubsActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        v.findViewById(R.id.lostLl).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), LostFoundActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        v.findViewById(R.id.buyLl).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), BuyActivity.class);
//                startActivity(intent);
//            }
//        });
//
//    }
//
//    private void academics() {
//
//        v.findViewById(R.id.cse).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                intent("Computer Science","http://www.nitj.ac.in/cse/");
//
//            }
//        });
//
//        v.findViewById(R.id.ice).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Instrumentation and Control");
//            }
//        });
//
//        v.findViewById(R.id.ece).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Electronics and Comm","http://www.nitj.ac.in/ece/");
//            }
//        });
//
//        v.findViewById(R.id.ee).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Electrical");
//            }
//        });
//
//        v.findViewById(R.id.me).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Mechanical","http://www.nitj.ac.in/mechanical/");
//            }
//        });
//
//        v.findViewById(R.id.it).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Information Technology");
//            }
//        });
//
//        v.findViewById(R.id.che).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Chemical");
//            }
//        });
//
//        v.findViewById(R.id.ipe).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Industrial and Production","http://www.nitj.ac.in/ipe/");
//            }
//        });
//
//        v.findViewById(R.id.civil).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Civil");
//            }
//        });
//
//        v.findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Biotech","http://www.nitj.ac.in/biotech/");
//            }
//        });
//
//        v.findViewById(R.id.textile).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Textile","http://www.nitj.ac.in/textile-technology.pdf");
//            }
//        });
//
//        v.findViewById(R.id.humanities).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Humanities");
//            }
//        });
//
//        v.findViewById(R.id.chemistry).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Chemistry","http://www.nitj.ac.in/chemistry/");
//
//            }
//        });
//
//        v.findViewById(R.id.physics).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Physics","http://www.nitj.ac.in/physics/");
//
//            }
//        });
//
//        v.findViewById(R.id.maths).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                intent("Mathematics");
//            }
//        });
//
//    }
//
//    public void intent(String branch){
//        Intent intent = new Intent(getActivity(), BranchesActivity.class);
//        intent.putExtra("branch",branch);
//        startActivity(intent);
//    }
//
//    public void intent(String branch,String webUrl){
//        Intent intent = new Intent(getActivity(), BranchesActivity.class);
//        intent.putExtra("branch",branch);
//        intent.putExtra("webUrl",webUrl);
//        startActivity(intent);
//    }

}
