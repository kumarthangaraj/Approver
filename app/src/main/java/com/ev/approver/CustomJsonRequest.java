package com.ev.approver;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Kumar_Thangaraj on 9/19/2018.
 */

public class CustomJsonRequest extends JsonObjectRequest {

    public CustomJsonRequest(int method, String url, JSONObject jsonRequest,
                             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, (jsonRequest == null) ? null : jsonRequest, listener,
                errorListener);
    }

    public CustomJsonRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener) {
        this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest,
                listener, errorListener);
    }
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response){
        try {
            if (response.data.length == 0) {
                byte[] responseData = "{}".getBytes("UTF8");
                response = new NetworkResponse(response.statusCode, responseData, response.headers, response.notModified);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return super.parseNetworkResponse(response);
    }
}
