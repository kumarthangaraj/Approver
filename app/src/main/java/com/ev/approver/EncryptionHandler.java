package com.ev.approver;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;

import java.io.ByteArrayInputStream;
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
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by Kumar_Thangaraj on 9/28/2018.
 */

public class EncryptionHandler {
    private final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private Cipher defaultCipher;
    private byte[] lastIv;
    private int mode;

    public void init(int mode){
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        }catch (NoSuchAlgorithmException | NoSuchProviderException e){
            throw new RuntimeException("Failed to get an instance of generator",e);
        }
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        }catch (NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
        this.mode = mode;
    }

    public String getLastIv(){
        return encodeBytes(lastIv);
    }

    public void createKey(String keyString){
        boolean fingerPrintMode = false;
        if(mode == CommonConstants.FINGER_PRINT_MODE)
            fingerPrintMode = true;
        try{
            mKeyStore.load(null);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyString,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(fingerPrintMode)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        }catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e){
            throw new RuntimeException(e);
        }
    }

    public void setLastIv(byte[] bytes){
        this.lastIv = bytes;
    }

    public Cipher getCipher(String keyString, int mode){
        try{
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyString, null);
            if(mode == Cipher.ENCRYPT_MODE) {
                defaultCipher.init(mode, key);
                lastIv = defaultCipher.getIV();
            }else {
                defaultCipher.init(mode,key,new IvParameterSpec(lastIv));
            }
            return defaultCipher;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public FingerprintManager.CryptoObject getCrypto(String keyString,int mode){
            getCipher(keyString, mode);
            return new FingerprintManager.CryptoObject(defaultCipher);
    }

    public String getEncryptedString(Cipher cipher, String keyValueString){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
        byte[] bytes = keyValueString.getBytes(Charset.defaultCharset());
        try {
            cipherOutputStream.write(bytes);
            cipherOutputStream.flush();
            cipherOutputStream.close();
            return encodeBytes(outputStream.toByteArray());
        }catch (Exception e){
            throw new RuntimeException("Unable to Encrypt",e);
        }
    }

    public String encodeBytes(byte[] data) {
        StringBuilder r = new StringBuilder(data.length*2);
        for ( byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    private int hexToBin( char ch ) {
        if( '0'<=ch && ch<='9' )    return ch-'0';
        if( 'A'<=ch && ch<='F' )    return ch-'A'+10;
        if( 'a'<=ch && ch<='f' )    return ch-'a'+10;
        return -1;
    }

    public byte[] decodeBytes(String inputString){
        final int len = inputString.length();

        // "111" is not a valid hex encoding.
        if( len%2 != 0 )
            throw new IllegalArgumentException("hexBinary needs to be even-length: "+inputString);

        byte[] out = new byte[len/2];

        for( int i=0; i<len; i+=2 ) {
            int h = hexToBin(inputString.charAt(i  ));
            int l = hexToBin(inputString.charAt(i+1));
            if( h==-1 || l==-1 )
                throw new IllegalArgumentException("contains illegal character for hexBinary: "+inputString);
            out[i/2] = (byte)(h*16+l);
        }
        return out;
    }

    public String getDecryptedString(Cipher cipher, String encryptedString){
        String strVal = null;
        byte[] decodedString = decodeBytes(encryptedString);
        CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(decodedString),cipher);
        ArrayList<Byte> byteValues = new ArrayList<>();
        int nextByte;
        try {
            while ((nextByte = cipherInputStream.read()) != -1) {
                byteValues.add((byte) nextByte);
            }
            cipherInputStream.close();
        }catch (IOException e){
            throw new RuntimeException("Unable to decrypt ",e);
        }
        byte[] bytes = new byte[byteValues.size()];
        for (int i = 0; i < byteValues.size(); i++) {
            bytes[i] = byteValues.get(i).byteValue();
        }
        strVal = new String(bytes, Charset.defaultCharset());

        return strVal;
    }

}
