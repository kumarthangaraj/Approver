package com.ev.approver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;

public class LaunchActivity extends AppCompatActivity {
    private static String TAG = "LaunchActivity";
    private DrawerLayout mDrawerLayout;

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

        if(isRegistered.equals("Y")) {
            signUp.setText(getString(R.string.login));
            loginPin.setVisibility(View.VISIBLE);
            welcomeTag.setVisibility(View.VISIBLE);
            welcomeTag.setText("Welcome "+clientId.toUpperCase());
        }else {
            signUp.setText(getString(R.string.signUp));
            loginPin.setVisibility(View.GONE);
            welcomeTag.setVisibility(View.GONE);
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
            RegisterHandler dev = new RegisterHandler(this);
            dev.registerDevice("Fo8wdMgOjFsgIIrz7n3Czef9mPCI8gnU");
        }else if(button.equals(getString(R.string.login))){
            LoginHandler loginHandler = new LoginHandler(this);
            String pin = ((EditText)findViewById(R.id.loginPin)).getText().toString();
            loginHandler.login(pin);
            Log.d("Debug","Login FLow called");
        }
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
}
