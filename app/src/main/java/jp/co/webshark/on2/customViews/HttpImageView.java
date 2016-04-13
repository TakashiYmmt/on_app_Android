package jp.co.webshark.on2.customViews;

import jp.co.webshark.on2.BitmapTrim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by takashi on 2015/07/23.
 */
public class HttpImageView extends ImageView {
    private ImageLoadTask task = null;
    private Bitmap cacheBitmap = null;

    public void setImageUrl(String server_url,int limitSize, Context context) {

        // キャッシュがあればそれを表示しておしまい
        if( cacheBitmap != null ){
            setImageBitmap(cacheBitmap);
            return;
        }

        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
        }

        // 表示クリア。必要であればロード中画像をセット
        setImageURI(null);

        // 画像のロード実行
        task = new ImageLoadTask();
        task.server_url = server_url;
        task.limitSize = limitSize;
        task.context = context;

        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        task.execute();
    }

    public void setImageUrl(String server_url,int limitSize, Context context, boolean drawCircle) {

        // キャッシュがあればそれを表示しておしまい
        if( cacheBitmap != null ){
            setImageBitmap(cacheBitmap);
            return;
        }

        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
        }

        // 表示クリア。必要であればロード中画像をセット
        setImageURI(null);

        // 画像のロード実行
        task = new ImageLoadTask();
        task.server_url = server_url;
        task.limitSize = limitSize;
        task.context = context;
        task.drawCircle = drawCircle;

        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        task.execute();
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
        private int limitSize = 0;
        Bitmap bitmap = null;
        boolean drawCircle = false;

        @Override
        protected Uri doInBackground(Void... params) {

            URL url = null;
            InputStream istream;
            HttpURLConnection con = null;

            try {
                url = new URL(server_url);

                con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(true);
                con.setRequestMethod("GET");
                con.setReadTimeout(500000);
                con.setConnectTimeout(50000);
                con.connect();

                //istream = url.openStream();
                istream = con.getInputStream();


                // --- 縮小処理 --- //

                // 画像サイズ情報を取得する
                BitmapFactory.Options imageOptions = new BitmapFactory.Options();
                imageOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(istream, null, imageOptions);

                istream.close();

                // もし、画像が大きかったら縮小して読み込む
                //  今回はimageSizeMaxの大きさに合わせる
                //Bitmap bitmap;
                int imageSizeMax = limitSize / 2;

                con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(true);
                con.setRequestMethod("GET");
                con.setReadTimeout(500000);
                con.setConnectTimeout(50000);
                con.connect();

                //istream = url.openStream();
                istream = con.getInputStream();

                float imageScaleWidth = (float)imageOptions.outWidth / imageSizeMax;
                float imageScaleHeight = (float)imageOptions.outHeight / imageSizeMax;

                // もしも、縮小できるサイズならば、縮小して読み込む
                if (imageScaleWidth > 2 && imageScaleHeight > 2) {
                    BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();

                    // 縦横、小さい方に縮小するスケールを合わせる
                    int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));

                    // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
                    for (int i = 2; i <= imageScale; i *= 2) {
                        imageOptions2.inSampleSize = i;
                    }

                    bitmap = BitmapFactory.decodeStream(istream, null, imageOptions2);
                } else {
                    bitmap = BitmapFactory.decodeStream(istream);
                }
                istream.close();
                // --- 縮小処理 --- //

                // トリミングして正方形にする
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                float scale = Math.max((float)imageSizeMax/w, (float)imageSizeMax/h);
                int size = Math.min(w, h);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap, (w-size)/2, (h-size)/2, size, size, matrix, true);

                //bitmap = BitmapFactory.decodeStream(istream);

                if (bitmap != null) {

                    //return Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/profile.jpg");
                    if( drawCircle ){
                        BitmapTrim bitmapTrim = new BitmapTrim(imageSizeMax, imageSizeMax);
                        bitmapTrim.setAntiAlias(true);
                        bitmapTrim.setTrimCircle(imageSizeMax/2, imageSizeMax/2, imageSizeMax/2);
                        bitmapTrim.drawBitmap(bitmap, 0, 0);
                        bitmap = bitmapTrim.getBitmap();
                    }

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
            //cacheBitmap = bitmap;
            bitmap = null;
        }

    }

}
