package com.ev.approver;

/**
 * Created by Kumar_Thangaraj on 8/2/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ev.approver.ConnectionManager;
import com.ev.approver.DashboardActivity;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kumar_Thangaraj on 7/23/2018.
 */

public class ApprovalHandler {
    private static String TAG = "ApprovalHandler";
    private static Context context;

    public ApprovalHandler(Context context){
        this.context = context;
    }
    public void getPendingRequests(String requestId){
        String restApi = "GetApprovalRequests";
        if(requestId == null) {
            SharedPreferences pref = context.getSharedPreferences("approver_app", Context.MODE_PRIVATE);
            requestId = pref.getString("requestId", "");
        }
        Log.d(TAG, "getPendingRequests: requestId is "+requestId);
        JSONObject jsonObject = new JSONObject();
        try {
            if(!requestId.equals(""))
                jsonObject.put("requestId", requestId);
        }catch (Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConnectionManager.getInstance(this.context).getUrl(restApi), jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG","JsonObjectResponse is "+response);
                        try {
                            JSONObject responseData = (JSONObject)response.get("data");
                            Intent approval = new Intent(context, ApprovalActivity.class);
                            approval.putExtra("requestId",responseData.getString("id"));
                            approval.putExtra("from_acct",responseData.getString("from_acct"));
                            approval.putExtra("to_acct",responseData.getString("to_acct"));
                            approval.putExtra("currency",responseData.getString("currency"));
                            approval.putExtra("amount",responseData.getString("amount"));
                            approval.putExtra("tran_particulars",responseData.getString("tran_particulars"));
                            context.startActivity(approval);
                            Log.d(TAG, "onResponse: Inside response "+response);
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

    public void updateApprovalRequest(JSONObject input){
        String restApi = "UpdateApprovalRequest";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConnectionManager.getInstance(this.context).getUrl(restApi), input, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG","JsonObjectResponse is "+response);
                        try {
                            Log.d(TAG, "onResponse: Inside response "+response);
                            Toast.makeText(context, (String) response.get("message"), Toast.LENGTH_LONG).show();
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

    public void getPendingRequestList(){
        //return getSampleList();

        //String restApi = "/api/TranDtls?filter=%7B%22where%22%3A%7B%22approval_status%22%3A%22N%22%7D%7D";
        String url = "http://192.168.0.2:3000/api/TranDtls?filter=%7B%22where%22%3A%7B%22approval_status%22%3A%22N%22%7D%7D";

        Log.d(TAG, "getPendingRequestList: inside ");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url,null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("DEBUG","JsonObjectResponse is "+response);
                        try {
                            Log.d(TAG, "onResponse: Response data "+response);
                            setListAdapterList((JSONArray) response);
                            /*JSONObject responseData = (JSONObject)response.get("data");
                            Intent approval = new Intent(context, ApprovalActivity.class);
                            approval.putExtra("requestId",responseData.getString("id"));
                            approval.putExtra("from_acct",responseData.getString("from_acct"));
                            approval.putExtra("to_acct",responseData.getString("to_acct"));
                            approval.putExtra("currency",responseData.getString("currency"));
                            approval.putExtra("amount",responseData.getString("amount"));
          JsonArrayRequest                  approval.putExtra("tran_particulars",responseData.getString("tran_particulars"));
                            context.startActivity(approval);*/
                            Log.d(TAG, "onResponse: Inside response "+response);
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
        ConnectionManager.getInstance(context).addToRequestQueue(jsonArrayRequest);
    }

    public ArrayList getSampleList(){
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from_acct", "ICIC000102");
            jsonObject.put("to_acct", "ICIC000103");
        }catch(Exception e){
            e.printStackTrace();
        }
        list.add(jsonObject);

        jsonObject = new JSONObject();
        try {
            jsonObject.put("from_acct", "ICIC000104");
            jsonObject.put("to_acct", "ICIC000105");
        }catch(Exception e){
            e.printStackTrace();
        }
        list.add(jsonObject);

        return list;
    }

    private final void setListAdapterList(JSONArray jsonArray){
        ListActivity.requestList.clear();
        if (jsonArray != null) {
            for (int i=0;i<jsonArray.length();i++){
                try {
                    ListActivity.requestList.add((JSONObject)jsonArray.get(i));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private JSONObject getFilterCriteria(){
        JSONObject jsonObject = new JSONObject();
        JSONObject innerJson = new JSONObject();
        try {
            innerJson.put("approval_status", "N");
            jsonObject.put("where", innerJson);
        }catch(Exception e){
            e.printStackTrace();
        }

        return jsonObject;
    }
}

