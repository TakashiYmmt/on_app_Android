package jp.co.webshark.on2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/**
 * Created by takashi on 2016/03/23.
 */
public class commonActivity extends Activity {
    boolean isDestroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        System.gc();
        super.onResume();
    }
    @Override
    public void onDestroy(){
        isDestroy = true;
        super.onDestroy();
        System.gc();
    }

    @Override
    public void onStart(){
        isDestroy = false;
        super.onStart();
    }
    @Override
    public void onPause(){
        isDestroy = true;
        super.onPause();
    }
}
