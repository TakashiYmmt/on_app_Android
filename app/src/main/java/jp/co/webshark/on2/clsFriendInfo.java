package jp.co.webshark.on2;

/**
 * Created by takashi on 15/07/05.
 */
public class clsFriendInfo {
    String friend_id;
    String friend_user_id;
    String name;
    String on_flg;
    String image_url;
    String profile_comment;
    String telephone_number;
    String on_update_time;
    String notification_off_flg;
    String block_flg;
    String time_ago;

    public String getFriendId() {
        return friend_id;
    }
    public void setFriendId(String friend_id) {
        this.friend_id = friend_id;
    }

    public String getFriendUserId() {
        return friend_user_id;
    }
    public void setFriendUserId(String friend_user_id) {
        this.friend_user_id = friend_user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnFlg() {
        return on_flg;
    }

    public void setOnFlg(String on_flg) {
        this.on_flg = on_flg;
    }

    public String getImageURL() {
        return image_url;
    }

    public void setImageURL(String image_url) {
        this.image_url = image_url;
    }

    public String getProfileComment() {
        return profile_comment;
    }

    public void setProfileComment(String profile_comment) {
        this.profile_comment = profile_comment;
    }

    public String getTelephoneNumber() {
        return telephone_number;
    }

    public void setTelephoneNumber(String telephone_number) {
        this.telephone_number = telephone_number;
    }

    public String getOnUpdateTime() {
        return on_update_time;
    }

    public void setOnUpdateTime(String on_update_time) {
        this.on_update_time = on_update_time;
    }

    public String getNotificationOffFlg() {
        return notification_off_flg;
    }

    public void setNotificationOffFlg(String notification_off_flg) {
        this.notification_off_flg = notification_off_flg;
    }

    public String getBlockFlg() {
        return block_flg;
    }

    public void setBlockFlg(String block_flg) {
        this.block_flg = block_flg;
    }

    public String getTimeAgo() {
        return time_ago;
    }

    public void setTimeAgo(String time_ago) {
        this.time_ago = time_ago;
    }
}
