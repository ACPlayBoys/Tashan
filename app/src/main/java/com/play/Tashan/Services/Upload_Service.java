package com.play.Tashan.Services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.play.Tashan.Main_Menu.MainMenuActivity;
import com.play.Tashan.SimpleClasses.Variables;
import com.play.Tashan.Video_Recording.AnimatedGifEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by AQEEL on 6/7/2018.
 */



// this the background service which will upload the video into database
public class  Upload_Service extends Service{

    private StorageReference mStorageRef;

    private final IBinder mBinder = new LocalBinder();
    private Uri dataGif;
    private byte[] dataJPG;

    public class LocalBinder extends Binder {
        public Upload_Service getService() {
            return Upload_Service.this;
        }
    }

    boolean mAllowRebind;
    ServiceCallback Callback;
    String videoName;
    String currentDateandTime;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }



    Uri uri;


    String description="";

    DatabaseReference mDatabase;

    SharedPreferences sharedPreferences;
    private Context context;

    public Upload_Service() {
        super();
    }

    public Upload_Service(ServiceCallback serviceCallback) {
        Callback=serviceCallback;
    }
    public Upload_Service(ServiceCallback serviceCallback,Context context) {
        Callback=serviceCallback;
        this.context=context;
    }


    public void setCallbacks(ServiceCallback serviceCallback){
        Callback=serviceCallback;
    }


    @Override
    public void onCreate() {
        sharedPreferences=getSharedPreferences(Variables.pref_name,MODE_PRIVATE);
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        if(intent!=null){
        if (intent.getAction().equals("startservice")) {
            showNotification();

            String uri_string= intent.getStringExtra("uri");
            uri = Uri.parse(uri_string);
            description=""+intent.getStringExtra("desc");

            new Thread(new Runnable() {
                @Override
                public void run() {



            Bitmap bmThumbnail;
            bmThumbnail = ThumbnailUtils.createVideoThumbnail(uri.getPath(),
                    MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                    Bitmap bmThumbnail_resized = Bitmap.createScaledBitmap(bmThumbnail,(int)(bmThumbnail.getWidth()*0.4), (int)(bmThumbnail.getHeight()*0.4), true);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmThumbnail_resized.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    dataJPG = baos.toByteArray();


            final File myVideo = new File(uri.getPath());
            Uri myVideoUri = Uri.parse(myVideo.toString());
            Log.e("upload -name",myVideo.getName());

            final MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
            mmRetriever.setDataSource(myVideo.getAbsolutePath());

            final MediaPlayer mp = MediaPlayer.create(getBaseContext(), myVideoUri);

            final ArrayList<Bitmap> frames = new ArrayList<Bitmap>();


            for (int i = 1000000; i < 2000 * 1000; i += 100000) {
                Bitmap bitmap = mmRetriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.4), (int)(bitmap.getHeight()*0.4), true);
                frames.add(resized);
            }
            dataGif = generateGIF(frames);




            JSONObject parameters = new JSONObject();
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            currentDateandTime = sdf.format(new Date());
            videoName=currentDateandTime+"output-filtered";

            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(Variables.sharedPreferences.getString(Variables.u_id,"")).child("uploads");

            ;
            

                


            

                    mStorageRef = FirebaseStorage.getInstance().getReference("videos");


            generateNoteOnSD("parameters",parameters.toString());
            StorageReference fileReference=mStorageRef.child(currentDateandTime+myVideo.getName());


            fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    String name=taskSnapshot.getMetadata().getName();
                    final String fname=name.substring(0,name.lastIndexOf('.'));
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            SimpleDateFormat sdf =   new SimpleDateFormat("dd-mm-yyyy hh-mm-ss", Locale.getDefault());
                            String created =sdf.format(new Date());
                            String url = uri.toString();
                            mDatabase.child(videoName).child("url").setValue(url);
                            mDatabase.child(videoName).child("created").setValue(created);
                            mDatabase.child(videoName).child("views").setValue(0);
                            mDatabase.getParent().getParent().getParent().child("Content").child(videoName).setValue(Variables.sharedPreferences.getString(Variables.u_id,""));
                            updateCount(mDatabase);
                            commitUpload();

                        }
                    });



                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                            Callback.ShowResponce("Their is some kind of problem from Server side Please Try Later");
                            stopForeground(true);
                            stopSelf();


                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e("upload",String.valueOf(taskSnapshot.getBytesTransferred()));


                }
            });

                }}).start();



        }
        else if(intent.getAction().equals("stopservice")){
            stopForeground(true);
            stopSelf();
           }

        }



        return Service.START_STICKY;
    }

    private void commitUpload()
    {
        mDatabase.child(videoName).child("sound_id").setValue(Variables.Selected_sound_id);
        mDatabase.child(videoName).child("description").setValue(description);

        mStorageRef=FirebaseStorage.getInstance().getReference("thumb");
        StorageReference fileReference=mStorageRef.child(currentDateandTime+".jpg");
        UploadTask uploadTask=fileReference.putBytes(dataJPG);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                String name=taskSnapshot.getMetadata().getName();
                final String fname=name.substring(0,name.lastIndexOf('.'));
                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String url = uri.toString();
                        mDatabase.child(videoName).child("thumb").setValue(url);

                    }
                });
            }
        });

        mStorageRef=FirebaseStorage.getInstance().getReference("gif");
        fileReference=mStorageRef.child(currentDateandTime+".gif");
        uploadTask=fileReference.putFile(dataGif);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                String name=taskSnapshot.getMetadata().getName();
                final String fname=name.substring(0,name.lastIndexOf('.'));
                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String url = uri.toString();
                        mDatabase.child(videoName).child("gif").setValue(url);

                    }
                });
            }
        });
        Callback.ShowResponce("Your Video is uploaded Successfully");
        stopForeground(true);
        stopSelf();
        
    }

    private void updateCount(final DatabaseReference ref) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String count=String.valueOf(dataSnapshot.getChildrenCount());
                ref.getParent().child("user_videos").setValue(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // this will show the sticky notification during uploading video
    private void showNotification() {

        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        final String CHANNEL_ID = "default";
        final String CHANNEL_NAME = "Default";

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        androidx.core.app.NotificationCompat.Builder builder = (androidx.core.app.NotificationCompat.Builder) new androidx.core.app.NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle("Uploading Video")
                .setContentText("Please wait! Video is uploading....")
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        android.R.drawable.stat_sys_upload))
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        startForeground(101, notification);
    }



    // for thumbnail
    public  String Bitmap_to_base64( Bitmap imagebitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagebitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] byteArray = baos .toByteArray();
        String base64= Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }



    // for video base64
    private String encodeFileToBase64Binary(Uri fileName)
            throws IOException {

        File file = new File(fileName.getPath());
        byte[] bytes = loadFile(file);
        String encodedString = Base64.encodeToString(bytes,Base64.DEFAULT);
        return encodedString;
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }




    //for video gif image
    public Uri generateGIF(ArrayList<Bitmap> bitmaps) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out);
            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

            encoder.addFrame(decoded);

        }

        encoder.finish();


        File filePath = new File(Variables.app_folder, "sample.gif");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(filePath);
            outputStream.write(bos.toByteArray());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        filePath=new File(Variables.app_folder+"sample.gif");
        Uri myVideoUri = Uri.fromFile(filePath);
        return myVideoUri;
    }


    public void generateNoteOnSD( String sFileName, String sBody) {
        try {
            File root = new File(Variables.app_folder, "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName+".txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            } catch (IOException e) {
            e.printStackTrace();
        }
    }


}