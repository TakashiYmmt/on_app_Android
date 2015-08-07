package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class startActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //this.sampleFileInput();
        //this.sampleFileOutput();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Handler hdl = new Handler();

        commonFucntion cf = new commonFucntion();
        //cf.setUserID(this.getApplicationContext(), "4");
        if( cf.getUserID(this.getApplicationContext()) != -1 ) {
            hdl.postDelayed(new splashHandlerMain(), 2000);
        }
        else{
            hdl.postDelayed(new splashHandlerRegist(), 2000);
        }

    }

    class splashHandlerRegist implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), telephoneActivity.class);
            startActivity(intent);
            startActivity.this.finish();
        }
    }
    class splashHandlerMain implements Runnable {
        public void run() {
            Intent intent = new Intent(getApplication(), homeActivity.class);
            startActivity(intent);
            startActivity.this.finish();
        }
    }
}
