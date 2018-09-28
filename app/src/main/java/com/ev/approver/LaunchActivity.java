package com.ev.approver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.Cipher;

public class LaunchActivity extends AppCompatActivity implements FingerprintHandler.Callback {
    private static String TAG = "LaunchActivity";
    private DrawerLayout mDrawerLayout;
    private String isFingerPrintRegistered = "";
    EncryptionHandler encryptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        //setOnclickListener();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(getMenuBar());
        SharedPreferences pref = getSharedPreferences("approver_app",MODE_PRIVATE);
        String isRegistered = pref.getString("registeredApp","");
        isFingerPrintRegistered = pref.getString("fingerPrintRegistered","N");
        String clientId = pref.getString("clientId","");
        String requestId = getIntent().getStringExtra("requestId");
        Log.d(TAG, "onCreate: requestId is "+requestId);
        if(requestId != null){
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("requestId",requestId).commit();
        }else {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("requestId","").commit();
        }
        Button signUp = (Button)findViewById(R.id.signUp);
        EditText loginPin = (EditText)findViewById(R.id.loginPin);
        TextView welcomeTag = (TextView)findViewById(R.id.welcomeTag);
        ImageView fingerprint = (ImageView) findViewById(R.id.fingerprintimage);

        if(isRegistered.equals("Y")) {
            signUp.setText(getString(R.string.login));
            welcomeTag.setText("Welcome "+clientId.toUpperCase());
            welcomeTag.setVisibility(View.VISIBLE);
            if(!isFingerPrintRegistered.equals("Y")) {
                loginPin.setVisibility(View.VISIBLE);
                fingerprint.setVisibility(View.GONE);
            }else {
                loginPin.setVisibility(View.GONE);
                fingerprint.setVisibility(View.VISIBLE);
            }
        }else {
            signUp.setText(getString(R.string.signUp));
            loginPin.setVisibility(View.GONE);
            welcomeTag.setVisibility(View.GONE);
            fingerprint.setVisibility(View.GONE);

        }
    }

    private void setOnclickListener(){
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onSignUp(View view){
        Log.d("DEBUG","Inside onSignUp");
        String button = (String)((Button)findViewById(R.id.signUp)).getText();
        if(button.equals(getString(R.string.signUp))) {
            //callQRCodeScanner();
            //callFingerPrintTest();
            callPinTest();
            /*RegisterHandler dev = new RegisterHandler(this);
            dev.registerDevice("Fo8wdMgOjFsgIIrz7n3Czef9mPCI8gnU");*/
        }else if(button.equals(getString(R.string.login))){
            if(!isFingerPrintRegistered.equals("Y")) {
                String pin = ((EditText) findViewById(R.id.loginPin)).getText().toString();
                String decryptedString = getClientKeyfromKeyStore(pin);
                /*LoginHandler loginHandler = new LoginHandler(this);
                String pin = ((EditText) findViewById(R.id.loginPin)).getText().toString();
                loginHandler.login(pin);
                Log.d("Debug", "Login FLow called");*/
            }else {
                Toast.makeText(this,"Swipe your finger",Toast.LENGTH_SHORT).show();
                startListeningFingerPrint();
            }
        }
    }

    private void startListeningFingerPrint(){
        encryptionHandler = new EncryptionHandler();
        encryptionHandler.init(CommonConstants.FINGER_PRINT_MODE);
        SharedPreferences pref = getSharedPreferences("approver_app",MODE_PRIVATE);
        String clientId = pref.getString("clientId","");
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
        //encryptionHandler.createKey(clientId);
        setLastIv("fingerprintIv");
        FingerprintHandler fingerprintHandler = new FingerprintHandler(fingerprintManager,this,getApplicationContext());
        fingerprintHandler.startListening(encryptionHandler.getCrypto(clientId, Cipher.DECRYPT_MODE));
    }

    public void setLastIv(String keyString){
        SharedPreferences pref = getSharedPreferences("approver_app",MODE_PRIVATE);
        String ivString = pref.getString(keyString, null);
        byte[] ivBytes = encryptionHandler.decodeBytes(ivString);
        encryptionHandler.setLastIv(ivBytes);
    }

    public void onAuthenticated(Cipher cipher){
        SharedPreferences pref = getSharedPreferences("approver_app",MODE_PRIVATE);
        String clientKey = pref.getString("clientKey", null);
        String decryptedString = encryptionHandler.getDecryptedString(cipher,clientKey);
        Toast.makeText(this,decryptedString,Toast.LENGTH_LONG).show();
    }

    public void onError(){
        Toast.makeText(this, "Authentication error",Toast.LENGTH_LONG).show();
    }

    protected void callQRCodeScanner(){
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }catch (Exception e){
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("DEBUG","Inside onActivityResult *****************");
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String secretCode = data.getStringExtra("SCAN_RESULT");
                RegisterHandler dev = new RegisterHandler(this);
                dev.registerDevice(secretCode);
                Log.d("DEBUG", "secretCode is "+secretCode);
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        this.setIntent(newIntent);

        // Now getIntent() returns the updated Intent
        String requestId = getIntent().getStringExtra("requestId");
        Log.d(TAG, "onNewIntent: requestId is "+requestId);
    }

    private Drawable getMenuBar(){
        Drawable icMenu = ContextCompat.getDrawable(this,R.drawable.ic_menu).mutate();
        icMenu.setTint(getColor(R.color.white));
        return icMenu;
    }

    private void callFingerPrintTest(){
        Intent fp = new Intent(this, FpRegisterActivity.class);
        fp.putExtra("clientId", (String) "test");
        fp.putExtra("clientKey", (String) "dfdfdge3535dfdf");
        this.startActivity(fp);
    }
    private void callPinTest(){
        Intent fp = new Intent(this, PINActivity.class);
        fp.putExtra("clientId", (String) "test");
        fp.putExtra("clientKey", (String) "dfdfdge3535dfdf");
        this.startActivity(fp);
    }

    private String getClientKeyfromKeyStore(String keyString){
        encryptionHandler = new EncryptionHandler();
        encryptionHandler.init(CommonConstants.PIN_MODE);
        setLastIv("pinIv");
        SharedPreferences pref = getSharedPreferences("approver_app",MODE_PRIVATE);
        String clientKey = pref.getString("clientKey", null);
        String decryptedString = encryptionHandler.getDecryptedString(
                                        encryptionHandler.getCipher(keyString,Cipher.DECRYPT_MODE),
                                        clientKey);
        Toast.makeText(this,decryptedString,Toast.LENGTH_LONG).show();
        return decryptedString;
    }
}
