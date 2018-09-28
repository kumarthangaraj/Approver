package com.ev.approver;

import android.*;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FpRegisterActivity extends AppCompatActivity implements FingerprintHandler.Callback{

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private String clientId;
    private String clientKey;
    EncryptionHandler encryptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fp_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        clientId = getIntent().getStringExtra("clientId");
        clientKey = getIntent().getStringExtra("clientKey");
        encryptionHandler = new EncryptionHandler();
        encryptionHandler.init(CommonConstants.FINGER_PRINT_MODE);
        /*try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        }catch (NoSuchAlgorithmException | NoSuchProviderException e){
            throw new RuntimeException("Failed to get an instance of generator",e);
        }
        Cipher defaultCipher;
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        }catch (NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }*/

        KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

        if (!keyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            Toast.makeText(this,
                    "Secure lock screen hasn't set up.\n"
                            + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if(!checkPermission(this)) {
            Toast.makeText(this,
                    "Fingerprint permission is not available",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (checkPermission(this) && !fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Go to 'Settings -> Security -> Fingerprint' and register at least one" +
                            " fingerprint",
                    Toast.LENGTH_LONG).show();
            return;
        }

        //createKey(clientId, true);
        encryptionHandler.createKey(clientId);
        FingerprintHandler fingerprintHandler = new FingerprintHandler(fingerprintManager,this,getApplicationContext());
        //initCipher(defaultCipher,clientId);
        //fingerprintHandler.startListening(new FingerprintManager.CryptoObject(defaultCipher));
        fingerprintHandler.startListening(encryptionHandler.getCrypto(clientId, Cipher.ENCRYPT_MODE));
        storeIv();
    }

    private Boolean checkPermission(Context context){
        Log.d("RegisterHandler",""+(ContextCompat.checkSelfPermission(context, android.Manifest.permission.USE_FINGERPRINT)));
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED)
            return true;

        Log.d("RegisterHandler","return false");
        return false;
    }

    /*public void createKey(String clientId, boolean invalidatedByBiometricEnrollment){
        try{
            mKeyStore.load(null);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(clientId,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        }catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e){
            throw new RuntimeException(e);
        }
    }*/

    private void storeIv(){
        SharedPreferences.Editor edit = getSharedPreferences("approver_app",Context.MODE_PRIVATE).edit();
        edit.putString("fingerprintIv",encryptionHandler.getLastIv()).commit();
    }

    /*private boolean initCipher(Cipher cipher, String clientId){
        try{
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(clientId, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();
            storeIv(iv);
            return true;
        }catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }*/

    public void onAuthenticated(Cipher cipher){
        /*ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
        byte[] bytes = clientKey.getBytes(Charset.defaultCharset());
        try {
            cipherOutputStream.write(bytes);
            cipherOutputStream.flush();
            cipherOutputStream.close();
            saveEncryptedData(EncryptionHandler.encodeBytes(outputStream.toByteArray()));
        }catch (Exception e){
            throw new RuntimeException("Unable to Encrypt",e);
        }*/
        saveEncryptedData(encryptionHandler.getEncryptedString(cipher,clientKey));
        Toast.makeText(this, "Registered Successfully",Toast.LENGTH_LONG).show();
        SharedPreferences pref = getSharedPreferences("approver_app",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("clientId",clientId).commit();
        //editor.putString("clientKey",clientKey).commit();
        editor.putString("registeredApp","Y").commit();
        editor.putString("fingerPrintRegistered","Y").commit();
        NotificationHandler notificationHandler = new NotificationHandler(getApplicationContext());
        notificationHandler.registerDevice();
        notificationHandler.updateFCMToken();
        NavUtils.navigateUpFromSameTask(this);
    }

    private void saveEncryptedData(String encryptedData){
        SharedPreferences.Editor edit = getSharedPreferences("approver_app",Context.MODE_PRIVATE).edit();
        edit.putString("clientKey",encryptedData).commit();
    }

    public void onError(){
        Toast.makeText(this, "Authentication error",Toast.LENGTH_LONG).show();
    }
}
