package in.edu.jaduniv.classroom.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.CurrentUser;
import in.edu.jaduniv.classroom.object.JoinRequest;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;

public class JoinClassActivity extends AppCompatActivity {

	Toolbar toolbar;
	EditText etSearch;
	Button btnSearch, btnJoinClass;
	CardView cvJoinClass;
	TextView tvJoinClassName, tvJoinClassCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_class);

		// Setup the toolbar
		setupToolbar();

		// Initialize references and their listeners
		initialize();
	}

	// Setup the toolbar
	private void setupToolbar() {
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		// For back button on top-left
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setHomeAsUpIndicator(R.drawable.up_button);
		}
	}

	// Initialize references and their listeners
	private void initialize() {
		etSearch = findViewById(R.id.et_class_search);
		btnSearch = findViewById(R.id.btn_search_class_code);
		cvJoinClass = findViewById(R.id.cv_join_class);
		tvJoinClassName = findViewById(R.id.tv_search_class_name);
		tvJoinClassCode = findViewById(R.id.tv_search_class_code);
		btnJoinClass = findViewById(R.id.btn_join_class);

		// Remove the found class view
		btnJoinClass.setEnabled(false);
		cvJoinClass.setVisibility(View.GONE);

		// When enter is pressed on the keyboard, search for the class
		etSearch.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					searchClass(etSearch.getText().toString());
					return true;
				}
				return false;
			}
		});

		// When search button is clicked, search for the class
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchClass(etSearch.getText().toString());
			}
		});

		// When join class button is clicked, sent request to join the class if allowed
		btnJoinClass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get the class code to request joining
				String classCode = tvJoinClassCode.getText().toString();

				// Get user's name and phone
				String name = CurrentUser.getInstance().getName();
				String phone = CurrentUser.getInstance().getPhone();

				// Check if user is already a participant of the class
				// If user is not a participant, send the join request
				FirebaseUtils.getDatabaseReference()
					.child("classes/" + classCode + "/participants/" + phone + "/phone")
					.addListenerForSingleValueEvent(
						new ValueEventListener() {

							@Override
							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								// If user is not a participant, send the join request
								if (!dataSnapshot.exists() || dataSnapshot.getValue() == null) {
									FirebaseUtils.getDatabaseReference().child("classes").child(classCode)
										.child("join_req").push()
										.setValue(new JoinRequest(name, phone));
								} else {
									Toast.makeText(JoinClassActivity.this,
										"You are already a participant of this class",
										Toast.LENGTH_LONG).show();
								}
							}

							@Override
							public void onCancelled(@NonNull DatabaseError databaseError) {
							}
						});
			}
		});
	}

	/**
	 * Search for the class given by a class code.
	 * If the class exists, it shows a card to join the class
	 * Otherwise, notifies user of non-existence of the requested class
	 *
	 * @param classCode The code of the class to search for
	 */
	private void searchClass(String classCode) {
		btnJoinClass.setEnabled(false);
		cvJoinClass.setVisibility(View.GONE);
		FirebaseUtils.getDatabaseReference().child("classes/" + classCode + "/name")
			.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
						cvJoinClass.setVisibility(View.VISIBLE);
						tvJoinClassName.setText(dataSnapshot.getValue().toString());
						tvJoinClassCode.setText(classCode);
						btnJoinClass.setEnabled(true);
					} else {
						Toast.makeText(JoinClassActivity.this, "No class found", Toast.LENGTH_LONG)
							.show();
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
	}
}
