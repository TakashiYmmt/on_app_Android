package jp.co.webshark.on2.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class EffectImageView extends ImageView {
    private int eri;
    private int sc;
    private Bitmap baseImage;
    private EffectImageView target = this;

    public EffectImageView(Context context) {
        super(context);
    }

    public EffectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSwitchEffect( final int effectResourceId, final int sleepCount ){
        this.eri = effectResourceId;
        this.sc = sleepCount;
    }

    public void doEffect(){
        // ハンドラを生成
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        baseImage = ((BitmapDrawable) getDrawable()).getBitmap();
                        setImageResource(eri);
                    }
                });
                try {
                    setClickable(false);
                    Thread.sleep(sc);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setImageBitmap(baseImage);
                        setClickable(true);
                    }
                });
            }
        }).start();
    }
}
