package com.play.Tashan.SoundLists;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;

import android.os.FileUtils;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.play.Tashan.Main_Menu.Custom_ViewPager;
import com.play.Tashan.Main_Menu.MainMenuActivity;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.Variables;
import com.play.Tashan.SoundLists.FavouriteSounds.Favourite_Sound_F;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class SoundList_Main_A extends AppCompatActivity implements View.OnClickListener{

    protected TabLayout tablayout;

    protected Custom_ViewPager pager;

    private ViewPagerAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_list_main);

        tablayout = (TabLayout) findViewById(R.id.groups_tab);
        pager = findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(2);
        pager.setPagingEnabled(false);

        // Note that we are passing childFragmentManager, not FragmentManager
        adapter = new ViewPagerAdapter(getResources(), getSupportFragmentManager());
        pager.setAdapter(adapter);
        tablayout.setupWithViewPager(pager);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        createDiag();
        fab.bringToFront();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClicked();
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        findViewById(R.id.Goback).setOnClickListener(this);


    }
    AlertDialog diag;
    int selection=-1;
    CharSequence[] values = {"Anime","Comedy","Shayri"};
    Uri selectedfile,finalUri;
    String name="No Name";
    private void createDiag() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Category");

        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    case 0:
                        Toast.makeText(MainMenuActivity.mainMenuActivity, "First Item Clicked", Toast.LENGTH_LONG).show();
                        selection=0;
                        diag.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        break;
                    case 1:

                        Toast.makeText(MainMenuActivity.mainMenuActivity, "Second Item Clicked", Toast.LENGTH_LONG).show();
                        selection=1;
                        diag.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        break;
                    case 2:

                        Toast.makeText(MainMenuActivity.mainMenuActivity, "Third Item Clicked", Toast.LENGTH_LONG).show();
                        selection=2;
                        diag.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        break;
                }
            }
        });


        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainMenuActivity.mainMenuActivity, "Yes button Clicked!", Toast.LENGTH_LONG).show();
                diagName();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainMenuActivity.mainMenuActivity, "No button Clicked!", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });


        diag = builder.create();
        diag.setCanceledOnTouchOutside(false);



    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 123);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {
            selectedfile = data.getData();

            executeCutVideoCommand(selectedfile);

            progressDialog.show();


        }
    }
    private void upload() {
        if(selection!=-1)
        {

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Sounds").child("sound").child(m_Text+".mp3");
            mStorageRef.putFile(finalUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    String name=taskSnapshot.getMetadata().getName();
                    final String fname=name.substring(0,name.lastIndexOf('.'));
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            commitUpload(url);

                        }
                    });


                }
            });
        }
        else
        {
            Toast.makeText(MainMenuActivity.mainMenuActivity, "Select Category", Toast.LENGTH_LONG).show();
        }
    }
    private String m_Text = "";
    AlertDialog diagg;
    private void diagName() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Name");

// Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter Name of Sound");
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0)
                    diagg.getButton(diag.BUTTON_POSITIVE).setEnabled(true);
                else
                    diagg.getButton(diag.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();

                upload();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        diagg=builder.create();
        diagg.setCanceledOnTouchOutside(false);
        diagg.show();
        diagg.getButton(diag.BUTTON_POSITIVE).setEnabled(false);
    }

    private void commitUpload(String Url) {
        String cat=String.valueOf(values[selection]);
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Sound").child(cat.toLowerCase()).child(m_Text);
        SimpleDateFormat sdf =   new SimpleDateFormat("dd-mm-yyyy", Locale.getDefault());
        String created =sdf.format(new Date());
        mDatabase.child("date_created").setValue(created);
        mDatabase.child("description").setValue(m_Text);
        mDatabase.child("id").setValue(values[selection]+"-"+m_Text);
        mDatabase.child("name").setValue(m_Text);
        mDatabase.child("path").setValue(Url);

        Task<Uri> firebaseUri = FirebaseStorage.getInstance().getReference("Sounds").child("thumb").child(values[selection]+".jpg").getDownloadUrl();
        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();
                mDatabase.child("thum").setValue(url);


            }
        });
    }

    private void fabClicked() {
        selection=-1;
        System.out.println("clicked");selectFile();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Goback:
                onBackPressed();
                break;
        }
    }
    public String getRealPathFromURI(Uri contentUri)
    {
        String[] proj = { MediaStore.Audio.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public String getFileName(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        else if (scheme.equals("content")) {
            String[] proj = { MediaStore.Images.Media.TITLE };
            Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                cursor.moveToFirst();
                fileName = cursor.getString(columnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }
    private void executeCutVideoCommand(Uri selectedVideoUri) {
        File moviesDir = new File(Variables.app_folder);

        String filePrefix = "cut audio";
        String fileExtn = ".mp3";
        name=filePrefix;

        String yourRealPath  =getRealPathFromURI(selectedVideoUri);

        File dest = new File(moviesDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }

        Log.d("TAG", "startTrim: src: " + filePrefix);
        Log.d("TAG", "startTrim: dest: " + dest.getAbsolutePath());


        String filePath = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-t", "60" ,"-i", yourRealPath, "-acodec", "copy" , filePath};
        execFFmpegBinary(complexCommand,Uri.fromFile(dest));

    }


    private void execFFmpegBinary(final String[] command, final Uri uri) {
        FFmpeg ffmpeg = FFmpeg.getInstance(MainMenuActivity.mainMenuActivity);
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d("TAG", "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    finalUri=uri;
                    progressDialog.dismiss();
                    diag.show();//The uri with the location of the file
                    diag.getButton(diag.BUTTON_POSITIVE).setEnabled(false);


                }

                @Override
                public void onProgress(String s) {

                    Log.d("TAG", "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d("TAG", "Trimming Audio" );

                }

                @Override
                public void onFinish() {
                    finalUri=uri;
                    progressDialog.dismiss();
                    diag.show();//The uri with the location of the file
                    diag.getButton(diag.BUTTON_POSITIVE).setEnabled(false);

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {


        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {

            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new Discover_SoundList_F();
                    break;
                case 1:
                    result = new Favourite_Sound_F();
                    break;
                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            switch (position) {
                case 0:
                    return "Discover";
                case 1:
                    return "My Favorites";

                default:
                    return null;

            }


        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */


        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }

}
