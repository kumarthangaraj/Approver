package com.ev.approver;

/**
 * Created by Kumar_Thangaraj on 7/13/2018.
 */
import android.content.Context;

import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class ConnectionManager {
    private static ConnectionManager instance = null;
    private static Context context;
    private RequestQueue queue;
    private String mode = "http://";
    private String serverPort = "192.168.43.81:3000";
    private String accessToken="";

    public static ConnectionManager getInstance(Context context){
        if(instance == null){
            instance = new ConnectionManager(context);
        }
        return instance;
    }

    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    private ConnectionManager(Context context){
        this.context = context;
        queue = Volley.newRequestQueue(this.context);
    }

    public String getServerPort(){
        return serverPort;
    }

    public String getMode(){
        return mode;
    }

    public String getUrl(String restApi){
        return mode+serverPort+"/"+restApi;
    }

    public String getUrlWithAccessToken(String restApi){
        return mode+serverPort+"/"+restApi+"?access_token="+accessToken;
    }

    public void addToRequestQueue(JsonObjectRequest jsonRequest){
        queue.add(jsonRequest);
    }

    public void addToRequestQueue(CustomJsonRequest customJsonRequest){
        queue.add(customJsonRequest);
    }

    public void addToRequestQueue(JsonArrayRequest jsonArrayRequest){
        queue.add(jsonArrayRequest);
    }

    public void addToRequestQueue(StringRequest stringRequest){
        queue.add(stringRequest);
    }
}
