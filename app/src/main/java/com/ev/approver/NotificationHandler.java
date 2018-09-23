package com.ev.approver;

/**
 * Created by Kumar_Thangaraj on 7/7/2018.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class NotificationHandler {
    private static final String TAG = "NotificationHandler";
    private Context context;

    public NotificationHandler(Context context){
        this.context = context;
    }

    public void registerDevice(){
        Log.d(TAG,"Inside Register Device");
        FirebaseMessaging.getInstance().subscribeToTopic("android");
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        Log.d(TAG,"after registeration device");
    }

    public void updateFCMToken(){
        Log.d(TAG,"Inside getToken");
        SharedPreferences pref = context.getSharedPreferences("approver_app",MODE_PRIVATE);
        String prefToken = pref.getString("FCMToken","");
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"token is "+token);
        if(prefToken != token){
            sendRegistrationToServer(token);
        }
    }

    public void sendRegistrationToServer(String token){
        final String  FCMToken = token;
        String restApi = "updateFCMToken";
        JSONObject inputObj = new JSONObject();
        SharedPreferences pref = context.getSharedPreferences("approver_app",MODE_PRIVATE);
        String clientId = pref.getString("clientId","");
        String device_unique_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        try {
            inputObj.put("deviceId",device_unique_id);
            inputObj.put("clientId", clientId);
            inputObj.put("token", token);
        }catch(Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConnectionManager.getInstance(this.context).getUrl(restApi), inputObj, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG","JsonObjectResponse is "+response);
                        try {
                            Log.d(TAG,"successfully token updated");
                            //Toast.makeText(context, (String) response.get("message"), Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor prefEditor = context.getSharedPreferences("approver_app",MODE_PRIVATE).edit();
                            prefEditor.putString("FCMToken",FCMToken).commit();

                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("DEBUG","Error Occurred. Error Message is "+error.getMessage());
                        if(error.networkResponse != null && error.networkResponse.statusCode == 300){
                            try {
                                JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data));
                                Log.d(TAG,"Token update failed");
                                Toast.makeText(context,(String)((JSONObject)errorResponse.get("error")).get("message"),Toast.LENGTH_LONG).show();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
        ConnectionManager.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

}
