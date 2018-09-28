package com.ev.approver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.os.CancellationSignal;
import android.widget.Toast;

import javax.crypto.Cipher;

/**
 * Created by Kumar_Thangaraj on 9/28/2018.
 */

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private static final long ERROR_TIMEOUT_MIILS = 1800;
    private static final long SUCCESS_DELAY_MILLS = 1200;
    private static final String TAG = "Fingerprint_Hander";
    private CancellationSignal cancellationSignal;
    public Context context;

    private final FingerprintManager fingerprintManager;

    private final Callback callback;
    FingerprintHandler(FingerprintManager fingerprintManager,
                       Callback callback, Context context){
        this.fingerprintManager = fingerprintManager;
        this.callback = callback;
        this.context = context;
    }

    public boolean checkPermission(){
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED)
            return true;

        Log.d(TAG,"return false");
        return false;
    }

    public boolean isFingerPrintAuthAvailable(){
        if(checkPermission())
            return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
        else
            return false;
    }


    public void startListening(FingerprintManager.CryptoObject cryptoObject){
        if(!isFingerPrintAuthAvailable()){
            return;
        }
        cancellationSignal = new CancellationSignal();
        if(checkPermission())
        fingerprintManager.authenticate(cryptoObject,cancellationSignal,0,this,null);

    }

    public void stopListening(){
        if(cancellationSignal != null){
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errMsg){
        showError(errMsg);
    }

    @Override
    public void onAuthenticationHelp(int errMsgId, CharSequence errMsg){
        showError(errMsg);
    }

    @Override
    public void onAuthenticationFailed(){
        showError(context.getResources().getString(R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result){
        Cipher cipher = result.getCryptoObject().getCipher();
        callback.onAuthenticated(cipher);
    }

    private void showError(CharSequence errMsg){
        Toast.makeText(context,errMsg, Toast.LENGTH_LONG).show();
    }

    public interface Callback {
        void onAuthenticated(Cipher cipher);
        void onError();

    }

}
