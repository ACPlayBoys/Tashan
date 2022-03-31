package com.play.Tashan.Comments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.play.Tashan.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.play.Tashan.R;
import com.play.Tashan.SimpleClasses.API_CallBack;
import com.play.Tashan.SimpleClasses.ApiRequest;
import com.play.Tashan.SimpleClasses.Fragment_Data_Send;
import com.play.Tashan.SimpleClasses.Functions;
import com.play.Tashan.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A simple {@link Fragment} subclass.
 */
public class Comment_F extends RootFragment {

    View view;
    Context context;

    RecyclerView recyclerView;

    Comments_Adapter adapter;

    ArrayList<Comment_Get_Set> data_list;

    String video_id;
    String user_id;

    EditText message_edit;
    ImageButton send_btn;
    ProgressBar send_progress;

    TextView comment_count_txt;

    FrameLayout comment_screen;

    public static int comment_count=0;
    public Comment_F() {

    }

    Fragment_Data_Send fragment_data_send;
    @SuppressLint("ValidFragment")
    public Comment_F(int count, Fragment_Data_Send fragment_data_send){
        comment_count=count;
        this.fragment_data_send=fragment_data_send;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_comment, container, false);
        context=getContext();


        comment_screen=view.findViewById(R.id.comment_screen);
        comment_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();

            }
        });

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();
            }
        });


        Bundle bundle=getArguments();
        if(bundle!=null){
            video_id=bundle.getString("video_id");
            user_id=bundle.getString("user_id");
        }


        comment_count_txt=view.findViewById(R.id.comment_count);

        recyclerView=view.findViewById(R.id.recylerview);
        LinearLayoutManager layoutManager=new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);


        data_list=new ArrayList<>();
        adapter=new Comments_Adapter(context, data_list, new Comments_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, Comment_Get_Set item, View view) {


            }
        });

        recyclerView.setAdapter(adapter);


        message_edit=view.findViewById(R.id.message_edit);


        send_progress=view.findViewById(R.id.send_progress);
        send_btn=view.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message=message_edit.getText().toString();
                if(!TextUtils.isEmpty(message)){
                    if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)){
                    Send_Comments(video_id,message);
                    message_edit.setText(null);
                    send_progress.setVisibility(View.VISIBLE);
                    send_btn.setVisibility(View.GONE);
                    }
                    else {
                        Toast.makeText(context, "Please Login into the app", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });




        Get_All_Comments();


        return view;
    }


    @Override
    public void onDetach() {
        Functions.hideSoftKeyboard(getActivity());

        super.onDetach();
    }

    // this funtion will get all the comments against post
    public void Get_All_Comments(){

        Functions.Call_Api_For_get_Comment(getActivity(), user_id,video_id, new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {
                ArrayList<Comment_Get_Set> arrayList1=arrayList;
                for(Comment_Get_Set item:arrayList1){
                     data_list.add(item);
                }
                comment_count_txt.setText(data_list.size()+" comments");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void OnSuccess(String responce) {

            }

            @Override
            public void OnFail(String responce) {

            }

        });

    }




    // this function will call an api to upload your comment
    public void Send_Comments(String video_id, final String comment){

        Functions.Call_Api_For_Send_Comment(getActivity(),user_id, video_id,comment ,new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {

                ArrayList<Comment_Get_Set> arrayList1=arrayList;
                for(Comment_Get_Set item:arrayList1){
                    data_list.add(0,item);
                    comment_count++;

                    SendPushNotification(getActivity(),user_id,comment);

                    comment_count_txt.setText(comment_count+" comments");

                    if(fragment_data_send!=null)
                        fragment_data_send.onDataSent(""+comment_count);

                }
                adapter.notifyDataSetChanged();
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);
            }

            @Override
            public void OnSuccess(String responce) {

            }

            @Override
            public void OnFail(String responce) {

            }
        });

    }



    public  void SendPushNotification(Activity activity,String user_id,String comment){

        final JSONObject notimap= new JSONObject();
        try {
            notimap.put("title",Variables.sharedPreferences.getString(Variables.u_name,"")+" Comment on your video");
            notimap.put("message",comment);
            notimap.put("icon",Variables.sharedPreferences.getString(Variables.u_pic,""));
            notimap.put("senderid",Variables.sharedPreferences.getString(Variables.u_id,""));
            notimap.put("receiverid", user_id);
            notimap.put("action_type","comment");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {

                    try {
                        sendPushNotification(notimap);
                    }catch(Exception e){}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        //ApiRequest.Call_Api(context,"https://onesignal.com/api/v1/notifications",notimap,null);


    }
    public static String sendPushNotification(JSONObject obj)
            throws IOException, JSONException {
        final  String AUTH_KEY_FCM ="AAAASE4Ojm8:APA91bHt12BC9gHPjzXELOkt-TfrnuHpawJFS6sEy5bQGHA8uTU37gvHBvAuUz6nO7C7_apNTjBedf7PBav4nYVJZLO53vKC0E3FM6jggbnBIPJOaW7XFlsjIXz8GUBecSbaK4l0msFl";
        String authKey = AUTH_KEY_FCM; // You FCM AUTH key
        String FMCurl =  "https://fcm.googleapis.com/fcm/send";;
        String DEVICE_ID= FirebaseInstanceId.getInstance().getToken();

        String DeviceIdKey = "/topics/test";
        System.out.println(DeviceIdKey);

        try {
            URL url = new URL(FMCurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key=" + authKey);
            conn.setRequestProperty("Content-Type", "application/json");
            System.out.println(DeviceIdKey);
            JSONObject data = new JSONObject();
            data.put("to", DeviceIdKey.trim());
            JSONObject info = new JSONObject();
            info.put("title", "FCM Notificatoin Title"); // Notification title
            info.put("body", "Hello First Test notification"); // Notification body
            data.put("data", obj);
            System.out.println(data.toString());
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data.toString());
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Resonse: " + response);

        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return "a";

    }






}
