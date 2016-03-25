package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import java.util.List;

public class profileEditReadQrActivity extends commonActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;
    Toast my_toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_read_qr);

        my_toast = Toast.makeText(profileEditReadQrActivity.this, "画面をタッチすると撮影します", Toast.LENGTH_LONG);
        my_toast.show();

        Window myWin = getWindow(); //現在の表示されているウィンドウを取得
        LayoutParams lp = new LayoutParams(); //LayoutParams作成
        lp.screenBrightness = 1.0f; //輝度最大
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        myWin.setAttributes(lp); //ウィンドウにLayoutParamsを設定

        mSurfaceView = (SurfaceView)findViewById(R.id.mySurfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setOnClickListener(onClickListener);
    }




    @Override
    public void onResume() {
        super.onResume();
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(callback);
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 生成されたとき
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            try {
                // プレビューをセットする
                mCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // 変更されたとき
            mCamera.stopPreview();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRotation(90);

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = previewSizes.get(0);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            // width, heightを変更する
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 破棄されたとき
            mCamera.release();
            mCamera = null;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // オートフォーカス
            if (mCamera != null) {
                mCamera.autoFocus(autoFocusCallback);
            }
        }
    };

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //if (success) {
                // 現在のプレビューをデータに変換
                camera.setOneShotPreviewCallback(previewCallback);
            //}
        }
    };

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // 読み込む範囲

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRotation(90);

            int previewWidth = parameters.getPreviewSize().width;
            int previewHeight = parameters.getPreviewSize().height;

            // プレビューデータから BinaryBitmap を生成
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                    data, previewWidth, previewHeight, 0, 0, previewWidth, previewHeight, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            try {
                Result result = reader.decode(bitmap);
                String resultCode = result.getText();
                if( resultCode.length() == 116 ){
                    Double.parseDouble(resultCode); // 保険の数値チェック

                    // 一方通行で開くだけ
                    my_toast.cancel();
                    Intent intent = new Intent(getApplicationContext(),profileEditQrResult.class);
                    intent.putExtra("qrResult", resultCode);
                    startActivityForResult(intent, 0);
                }else{
                    my_toast.cancel();
                    my_toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.profEditAct_QrIllegal), Toast.LENGTH_LONG);
                    my_toast.show();
                }


            } catch (Exception e) {
                my_toast.cancel();
                my_toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.profEditAct_QrIllegal), Toast.LENGTH_LONG);
                my_toast.show();
            }
        }
    };


    // 戻るリンク
    public void profileReadQrClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate",false);
        finish();
    }
}
