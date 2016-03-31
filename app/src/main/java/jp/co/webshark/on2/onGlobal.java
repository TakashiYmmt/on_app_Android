package jp.co.webshark.on2;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import java.util.HashMap;

public class onGlobal extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    //グローバルに使用する変数たち
    private HashMap<String,Object> shareData = new HashMap<>();

    public void shareDataRemoveAll(){
        shareData.clear();
        return;
    }
    public void shareDataRemove(String key){
        shareData.remove(key);
        return;
    }

    public void setShareData(String key,Object value){
        shareData.put(key, value);
        return;
    }
    public Object getShareData(String key){
        return shareData.get(key);
    }
}
