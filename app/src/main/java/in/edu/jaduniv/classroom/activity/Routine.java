package in.edu.jaduniv.classroom.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Calendar;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.adapters.RoutineDayAdapter;
import in.edu.jaduniv.classroom.object.Class;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;

public class Routine extends AppCompatActivity {

    //Time table
    ArrayList<ArrayList<Class>> timeTable = new ArrayList<>();
    ArrayList<ArrayList<String>> timeTableKeys = new ArrayList<>();

    //Adapter for day fragment of ViewPager
    RoutineDayAdapter routineDayAdapter;

    //Class code
    private String classCode;

    //Firebase
    private DatabaseReference routineReference;
    private ChildEventListener routineEventListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        Intent intent = getIntent();
        classCode = intent.getStringExtra("class");

        initializeVariables();          //Initialize variables for activity and firebase

        setRoutineListener();           //Setup listener for routine of the class
    }

    protected void setRoutineListener() {
        if (routineEventListener == null) {
            routineEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    final Class _class = dataSnapshot.getValue(Class.class);
                    if (_class == null)
                        return;
                    int day = _class.getDay();
                    timeTable.get(day).add(_class);
                    timeTableKeys.get(day).add(dataSnapshot.getKey());
                    routineDayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    final Class _class = dataSnapshot.getValue(Class.class);
                    if (_class == null)
                        return;
                    int i1 = -1, i2 = -1;
                    String key = dataSnapshot.getKey();
                    for (int i = 0; i < timeTableKeys.size(); ++i) {
                        int index;
                        if ((index = timeTableKeys.get(i).indexOf(key)) >= 0) {
                            i1 = i;
                            i2 = index;
                            break;
                        }
                    }
                    timeTable.get(i1).set(i2, _class);
                    routineDayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    final Class _class = dataSnapshot.getValue(Class.class);
                    if (_class == null)
                        return;
                    int day = _class.getDay();
                    int index = timeTableKeys.get(day).indexOf(dataSnapshot.getKey());
                    if (index < 0)
                        return;
                    timeTableKeys.remove(index);
                    timeTable.remove(index);
                    routineDayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
        }
        routineReference.addChildEventListener(routineEventListener);
    }

    private void initializeVariables() {

        //TIME TABLE
        for (int i = 0; i < 6; ++i) {
            timeTable.add(new ArrayList<>());
            timeTableKeys.add(new ArrayList<>());
        }

        //TOOLBAR
        Toolbar toolbar = findViewById(R.id.routine_toolbar);
        toolbar.setTitle("Routine");
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.up_button);
        }

        //TAB-LAYOUT and VIEW-PAGER
        ViewPager dayPager = findViewById(R.id.routine_pager);
        routineDayAdapter = new RoutineDayAdapter(getSupportFragmentManager(), timeTable);
        dayPager.setAdapter(routineDayAdapter);
        TabLayout daysTab = findViewById(R.id.tabs);
        daysTab.setSelectedTabIndicatorColor(getResources().getColor(android.R.color.white));
        daysTab.setTabTextColors(Color.parseColor("#444444"), Color.parseColor("#000000"));
        daysTab.setupWithViewPager(dayPager, true);
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        dayPager.setCurrentItem(today >= 0 && today <= 5 ? today : 0, true);

        //FIREBASE
        routineReference = FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("routine");
    }
}