package jp.co.webshark.on2;

import java.util.ArrayList;

/**
 * Created by takashi on 2015/07/23.
 */
public class clsUserInfo {
    String id;
    String name;
    String comment;
    String imageURL;
    String profineId;
    String telephoneNumber;
    String validateKey;
    String pushNotifyKey;
    String deviceType;
    String valid;
    String status;
    String friend_flag;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageURL() {
        return imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getProfileId() {
        return profineId;
    }
    public void setProfileId(String profineId) {
        this.profineId = profineId;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }
    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getValidateKey() {
        return validateKey;
    }
    public void setValidateKey(String validateKey) {
        this.validateKey = validateKey;
    }

    public String getPushNotifyKey() {
        return pushNotifyKey;
    }
    public void setPushNotifyKey(String pushNotifyKey) {
        this.pushNotifyKey = pushNotifyKey;
    }

    public String getDeviceType() {
        return deviceType;
    }
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getValid() {
        return valid;
    }
    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getFriendFlag() {
        return friend_flag;
    }
    public void setFriendFlag(String friend_flag) {
        this.friend_flag = friend_flag;
    }
}
