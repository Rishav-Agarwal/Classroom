package in.edu.jaduniv.classroom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.edu.jaduniv.classroom.R;

public class LoginInfoActivity extends AppCompatActivity {

    public static final int RC_LOGIN_INFO = 1001;
    public static String RESULT_NAME = "name";
    public static String RESULT_EMAIL = "email";
    EditText etLoginName, etLoginEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_info);

        etLoginEmail = findViewById(R.id.et_login_email);
        etLoginName = findViewById(R.id.et_login_name);
    }

    public void completeLogin(View view) {
        boolean validInfo;
        String enteredName = etLoginName.getText().toString();
        String enteredEmail = etLoginEmail.getText().toString();
        //validating information provided
        validInfo = !(enteredName.equals("")) && validateEmail(enteredEmail);
        if (!validInfo) {
            Toast.makeText(this, "Please enter valid information!", Toast.LENGTH_SHORT).show();
        } else {
            Intent result = new Intent();
            result.putExtra(RESULT_EMAIL, etLoginEmail.getText().toString());
            result.putExtra(RESULT_NAME, etLoginName.getText().toString());
            setResult(RC_LOGIN_INFO, result);
            finish();
        }
    }

    //function to check whether the entered email was valid
    private boolean validateEmail(String email) {
        email = email.trim();
        if (email.equals(""))
            return false;

        //Regular expression
        final String EMAIL_REGEX =
                "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        final Pattern EMAIL_PATTERN =
                Pattern.compile(EMAIL_REGEX);

        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
    }
}