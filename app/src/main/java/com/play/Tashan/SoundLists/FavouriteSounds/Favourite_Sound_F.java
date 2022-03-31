package com.play.Tashan.SoundLists.FavouriteSounds;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.play.Tashan.Main_Menu.MainMenuActivity;
import com.play.Tashan.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.ApiRequest;
import com.play.Tashan.SimpleClasses.Callback;
import com.play.Tashan.SimpleClasses.Functions;
import com.play.Tashan.SimpleClasses.Variables;
import com.play.Tashan.SoundLists.Sounds_GetSet;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Favourite_Sound_F extends RootFragment implements Player.EventListener {


    Context context;
    View view;

    ArrayList<Sounds_GetSet> datalist;
    Favourite_Sound_Adapter adapter;

    static boolean active = false;

    RecyclerView listview;

    DownloadRequest prDownloader;

    IOSDialog iosDialog;
    public static String running_sound_id;


    SwipeRefreshLayout swiperefresh;

    public Favourite_Sound_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.activity_sound_list, container, false);

        context=getContext();

        running_sound_id="none";

        iosDialog = new IOSDialog.Builder(context)
                .setCancelable(false)
                .setSpinnerClockwise(false)
                .setMessageContentGravity(Gravity.END)
                .build();

        PRDownloader.initialize(context);

        listview = view.findViewById(R.id.listview);
        listview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listview.setNestedScrollingEnabled(false);

        swiperefresh=view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                Call_Api_For_get_allsound();
            }
        });

        Call_Api_For_get_allsound();
        return view;
    }


    public void Set_adapter(){

        adapter=new Favourite_Sound_Adapter(context, datalist, new Favourite_Sound_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view,int postion, Sounds_GetSet item) {

                if(view.getId()==R.id.done){
                    StopPlaying();
                    Down_load_mp3(item.id,item.sound_name,item.acc_path);
                }
                else if(view.getId()==R.id.fav_btn){
                    Call_Api_For_Fav_sound(item.id);
                }
                else {
                    if (thread != null && !thread.isAlive()) {
                        StopPlaying();
                        playaudio(view, item);
                    } else if (thread == null) {
                        StopPlaying();
                        playaudio(view, item);
                    }
                }

            }
        });

        listview.setAdapter(adapter);


    }



    private void Call_Api_For_get_allsound() {
        datalist=new ArrayList<>();
        DatabaseReference mDatabase=FirebaseDatabase.getInstance().getReference("users").child(Variables.sharedPreferences.getString(Variables.u_id,"0")).child("Fav_sounds");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapsot:dataSnapshot.getChildren())
                {
                    String ar[]=postSnapsot.getKey().split("-");
                    final String Category=ar[0];
                    String name=ar[1];
                    DatabaseReference mDatabase2=FirebaseDatabase.getInstance().getReference("Sound").child(Category).child(name);
                    mDatabase2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            Parse_data(dataSnapshot2, Category);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void Parse_data(DataSnapshot postSnapshot2, String category){



        try {

            Sounds_GetSet item = new Sounds_GetSet();
            System.out.println(postSnapshot2.hasChild("id"));

            item.id = postSnapshot2.child("id").getValue().toString();

            item.acc_path =  postSnapshot2.child("path").getValue().toString();


            item.sound_name =  postSnapshot2.child("name").getValue().toString();;
            item.description =  postSnapshot2.child("description").getValue().toString();;
            item.section =  category;
            item.thum =  postSnapshot2.child("thum").getValue().toString();;
            item.date_created =  postSnapshot2.child("date_created").getValue().toString();

                    datalist.add(item);
                    Set_adapter();

            swiperefresh.setRefreshing(false);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    @Override
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }




    View previous_view;
    Thread thread;
    SimpleExoPlayer player;
    String previous_url="none";
    public void playaudio(View view, final Sounds_GetSet item){
        previous_view=view;

        if(previous_url.equals(item.acc_path)){

            previous_url="none";
            running_sound_id="none";
        }else {

            previous_url=item.acc_path;
            running_sound_id=item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "TikTok"));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(item.acc_path));


            player.prepare(videoSource);
            player.addListener(this);


            player.setPlayWhenReady(true);



        }

    }


    public void StopPlaying(){
        if(player!=null){
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }




    @Override
    public void onStart() {
        super.onStart();
        active=true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active=false;

        running_sound_id="null";

        if(player!=null){
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }




    public void Show_Run_State(){

        if (previous_view != null) {
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.done).setVisibility(View.VISIBLE);
        }

    }


    public void Show_loading_state(){
        previous_view.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previous_view.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void show_Stop_state(){

        if (previous_view != null) {
            previous_view.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            previous_view.findViewById(R.id.done).setVisibility(View.GONE);
        }

        running_sound_id="none";

    }



    ProgressDialog progressDialog;
    public void Down_load_mp3(final String id,final String sound_name, String url){
        progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Please wait...Downloading");
        progressDialog.setCancelable(false);
        Functions.Show_determinent_loader(this.getContext(),false,false);

        prDownloader= PRDownloader.download(url, Variables.app_folder, Variables.SelectedAudio_AAC)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        Functions.Show_loading_progress((int)(progress.currentBytes/progress.totalBytes)*100);

                    }
                });

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                Functions.cancel_determinent_loader();
                progressDialog.show();
                Convert_Mp3_to_acc(sound_name,id);

            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });

    }
    public void Convert_Mp3_to_acc(final String sound_name, final String id){

        AndroidAudioConverter.load(MainMenuActivity.mainMenuActivity, new ILoadCallback() {
            @Override
            public void onSuccess() {
                File flacFile = new File(Variables.app_folder, Variables.SelectedAudio_MP3);

                IConvertCallback callback = new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {
                        Log.d("TAG", "Finished command : ffmpeg " );
                        progressDialog.dismiss();
                        Intent output = new Intent();
                        output.putExtra("isSelected","yes");
                        output.putExtra("sound_name",sound_name);
                        output.putExtra("sound_id",id);
                        getActivity().setResult(RESULT_OK, output);
                        getActivity().finish();
                        getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);


                    }
                    @Override
                    public void onFailure(Exception error) {

                        Toast.makeText(MainMenuActivity.mainMenuActivity, ""+error, Toast.LENGTH_SHORT).show();
                    }
                };
                AndroidAudioConverter.with(MainMenuActivity.mainMenuActivity)
                        .setFile(flacFile)
                        .setFormat(AudioFormat.AAC)
                        .setCallback(callback)
                        .convert();
            }

            @Override
            public void onFailure(Exception error) {
                progressDialog.dismiss();
            }
        });



    }

    public String getRealPathFromURI(Uri contentUri)
    {
        String[] proj = { MediaStore.Audio.Media.DATA };
        Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private void executeCutVideoCommand(final String sound_name, final String id) {
        File moviesDir = new File(Variables.app_folder);

        String filePrefix = "cut_audio";
        String fileExtn = ".mp3";

        String yourRealPath  =Variables.app_folder+Variables.SelectedAudio_MP3;
        File dest = new File(Variables.app_folder+Variables.SelectedAudio_AAC);

        Log.d("TAG", "startTrim: src: " + yourRealPath);
        Log.d("TAG", "startTrim: dest: " + dest.getAbsolutePath());


        String filePath = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};

        String[] complexCommand = {"-i", yourRealPath, "-c:a", "aac" ,"-b:a", "192k",filePath};
        execFFmpegBinary(complexCommand,sound_name,id);

    }


    private void execFFmpegBinary(final String[] command, final String sound_name, final String id) {
        FFmpeg ffmpeg = FFmpeg.getInstance(MainMenuActivity.mainMenuActivity);
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d("TAG", "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d("TAG", "SUCCESS with output : " + s);
                    //You have to create a class of Preview Activity
                    //If you don't have please remove below Intent code

                }

                @Override
                public void onProgress(String s) {

                    Log.d("TAG", "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d("TAG", "Started command : ffmpeg " + command);

                }

                @Override
                public void onFinish() {
                    Log.d("TAG", "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                    Intent output = new Intent();
                    output.putExtra("isSelected","yes");
                    output.putExtra("sound_name",sound_name);
                    output.putExtra("sound_id",id);
                    getActivity().setResult(RESULT_OK, output);
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }



    private void Call_Api_For_Fav_sound(String video_id) {

        iosDialog.show();

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));
            parameters.put("sound_id",video_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(context, Variables.fav_sound, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                iosDialog.cancel();
            }
        });


    }


    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if(playbackState==Player.STATE_BUFFERING){
            Show_loading_state();
        }
        else if(playbackState==Player.STATE_READY){
            Show_Run_State();
        }else if(playbackState==Player.STATE_ENDED){
            show_Stop_state();
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }





}
