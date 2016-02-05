package jp.co.webshark.on2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class clsJson2Objects {

    // 汎用の成否判定だけ
    public static boolean isOK(String strJeson){
        String strResult = "";
        try{
            JSONObject json = new JSONObject(strJeson);
            strResult = json.getString("result");
        }catch (JSONException e) {
            e.printStackTrace();
        }

        if( strResult.equals("0") ){
            return true;
        }else{
            return false;
        }
    }

    // 特定の結果エレメントだけ
    public static String getElement(String strJeson, String elementName){
        String strResult = "";
        try{
            JSONObject json = new JSONObject(strJeson);
            strResult = json.getString(elementName);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    // プロフィール情報(getUserInfo)
    public static clsUserInfo setUserInfo(String strJeson) {
        clsUserInfo userInfo = new clsUserInfo();
        try{
            JSONObject json = new JSONObject(strJeson);

            userInfo.setId(json.getString("user_id"));
            userInfo.setTelephoneNumber(json.getString("telephone_number"));
            userInfo.setValidateKey(json.getString("validate_key"));
            userInfo.setPushNotifyKey(json.getString("push_notify_key"));
            userInfo.setValid(json.getString("valid"));
            userInfo.setStatus(json.getString("status"));
            userInfo.setName(json.getString("name"));
            userInfo.setImageURL(json.getString("image_url"));
            userInfo.setComment(json.getString("profile_comment"));
            userInfo.setProfileId(json.getString("profile_id"));

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    // システム情報(getSysInfo)
    public static clsSystemInfo setSysInfo(String strJeson) {
        clsSystemInfo sysInfo = new clsSystemInfo();
        try{
            JSONObject json = new JSONObject(strJeson);

            sysInfo.setNewestVersion(json.getString("newestVersion"));
            sysInfo.setForceUpdateVersion(json.getString("forceUpdateVersion"));
            sysInfo.setStoreUrl(json.getString("storeUrl"));
            sysInfo.setD064BaseUrl(json.getString("d064BaseUrl"));
            sysInfo.setD064UrlParams(json.getString("d064UrlParams"));

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return sysInfo;
    }

    // ONカウント情報(getUserInfo)
    public static clsCountInfo setCountInfo(String strJeson) {
        clsCountInfo countInfo = new clsCountInfo();
        try{
            JSONObject json = new JSONObject(strJeson);

            countInfo.setCount(json.getString("persons"));

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return countInfo;
    }

    //グループ情報(getGroupInfo)
    public static ArrayList<clsGroupInfo> setGroupInfo(String strJeson) {
        ArrayList<clsGroupInfo> list = new ArrayList<clsGroupInfo>();
        try{
            JSONObject json = new JSONObject(strJeson);
            String strCount = json.getString("count");

            for( int i = 0 ; i < Integer.parseInt(strCount) ; i++ ){
                clsGroupInfo grouptInfo = new clsGroupInfo();

                grouptInfo.setTagId((String) (json.getJSONArray("tag_id")).get(i));
                grouptInfo.setTagName((String) (json.getJSONArray("tag_name")).get(i));
                grouptInfo.setStatus((String) (json.getJSONArray("status")).get(i));
                grouptInfo.setTagCount((String) (json.getJSONArray("tag_count")).get(i));
                ////grouptInfo.setNotificationOffFlg((String) (json.getJSONArray("notification_off_flg")).get(i));
                ////grouptInfo.setMargeNotifyFlg((String) (json.getJSONArray("notify_marge")).get(i));
                //grouptInfo.setTagIconUrl((String) (json.getJSONArray("tag_icon_url")).get(i));
                grouptInfo.setOnFlg((String) (json.getJSONArray("on_flg")).get(i));

                list.add(grouptInfo);
            }
            //countInfo.setCount(json.getString("persons"));

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<clsFriendInfo> setFriendList(String strJeson) {
        ArrayList<clsFriendInfo> list = new ArrayList<clsFriendInfo>();
        try{
            JSONObject json = new JSONObject(strJeson);
            String strCount = json.getString("count");
            JSONArray jArray = json.getJSONArray("friends");

            for( int i = 0 ; i < Integer.parseInt(strCount) ; i++ ){
                clsFriendInfo friendInfo = new clsFriendInfo();
                JSONObject row = (JSONObject) jArray.get(i);
                friendInfo.setFriendId(row.getString("friend_id"));
                friendInfo.setFriendUserId(row.getString("friend_user_id"));
                friendInfo.setName(row.getString("name"));
                //friendInfo.setCustomName(row.getString("custom_name"));
                friendInfo.setOnFlg(row.getString("on_flg"));
                friendInfo.setImageURL(row.getString("image_url"));
                friendInfo.setProfileComment(row.getString("profile_comment"));
                friendInfo.setTelephoneNumber(row.getString("telephone_number"));
                friendInfo.setOnUpdateTime(row.getString("on_update_time"));
                friendInfo.setNotificationOffFlg(row.getString("notification_off_flg"));
                friendInfo.setBlockFlg(row.getString("block_flg"));
                friendInfo.setTagId(row.getString("tag_id"));

                list.add(friendInfo);
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static HashMap<String,String> setTelephoneMap(String strJeson) {
        HashMap<String,String> resultMap = new HashMap<String,String>();

        try{
            JSONObject json = new JSONObject(strJeson);
            String strCount = json.getString("count");
            JSONArray jArray = json.getJSONArray("friends");

            for( int i = 0 ; i < Integer.parseInt(strCount) ; i++ ){
                JSONObject row = (JSONObject) jArray.get(i);
                resultMap.put(row.getString("telephone_number"), row.getString("telephone_number"));
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public static ArrayList<clsFriendInfo> setHiList(String strJeson) {
        ArrayList<clsFriendInfo> list = new ArrayList<clsFriendInfo>();
        try{
            JSONObject json = new JSONObject(strJeson);
            String strCount = json.getString("count");
            JSONArray jArrayFriendId = json.getJSONArray("friend_id");
            JSONArray jArrayName = json.getJSONArray("name");
            JSONArray jArrayProfileComment = json.getJSONArray("profile_comment");
            JSONArray jArrayTimeAgo = json.getJSONArray("time_ago");
            JSONArray jArrayOnFlg = json.getJSONArray("on_flg");
            JSONArray jArrayImageUrl = json.getJSONArray("image_url");
            JSONArray jArrayNOF = json.getJSONArray("notification_off_flg");
            JSONArray jArrayBlockFlg = json.getJSONArray("block_flg");
            JSONArray jArrayFlagsFriendId = json.getJSONArray("flags_friend_id");

            for( int i = 0 ; i < Integer.parseInt(strCount) ; i++ ){
                clsFriendInfo friendInfo = new clsFriendInfo();
                friendInfo.setFriendId(jArrayFriendId.getString(i));
                friendInfo.setName(jArrayName.getString(i));
                friendInfo.setProfileComment(jArrayProfileComment.getString(i));
                friendInfo.setTimeAgo(jArrayTimeAgo.getString(i));
                friendInfo.setOnFlg(jArrayOnFlg.getString(i));
                friendInfo.setImageURL(jArrayImageUrl.getString(i));
                friendInfo.setNotificationOffFlg(jArrayNOF.getString(i));
                friendInfo.setBlockFlg(jArrayBlockFlg.getString(i));
                friendInfo.setFlagsFriendId(jArrayFlagsFriendId.getString(i));

                list.add(friendInfo);
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<clsFriendInfo> setOnList(String strJeson) {
        ArrayList<clsFriendInfo> list = new ArrayList<clsFriendInfo>();
        try{
            JSONObject json = new JSONObject(strJeson);
            String strCount = json.getString("count");
            JSONArray jArrayFriendId = json.getJSONArray("friend_id");
            JSONArray jArrayName = json.getJSONArray("name");
            JSONArray jArrayProfileComment = json.getJSONArray("profile_comment");
            JSONArray jArrayOnUpdateTime = json.getJSONArray("on_update_time");
            JSONArray jArrayOnFlg = json.getJSONArray("on_flg");
            JSONArray jArrayImageUrl = json.getJSONArray("image_url");
            JSONArray jArrayNOF = json.getJSONArray("notification_off_flg");
            JSONArray jArrayBlockFlg = json.getJSONArray("block_flg");
            JSONArray jArrayFlagsFriendId = json.getJSONArray("flags_friend_id");

            for( int i = 0 ; i < Integer.parseInt(strCount) ; i++ ){
                clsFriendInfo friendInfo = new clsFriendInfo();
                friendInfo.setFriendId(jArrayFriendId.getString(i));
                friendInfo.setName(jArrayName.getString(i));
                friendInfo.setProfileComment(jArrayProfileComment.getString(i));
                friendInfo.setOnUpdateTime(jArrayOnUpdateTime.getString(i));
                friendInfo.setOnFlg(jArrayOnFlg.getString(i));
                friendInfo.setImageURL(jArrayImageUrl.getString(i));
                friendInfo.setNotificationOffFlg(jArrayNOF.getString(i));
                friendInfo.setBlockFlg(jArrayBlockFlg.getString(i));
                friendInfo.setFlagsFriendId(jArrayFlagsFriendId.getString(i));
                friendInfo.setType("1");

                list.add(friendInfo);
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
    public static ArrayList<clsFriendInfo> setOnLog(String strJeson) {
        ArrayList<clsFriendInfo> list = new ArrayList<clsFriendInfo>();
        try{
            JSONObject json = new JSONObject(strJeson);
            String strCount = json.getString("log_count");
            JSONArray jArrayFriendId = json.getJSONArray("log_friend_id");
            JSONArray jArrayName = json.getJSONArray("log_name");
            JSONArray jArrayProfileComment = json.getJSONArray("log_profile_comment");
            JSONArray jArrayOnUpdateTime = json.getJSONArray("log_on_update_time");
            JSONArray jArrayOnFlg = json.getJSONArray("log_on_flg");
            JSONArray jArrayImageUrl = json.getJSONArray("log_image_url");
            JSONArray jArrayNOF = json.getJSONArray("log_notification_off_flg");
            JSONArray jArrayBlockFlg = json.getJSONArray("log_block_flg");
            JSONArray jArrayFlagsFriendId = json.getJSONArray("log_flags_friend_id");

            for( int i = 0 ; i < Integer.parseInt(strCount) ; i++ ){
                clsFriendInfo friendInfo = new clsFriendInfo();
                friendInfo.setFriendId(jArrayFriendId.getString(i));
                friendInfo.setName(jArrayName.getString(i));
                friendInfo.setProfileComment(jArrayProfileComment.getString(i));
                friendInfo.setOnUpdateTime(jArrayOnUpdateTime.getString(i));
                friendInfo.setOnFlg(jArrayOnFlg.getString(i));
                friendInfo.setImageURL(jArrayImageUrl.getString(i));
                friendInfo.setNotificationOffFlg(jArrayNOF.getString(i));
                friendInfo.setBlockFlg(jArrayBlockFlg.getString(i));
                friendInfo.setFlagsFriendId(jArrayFlagsFriendId.getString(i));
                friendInfo.setType("2");

                list.add(friendInfo);
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String setFbFriendId(JSONObject json) {
        String result = "";
        try{
            JSONArray data = json.getJSONArray("data");
            for( int i = 0 ; i < data.length() ; i++ ){
                if( !data.getJSONObject(i).getString("id").equals("") ){
                    if( result.equals("") ){
                        result = data.getJSONObject(i).getString("id");
                    }else{
                        result = result + "\",\"" + data.getJSONObject(i).getString("id");
                    }
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        result = "[\""+result+"\"]";
        return result;
    }
}
