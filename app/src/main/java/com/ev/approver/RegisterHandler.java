package com.ev.approver;

import android.*;
import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ev.approver.ConnectionManager;
import com.ev.approver.PINActivity;

import org.json.JSONObject;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by Kumar_Thangaraj on 7/16/2018.
 */

public class RegisterHandler {
    private static Context context;
    String restApi = "RegisterDevice";

    public RegisterHandler(Context context){
        this.context = context;
    }
    public void registerDevice(String secretCode){
        //secretCode = "KXxEVYaFNgwJ3wccPzt0fFjGl4cnlqfq";
        JSONObject jsonObject = new JSONObject();
        JSONObject config = new JSONObject();
        JSONObject inputObj = new JSONObject();
        String serviceName = Context.TELEPHONY_SERVICE;
        TelephonyManager m_telephonyManager = (TelephonyManager) context.getSystemService(serviceName);

        try {
            jsonObject.put("Model", Build.MODEL);
            jsonObject.put("Brand", Build.BRAND);
            jsonObject.put("Device", Build.DEVICE);
            jsonObject.put("FingerPrint", Build.FINGERPRINT);
            jsonObject.put("Display", Build.DISPLAY);
            jsonObject.put("Hardware", Build.HARDWARE);
            jsonObject.put("Manufacture", Build.MANUFACTURER);
            jsonObject.put("Product", Build.PRODUCT);
            jsonObject.put("Host", Build.HOST);
            jsonObject.put("Base OS", Build.VERSION.BASE_OS);
            jsonObject.put("SDK_INT", Build.VERSION.SDK_INT);
            jsonObject.put("Model", Build.getRadioVersion());
            jsonObject.put("network operator", m_telephonyManager.getNetworkOperator());
            String device_unique_id = Settings.Secure.getString(context.getContentResolver(),
                                                                    Settings.Secure.ANDROID_ID);
            jsonObject.put("IMEI", device_unique_id);
            //jsonObject.put("List", m_telephonyManager.getAllCellInfo());
            //jsonObject.put("IMSI", m_telephonyManager.getSubscriberId());
            config.put("deviceId",device_unique_id);
            config.put("config", jsonObject);
            inputObj.put("code",secretCode);
            inputObj.put("config",config);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Log.d("DEBUG",jsonObject.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConnectionManager.getInstance(this.context).getUrl(restApi), inputObj, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG","JsonObjectResponse is "+response);
                        try {
                            if(isFingerPrintAvailable(context)){
                                Intent fp = new Intent(context, FpRegisterActivity.class);
                                fp.putExtra("clientId", (String) response.get("clientId"));
                                fp.putExtra("clientKey", (String) response.get("clientKey"));
                                context.startActivity(fp);
                            }else {
                                Intent pin = new Intent(context, PINActivity.class);
                                pin.putExtra("clientId", (String) response.get("clientId"));
                                pin.putExtra("clientKey", (String) response.get("clientKey"));
                                context.startActivity(pin);
                            }
                            //Toast.makeText(context, (String) response.get("message"), Toast.LENGTH_LONG).show();

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
                                Toast.makeText(context,(String)((JSONObject)errorResponse.get("error")).get("message"),Toast.LENGTH_LONG).show();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
        ConnectionManager.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    private Boolean isFingerPrintAvailable(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
            if(checkPermission(context)) {
                if (fingerprintManager.isHardwareDetected()) {
                    Log.d("RegisterHandler","HardwareDetected");
                    Log.d("RegisterHandler","hasEntrolledFingerPrints "+fingerprintManager.hasEnrolledFingerprints());
                    if (fingerprintManager.hasEnrolledFingerprints()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private Boolean checkPermission(Context context){
        Log.d("RegisterHandler",""+(ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)));
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED)
            return true;

        Log.d("RegisterHandler","return false");
        return false;
    }
}
