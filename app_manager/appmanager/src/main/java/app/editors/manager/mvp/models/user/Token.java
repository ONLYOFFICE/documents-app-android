package app.editors.manager.mvp.models.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Token {

    @SerializedName("token")
    @Expose
    private String token = "";

    @SerializedName("expires")
    @Expose
    private String expires = "";

    @SerializedName("sms")
    @Expose
    private boolean sms = false;

    @SerializedName("phoneNoise")
    @Expose
    private String phoneNoise = "";

    @SerializedName("tfa")
    @Expose
    private boolean tfa = false;

    @SerializedName("tfaKey")
    @Expose
    private String tfaKey = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public boolean getSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    public String getPhoneNoise() {
        return phoneNoise;
    }

    public void setPhoneNoise(String phoneNoise) {
        this.phoneNoise = phoneNoise;
    }

    public boolean getTfa() {
        return tfa;
    }

    public void setTfa(boolean tfa) {
        this.tfa = tfa;
    }

    public String getTfaKey() {
        return tfaKey;
    }

    public void setTfaKey(String tfaKey) {
        this.tfaKey = tfaKey;
    }
}