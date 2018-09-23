package com.ev.approver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ev.approver.ConnectionManager;
import com.ev.approver.DashboardActivity;

import org.json.JSONObject;

/**
 * Created by Kumar_Thangaraj on 7/23/2018.
 */

public class LoginHandler {
    private static Context context;
    String loginApi = "api/BankUsers/login";
    String logoutApi = "api/BankUsers/logout";

    public LoginHandler(Context context){
        this.context = context;
    }
    public void login(String pin){
        JSONObject jsonObject = new JSONObject();
        SharedPreferences pref = context.getSharedPreferences("approver_app",Context.MODE_PRIVATE);
        String clientId = pref.getString("clientId","");
        String clientKey = pref.getString("clientKey","");
        try {
            jsonObject.put("username", clientId);
            jsonObject.put("password", clientKey);
        }catch (Exception e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConnectionManager.getInstance(this.context).getUrl(loginApi), jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG","JsonObjectResponse is "+response);
                        try {

                            //Navigating to dasboard Page
                            /*Intent intent = new Intent(context,DashboardActivity.class);
                            context.startActivity(intent);*/
                            ConnectionManager.getInstance(context).setAccessToken(response.getString("id"));
                            Log.d("Login ::","accessToken is "+response.getString("id"));
                            if(!getCaller().equals("notification")) {
                                //Navigating to List Page
                                Intent intent = new Intent(context, ListActivity.class);
                                context.startActivity(intent);
                            }else {
                                //Navigating to Arppoval activity
                                ApprovalHandler approvalHandler = new ApprovalHandler(context);
                                approvalHandler.getPendingRequests("");
                            }

                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("DEBUG","Error Occurred. Error Message is "+error.getMessage());
                        if(error.networkResponse != null){
                            try {
                                if( error.networkResponse.statusCode == 300 || error.networkResponse.statusCode == 401) {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data));
                                    Toast.makeText(context, (String) ((JSONObject)errorResponse.get("error")).get("message"), Toast.LENGTH_LONG).show();
                                }else {
                                    String errorResponse = new String(error.networkResponse.data);
                                    Log.d("DEBUG","Error Occurred. Error Message is "+errorResponse);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
        ConnectionManager.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void logout(){
        CustomJsonRequest jsonObjectRequest = new CustomJsonRequest
                (Request.Method.POST, ConnectionManager.getInstance(this.context).getUrlWithAccessToken(logoutApi), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG","JsonObjectResponse is "+response);
                        Toast.makeText(context,"Logged out successfully",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, LaunchActivity.class);
                        context.startActivity(intent);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("DEBUG","Error Occurred. Error Message is "+error);
                        if(error.networkResponse != null){
                            try {
                                if( error.networkResponse.statusCode == 300 || error.networkResponse.statusCode == 401) {
                                    JSONObject errorResponse = new JSONObject(new String(error.networkResponse.data));
                                    Toast.makeText(context, (String) ((JSONObject)errorResponse.get("error")).get("message"), Toast.LENGTH_LONG).show();
                                }else {
                                    String errorResponse = new String(error.networkResponse.data);
                                    Log.d("DEBUG","Error Occurred. Error Message is "+errorResponse);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
        ConnectionManager.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private String getCaller(){
        SharedPreferences pref = context.getSharedPreferences("approver_app", Context.MODE_PRIVATE);
        String requestId = pref.getString("requestId", "");
        if(requestId.equals(""))
            return "login";
        else
            return "notification";
    }
}
