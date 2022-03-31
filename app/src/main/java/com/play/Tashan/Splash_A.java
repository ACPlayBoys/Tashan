package com.play.Tashan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.play.Tashan.Main_Menu.MainMenuActivity;
import com.play.Tashan.SimpleClasses.Variables;

import java.io.File;

public class Splash_A extends AppCompatActivity {


    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);


        Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);

        countDownTimer = new CountDownTimer(2500, 500) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

                sign();




            }
        }.start();
        File folder = new File(Variables.app_folder);
        boolean success = true;
        if (!folder.exists()) {
            //Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
            success = folder.mkdir();
        }





    }
    private void sign() {
        final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();;
        if(user!=null) {

            String namaywa=user.getDisplayName();
            if(!user.isAnonymous()) {
                Toast.makeText(this.getBaseContext(), "Signed in As " + namaywa, Toast.LENGTH_SHORT).show();

            }
            else
            {
                Toast.makeText(this.getBaseContext(), "Please Login or Re-Login", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
                editor.putBoolean(Variables.islogin, false);
                editor.commit();
            }
            finshh();
        }
        else {
            Toast.makeText(this.getBaseContext(), "Please Login or Re-Login", Toast.LENGTH_SHORT).show();
            final FirebaseAuth mAuth=FirebaseAuth.getInstance();
           mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                System.out.println(user.getDisplayName()+"annonymus");
                                finshh();


                            } else {

                            }

                            // ...
                        }
                    });



            SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
            editor.putBoolean(Variables.islogin, false);
            editor.commit();
        }

    }
    public void finshh() {
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        Intent intent=new Intent(Splash_A.this, MainMenuActivity.class);

        if(getIntent().getExtras()!=null) {
            intent.putExtras(getIntent().getExtras());
            setIntent(null);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }


}
