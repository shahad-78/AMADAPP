package com.example.amadapp.Model;

public class UsersChalleng {

    String challengID;
    String status;
    String proofImage;
    int progress;

    public UsersChalleng() {
    }

    public UsersChalleng(String challengID, String status, String proofImage, int progress) {
        this.challengID = challengID;
        this.status = status;
        this.proofImage = proofImage;
        this.progress = progress;
    }

    public String getChallengID() {
        return challengID;
    }

    public void setChallengID(String challengID) {
        this.challengID = challengID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getProofImage() {
        return proofImage;
    }

    public void setProofImage(String proofImage) {
        this.proofImage = proofImage;
    }
}
