package com.ev.approver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.crypto.Cipher;

public class PINActivity extends AppCompatActivity {

    String clientId;
    String clientKey;
    private EncryptionHandler encryptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent pinIntent = getIntent();
        clientId = pinIntent.getStringExtra("clientId");
        clientKey = pinIntent.getStringExtra("clientKey");
        Log.d("Debug", "Client Secrets are "+clientId+"|"+clientKey);
        setContentView(R.layout.activity_pin);
    }

    protected void storeKeys(View view){
        EditText pin = (EditText)findViewById(R.id.pin);
        EditText confirmPin = (EditText)findViewById(R.id.confirmPin);
        if(pin.getText().length() < Integer.parseInt(getString(R.string.pin_length))) {
            Toast.makeText(this,getString(R.string.pin_length_error)+" "+getString(R.string.pin_length), Toast.LENGTH_LONG).show();
            return;
        }
        Log.d("Debug","Pin Value "+pin.getText());
        Log.d("Debug","ConfirmPin Value "+confirmPin.getText());
        if(!(pin.getText().toString()).equals(confirmPin.getText().toString())) {
            Toast.makeText(this, getString(R.string.pin_mismatch_error), Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences pref = getSharedPreferences("approver_app",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("clientId",clientId).commit();
        setEncryptedData(pin.getText().toString());
        editor.putString("registeredApp","Y").commit();
        editor.putString("fingerPrintRegistered","N").commit();
        NotificationHandler notificationHandler = new NotificationHandler(getApplicationContext());
        notificationHandler.registerDevice();
        notificationHandler.updateFCMToken();
        NavUtils.navigateUpFromSameTask(this);
        return;
    }

    private void setEncryptedData(String keyString){
        encryptionHandler = new EncryptionHandler();
        encryptionHandler.init(CommonConstants.PIN_MODE);
        encryptionHandler.createKey(keyString);
        String encryptedData = encryptionHandler.getEncryptedString(
                                        encryptionHandler.getCipher(keyString, Cipher.ENCRYPT_MODE),
                                        clientKey);
        storeIv();
        SharedPreferences.Editor edit = getSharedPreferences("approver_app", Context.MODE_PRIVATE).edit();
        edit.putString("clientKey",encryptedData).commit();
    }

    private void storeIv(){
        SharedPreferences.Editor edit = getSharedPreferences("approver_app",Context.MODE_PRIVATE).edit();
        edit.putString("pinIv",encryptionHandler.getLastIv()).commit();
    }
}
