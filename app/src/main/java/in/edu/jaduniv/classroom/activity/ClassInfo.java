package in.edu.jaduniv.classroom.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import in.edu.jaduniv.classroom.object.JoinRequest;
import in.edu.jaduniv.classroom.object.Participant;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;
import in.edu.jaduniv.classroom.adapters.JoinRequestAdapter;
import in.edu.jaduniv.classroom.adapters.ParticipantAdapter;

public class ClassInfo extends AppCompatActivity {

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

    private static String classCode;

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
        toolbar = (Toolbar) findViewById(R.id.toolbar_class_info);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.up_button);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rvJoinReq = (RecyclerView) findViewById(R.id.rv_join_requests);
        lvParticipants = (ListView) findViewById(R.id.lv_participants);

        l_join_requests = (LinearLayout) findViewById(R.id.layout_join_request);

        participants = new ArrayList<>();
        joinRequests = new ArrayList<>();
        participantsKey = new ArrayList<>();
        joinRequestKeys = new ArrayList<>();
        joinRequestAdapter = new JoinRequestAdapter(joinRequests);
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
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    joinRequests.add(dataSnapshot.getValue(JoinRequest.class));
                    joinRequestKeys.add(dataSnapshot.getKey());
                    joinRequestAdapter.notifyDataSetChanged();
                    l_join_requests.setVisibility(View.VISIBLE);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    joinRequests.remove(joinRequestKeys.indexOf(dataSnapshot.getKey()));
                    joinRequestKeys.remove(joinRequestKeys.indexOf(dataSnapshot.getKey()));
                    joinRequestAdapter.notifyDataSetChanged();
                    if (joinRequests.size() == 0)
                        l_join_requests.setVisibility(View.GONE);
                    //TODO: Add to participants if tick was clicked
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
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
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                participants.add(dataSnapshot.getValue(Participant.class));
                participantsKey.add(dataSnapshot.getKey());
                participantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                participants.set(participantsKey.indexOf(dataSnapshot.getKey()), dataSnapshot.getValue(Participant.class));
                participantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                participants.remove(participantsKey.indexOf(dataSnapshot.getKey()));
                participantAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}