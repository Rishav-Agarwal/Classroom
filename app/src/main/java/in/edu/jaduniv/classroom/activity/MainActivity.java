package in.edu.jaduniv.classroom.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.fragment.ClassFragment;
import in.edu.jaduniv.classroom.fragment.NavigationDrawerNoClass;
import in.edu.jaduniv.classroom.object.CurrentUser;
import in.edu.jaduniv.classroom.object.JoinRequest;
import in.edu.jaduniv.classroom.other.GlideApp;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;

import static in.edu.jaduniv.classroom.utility.FirebaseUtils.PERMISSION_STORAGE;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1000;
    public static DatabaseReference referenceUsers;
    public static DatabaseReference referenceClasses;
    public static int navItemSelectedIndex = -1;
    private static DatabaseReference referenceJoinReq = null;
    FirebaseDatabase firebaseDatabase;
    DrawerLayout drawer;
    NavigationView navigationView;
    View navigationDrawerHeader;
    ImageView ivNavHeaderBackground, ivNavProfilePic;
    TextView tvUsername, tvPhoneNumber;
    Menu navigationMenu;
    MenuItem itemClasses;
    SubMenu classes;
    Handler mHandler;
    Toolbar toolbar;
    ArrayList<String> classFragmentTag;
    String navItemSelectedTag = "-1";
    private FirebaseAuth.AuthStateListener authStateListener = null;
    private FirebaseAnalytics analytics = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        classFragmentTag = new ArrayList<>();

        initiateVariables();        //Function call to initiate variables

        //Navigation menu
        navigationMenu = navigationView.getMenu();
        itemClasses = navigationMenu.getItem(0);
        classes = itemClasses.getSubMenu();

        setUpNavigationView();          //Setup the navigation drawer

        //If user is already in at least one class, open the first class
        if (classFragmentTag.size() > 0) {
            navItemSelectedIndex = 0;
            navItemSelectedTag = classFragmentTag.get(navItemSelectedIndex);
            loadFragment();
        } else {
            loadDefaultFragment();          //Loads a default page when user hasn't joined any class
        }

        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected()) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        if (authStateListener == null) {
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser firebaseUser = CurrentUser.getFirebaseUser();
                    if (firebaseUser != null) {
                        loadNavigationHeader();
                        loadClasses();
                    } else {
                        if (isNetworkConnected()) {
                            Intent loginIntent = new Intent(MainActivity.this, LoginInfoActivity.class);
                            startActivityForResult(loginIntent, LoginInfoActivity.RC_LOGIN_INFO);
                        } else {
                            Toast.makeText(getApplicationContext(), "Check your internet connection!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < classes.size(); ++i)
            classes.removeItem(i);
        navItemSelectedIndex = -1;
        classFragmentTag.clear();
        navItemSelectedTag = "-1";
    }

    //Function to setup toolbar
    public void setupToolbar() {
        toolbar = findViewById(R.id.app_main_toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (navItemSelectedIndex == -1)
                    return;
                String openedClassTag = navItemSelectedTag;
                Intent intent = new Intent(MainActivity.this, ClassInfo.class);
                intent.putExtra("class", openedClassTag);
                startActivity(intent);
            }
        });

        setSupportActionBar(toolbar);
    }

    //Function to initiate variables
    public void initiateVariables() {
        //Drawer
        drawer = findViewById(R.id.nav_drawer);
        navigationView = findViewById(R.id.nav_menu);
        navigationDrawerHeader = navigationView.getHeaderView(0);

        //Drawer header
        ivNavHeaderBackground = navigationDrawerHeader.findViewById(R.id.nav_header_bg_image);
        ivNavProfilePic = navigationDrawerHeader.findViewById(R.id.nav_profile_img);
        tvUsername = navigationDrawerHeader.findViewById(R.id.nav_username);
        tvPhoneNumber = navigationDrawerHeader.findViewById(R.id.nav_phone_number);

        firebaseDatabase = FirebaseUtils.getDatabase();
        referenceClasses = FirebaseUtils.getDatabaseReference().child("classes");
        referenceUsers = FirebaseUtils.getDatabaseReference().child("users");
        analytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, "Phone");
        bundle.putInt("request_code", requestCode);
        bundle.putInt("result_code", resultCode);
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else if (resultCode == RESULT_OK) {
                bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.METHOD, "Phone");
                try {
                    bundle.putString("name", CurrentUser.getInstance().getName());
                    bundle.putString("email", CurrentUser.getInstance().getEmail());
                    bundle.putString("phone", CurrentUser.getInstance().getPhone());
                    bundle.putString("uid", CurrentUser.getInstance().getUid());
                } finally {
                    analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                }
                //TODO: Update code for token
                CurrentUser.getInstance().setToken(FirebaseInstanceId.getInstance().getToken());
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", CurrentUser.getInstance().getName());
                userMap.put("email", CurrentUser.getInstance().getEmail());
                userMap.put("phone", CurrentUser.getInstance().getPhone());
                userMap.put("uid", CurrentUser.getInstance().getUid());
                userMap.put("token", CurrentUser.getInstance().getToken());
                referenceUsers.child(CurrentUser.getInstance().getPhone()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("Success", task.isSuccessful());
                        bundle.putBoolean("Cancel", task.isCanceled());
                        bundle.putBoolean("Complete", task.isComplete());
                        bundle.putString("Failure", task.getException() == null ? "null" : task.getException().getMessage());
                        analytics.logEvent("update_user", bundle);
                        loadNavigationHeader();
                        loadClasses();
                    }
                });
            }
        }
        if (requestCode == LoginInfoActivity.RC_LOGIN_INFO) {
            CurrentUser.getInstance().setName(data.getStringExtra(LoginInfoActivity.RESULT_NAME));
            CurrentUser.getInstance().setEmail(data.getStringExtra(LoginInfoActivity.RESULT_EMAIL));
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(true)
                    .setTheme(R.style.FirebaseTheme)
                    .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build()))
                    .build(), RC_SIGN_IN);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Do nothing and continue;
        } else {
            Toast.makeText(this, "Storage permission denied! :(\nWe need your consent to do that.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //Function to load default fragment in drawer layout
    public void loadDefaultFragment() {
        Fragment fragment = new NavigationDrawerNoClass();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.app_main_frame, fragment, navItemSelectedTag);
        fragmentTransaction.commit();
    }

    //Function to load class fragment
    public void loadFragment() {
        //Setting the opened class to be checked in the drawer
        for (int i = 0; i < classes.size(); ++i)
            classes.getItem(i).setChecked(false);
        classes.getItem(navItemSelectedIndex).setChecked(true);

        getSupportActionBar().setTitle(classes.getItem(navItemSelectedIndex).getTitle());

        //If user selects the opened class again, just close the drawer (don't do anything)
        if (getSupportFragmentManager().findFragmentByTag(navItemSelectedTag) != null) {
            drawer.closeDrawers();
            return;
        }

        //Runnable to replace the class fragment
        Runnable fragmentRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Fragment fragment = new ClassFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("class", navItemSelectedTag);
                    fragment.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.app_main_frame, fragment, navItemSelectedTag);
                    fragmentTransaction.commit();
                } catch (IllegalStateException e) {
                    Log.e("Navigation fragment err", e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        mHandler.post(fragmentRunnable);
        drawer.closeDrawers();      //Closes the navigation drawer
        invalidateOptionsMenu();    //Refresh
    }

    public void loadNavigationHeader() {
        GlideApp.with(this).load("https://atgbcentral.com/data/out/193/5690319-material-wallpaper.png").diskCacheStrategy(DiskCacheStrategy.ALL).into(ivNavHeaderBackground);
        //When connected with firebase, load user data into navigation header
        referenceUsers.child(CurrentUser.getInstance().getPhone()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals("phone")) {
                    tvPhoneNumber.setText(CurrentUser.getInstance().getPhone());
                }
                if (dataSnapshot.getKey().equals("name")) {
                    CurrentUser.getInstance().setName(String.valueOf(dataSnapshot.getValue()));
                    tvUsername.setText(CurrentUser.getInstance().getName());
                }
                        /*if (dataSnapshot.getKey().equals("uid")) {
                            CurrentUser.getInstance().setUid(String.valueOf(dataSnapshot.getValue()));
                        }*/
                if (dataSnapshot.getKey().equals("token")) {
                    CurrentUser.getInstance().setToken(String.valueOf(dataSnapshot.getValue()));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals("phone")) {
                    //CurrentUser.getInstance().setPhone(String.valueOf(dataSnapshot.getValue()));
                    tvPhoneNumber.setText(CurrentUser.getInstance().getPhone());
                }
                if (dataSnapshot.getKey().equals("name")) {
                    CurrentUser.getInstance().setName(String.valueOf(dataSnapshot.getValue()));
                    tvUsername.setText(CurrentUser.getInstance().getName());
                }
                        /*if (dataSnapshot.getKey().equals("uid")) {
                            CurrentUser.getInstance().setUid(String.valueOf(dataSnapshot.getValue()));
                        }*/
                if (dataSnapshot.getKey().equals("token")) {
                    CurrentUser.getInstance().setToken(String.valueOf(dataSnapshot.getValue()));
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void loadClasses() {
        //When firebase is connected, load user's classes into navigation drawer
        referenceUsers.child(CurrentUser.getInstance().getPhone()).child("classes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                final String[] extractedClass = new String[1];
                String extractedClassId = String.valueOf(dataSnapshot.getValue());
                classFragmentTag.add(extractedClassId);
                referenceClasses.child(extractedClassId).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getKey().equals("name")) {
                            extractedClass[0] = String.valueOf(dataSnapshot.getValue());
                            MenuItem newItem = classes.add(R.id.nav_grp_classes, classFragmentTag.size(), Menu.NONE, extractedClass[0]);
                            newItem.setIcon(R.drawable.class_icon);
                            classes.setGroupCheckable(R.id.nav_grp_classes, true, true);
                            setUpNavigationView();

                            //If user is already in at least one class, open the first class
                            if (classFragmentTag.size() == 1) {
                                navItemSelectedIndex = 0;
                                navItemSelectedTag = classFragmentTag.get(navItemSelectedIndex);
                                loadFragment();
                            } else {
                                loadDefaultFragment();          //Loads a default page when user hasn't joined any class
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Setup navigation drawer item select listener and syncing drawer toggle button
    private void setUpNavigationView() {
        //Item select listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                for (int i = 0; i < classes.size(); ++i) {
                    if (item.getItemId() == classes.getItem(i).getItemId()) {
                        //If item is matched, set the correct index and tag, then load the required fragment
                        navItemSelectedIndex = i;
                        navItemSelectedTag = classFragmentTag.get(i);
                        item.setChecked(true);
                        loadFragment();
                        return true;
                    }
                    //TODO: Add conditions for "About Us" and "Privacy Policy"
                }
                return false;
            }
        });

        //Drawer toggle button
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we don't want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we don't want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_class:
                if (CurrentUser.getFirebaseUser() == null) {
                    Toast.makeText(this, "Please wait!", Toast.LENGTH_SHORT).show();
                    break;
                }
                CurrentUser user = CurrentUser.getInstance();
                JoinRequest joinRequest = new JoinRequest(user.getName(), user.getPhone());
                DatabaseReference juitRef = referenceClasses.child("juit1620");
                if (referenceJoinReq == null)
                    referenceJoinReq = juitRef.child("join_req");
                String phone = user.getPhone();
                juitRef.child("participants").child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            referenceJoinReq.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        referenceJoinReq.child(phone).setValue(joinRequest);
                                        Toast.makeText(MainActivity.this, "Join request sent!\nYou will soon be a part of an awesome group \uD83D\uDE0E", Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(MainActivity.this, "You are already a part of the awesome group \uD83D\uDE0E", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else
                            Toast.makeText(MainActivity.this, "You are already a part of the awesome group \uD83D\uDE0E", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                /*
                String[] joinOrCreate = {"Join a class", "Create a class"};
                AlertDialog.Builder joinCreateBuilder = new AlertDialog.Builder(this);
                joinCreateBuilder.setAdapter(new ListAdapter() {
                    @Override
                    public boolean areAllItemsEnabled() {
                        return false;
                    }

                    @Override
                    public boolean isEnabled(int position) {
                        return false;
                    }

                    @Override
                    public void registerDataSetObserver(DataSetObserver observer) {

                    }

                    @Override
                    public void unregisterDataSetObserver(DataSetObserver observer) {

                    }

                    @Override
                    public int getCount() {
                        return 0;
                    }

                    @Override
                    public Object getItem(int position) {
                        return null;
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public boolean hasStableIds() {
                        return false;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_syllabus, parent, false);
                        }
                        return convertView;
                    }

                    @Override
                    public int getItemViewType(int position) {
                        return 0;
                    }

                    @Override
                    public int getViewTypeCount() {
                        return 1;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                joinCreateBuilder.show();
                break;*/
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkConnected() {
        //Check connected to a network and internet available
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
        return info != null && info.isConnectedOrConnecting();
    }
}