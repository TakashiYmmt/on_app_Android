package jp.co.webshark.on2;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;

import jp.co.webshark.on2.customViews.HttpImageView;

public class profileEditDrawQrActivity extends commonActivity {

    //private AsyncPost profileGetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_draw_qr);
    }


    @Override
    public void onResume() {
        super.onResume();

        this.getRandKey();
    }

    private void getRandKey(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getRandCodeForQR");
        body.put("user_id", String.valueOf(user_id));

        // プロフィール取得用API通信のコールバック
        AsyncPost profileGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    drawQrCode(clsJson2Objects.getElement(result,"rand_code"));
                }

            }
        });
        // API通信のPOST処理
        profileGetter.setParams(strURL, body);
        profileGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawQrCode( String keyString ){
        HttpImageView qrImageView = (HttpImageView)findViewById(R.id.qrImageView);

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bm = writer.encode(keyString, BarcodeFormat.QR_CODE, qrImageView.getHeight(), qrImageView.getHeight());

            int width = bm.getWidth();
            int height = bm.getHeight();
            int[] pixels = new int[width * height];

            // データがあるところだけ黒にする
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bm.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            qrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    // 戻るリンク
    public void profileDrawQrClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate",false);
        finish();
    }
}
