package com.play.Tashan.Profile.Liked_Videos;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
public class Liked_Video_F extends Fragment {

   public static RecyclerView recyclerView;
    ArrayList<Home_Get_Set> data_list;
    MyVideos_Adapter adapter;

    View view;
    Context context;

    String user_id;

    RelativeLayout no_data_layout;

    public Liked_Video_F() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public Liked_Video_F(String user_id) {
        this.user_id=user_id;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_user_likedvideo, container, false);

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

        Call_Api_For_get_Allvideos();




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
    public long lenc=0;
    public long len=0;
    private void Call_Api_For_get_Allvideos() {
        is_api_run=true;

        final DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference("users").child(user_id).child("Liked_Videos");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    no_data_layout.setVisibility(View.GONE);
                    data_list.clear();
                    len=dataSnapshot.getChildrenCount();
                    lenc=0;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final String key = postSnapshot.getKey();
                        DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference("users").child(postSnapshot.getValue().toString());
                        mDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Parse_data(dataSnapshot, key);
                                Log.e("discover", dataSnapshot.getKey());


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }
                else{

                    no_data_layout.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }


    public void Parse_data(DataSnapshot snapshot,String key){



        try {
            lenc++;


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
            item.thum=""+postSnapshot.child("thumb").getValue().toString();
            item.created_date=""+postSnapshot.child("created").getValue().toString();

            data_list.add(item);
            if(len==lenc)
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



       /* try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");

                JSONObject data=msgArray.getJSONObject(0);
                JSONObject user_info=data.optJSONObject("user_info");

                JSONArray user_videos=data.getJSONArray("user_videos");


                if(!user_videos.toString().equals("["+"0"+"]")){

                    no_data_layout.setVisibility(View.GONE);

                    for (int i=0;i<user_videos.length();i++) {
                        JSONObject itemdata = user_videos.optJSONObject(i);

                        Home_Get_Set item=new Home_Get_Set();
                        item.fb_id=itemdata.optString("fb_id");

                        item.first_name=user_info.optString("first_name");
                        item.last_name=user_info.optString("last_name");
                        item.profile_pic=user_info.optString("profile_pic");

                        JSONObject count=itemdata.optJSONObject("count");
                        item.like_count=count.optString("like_count");
                        item.video_comment_count=count.optString("video_comment_count");
                        item.views=count.optString("view");


                        JSONObject sound_data=itemdata.optJSONObject("sound");
                        item.sound_id=sound_data.optString("id");
                        item.sound_name=sound_data.optString("sound_name");
                        item.sound_pic=sound_data.optString("thum");



                        item.video_id=itemdata.optString("id");
                        item.liked=itemdata.optString("liked");
                        item.gif=Variables.base_url+itemdata.optString("gif");
                        item.video_url=Variables.base_url+itemdata.optString("video");
                        item.thum=Variables.base_url+itemdata.optString("thum");
                        item.created_date=itemdata.optString("created");
                        item.video_description=itemdata.optString("description");

                        data_list.add(item);
                    }

                }else {
                    no_data_layout.setVisibility(View.VISIBLE);
                }



                adapter.notifyDataSetChanged();

            }else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }*/



    private void OpenWatchVideo(int postion) {
        Intent intent=new Intent(getActivity(),WatchVideos_F.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position",postion);
        startActivity(intent);
    }



}
