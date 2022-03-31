package com.play.Tashan.Home;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.play.Tashan.SimpleClasses.ApiRequest;
import com.play.Tashan.SimpleClasses.Callback;
import com.play.Tashan.SoundLists.VideoSound_A;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.play.Tashan.Comments.Comment_F;
import com.play.Tashan.Main_Menu.MainMenuActivity;
import com.play.Tashan.Main_Menu.MainMenuFragment;
import com.play.Tashan.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.play.Tashan.Profile.Profile_F;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.API_CallBack;
import com.play.Tashan.SimpleClasses.Fragment_Callback;
import com.play.Tashan.SimpleClasses.Fragment_Data_Send;
import com.play.Tashan.SimpleClasses.Functions;
import com.play.Tashan.SimpleClasses.Variables;
import com.play.Tashan.Taged.Taged_Videos_F;
import com.play.Tashan.VideoAction.VideoAction_F;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
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
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */

// this is the main view which is show all  the video in list
public class Home_F extends RootFragment implements Player.EventListener, Fragment_Data_Send {

    View view;
    Context context;


    RecyclerView recyclerView;
    ArrayList<Home_Get_Set> data_list;
    int currentPage=-1;
    LinearLayoutManager layoutManager;

    ProgressBar p_bar;

    SwipeRefreshLayout swiperefresh;

    public Home_F() {
        // Required empty public constructor
    }

    int swipe_count=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_home, container, false);
        context=getContext();

        p_bar=view.findViewById(R.id.p_bar);

        recyclerView=view.findViewById(R.id.recylerview);
        layoutManager=new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper =  new PagerSnapHelper();
         snapHelper.attachToRecyclerView(recyclerView);



        // this is the scroll listener of recycler view which will tell the current item number
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no=scrollOffset / height;

                if(page_no!=currentPage ){
                    currentPage=page_no;

                    Release_Privious_Player();
                    System.out.println("cpage"+currentPage+"  pPage "+page_no);
                    Set_Player(currentPage);

                }
            }
        });



        swiperefresh=view.findViewById(R.id.swiperefresh);
        swiperefresh.setProgressViewOffset(false, 0, 200);


        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage=-1;
                Call_Api_For_get_Allvideos();
            }
        });

        Call_Api_For_get_Allvideos();

        Load_add();

        return view;
    }




    InterstitialAd mInterstitialAd;
    public void Load_add() {

        // this is test app id you will get the actual id when you add app in your
        //add mob account
        MobileAds.initialize(context,
                getResources().getString(R.string.ad_app_id));


        //code for intertial add
        mInterstitialAd = new InterstitialAd(context);

        //here we will get the add id keep in mind above id is app id and below Id is add Id
        mInterstitialAd.setAdUnitId(context.getResources().getString(R.string.my_Interstitial_Add));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });


    }



    boolean is_add_show=false;
    Home_Adapter adapter;
    public void Set_Adapter(){

         adapter=new Home_Adapter(context, data_list, new Home_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, final Home_Get_Set item, View view) {

                switch(view.getId()) {

                    case R.id.user_pic:
                        onPause();
                        OpenProfile(item,false);
                        break;

                    case R.id.username:
                        onPause();
                        OpenProfile(item,false);
                        break;

                    case R.id.like_layout:
                        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
                        Like_Video(postion, item);
                        }else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.comment_layout:
                        OpenComment(item);
                        break;

                    case R.id.shared_layout:
                        if (!is_add_show && mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                            is_add_show = true;
                        } else{
                            is_add_show=false;
                            final VideoAction_F fragment = new VideoAction_F(item.video_id, new Fragment_Callback() {
                                @Override
                                public void Responce(Bundle bundle) {

                                    if(bundle.getString("action").equals("save")){
                                        Save_Video(item);
                                    }
                                }
                            });
                            fragment.show(getChildFragmentManager(), "");
                        }

                        break;


                    case R.id.sound_image_layout:
                        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
                            if(check_permissions()) {
                                Intent intent = new Intent(getActivity(), VideoSound_A.class);
                                intent.putExtra("data", item);
                                startActivity(intent);
                            }
                        }else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }

                        break;
                }

            }
        });

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

    }



    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void Call_Api_For_get_Allvideos() {
        Log.e("man","calling");
        data_list=new ArrayList<>();
        try {
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Content");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();

                    Log.e("man",String.valueOf(count));
                    long length = 5;
                    if (count < length)
                        length = count;
                    for (long l = 1; l <= length; l++) {
                        long questionCount = count;
                        long rand = ThreadLocalRandom.current().nextLong(questionCount);
                        Iterator itr = dataSnapshot.getChildren().iterator();
                        for (int i = 0; i < rand; i++) {
                            if (itr.hasNext())
                                itr.next();
                        }
                        DataSnapshot childSnapshot;
                        if (itr.hasNext())
                            childSnapshot = (DataSnapshot) itr.next();
                        else
                            childSnapshot = (DataSnapshot) itr;
                        Log.e("man", childSnapshot.getKey());
                        final String key=childSnapshot.getKey();
                        DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference("users").child(childSnapshot.getValue().toString());//.child("uploads").child(childSnapshot.getKey());
                        mDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                swiperefresh.setRefreshing(false);
                                Parse_data(dataSnapshot,key);
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
        }catch (Exception e){Log.e("man",e.getLocalizedMessage());}




    }
    int set=0;

    public void Parse_data(DataSnapshot snapshot,String key){



        try {

                    Home_Get_Set item=new Home_Get_Set();
                    item.fb_id=snapshot.child("id").getValue() .toString();

                    //JSONObject user_info=itemdata.optJSONObject("user_info");
                    item.first_name=snapshot.child("first_name").getValue().toString();
                    item.last_name=snapshot.child("last_name").getValue().toString();
                    item.profile_pic=snapshot.child("profile_pic").getValue().toString();
                    DataSnapshot postSnapshot=snapshot.child("uploads").child(key);

                    //JSONObject sound_data=itemdata.optJSONObject("sound");
                    item.sound_id=postSnapshot.child("sound_id").toString();
                    //item.sound_name=sound_data.optString("sound_name");
                    //item.sound_pic=sound_data.optString("thum");




                    //JSONObject count=itemdata.optJSONObject("count");
                    item.heart="0";
                    if(snapshot.hasChild("total_heart"))
                    {
                         item.heart=String.valueOf(snapshot.child("total_heart").getValue());

                     }
            item.like_count="0";
            if(postSnapshot.hasChild("likedUsers"))
            {
                if(postSnapshot.child("likedUsers").hasChildren()){
                    item.like_count=String.valueOf(postSnapshot.child("likedUsers").getChildrenCount());
                }
            }
                    item.video_comment_count="0";
                    if((postSnapshot.hasChild("comments"))){
                        if(postSnapshot.child("comments").hasChildren()){
                            item.video_comment_count=String.valueOf(postSnapshot.child("comments").getChildrenCount());
                        }
                    }
                    item.video_id=""+postSnapshot.getKey().toString();
                    item.liked="0";
                    if(postSnapshot.hasChild("likedUsers")){
                        if(postSnapshot.child("likedUsers").hasChild(Variables.sharedPreferences.getString(Variables.u_id, ""))){
                            item.liked = "" + postSnapshot.child("likedUsers").child(Variables.sharedPreferences.getString(Variables.u_id, "")).getValue().toString();
                        }
                    }
                    item.video_url=""+postSnapshot.child("url").getValue().toString();
                    item.video_description=""+postSnapshot.child("description").getValue().toString();
                    item.thum = "";
                    if((postSnapshot.child("thumb").getValue()!=null))
                        item.thum = "" + postSnapshot.child("thumb").getValue().toString();

                    item.created_date=""+postSnapshot.child("created").getValue().toString();

                    data_list.add(item);
                    if(set==0) {
                        Set_Adapter();set=1;
                    }
                    else
                        adapter.notifyDataSetChanged();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private void Call_Api_For_Singlevideos(final int postion,boolean ch) {

        try {
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Content");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();

                    Log.e("man",String.valueOf(count));
                    long length = 5;
                    if (count < length)
                        length = count;
                    long questionCount = count;
                    long rand = ThreadLocalRandom.current().nextLong(questionCount);
                    Iterator itr = dataSnapshot.getChildren().iterator();
                    for (int i = 0; i < rand; i++) {
                        if (itr.hasNext())
                            itr.next();
                    }
                    DataSnapshot childSnapshot;
                    if (itr.hasNext())
                        childSnapshot = (DataSnapshot) itr.next();
                    else
                        childSnapshot = (DataSnapshot) itr;
                    Log.e("man", childSnapshot.getKey());
                    final String key=childSnapshot.getKey();
                    DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference("users").child(childSnapshot.getValue().toString());//.child("uploads").child(childSnapshot.getKey());
                    mDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Singal_Video_Parse_data(dataSnapshot,key,postion);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){Log.e("man",e.getLocalizedMessage());}
    }

    public void Singal_Video_Parse_data(DataSnapshot snapshot,String key,int pos){



        try {

            Home_Get_Set item=new Home_Get_Set();
            item.fb_id=snapshot.child("id").getValue() .toString();

            //JSONObject user_info=itemdata.optJSONObject("user_info");
            item.first_name=snapshot.child("first_name").getValue().toString();
            item.last_name=snapshot.child("last_name").getValue().toString();
            item.profile_pic=snapshot.child("profile_pic").getValue().toString();
            DataSnapshot postSnapshot=snapshot.child("uploads").child(key);

            //JSONObject sound_data=itemdata.optJSONObject("sound");
            item.sound_id=postSnapshot.child("sound_id").toString();
            //item.sound_name=sound_data.optString("sound_name");
            //item.sound_pic=sound_data.optString("thum");




            //JSONObject count=itemdata.optJSONObject("count");
            item.like_count="0";
            if(snapshot.hasChild("total_heart"))
            {
                    item.like_count=String.valueOf(snapshot.child("total_heart").getValue());

            }
            item.video_comment_count="0";
            if((postSnapshot.hasChild("comments"))){
                if(postSnapshot.child("comments").hasChildren()){
                    item.video_comment_count=String.valueOf(postSnapshot.child("comments").getChildrenCount());
                }
            }
            item.views=String.valueOf(postSnapshot.child("views").getChildrenCount());

            item.video_id=""+postSnapshot.getKey().toString();
            item.liked="0";
            if(postSnapshot.hasChild("likedUsers")){
                if(postSnapshot.child("likedUsers").hasChild(Variables.sharedPreferences.getString(Variables.u_id, ""))){
                    item.liked = "" + postSnapshot.child("likedUsers").child(Variables.sharedPreferences.getString(Variables.u_id, "")).getValue().toString();
                }
            }
            item.video_url=""+postSnapshot.child("url").getValue().toString();
            item.video_description=""+postSnapshot.child("description").getValue().toString();
            item.thum=""+postSnapshot.child("thumb").getValue().toString();
            item.created_date=""+postSnapshot.child("created").getValue().toString();

            data_list.add(item);
            dataSourceFactory2 = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "Tashan"));
            int pos2=pos+1;
            Log.d("tag",String.valueOf(pos2));

            videoSource2 = new ExtractorMediaSource.Factory(dataSourceFactory2)
                    .createMediaSource(Uri.parse(data_list.get(pos2).video_url));
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player2 = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
            player2.prepare(videoSource2);
            loaded=false;





        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    // this will call when swipe for another video and
    // this function will set the player to the current video
    boolean loaded=true;
    DataSource.Factory dataSourceFactory2;
    MediaSource videoSource2;
    SimpleExoPlayer player2;

    public void Set_Player(final int currentPage){
        Log.d("tag",String.valueOf(currentPage));

            final Home_Get_Set item= data_list.get(currentPage);
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            SimpleExoPlayer tplayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
            DataSource.Factory dataSourceFactory;
            MediaSource videoSource;
            if(currentPage<2)
            {
                dataSourceFactory = new DefaultDataSourceFactory(context,
                        Util.getUserAgent(context, "Tashan"));

                 videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(item.video_url));
                tplayer.prepare(videoSource);




            }
            else
            {
                dataSourceFactory = dataSourceFactory2;

                videoSource = videoSource2;
                tplayer=player2;

            }
            final SimpleExoPlayer player = tplayer;

            Log.d("resp",item.video_url);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.addListener(this);





         View layout=layoutManager.findViewByPosition(currentPage);
         final PlayerView playerView=layout.findViewById(R.id.playerview);
          playerView.setPlayer(player);


        player.setPlayWhenReady(is_visible_to_user);
        privious_player=player;




        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                     super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                    if((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                        if(deltaX > 0)
                        {
                            OpenProfile(item,true);
                        }
                    }


                    return true;
                }

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    super.onSingleTapUp(e);
                    if(!player.getPlayWhenReady()){
                        privious_player.setPlayWhenReady(true);
                    }else{
                        privious_player.setPlayWhenReady(false);
                    }


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    Show_video_option(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if(!player.getPlayWhenReady()){
                        privious_player.setPlayWhenReady(true);
                    }


                    if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
                        Show_heart_on_DoubleTap(item, mainlayout, e);
                        Like_Video(currentPage, item);
                    }else {
                        Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
                    }
                    return super.onDoubleTap(e);

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        TextView desc_txt=layout.findViewById(R.id.desc_txt);
        HashTagHelper.Creator.create(context.getResources().getColor(R.color.maincolor), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {

                onPause();
                OpenHashtag(hashTag);

            }
        }).handle(desc_txt);



        LinearLayout soundimage = (LinearLayout)layout.findViewById(R.id.sound_image_layout);
        Animation sound_animation = AnimationUtils.loadAnimation(context,R.anim.d_clockwise_rotation);
        soundimage.startAnimation(sound_animation);

        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false))
        Functions.Call_Api_For_update_view(getActivity(),item.video_id,item.views,item.fb_id);


        swipe_count++;
        if(swipe_count>4){
            Show_add();
            swipe_count=0;
        }



        Call_Api_For_Singlevideos(currentPage,true);

    }


    public void Show_heart_on_DoubleTap(Home_Get_Set item,final RelativeLayout mainlayout,MotionEvent e){

        int x = (int) e.getX()-100;
        int y = (int) e.getY()-100;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(getApplicationContext());
        lp.setMargins(x, y, 0, 0);
        iv.setLayoutParams(lp);
        if(item.liked.equals("1"))
        iv.setImageDrawable(getResources().getDrawable(
                R.drawable.ic_like));
        else
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like_fill));

        mainlayout.addView(iv);
        Animation fadeoutani = AnimationUtils.loadAnimation(context,R.anim.fade_out);

        fadeoutani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainlayout.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv.startAnimation(fadeoutani);

    }



    public void Show_add(){
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }


    @Override
    public void onDataSent(String yourData) {
        int comment_count =Integer.parseInt(yourData);
        Home_Get_Set item=data_list.get(currentPage);
        item.video_comment_count=""+comment_count;
        data_list.remove(currentPage);
        data_list.add(currentPage,item);
        adapter.notifyDataSetChanged();
    }



    // this will call when go to the home tab From other tab.
    // this is very importent when for video play and pause when the focus is changes
    boolean is_visible_to_user;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        is_visible_to_user=isVisibleToUser;

        if(privious_player!=null && isVisibleToUser){
            privious_player.setPlayWhenReady(true);
        }else if(privious_player!=null && !isVisibleToUser){
            privious_player.setPlayWhenReady(false);
        }
    }



   // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer privious_player;
    public void Release_Privious_Player(){
        if(privious_player!=null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
    }




    // this function will call for like the video and Call an Api for like the video
    public void Like_Video(final int position, final Home_Get_Set home_get_set){
        String action=home_get_set.liked;

        if(action.equals("1")){
            action="0";
            home_get_set.like_count=""+(Integer.parseInt(home_get_set.like_count) -1);
            home_get_set.heart=""+(Integer.parseInt(home_get_set.heart) -1);
        }else {
            action="1";
            home_get_set.like_count=""+(Integer.parseInt(home_get_set.like_count) +1);
            home_get_set.heart=""+(Integer.parseInt(home_get_set.heart) +1);
        }


        data_list.remove(position);
        home_get_set.liked=action;
        data_list.add(position,home_get_set);
        adapter.notifyDataSetChanged();

        Functions.Call_Api_For_like_video(getActivity(), home_get_set.video_id,home_get_set.fb_id,home_get_set.like_count ,action);

    }



    // this will open the comment screen
    private void OpenComment(Home_Get_Set item) {

        int comment_counnt=Integer.parseInt(item.video_comment_count);

        Fragment_Data_Send fragment_data_send=this;

        Comment_F comment_f = new Comment_F(comment_counnt,fragment_data_send);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id",item.video_id);
        args.putString("user_id",item.fb_id);
        comment_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, comment_f).commit();


    }



    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenProfile(Home_Get_Set item,boolean from_right_to_left) {
        if(Variables.sharedPreferences.getString(Variables.u_id,"0").equals(item.fb_id)){

            TabLayout.Tab profile= MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        }else {
            Profile_F profile_f = new Profile_F(new Fragment_Callback() {
                @Override
                public void Responce(Bundle bundle) {
                    Call_Api_For_Singlevideos(currentPage,false);
                }
            });
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            if(from_right_to_left)
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            else
                transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

            Bundle args = new Bundle();
            args.putString("user_id", item.fb_id);
            args.putString("user_name",item.first_name+" "+item.last_name);
            args.putString("user_pic",item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, profile_f).commit();
        }

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenHashtag(String tag) {

            Taged_Videos_F taged_videos_f = new Taged_Videos_F();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
            Bundle args = new Bundle();
            args.putString("tag", tag);
            taged_videos_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, taged_videos_f).commit();


    }



    private void Show_video_option(final Home_Get_Set home_get_set) {

        final CharSequence[] options = { "Save Video","Cancel" };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context,R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Save Video"))

                {
                    if(Functions.Checkstoragepermision(getActivity()))
                    Save_Video(home_get_set);

                }


                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    public void Save_Video(final Home_Get_Set item){

        Functions.Show_determinent_loader(context,false,false);
        PRDownloader.initialize(getActivity().getApplicationContext());
        DownloadRequest prDownloader= PRDownloader.download(item.video_url, Variables.app_folder, item.video_id+"no_watermark"+".mp4")
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

                        int prog=(int)((progress.currentBytes*100)/progress.totalBytes);
                        Functions.Show_loading_progress(prog/2);

                    }
                });


              prDownloader.start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    Applywatermark(item);
                   }

                @Override
                public void onError(Error error) {
                    Delete_file_no_watermark(item);
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    Functions.cancel_determinent_loader();
                }


            });




    }

    public void Applywatermark(final Home_Get_Set item){

         Bitmap myLogo = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_watermark_image)).getBitmap();
         Bitmap bitmap_resize=Bitmap.createScaledBitmap(myLogo, 50, 50, false);
         GlWatermarkFilter filter=new GlWatermarkFilter(bitmap_resize, GlWatermarkFilter.Position.LEFT_TOP);
         new GPUMp4Composer(Variables.app_folder+item.video_id+"no_watermark"+".mp4",
                 Variables.app_folder+item.video_id+".mp4")
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp",""+(int) (progress*100));
                        Functions.Show_loading_progress((int)((progress*100)/2)+50);

                    }

                    @Override
                    public void onCompleted() {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancel_determinent_loader();
                                Delete_file_no_watermark(item);
                                Scan_file(item);

                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp",exception.toString());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Delete_file_no_watermark(item);
                                    Functions.cancel_determinent_loader();
                                    Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();

                                }catch (Exception e){

                                }
                            }
                        });

                    }
                })
                .start();
    }


    public void Delete_file_no_watermark(Home_Get_Set item){
        File file=new File(Variables.app_folder+item.video_id+"no_watermark"+".mp4");
        if(file.exists()){
            file.delete();
        }
    }

    public void Scan_file(Home_Get_Set item){
        MediaScannerConnection.scanFile(getActivity(),
                new String[] {Variables.app_folder+item.video_id+".mp4" },
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }



    public boolean is_fragment_exits(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if(fm.getBackStackEntryCount()==0){
            return false;
        }else {
            return true;
        }

    }

    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onResume() {
        super.onResume();
        if((privious_player!=null && is_visible_to_user) && !is_fragment_exits() ){
            privious_player.setPlayWhenReady(true);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if(privious_player!=null){
            privious_player.setPlayWhenReady(false);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if(privious_player!=null){
            privious_player.setPlayWhenReady(false);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(privious_player!=null){
            privious_player.release();
        }
    }



    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 2);
        }else {

            return true;
        }

        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }





    // Bottom all the function and the Call back listener of the Expo player
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
            p_bar.setVisibility(View.VISIBLE);
        }
        else if(playbackState==Player.STATE_READY){
             p_bar.setVisibility(View.GONE);
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
