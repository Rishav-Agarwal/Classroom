package in.edu.jaduniv.classroom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.adapters.JoinRequestAdapter;
import in.edu.jaduniv.classroom.adapters.ParticipantAdapter;
import in.edu.jaduniv.classroom.object.JoinRequest;
import in.edu.jaduniv.classroom.object.Participant;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;

public class ClassInfo extends AppCompatActivity {

    private static String classCode;
    Toolbar toolbar;
    RecyclerView rvJoinReq;
    ListView lvParticipants;
    LinearLayout l_join_requests;
    ArrayList<JoinRequest> joinRequests;
    ArrayList<String> joinRequestKeys;
    ArrayList<Participant> participants;
    ArrayList<String> participantsKey;
    JoinRequestAdapter joinRequestAdapter;
    ParticipantAdapter participantAdapter;
    private FirebaseUtils.AdminState adminState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_info);

        Intent intent = getIntent();
        classCode = intent.getStringExtra("class");

        initializeVariables();
        loadParticipants();
    }

    private void initializeVariables() {
        toolbar = findViewById(R.id.toolbar_class_info);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.up_button);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        rvJoinReq = findViewById(R.id.rv_join_requests);
        lvParticipants = findViewById(R.id.lv_participants);

        l_join_requests = findViewById(R.id.layout_join_request);

        participants = new ArrayList<>();
        joinRequests = new ArrayList<>();
        participantsKey = new ArrayList<>();
        joinRequestKeys = new ArrayList<>();
        joinRequestAdapter = new JoinRequestAdapter(this, joinRequests, joinRequestKeys, classCode);
        participantAdapter = new ParticipantAdapter(this, participants);
        rvJoinReq.setAdapter(joinRequestAdapter);
        lvParticipants.setAdapter(participantAdapter);

        try {
            adminState = new FirebaseUtils.AdminState(classCode, new in.edu.jaduniv.classroom.interfaces.ValueEventListener() {
                @Override
                public void onValueChanged(Object newValue, Object oldValue) {
                    updateAdminStatus((Boolean) newValue);
                }
            });
        } catch (IllegalAccessException e) {
            Log.e("User not defined", "Probably user is not signed in");
        }
    }

    private void updateAdminStatus(boolean isAdmin) {
        if (isAdmin) {
            joinRequestKeys.clear();
            joinRequests.clear();
            FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("join_req").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    joinRequests.add(dataSnapshot.getValue(JoinRequest.class));
                    joinRequestKeys.add(dataSnapshot.getKey());
                    joinRequestAdapter.notifyDataSetChanged();
                    l_join_requests.setVisibility(View.VISIBLE);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    joinRequests.remove(joinRequestKeys.indexOf(dataSnapshot.getKey()));
                    joinRequestKeys.remove(joinRequestKeys.indexOf(dataSnapshot.getKey()));
                    joinRequestAdapter.notifyDataSetChanged();
                    if (joinRequests.size() == 0)
                        l_join_requests.setVisibility(View.GONE);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            joinRequestKeys.clear();
            joinRequests.clear();
            l_join_requests.setVisibility(View.GONE);
        }
    }

    private void loadParticipants() {
        FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("participants").orderByChild("name").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                participants.add(dataSnapshot.getValue(Participant.class));
                participantsKey.add(dataSnapshot.getKey());
                participantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                participants.set(participantsKey.indexOf(dataSnapshot.getKey()), dataSnapshot.getValue(Participant.class));
                participantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int index = participantsKey.indexOf(dataSnapshot.getKey());
                participants.remove(index);
                participantsKey.remove(index);
                participantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}