package jp.co.webshark.on2.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;
import jp.co.webshark.on2.R;

public class EffectImageView2 extends ImageView {
    private int eri;
    private int sc;
    private int effectSize;
    private Bitmap baseImage;
    private EffectImageView2 target = this;
    private int buttonImage;
    private int effectImage;
    private TextView label;

    /** 読み込み中のクルクル回るアニメーション */
    private int startDegree = new Random().nextInt(3000);
    private RotateAnimation rotateAnim = new RotateAnimation(startDegree, startDegree + 300, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private ScaleType orignalScaleType = ScaleType.CENTER;
    private boolean isLoadCompleted = false;
    private boolean nowLoading = true;
    private Handler handler = new Handler();


    public EffectImageView2(Context context) {
        super(context);
    }

    public EffectImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectImageView2(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public void setImages( final int mainImageId, final int effectImageId ){
        this.buttonImage = mainImageId;
        this.effectImage = effectImageId;
    }

    public void setLabelObject( TextView labelObject ){
        this.label = labelObject;
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
                        ViewGroup.LayoutParams params = getLayoutParams();
                        params.height = baseImage.getHeight();
                        params.width = baseImage.getWidth();
                        label.setVisibility(View.INVISIBLE);

                        setImageResource(effectImage);
                        setLayoutParams(params);

                        setScaleType(ScaleType.FIT_CENTER);
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
                        //setImageBitmap(baseImage);
                        setImageResource(buttonImage);
                        setScaleType(ScaleType.FIT_XY);
                        setClickable(true);

                        label.setVisibility(View.VISIBLE);
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
