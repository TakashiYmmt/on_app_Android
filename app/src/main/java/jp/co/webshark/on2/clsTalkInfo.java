package jp.co.webshark.on2;

/**
 * Created by takashi on 2016/03/02.
 */
public class clsTalkInfo {

    String serial_id;
    String talk_id;
    String talk_user_id;
    String message;
    String datatime;

    public String getSerialId() {
        return serial_id;
    }
    public void setSerialId(String serial_id) {
        this.serial_id = serial_id;
    }

    public String getTalkId() {
        return talk_id;
    }
    public void setTalkId(String talk_id) {
        this.talk_id = talk_id;
    }

    public String getTalkUserId() {
        return talk_user_id;
    }
    public void setTalkUserId(String talk_user_id) {
        this.talk_user_id = talk_user_id;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getDatatime() {
        return datatime;
    }
    public void setDatatime(String datatime) {
        this.datatime = datatime;
    }

}
