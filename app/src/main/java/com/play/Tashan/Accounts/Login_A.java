package com.play.Tashan.Accounts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.play.Tashan.Main_Menu.MainMenuActivity;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.ApiRequest;
import com.play.Tashan.SimpleClasses.Callback;
import com.play.Tashan.SimpleClasses.Variables;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Login_A extends Activity {


    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    IOSDialog iosDialog;

    SharedPreferences sharedPreferences;

    View top_view;

    TextView login_title_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        this.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);



        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();

        // if the user is already login trought facebook then we will logout the user automatically
        LoginManager.getInstance().logOut();

        iosDialog = new IOSDialog.Builder(this)
                .setCancelable(false)
                .setSpinnerClockwise(false)
                .setMessageContentGravity(Gravity.END)
                .build();

        sharedPreferences=getApplicationContext().getSharedPreferences(Variables.pref_name,MODE_PRIVATE);

        findViewById(R.id.facebook_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loginwith_FB();
            }
        });



        findViewById(R.id.google_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sign_in_with_gmail();
            }
        });



        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        top_view=findViewById(R.id.top_view);



        login_title_txt=findViewById(R.id.login_title_txt);
        login_title_txt.setText("You need a "+getString(R.string.app_name)+"\naccount to Continue");



        SpannableString ss = new SpannableString("By signing up, you confirm that you agree to our \n Terms of Use and have read and understood \n our Privacy Policy.");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Open_Privacy_policy();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 99, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.login_terms_condition_txt);
        textView.setText(ss);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


        printKeyHash();


    }


    public void Open_Privacy_policy(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Variables.privacy_policy));
        startActivity(browserIntent);
    }


    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        top_view.startAnimation(anim);
        top_view.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        top_view.setVisibility(View.GONE);
        finish();
        overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

    }


    // Bottom two function are related to Fb implimentation
    private CallbackManager mCallbackManager;
    //facebook implimentation
    public void Loginwith_FB(){

        LoginManager.getInstance()
                .logInWithReadPermissions(Login_A.this,
                        Arrays.asList("public_profile","email"));

        // initialze the facebook sdk and request to facebook for login
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>()  {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                Log.d("resp_token",loginResult.getAccessToken()+"");
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(Login_A.this, "Login Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("resp",""+error.toString());
                Toast.makeText(Login_A.this, "Login Error"+error.toString(), Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void handleFacebookAccessToken(final AccessToken token) {
        // if user is login then this method will call and
        // facebook will return us a token which will user for get the info of user
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.d("resp_token",token.getToken()+"");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            iosDialog.show();
                            final String id = Profile.getCurrentProfile().getId();
                            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject user, GraphResponse graphResponse) {

                                    Log.d("resp",user.toString());
                                    //after get the info of user we will pass to function which will store the info in our server

                                    String fname=""+user.optString("first_name");
                                    String lname=""+user.optString("last_name");


                                    if(fname.equals("") || fname.equals("null"))
                                        fname=getResources().getString(R.string.app_name);

                                    if(lname.equals("") || lname.equals("null"))
                                        lname="";

                                    Call_Api_For_Signup(""+id,fname
                                            ,lname,
                                            "https://graph.facebook.com/"+id+"/picture?width=500&width=500",
                                            "facebook",token.getToken());

                                }
                            });

                            // here is the request to facebook sdk for which type of info we have required
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "last_name,first_name,email");
                            request.setParameters(parameters);
                            request.executeAsync();
                        } else {

                            Toast.makeText(Login_A.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        if(requestCode==123){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else if(mCallbackManager!=null)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }



    //google Implimentation
    GoogleSignInClient mGoogleSignInClient;
    public void Sign_in_with_gmail(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Login_A.this);


        if (account != null) {
            String id=account.getId();

            firebaseAuthWithGoogle(account.getIdToken());
            String fname=""+account.getGivenName();
            String lname=""+account.getFamilyName();

            String pic_url;
            if(account.getPhotoUrl()!=null) {
                pic_url = account.getPhotoUrl().toString();
            }else {
                pic_url="null";
            }



            if(fname.equals("") || fname.equals("null"))
                fname=getResources().getString(R.string.app_name);

            if(lname.equals("") || lname.equals("null"))
                lname="_";

            Call_Api_For_Signup(id,fname,lname,pic_url,"gmail",account.getIdToken());


        }
        else {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 123);
        }

    }


    //Relate to google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String id=account.getId();

                firebaseAuthWithGoogle(account.getIdToken());
                String fname=""+account.getGivenName();
                String lname=""+account.getFamilyName();


                // if we do not get the picture of user then we will use default profile picture

                String pic_url;
                if(account.getPhotoUrl()!=null) {
                    pic_url = account.getPhotoUrl().toString();
                }else {
                    pic_url="null";
                }


                if(fname.equals("") || fname.equals("null"))
                    fname=getResources().getString(R.string.app_name);

                if(lname.equals("") || lname.equals("null"))
                    lname="";
                Log.e("aniket",account.getIdToken());



                Call_Api_For_Signup(id,fname,lname,pic_url,"gmail",account.getIdToken());



            }

        } catch (ApiException e) {
            Toast.makeText(this, "signInResult:failed code=" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }

    }




    // this function call an Api for Signin
    private void Call_Api_For_Signup(final String id,
                                     final String f_name,
                                     final String l_name,
                                     final String picture,
                                     String singnup_type, final String token) {

         final DatabaseReference mDatabase;
// ...
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(id);
        PackageInfo packageInfo = null;
        try {
            packageInfo =getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appversion=packageInfo.versionName;

        final JSONObject parameters = new JSONObject();
        try {

            parameters.put("id", id);
            parameters.put("first_name",""+f_name);
            parameters.put("last_name", ""+l_name);
            parameters.put("profile_pic",picture);
            parameters.put("gender","m");
            parameters.put("version",appversion);
            parameters.put("signup_type",singnup_type);
            parameters.put("device",Variables.device);
            parameters.put("bio","");


        } catch (JSONException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        iosDialog.show();

        mDatabase.child("version").setValue(appversion);
        mDatabase.child("signup_type").setValue(singnup_type);
        mDatabase.child("device").setValue(Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild("id")) {

                    mDatabase.child("id").setValue(id);

                }
                if (!snapshot.hasChild("first_name")) {

                    mDatabase.child("first_name").setValue(f_name);

                }
                if (!snapshot.hasChild("last_name")) {

                    mDatabase.child("last_name").setValue(l_name);

                }
                if (!snapshot.hasChild("profile_pic")) {

                    mDatabase.child("profile_pic").setValue(picture);

                }
                if (!snapshot.hasChild("gender")) {

                    mDatabase.child("gender").setValue("male");

                }
                if (!snapshot.hasChild("total_following")) {
                    createChild(mDatabase,"total_following");

                }
                if (!snapshot.hasChild("total_fans")) {
                    createChild(mDatabase,"total_fans");

                }
                if (!snapshot.hasChild("total_heart")) {
                    createChild(mDatabase,"total_heart");

                }if (!snapshot.hasChild("user_videos")) {
                    createChild(mDatabase,"user_videos");

                }
                Parse_signup_data(parameters,token);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        iosDialog.cancel();






    }

    private void createChild(DatabaseReference mDatabase, String child) {
        mDatabase.child(child).setValue(0);
    }


    // if the signup successfull then this method will call and it store the user info in local
    public void Parse_signup_data(JSONObject snapshot,String token){



                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(Variables.u_id,snapshot.optString("id"));
                editor.putString(Variables.f_name,snapshot.optString("first_name"));
                editor.putString(Variables.l_name,snapshot.optString("last_name"));
                editor.putString(Variables.u_name,snapshot.optString("first_name")+" "+snapshot.optString("last_name"));
                editor.putString(Variables.gender,snapshot.optString("gender"));
                editor.putString(Variables.u_pic,snapshot.optString("profile_pic"));
                editor.putString(Variables.token,token);
                editor.putBoolean(Variables.islogin,true);
        editor.apply();
                editor.commit();

                Variables.user_id=Variables.sharedPreferences.getString(Variables.u_id,"");
                Toast.makeText(this, Variables.sharedPreferences.getString(Variables.u_id,""), Toast.LENGTH_SHORT).show();

                top_view.setVisibility(View.GONE);
                finish();
                startActivity(new Intent(this, MainMenuActivity.class));

    }



    // this function will print the keyhash of your project
    // which is very helpfull during Fb login implimentation
    public void printKeyHash()  {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName() , PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("keyhash" , Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    private void firebaseAuthWithGoogle(final String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", idToken);
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                        }

                        // ...
                    }
                });
    }

}
