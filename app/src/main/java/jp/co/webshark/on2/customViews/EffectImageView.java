package jp.co.webshark.on2.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import java.util.Random;
import jp.co.webshark.on2.R;

public class EffectImageView extends ImageView {
    private int eri;
    private int sc;
    private int effectSize;
    private Bitmap baseImage;
    private EffectImageView target = this;

    /** 読み込み中のクルクル回るアニメーション */
    private int startDegree = new Random().nextInt(3000);
    private RotateAnimation rotateAnim = new RotateAnimation(startDegree, startDegree + 300, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private ScaleType orignalScaleType = ScaleType.CENTER;
    private boolean isLoadCompleted = false;
    private boolean nowLoading = true;
    private Handler handler = new Handler();


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
        this.effectSize = 100;
    }

    public void setSwitchEffect( final int effectResourceId, final int sleepCount, final int size ){
        this.eri = effectResourceId;
        this.sc = sleepCount;
        this.effectSize = size;
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
                    rotateAnim.setRepeatCount(Animation.INFINITE);
                    rotateAnim.setDuration(sc);
                    startLoadingAnimation();
                    Thread.sleep(sc);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopLoadingAnimation();
                        setImageBitmap(baseImage);
                        setScaleType(ScaleType.FIT_XY);
                        setClickable(true);
                        //setScaleType(orignalScaleType);
                    }
                });
            }
        }).start();
    }

    /** 読込中のクルクルを表示開始する */
    private void startLoadingAnimation() {
        this.post(new Runnable() {
            public void run() {
                if (nowLoading == false) return;
                setScaleType(ScaleType.CENTER);
                //setImageResource(loadingResID);
                startAnimation(rotateAnim);
            }
        });
    }

    /** 読込中のクルクルを表示終了、画像を空にする */
    private void stopLoadingAnimation() {
        rotateAnim.reset();
        setAnimation(null);
        setImageDrawable(null);
        setScaleType(orignalScaleType);
    }
}
