package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import jp.co.webshark.on2.customViews.HttpImageView;


public class registProfileActivity extends Activity {
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    private EditText nameInputEditText;
    private Uri mPictureUri;
    private AsyncPost profileSender;
    private Context mContext;
    private String  picPath,ba1;
    private ImageView profileImageView;
    private boolean drawBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_profile);

        // 画面上のオブジェクト
        nameInputEditText = (EditText) findViewById(R.id.idInputEditText); // EditTextオブジェクト
        profileImageView = (ImageView) findViewById(R.id.imageButton); // ImageButtonオブジェクト

        Bitmap bm = ((BitmapDrawable)profileImageView.getDrawable()).getBitmap();
        BitmapTrim bitmapTrim = new BitmapTrim(bm.getWidth(), bm.getHeight());
        bitmapTrim.setAntiAlias(true);
        bitmapTrim.setTrimCircle(bm.getWidth() / 2, bm.getWidth() / 2, bm.getWidth() / 2);
        bitmapTrim.drawBitmap(bm, 0, 0);
        bm = bitmapTrim.getBitmap();
        profileImageView.setImageBitmap(bm);
        bm = null;

        profileImageView.setDrawingCacheEnabled(true);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        drawBitmap = false;
    }
    // APIコールバック定義
    private void setProfileSender(){
        // プロフィール取得用API通信のコールバック
        profileSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列を確認してからローカル情報の作成と画面遷移をする
                registCheck(clsJson2Objects.isOK(result));
            }
        });
    }

    private void registCheck(boolean result){

        // resultから成否を確認してから
        if(result){
            // 仮で持っていたユーザIDをローカルに保存する
            onGlobal global = (onGlobal)getApplication();
            commonFucntion.setUserID(this.getApplicationContext(),(String)global.getShareData("user_id"));

            Intent intent = new Intent(getApplicationContext(),homeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // コールバックの初期化
        this.setProfileSender();

        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

    }

    /**
     * EditText編集時に背景をタップしたらキーボードを閉じるようにするタッチイベントの処理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

        return false;
    }

    public void openLibrary(View view){
        //ImageView button = (ImageView) findViewById(R.id.imageButton);
        try {
            // ギャラリーから選択
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);

            // カメラで撮影
            String filename = System.currentTimeMillis() + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            //mPictureUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mPictureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i2.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

            // ギャラリー選択のIntentでcreateChooser()
            Intent chooserIntent = Intent.createChooser(i, "Pick Image");
            // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { i2 });

            startActivityForResult(chooserIntent, 0);

            InputStream is = getResources().getAssets().open("image.jpg");
            Bitmap bm = BitmapFactory.decodeStream(is);
            profileImageView.setImageBitmap(bm);
        } catch (IOException e) {
            /* 例外処理 */
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( data == null && requestCode == 0 && resultCode == 0 ){
            return;
        }

        if (requestCode == 0) {

            if (resultCode != RESULT_OK) {
                if (mPictureUri != null) {
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            try {
                InputStream istream = null;
                Bitmap bitmap = null;

                if( data == null ){
                    //File file = new File(mPictureUri.toString());
                    //istream = new FileInputStream(file);
                    istream = getContentResolver().openInputStream(mPictureUri);
                }else{
                    istream = getContentResolver().openInputStream(data.getData());
                }

                // --- 縮小処理 --- //

                // 画像サイズ情報を取得する
                BitmapFactory.Options imageOptions = new BitmapFactory.Options();
                imageOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(istream, null, imageOptions);

                istream.close();

                // もし、画像が大きかったら縮小して読み込む
                int imageSizeMax = getResources().getDimensionPixelSize(R.dimen.profile_edit_image_height) / 2;

                if( data == null ){
                    //File file = new File(mPictureUri.toString());
                    //istream = new FileInputStream(file);
                    istream = getContentResolver().openInputStream(mPictureUri);
                }else{
                    istream = getContentResolver().openInputStream(data.getData());
                }

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

                // トリミングして正方形にする
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                float scale = Math.max((float)bitmap.getWidth()/w, (float)bitmap.getHeight()/h);
                int size = Math.min(w, h);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap, (w - size) / 2, (h - size) / 2, size, size, matrix, true);

                // 円形にトリミング
                BitmapTrim bitmapTrim = new BitmapTrim(bitmap.getWidth(), bitmap.getHeight());
                bitmapTrim.setAntiAlias(true);
                bitmapTrim.setTrimCircle(bitmap.getWidth() / 2, bitmap.getWidth() / 2, bitmap.getWidth() / 2);
                bitmapTrim.drawBitmap(bitmap, 0, 0);
                bitmap = bitmapTrim.getBitmap();

                profileImageView.setImageBitmap(bitmap);
                drawBitmap = true;

            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
            // 画像を取得
            Uri result = (data == null) ? mPictureUri : data.getData();
            //ImageView button = (ImageView) findViewById(R.id.imageButton);
            profileImageView.setImageURI(result);
            */

            // サイズ調整
            ViewGroup.LayoutParams params = profileImageView.getLayoutParams();
            // 縦幅に合わせる
            params.height = params.width;
            profileImageView.setLayoutParams(params);

            //mPictureUri = null;
        }
    }


    // 登録ボタン
    public void onProfileRegistButtonClick(View view){
        SpannableStringBuilder sp = (SpannableStringBuilder)nameInputEditText.getText();
        String inputText = sp.toString();

        onGlobal global = (onGlobal)getApplication();
        commonFucntion.setUserID(this.getApplicationContext(),(String)global.getShareData("user_id"));

        if(inputText.isEmpty()){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setMessage(getResources().getString(R.string.profileAct_nameInputCheck));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else{
            String strURL = getResources().getString(R.string.api_url);
            HashMap<String,String> body = new HashMap<String,String>();

            body.put("entity","updateUserinfo");
            body.put("name",inputText);
            body.put("profile_comment","");
            body.put("user_id", (String)global.getShareData("user_id"));

            //サーバーにアップロード //action: updateUserinfo
            /*
            String[] pojo = { MediaStore.MediaColumns.DATA };

            Cursor cursor = getApplicationContext().getContentResolver().query(mPictureUri, pojo, null, null, null);

            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
                cursor.moveToFirst();
                picPath = cursor.getString(columnIndex);
            }
            */

            if( drawBitmap ){/*
            if (picPath != null &&
                    (
                            picPath.endsWith(".png") ||
                                    picPath.endsWith(".PNG") ||
                                    picPath.endsWith(".jpg") ||
                                    picPath.endsWith(".JPG") ||
                                    picPath.endsWith(".webp") ||
                                    picPath.endsWith(".WEBP")
                    )
                    )
            {
                Bitmap bm = ((BitmapDrawable)profileImageView.getDrawable()).getBitmap();
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                if(picPath.endsWith(".jpg")||picPath.endsWith(".JPG")) {

                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                }
                else  if(picPath.endsWith(".png")||picPath.endsWith(".PNG"))
                {

                    bm.compress(Bitmap.CompressFormat.PNG, 100, bao);

                }
                else if(picPath.endsWith(".webp")||picPath.endsWith(".WEBP"))
                {
                    bm.compress(Bitmap.CompressFormat.WEBP, 100, bao);

                }
                byte[] ba = bao.toByteArray();

                // API通信のPOST処理
                profileSender.setParams(strURL,body,"image.jpg",ba);
                profileSender.execute();
            */

                Bitmap bm = ((BitmapDrawable)profileImageView.getDrawable()).getBitmap();
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                if( picPath == null ){
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                }else{
                    if(picPath.endsWith(".jpg")||picPath.endsWith(".JPG")) {

                        bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                    }
                    else  if(picPath.endsWith(".png")||picPath.endsWith(".PNG"))
                    {

                        bm.compress(Bitmap.CompressFormat.PNG, 100, bao);

                    }else {
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                    }
                }
                byte[] ba = bao.toByteArray();

                // API通信のPOST処理
                profileSender.setParams(strURL,body,"image.jpg",ba);
                profileSender.execute();
                drawBitmap = false;
            } else {
                Toast.makeText(this, "JPG, PNG, WEBP only", Toast.LENGTH_LONG).show();
            }
        }
    }
}
