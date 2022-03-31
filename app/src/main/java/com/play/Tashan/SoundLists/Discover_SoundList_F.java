package com.play.Tashan.SoundLists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class Discover_SoundList_F extends RootFragment implements Player.EventListener{

    RecyclerView listview;
    Sounds_Adapter adapter;
    ArrayList<Sound_catagory_Get_Set> datalist;

    DownloadRequest prDownloader;
    static boolean active = false;

    View view;
    Context context;

    IOSDialog iosDialog;


    SwipeRefreshLayout swiperefresh;


    public static String running_sound_id;

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


        datalist=new ArrayList<>();

        listview = view.findViewById(R.id.listview);
        listview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listview.setNestedScrollingEnabled(false);
        listview.setHasFixedSize(true);
        listview.getLayoutManager().setMeasurementCacheEnabled(false);


        swiperefresh=view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                previous_url="none";
                StopPlaying();
                Call_Api_For_get_allsound();
            }
        });
        Set_adapter();

        Call_Api_For_get_allsound();

        return view;
    }



    public void Set_adapter(){

        adapter=new Sounds_Adapter(context, datalist, new Sounds_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view,int postion, Sounds_GetSet item) {

                Log.d("resp",item.acc_path);

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
        DatabaseReference mDataBase= FirebaseDatabase.getInstance().getReference("Sound");
        mDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Parse_data(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        swiperefresh.setRefreshing(false);


    }



    public void Parse_data(DataSnapshot dataSnapshot){
        boolean check=false;


        try {

                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()) {

                    ArrayList<Sounds_GetSet> sound_list=new ArrayList<>();
                    Sound_catagory_Get_Set sound_catagory_get_set = new Sound_catagory_Get_Set();
                    System.out.println(postSnapshot.getKey());
                    for (DataSnapshot postSnapshot2:postSnapshot.getChildren()) {
                        System.out.println(postSnapshot2.getKey());

                        Sounds_GetSet item = new Sounds_GetSet();

                        item.id = postSnapshot2.child("id").getValue().toString();

                        item.acc_path =  postSnapshot2.child("path").getValue().toString();


                        item.sound_name =  postSnapshot2.child("name").getValue().toString();;
                        item.description =  postSnapshot2.child("description").getValue().toString();;
                        item.section =  postSnapshot.getKey();
                        item.thum =  postSnapshot2.child("thum").getValue().toString();;
                        item.date_created =  postSnapshot2.child("date_created").getValue().toString();;

                        sound_list.add(item);
                    }


                    sound_catagory_get_set.catagory = postSnapshot.getKey();
                    sound_catagory_get_set.sound_list = sound_list;

                    datalist.add(sound_catagory_get_set);
                    if(check)
                    {
                        adapter.notifyDataSetChanged();
                        check=true;
                    }
                    else
                    {
                        Set_adapter();
                        check=false;
                    }

                }






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



        prDownloader= PRDownloader.download(url, Variables.app_folder, Variables.SelectedAudio_MP3)
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



    private void Call_Api_For_Fav_sound(String video_id) {

        iosDialog.show();

        FirebaseDatabase.getInstance().getReference("users").child(Variables.sharedPreferences.getString(Variables.u_id,"0")).child("Fav_sounds").child(video_id).setValue(1);


                iosDialog.cancel();


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
