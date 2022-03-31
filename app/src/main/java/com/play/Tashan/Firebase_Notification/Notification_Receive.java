package com.play.Tashan.Firebase_Notification;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.play.Tashan.Chat.Chat_Activity;
import com.play.Tashan.Main_Menu.MainMenuActivity;
import com.play.Tashan.Main_Menu.MainMenuFragment;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.Variables;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * Created by AQEEL on 5/22/2018.
 */

public class Notification_Receive extends FirebaseMessagingService {


    SharedPreferences sharedPreferences;
    String  pic;
    String  title;
    String  message;
    String senderid;
    String receiverid;
    String action_type;



    Handler handler=new Handler();
    Runnable runnable;
    private static final String NOTIFICATION_ID_EXTRA = "Tashan";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID ="Tashan";
    private NotificationManager notificationManager;
    Snackbar snackbar;


    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        if (remoteMessage.getData().size() > 0) {
            sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);


            String[] msg = {"title","message","pic","senderid","receiverid","action_type"};
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("message");
            pic = remoteMessage.getData().get("icon");
            senderid = remoteMessage.getData().get("senderid");
            receiverid =remoteMessage.getData().get("receiverid");
            action_type = remoteMessage.getData().get("action_type");
            System.out.println("my id  "+Variables.sharedPreferences.getString(Variables.u_id,""));
            System.out.println("reciver id  "+receiverid);
            System.out.println("sender id  "+senderid);

            System.out.println(Variables.notifid);
            if (Variables.notifid.equals(senderid))
            System.out.println("visibilty  ");

            //if(receiverid.equals(Variables.sharedPreferences.getString(Variables.u_id,""))&&!Variables.notifid.equals(senderid))
            {

                sendNotification sendNotification = new sendNotification(this);
                sendNotification.execute(pic);


                if (remoteMessage.getData().size() > 0) {

                    Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                    PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            0 /* Request code */, notificationIntent,
                            PendingIntent.FLAG_ONE_SHOT);

                    int notificationId = new Random().nextInt(60000);
                    Bitmap bitmap = getBitmapfromUrl(pic);


                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                        setupChannels();

                    }

                    NotificationCompat.Builder notificationBuilder =
                            new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                                    .setLargeIcon(bitmap)
                                    .setSmallIcon(R.drawable.ic_notif)
                                    .setContentTitle(title)
                                    .setStyle(new NotificationCompat.BigPictureStyle()
                                            .setSummaryText(message)
                                            .bigPicture(bitmap))/*Notification with Image*/
                                    .setContentText(message)
                                    .setAutoCancel(true)
                                    .setSound(defaultSoundUri)
                                    .setContentIntent(pendingIntent);
                            ;

                    notificationManager.notify(notificationId, notificationBuilder.build());


                }
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users").child(receiverid).child("Notifications").child(message);
                reference.child("Sender id").setValue(senderid);
                reference.child("Message").setValue(message);
                reference.child("title").setValue(title);
                reference.child("receiverid").setValue(receiverid);
                reference.child("action").setValue(action_type);
            }

        }
    }



        private void setupChannels(){
            CharSequence adminChannelName = NOTIFICATION_ID_EXTRA;
            String adminChannelDescription = NOTIFICATION_ID_EXTRA;

            NotificationChannel adminChannel;
            adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_DEFAULT);
            adminChannel.setDescription(adminChannelDescription);
            adminChannel.enableLights(true);
            adminChannel.setLightColor(Color.RED);
            adminChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(adminChannel);
            }
        }



        public Bitmap getBitmapfromUrl(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }





        // this will store the user firebase token in local storage
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sharedPreferences=getSharedPreferences(Variables.pref_name,MODE_PRIVATE);

        if(s==null){

        }else if(s.equals("null")){

        }
        else if(s.equals("")){

        }
        else if(s.length()<6){

        }
        else {
            sharedPreferences.edit().putString(Variables.device_token, s).commit();
        }

    }




    private class sendNotification extends AsyncTask<String, Void, Bitmap> {

        Context ctx;


        public sendNotification(Context context) {
            super();
            this.ctx = context;
        }


        @Override
        protected Bitmap doInBackground(String... params) {

            // in notification first we will get the image of the user and then we will show the notification to user
            // in onPostExecute
            InputStream in;
            try {

                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @SuppressLint("WrongConstant")
        @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);


            if(MainMenuActivity.mainMenuActivity!=null){


                if(snackbar !=null){
                    snackbar.getView().setVisibility(View.INVISIBLE);
                    snackbar.dismiss();
                }

                if(handler!=null && runnable!=null) {
                    handler.removeCallbacks(runnable);
                }


                View layout = MainMenuActivity.mainMenuActivity.getLayoutInflater().inflate(R.layout.item_layout_custom_notification,null);
                TextView titletxt= layout.findViewById(R.id.username);
                TextView messagetxt=layout.findViewById(R.id.message);
                ImageView imageView=layout.findViewById(R.id.user_image);
                titletxt.setText(title);
                messagetxt.setText(message);

                if(result!=null)
                imageView.setImageBitmap(result);


                snackbar = Snackbar.make(MainMenuActivity.mainMenuActivity.findViewById(R.id.container), "", Snackbar.LENGTH_LONG);

                Snackbar.SnackbarLayout snackbarLayout= (Snackbar.SnackbarLayout) snackbar.getView();
                TextView textView = (TextView) snackbarLayout.findViewById(R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);

                final ViewGroup.LayoutParams params = snackbar.getView().getLayoutParams();
                if (params instanceof CoordinatorLayout.LayoutParams) {
                    ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.TOP;
                } else {
                    ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP;
                }

                snackbarLayout.setPadding(0,0,0,0);
                snackbarLayout.addView(layout, 0);


                snackbar.getView().setVisibility(View.INVISIBLE);

                snackbar.setCallback(new Snackbar.Callback(){
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                        snackbar.getView().setVisibility(View.VISIBLE);
                    }

                });


                runnable=new Runnable() {
                    @Override
                    public void run() {
                        snackbar.getView().setVisibility(View.INVISIBLE);

                    }
                };

                handler.postDelayed(runnable, 2750);


                snackbar.setDuration(Snackbar.LENGTH_LONG);
                snackbar.show();



                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        snackbar.dismiss();
                        snackbar.getView().setVisibility(View.INVISIBLE);

                        if(action_type.equals("message"))
                        chatFragment(senderid,title,pic);

                    }
                });


            }


        }

    }



    public void chatFragment(String receiverid, String name, String picture){

        if(sharedPreferences.getBoolean(Variables.islogin,false)) {

            if (MainMenuFragment.tabLayout != null) {
                TabLayout.Tab tab3 = MainMenuFragment.tabLayout.getTabAt(3);
                tab3.select();
            }

            Chat_Activity chat_activity = new Chat_Activity();
            FragmentTransaction transaction = MainMenuActivity.mainMenuActivity.getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);

            Bundle args = new Bundle();
            args.putString("user_id", receiverid);
            args.putString("user_name", name);
            args.putString("user_pic", picture);

            chat_activity.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, chat_activity).commit();

        }


    }




}
