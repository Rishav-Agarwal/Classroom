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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.adapters.PostRequestAdapter;
import in.edu.jaduniv.classroom.adapters.PostRequestAdapter.RecyclerViewEmptyListener;
import in.edu.jaduniv.classroom.interfaces.ValueEventListener;
import in.edu.jaduniv.classroom.object.Post;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class PostRequests extends AppCompatActivity {

    RecyclerView rvPostRequests;
    TextView tvRvEmmpty;

    ArrayList<Post> postRequests;
    ArrayList<String> postRequestKeys;
    PostRequestAdapter postRequestAdapter;

    RecyclerViewEmptyListener emptyListener;
    Toolbar toolbar;
    FirebaseUtils.AdminState adminState;
    private String classCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_requests);

        Intent postReqIntent = getIntent();
        classCode = postReqIntent.getStringExtra("class");

        initializeVariables();      //Initialize variables
        loadPostRequests();         //Load post requests and attach listeners
    }

    private void initializeVariables() {
        toolbar = findViewById(R.id.toolbar_post_req);
        tvRvEmmpty = findViewById(R.id.tv_empty_rv);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.up_button);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        try {
            adminState = new FirebaseUtils.AdminState(classCode, new ValueEventListener() {
                @Override
                public void onValueChanged(Object newValue, Object oldValue) {
                    boolean isAdmin = (Boolean) newValue;
                    if (!isAdmin) {
                        finish();
                        Toast.makeText(getApplicationContext(), "You are no longer admin of this group", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (IllegalAccessException e) {
            Log.e("User not defined", "Probably user is not signed in");
        }

        rvPostRequests = findViewById(R.id.rv_post_requests);
        rvPostRequests.setItemAnimator(new FadeInAnimator());
        postRequests = new ArrayList<>();
        postRequestKeys = new ArrayList<>();
        emptyListener = new RecyclerViewEmptyListener() {
            @Override
            public void onRecyclerViewEmptied() {
                rvPostRequests.setVisibility(View.GONE);
                tvRvEmmpty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRecyclerViewPopulated() {
                rvPostRequests.setVisibility(View.VISIBLE);
                tvRvEmmpty.setVisibility(View.GONE);
            }
        };
        postRequestAdapter = new PostRequestAdapter(this, postRequests, postRequestKeys, classCode, emptyListener);
        rvPostRequests.setAdapter(postRequestAdapter);
    }

    private void loadPostRequests() {
        FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("post_req").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                String title = (String) dataSnapshot.child("title").getValue();
                String description = (String) dataSnapshot.child("description").getValue();
                boolean pinned = (boolean) dataSnapshot.child("pinned").getValue();
                String postedByNumber = (String) dataSnapshot.child("postedByNumber").getValue();
                String postedByName = (String) dataSnapshot.child("postedByName").getValue();
                Long time = (Long) dataSnapshot.child("time").getValue();
                String uri = String.valueOf(dataSnapshot.child("url").getValue());
                String fileName = String.valueOf(dataSnapshot.child("fileName").getValue());
                String resourceType = String.valueOf(dataSnapshot.child("resourceType").getValue());
                String publicId = String.valueOf(dataSnapshot.child("publicId").getValue());
                String mimeType = String.valueOf(dataSnapshot.child("mimeType").getValue());
                Post postRequest = new Post(title, description, pinned, postedByNumber, postedByName, null, time, uri, fileName, resourceType, publicId, mimeType);
                postRequests.add(postRequest);
                postRequestKeys.add(dataSnapshot.getKey());
                if (postRequests.size() == 1) {
                    emptyListener.onRecyclerViewPopulated();
                }
                postRequestAdapter.notifyItemInserted(postRequests.size() - 1);
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
}