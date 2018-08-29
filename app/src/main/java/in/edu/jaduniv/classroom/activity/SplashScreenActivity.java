package in.edu.jaduniv.classroom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import in.edu.jaduniv.classroom.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Delays for 1 second and then launches the main activity
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullscreen();   //Make the activity fullscreen as soon as it comes in foreground
    }

    //Function to make the activity fullscreen each time it starts/resumes
    public void makeFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION       //Hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN            //Hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
