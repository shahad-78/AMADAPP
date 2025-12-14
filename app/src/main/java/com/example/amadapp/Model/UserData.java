package com.example.amadapp.Model;

import android.util.Log;

import com.example.amadapp.EncryptionHelper;

public class UserData {
    private String encryptedFullName;
    private String encryptedEmail;
    private String encryptedPass;
    private String encryptedAddress;
    private String encryptedLng;
    private String encryptedLat;

    public UserData() {

    }

    public UserData(String fullName, String email, String pass, String lng, String lat, String address) {
        try {
            this.encryptedFullName = EncryptionHelper.encrypt(fullName);
            this.encryptedEmail = EncryptionHelper.encrypt(email);
            this.encryptedPass = EncryptionHelper.encrypt(pass);
            this.encryptedAddress = EncryptionHelper.encrypt(address);
            this.encryptedLng = EncryptionHelper.encrypt(lng);
            this.encryptedLat = EncryptionHelper.encrypt(lat);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERER404", e.toString());
        }
    }

    public String getEncryptedFullName() {
        return encryptedFullName;
    }

    public String getEncryptedEmail() {
        return encryptedEmail;
    }

    public String getEncryptedPass() {
        return encryptedPass;
    }

    public String getEncryptedAddress() {
        return encryptedAddress;
    }

    public String getEncryptedLng() {
        return encryptedLng;
    }

    public String getEncryptedLat() {
        return encryptedLat;
    }

    public void setEncryptedFullName(String encryptedFullName) {
        this.encryptedFullName = encryptedFullName;
    }



    public void setEncryptedEmail(String encryptedEmail) {
        this.encryptedEmail = encryptedEmail;
    }



    public void setEncryptedPass(String encryptedPass) {
        this.encryptedPass = encryptedPass;
    }



    public void setEncryptedAddress(String encryptedAddress) {
        this.encryptedAddress = encryptedAddress;
    }



    public void setEncryptedLng(String encryptedLng) {
        this.encryptedLng = encryptedLng;
    }



    public void setEncryptedLat(String encryptedLat) {
        this.encryptedLat = encryptedLat;
    }
}