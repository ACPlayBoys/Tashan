package com.play.Tashan.Discover;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.play.Tashan.Home.Home_Get_Set;
import com.play.Tashan.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.ApiRequest;
import com.play.Tashan.SimpleClasses.Callback;
import com.play.Tashan.SimpleClasses.Variables;
import com.play.Tashan.Splash_A;
import com.play.Tashan.WatchVideos.WatchVideos_F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executor;

import javax.security.auth.login.LoginException;

/**
 * A simple {@link Fragment} subclass.
 */
public class Discover_F extends RootFragment {

    View view;
    Context context;

    RecyclerView recyclerView;
    EditText search_edit;


    SwipeRefreshLayout swiperefresh;

    public Discover_F() {
        // Required empty public constructor
    }

    ArrayList<Discover_Get_Set> datalist;

    Discover_Adapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_discover, container, false);
        context=getContext();


        datalist=new ArrayList<>();


        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context,4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter=new Discover_Adapter(context, datalist, new Discover_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(ArrayList<Home_Get_Set> datalist, int postion) {
                OpenWatchVideo(postion,datalist);
            }
        });



        recyclerView.setAdapter(adapter);


        search_edit=view.findViewById(R.id.search_edit);
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String query=search_edit.getText().toString();
                if(adapter!=null)
                    adapter.getFilter().filter(query);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        swiperefresh=view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Call_Api_For_get_Allvideos();
            }
        });


        Call_Api_For_get_Allvideos();

        return view;
    }


    // Bottom two function will get the Discover videos
    // from api and parse the json data which is shown in Discover tab

    private void Call_Api_For_get_Allvideos() {
        datalist.clear();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Content");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference("users").child(postSnapshot.getValue().toString());
                    mDatabase1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Parse_data(dataSnapshot);
                            Log.e("discover",dataSnapshot.getKey());

                            swiperefresh.setRefreshing(false);

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


    public void Parse_data(DataSnapshot snapshot){



        try {
            for (DataSnapshot postSnapshot: snapshot.child("uploads").getChildren()) {
                Discover_Get_Set discover_get_set=new Discover_Get_Set();
                ArrayList<Home_Get_Set> video_list = new ArrayList<>();


                Home_Get_Set item=new Home_Get_Set();
                item.fb_id=snapshot.child("id").getValue().toString();;

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
                    if(postSnapshot.child("likedUsers").hasChild(Variables.sharedPreferences.getString(Variables.u_id, ""))){
                        item.liked = "" + postSnapshot.child("likedUsers").child(Variables.sharedPreferences.getString(Variables.u_id, "")).getValue().toString();
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
                discover_get_set.title=item.video_description;
                Log.e("discover",item.video_description);
                video_list.add(item);
                discover_get_set.arrayList=video_list;


                datalist.add(discover_get_set);
            }







                adapter.notifyDataSetChanged();



        } catch (Exception e) {
            Log.e("discover",e.getLocalizedMessage());
        }

    }



    // When you click on any Video a new activity is open which will play the Clicked video
    private void OpenWatchVideo(int postion,ArrayList<Home_Get_Set> data_list) {

        Intent intent=new Intent(getActivity(),WatchVideos_F.class);
        Log.e("anikete",String.valueOf(postion));

        intent.putExtra("arraylist", data_list);
        intent.putExtra("position",0);
        startActivity(intent);

    }




}
