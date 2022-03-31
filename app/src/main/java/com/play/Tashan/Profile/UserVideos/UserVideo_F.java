package com.play.Tashan.Profile.UserVideos;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.play.Tashan.Home.Home_Get_Set;
import com.play.Tashan.Profile.MyVideos_Adapter;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.ApiRequest;
import com.play.Tashan.SimpleClasses.Callback;
import com.play.Tashan.SimpleClasses.Variables;
import com.play.Tashan.WatchVideos.WatchVideos_F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserVideo_F extends Fragment {

    public RecyclerView recyclerView;
     ArrayList<Home_Get_Set> data_list;
     MyVideos_Adapter adapter;
     View view;
     Context context;
     String user_id;

    RelativeLayout no_data_layout;
     public static int myvideo_count=0;

    public UserVideo_F() {

    }


    @SuppressLint("ValidFragment")
    public UserVideo_F(String user_id) {

        this.user_id=user_id;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_user_video, container, false);

        context=getContext();


        recyclerView=view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        data_list=new ArrayList<>();
        adapter=new MyVideos_Adapter(context, data_list, new MyVideos_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion,Home_Get_Set item, View view) {

                OpenWatchVideo(postion);

            }
        });

        recyclerView.setAdapter(adapter);

        no_data_layout=view.findViewById(R.id.no_data_layout);

        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
            Call_Api_For_get_Allvideos();
        }



        return view;

    }

    Boolean isVisibleToUser=false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser=isVisibleToUser;
        if(view!=null && isVisibleToUser){
            Call_Api_For_get_Allvideos();
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        if((view!=null && isVisibleToUser) && (!data_list.isEmpty() && !is_api_run)){
            Call_Api_For_get_Allvideos();
        }
    }


    Boolean is_api_run=false;
    //this will get the all videos data of user and then parse the data
    private void Call_Api_For_get_Allvideos() {
        is_api_run=true;

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users").child(user_id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                 Parse_data(snapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });




    }

    public void Parse_data(DataSnapshot snapshot){

        data_list.clear();

        try {
                String user_videos=snapshot.child("user_videos").getValue().toString();
                if(!user_videos.toString().equals("0")){


                    no_data_layout.setVisibility(View.GONE);

                    for (DataSnapshot postSnapshot: snapshot.child("uploads").getChildren()) {


                        Home_Get_Set item=new Home_Get_Set();
                        item.fb_id=user_id;

                        item.first_name=snapshot.child("first_name").getValue().toString();

                        item.last_name=snapshot.child("last_name").getValue().toString();
                        item.profile_pic=snapshot.child("profile_pic").getValue().toString();
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
                            if(postSnapshot.child("likedUsers").hasChild(user_id)){
                                item.liked = "" + postSnapshot.child("likedUsers").child(user_id).getValue().toString();
                            }
                        }
                       // item.views=postSnapshot.child("view").toString();


                        item.sound_id=postSnapshot.child("sound_id").toString();
                        //item.sound_name=sound_data.optString("sound_name");
                        //item.sound_pic=sound_data.optString("thum");


                        item.video_id=""+postSnapshot.getKey().toString();
                        Log.e("aniket",item.liked);
                        item.gif=""+postSnapshot.child("gif").getValue().toString();

                        item.video_url=""+postSnapshot.child("url").getValue().toString();
                        item.thum=""+postSnapshot.child("thumb").getValue().toString();
                        item.created_date=""+postSnapshot.child("created").getValue().toString();

                        item.video_description=""+postSnapshot.child("description").getValue().toString();


                        data_list.add(item);
                    }

                    myvideo_count=data_list.size();

                }else {
                    no_data_layout.setVisibility(View.VISIBLE);
                }




                adapter.notifyDataSetChanged();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }





    private void OpenWatchVideo(int postion) {

        Intent intent=new Intent(getActivity(),WatchVideos_F.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position",postion);
        startActivity(intent);

    }



}
