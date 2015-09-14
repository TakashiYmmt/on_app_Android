package jp.co.webshark.on2.customViews;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public final class WorkerThread extends Thread {
    private final Channel channel;

    public WorkerThread(String name, Channel channel) {
        super(name);
        this.channel = channel;
    }

    @Override
    public void run() {
        while (true) {
            Request request = channel.takeRequest();
            request.setStatus(Request.Status.LOADING);
            SoftReference<Bitmap> image = ImageCache.getImage(
                    request.getCacheDir(), request.getUrl());

            if (image == null || image.get() == null) {
                image = getImage(request.getUrl());
                if (image != null && image.get() != null) {
                    ImageCache.saveBitmap(request.getCacheDir(),
                            request.getUrl(), image.get());
                }
            }
            request.setStatus(Request.Status.LOADED);
            request.getRunnable().run();
        }
    }

    private SoftReference<Bitmap> getImage(String url) {
        try {
            return new SoftReference<Bitmap>(getBitmapFromURL(url));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getBitmapFromURL(String strUrl) throws IOException {
        HttpURLConnection con = null;
        InputStream in = null;

        try {
            URL url = new URL(strUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(true);
            con.setRequestMethod("GET");
            con.setReadTimeout(500000);
            con.setConnectTimeout(50000);
            con.connect();
            in = con.getInputStream();

            // --- 縮小処理 --- //

            // 画像サイズ情報を取得する
            BitmapFactory.Options imageOptions = new BitmapFactory.Options();
            imageOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, imageOptions);
            in.close();

            // もし、画像が大きかったら縮小して読み込む
            //  今回はimageSizeMaxの大きさに合わせる
            Bitmap bitmap;
            int imageSizeMax = 500;

            con.setUseCaches(true);
            con.setRequestMethod("GET");
            con.setReadTimeout(500000);
            con.setConnectTimeout(50000);
            con.connect();
            in = con.getInputStream();

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

                bitmap = BitmapFactory.decodeStream(in, null, imageOptions2);
            } else {
                bitmap = BitmapFactory.decodeStream(in);
            }
            in.close();
            // --- 縮小処理 --- //

            return bitmap;
        } finally {
            try {
                if (con != null)
                    con.disconnect();
                if (in != null)
                    in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}