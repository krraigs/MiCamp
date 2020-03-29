package com.kraigs.android.micamp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.kraigs.android.micamp.BuyAndSell.AdsHistoryActivity;
import com.kraigs.android.micamp.Chat.ChatActivity;
import com.kraigs.android.micamp.Home.Blog.BloghistoryActivity;
import com.kraigs.android.micamp.Home.Books.BooksPdfActivity;
import com.kraigs.android.micamp.Home.Updates.UpdatesActivity;
import com.kraigs.android.micamp.Home.ExploreFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kraigs.android.micamp.Chat.ChatFragment;
import com.kraigs.android.micamp.Drawer.ContactActivity;
import com.kraigs.android.micamp.Home.Calendar.CalendarNewActivity;
import com.kraigs.android.micamp.Hostel.HostelActivity;
import com.kraigs.android.micamp.Login.LoginActivity;
import com.kraigs.android.micamp.Mentor.MentorsFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kraigs.android.micamp.UserProfile.ComplaintActivity;
import com.kraigs.android.micamp.UserProfile.EditProfileActivity;
import com.kraigs.android.micamp.UserProfile.MyOrdersActivity;
import com.kraigs.android.micamp.UserProfile.ProfileActivity;
import com.kraigs.android.micamp.extras.ErrorActivity;
import com.kraigs.android.micamp.extras.NotificationActivity;
import com.kraigs.android.micamp.extras.SharedPrefs;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_REQUEST_CODE = 101;
    private BottomNavigationView bottomNavigation;
    private FirebaseAuth mFirebaseAuth;
    private String currentUserId;
    private DatabaseReference rootRef;
    private CircleImageView profilePicDrawer;
    private TextView userNameDrawer;
    private TextView userIDDrawer;
    FirebaseAuth mAuth;
    CollectionReference userCol;
    LinearLayout toolbarLl;
    TextView exploreTv;
    Fragment fragment = new HomeFragment();
    Toolbar toolbar;
    AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarLl = findViewById(R.id.toolbarLl);
        exploreTv = findViewById(R.id.exploreTv);

        rootRef = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();
        userCol = FirebaseFirestore.getInstance().collection("User");

        Window window = getWindow();
        Drawable background = getResources().getDrawable(R.drawable.round_main_toolbar);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);

        updateUserStatus();
        setUpCollegeSpinner();


        setSupportActionBar(toolbar);

        checkUpdatesndMaintenance();
        drawerImplementation();

        bottomNavigationImplement();
        backToMainActivity();
        click();

    }

//    private void appUpdates() {
//
//        // Creates instance of the manager.
//         appUpdateManager = AppUpdateManagerFactory.create(this);
//
//// Returns an intent object that you use to check for an update.
//        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
//
//// Checks that the platform will allow the specified type of update.
//        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
//            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
//                    // For a flexible update, use AppUpdateType.FLEXIBLE
//                    && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
//                // Request the update.
//
//                try {
//                    appUpdateManager.startUpdateFlowForResult(
//                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
//                            appUpdateInfo,
//                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
//                            IMMEDIATE,
//                            // The current activity making the update request.
//                            this,
//                            // Include a request code to later monitor this update request.
//                            MY_REQUEST_CODE);
//                } catch (IntentSender.SendIntentException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        });
//
//        // Create a listener to track request state updates.
//
//        InstallStateUpdatedListener installStateUpdatedListener = state -> {
//            if (state.installStatus() == InstallStatus.DOWNLOADING){
////                long bytesDownloaded = state.bytesDownloaded();
////                long totalBytesToDownload = state.totalBytesToDownload();
//                Toast.makeText(this, "Downloading", Toast.LENGTH_SHORT).show();
//
//            } else if (state.installStatus() == InstallStatus.DOWNLOADED){
//                Toast.makeText(this, "Downloaded", Toast.LENGTH_SHORT).show();
//                popupSnackbarForCompleteUpdate();
//            } else if (state.installStatus() == InstallStatus.INSTALLED){
//                if (appUpdateManager != null) {
//                    Toast.makeText(this, "Installed", Toast.LENGTH_SHORT).show();
////                    appUpdateManager.unregisterListener(installStateUpdatedListener);
//                }
//
//            } else {
//                Log.i("AppUpdate", "InstallStateUpdatedListener: state: " + state.installStatus());
//            }
//        };
//
//
//// Before starting an update, register a listener for updates.
//        appUpdateManager.registerListener(installStateUpdatedListener);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        appUpdateManager
//                .getAppUpdateInfo()
//                .addOnSuccessListener(
//                        appUpdateInfo -> {
//
//                            if (appUpdateInfo.updateAvailability()
//                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
//                                // If an in-app update is already running, resume the update.
//                                try {
//                                    appUpdateManager.startUpdateFlowForResult(
//                                            appUpdateInfo,
//                                            IMMEDIATE,
//                                            this,
//                                            MY_REQUEST_CODE);
//                                } catch (IntentSender.SendIntentException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == MY_REQUEST_CODE) {
//            if (resultCode != RESULT_OK) {
//                Log.d("AppUpdate","Update flow failed! Result code: " + resultCode);
//                // If the update is cancelled or fails,
//                // you can request to start the update again.
//            }
//        }
//    }
//

    private void click(){
        LinearLayout downloadsLl,booksLl,hostelsLl;

        hostelsLl = findViewById(R.id.hostelLl);
        downloadsLl = findViewById(R.id.downloadsLl);
        booksLl = findViewById(R.id.booksLl);

        booksLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isReadStoragePermissionGranted() && isWriteStoragePermissionGranted()) {
                    Intent intent = new Intent(MainActivity.this, BooksPdfActivity.class);
                    startActivity(intent);
                }
            }
        });

        hostelsLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostel();
            }
        });

        downloadsLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isReadStoragePermissionGranted() && isWriteStoragePermissionGranted()) {
                    Intent intent = new Intent(MainActivity.this, UpdatesActivity.class);
                    startActivity(intent);
                }

            }
        });

    }

    private void checkUpdatesndMaintenance(){
        long versionCodeExist = BuildConfig.VERSION_CODE;

        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference().child("appExtras");
        updateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long versionCode = (long) dataSnapshot.child("versionCode").getValue();
                if (versionCode > versionCodeExist) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("Update").setMessage("Now new update of our app is available. Download to enjoy more.").setPositiveButton("Check", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse("market://details?id=" + getBaseContext().getPackageName());
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            try {
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName())));
                            }
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        DatabaseReference errorRef = FirebaseDatabase.getInstance().getReference().child("appExtras");
        errorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("maintenance")){
                        String maintenance = dataSnapshot.child("maintenance").getValue().toString();
                        if (maintenance.equals("true")){
                            Intent intent = new Intent(MainActivity.this, ErrorActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void drawerImplementation(){

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        userNameDrawer = (TextView) header.findViewById(R.id.userNameDrawer);
        userIDDrawer = (TextView) header.findViewById(R.id.userIDDrawer);
        profilePicDrawer = (CircleImageView) header.findViewById(R.id.userProfilePicDrawer);

        header.findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                intent.putExtra("purpose", "edit");
                startActivity(intent);
            }
        });

        userCol.document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot.exists()){
                    String name = snapshot.get("name").toString();
                    String userID = snapshot.get("userID").toString();

                    userNameDrawer.setText(name);
                    userIDDrawer.setText(userID);

                    if (snapshot.contains("image")){
                        String image = snapshot.get("image").toString();
                        Picasso.get().load(image).placeholder(R.drawable.user_profile_image).into(profilePicDrawer);
                    }
                }
            }
        });

    }

    private void bottomNavigationImplement() {
        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_home:
                        toolbarLl.setVisibility(View.VISIBLE);
                        exploreTv.setVisibility(View.GONE);
                        fragment = new HomeFragment();
                        break;
                    case R.id.action_events:
                        toolbarLl.setVisibility(View.GONE);
                        exploreTv.setVisibility(View.GONE);
                        fragment = new MentorsFragment();
                        break;
                    case R.id.action_chat:
                        toolbarLl.setVisibility(View.GONE);
                        exploreTv.setVisibility(View.GONE);
                        fragment = new ChatFragment();
                        break;
                    case R.id.action_explore:
                        toolbarLl.setVisibility(View.GONE);
                        exploreTv.setVisibility(View.GONE);
                        fragment = new ExploreFragment();
                        break;

                }
                return loadFragment(fragment);
            }
        });

    }

    private void backToMainActivity(){

        if (getIntent().getStringExtra("type") != null){
            String type = getIntent().getStringExtra("type");
            switch (type) {
                case "home":
                    bottomNavigation.setSelectedItemId(R.id.action_home);
                    fragment = new HomeFragment();
                    break;
                case "mentors":
                    bottomNavigation.setSelectedItemId(R.id.action_events);
                    toolbarLl.setVisibility(View.VISIBLE);
                    exploreTv.setVisibility(View.GONE);
                    toolbarLl.setVisibility(View.GONE);
                    exploreTv.setVisibility(View.GONE);
                    fragment = new MentorsFragment();
                    break;
                case "explore":
                    bottomNavigation.setSelectedItemId(R.id.action_explore);
                    toolbarLl.setVisibility(View.GONE);
                    exploreTv.setVisibility(View.GONE);
                    fragment = new ExploreFragment();
                    break;
                case "chat":
                    bottomNavigation.setSelectedItemId(R.id.action_chat);
                    toolbarLl.setVisibility(View.GONE);
                    exploreTv.setVisibility(View.GONE);
                    fragment = new ChatFragment();
                    break;
            }

            loadFragment(fragment);
        } else {
            fragment = new HomeFragment();
            loadFragment(fragment);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;

    }

    private void setUpCollegeSpinner() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        Spinner collegeSpinner = findViewById(R.id.collegeSp);
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_college, R.layout.spinner_item);
        categorySpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        collegeSpinner.setAdapter(categorySpinnerAdapter);

        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position ==1){
                    new SharedPrefs("Nit");
                    editor.putString("college","Nit");

                } else if (position==2)
                {
                    editor.putString("college",(String) parent.getItemAtPosition(position));
                    new SharedPrefs((String) parent.getItemAtPosition(position));
                }

                editor.apply();

                int id2 = bottomNavigation.getSelectedItemId();
                switch (id2) {
                    case R.id.action_home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.action_events:
                        fragment = new MentorsFragment();
                        break;
                    case R.id.action_chat:
                        fragment = new ChatFragment();
                        break;
                    case R.id.action_explore:
                        fragment = new ExploreFragment();
                        break;

                }
                loadFragment(fragment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String college = SharedPrefs.getCollege();

        if(college != null){

            switch (college) {
                case "Select your college":
                    collegeSpinner.setSelection(0);
                    break;
                case "Nit":
                    collegeSpinner.setSelection(1);
                    break;
                default:
                    collegeSpinner.setSelection(2);
                    break;
            }
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (doubleBackToExitPressedOnce) {

            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }


        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Press again to leave.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.log_out) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Logout").setMessage("Do you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    GoogleSignInClient mGoogleSignInClient;
                    mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i2);
                            finish();
                            FirebaseAuth.getInstance().signOut();
                        }
                    });
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
        }else if (id == R.id.contact_drawer) {
            Intent intent = new Intent(MainActivity.this, ContactActivity.class);
            startActivity(intent);
        } else if (id == R.id.calendar_drawer) {
            if (isReadStoragePermissionGranted() && isWriteStoragePermissionGranted()) {
                Intent intent = new Intent(MainActivity.this, CalendarNewActivity.class);
                startActivity(intent);
            }
        }
        else if (id == R.id.blogs_drawer) {
            Intent intent = new Intent(MainActivity.this, BloghistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.ads_drawer) {
            Intent intent = new Intent(MainActivity.this, AdsHistoryActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.contact_us_drawer) {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("visit_user_id", "S7bX2u7HCjOIPvGSG0GTnw7gy4R2");
            startActivity(intent);
        }else if (id == R.id.ur_order) {
            Intent intent = new Intent(MainActivity.this, MyOrdersActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.ic_user) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.ic_noti) {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            intent.putExtra("type","main");
            startActivity(intent);
        }

        return true;
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else {
            return true;
        }
    }

    private void updateUserStatus() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("online", "true");

        rootRef.child("OfflineUsers").child(currentUserId).updateChildren(map);
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("timestamp",ServerValue.TIMESTAMP);
        onlineStateMap.put("online", "false");
        rootRef.child("OfflineUsers").child(currentUserId).onDisconnect().setValue(onlineStateMap);


    }

    private void hostel(){
        BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.zlayout_hostel_category,null);
        dialog.setContentView(sheetView);

        RelativeLayout messOfRl = sheetView.findViewById(R.id.messOffRl);
        RelativeLayout complaintRl = sheetView.findViewById(R.id.complaintRl);
        RelativeLayout leaveRl = sheetView.findViewById(R.id.leaveRl);

        complaintRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, ComplaintActivity.class);
                intent.putExtra("type","hostel");
                startActivity(intent);
            }
        });

        messOfRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, HostelActivity.class);
                intent.putExtra("type","mess");
                startActivity(intent);
            }
        });

        leaveRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, HostelActivity.class);
                intent.putExtra("type","leave");
                startActivity(intent);
            }
        });

        dialog.show();
    }


}
