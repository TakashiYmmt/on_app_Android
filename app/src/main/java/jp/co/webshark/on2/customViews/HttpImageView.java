package jp.co.webshark.on2.customViews;

import jp.co.webshark.on2.commonFucntion;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by takashi on 2015/07/23.
 */
public class HttpImageView extends ImageView {
    private ImageLoadTask task = null;

    public void setImageUrl(String server_url, Context context) {

        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
        }

        // 表示クリア。必要であればロード中画像をセット
        setImageURI(null);

        // 画像のロード実行
        task = new ImageLoadTask();
        task.server_url = server_url;
        task.context = context;
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // taskが実行中であれば止める
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
        }
    }

    public HttpImageView(Context context) {
        super(context);
    }

    public HttpImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HttpImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 画像を取得するAsyncTask
    private class ImageLoadTask extends AsyncTask<Void, Void, Uri>  {

        private String server_url = null;
        private Context context = null;
        Bitmap bitmap = null;

        @Override
        protected Uri doInBackground(Void... params) {

            URL url = null;
            InputStream istream;

            try {
                url = new URL(server_url);
                istream = url.openStream();
                bitmap = BitmapFactory.decodeStream(istream);

                if (bitmap != null) {
                    //return Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/profile.jpg");
                    return Uri.parse(server_url);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Uri result) {
            //setImageURI(result);
            //ずれはキャッシュしたローカルファイルを参照したいけど、今はクラス内にバイナリを持って扱う
            setImageBitmap(bitmap);
        }

    }
}
