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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class registProfileActivity extends Activity {
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    private EditText nameInputEditText;
    private Uri mPictureUri;
    private AsyncPost profileSender;
    private Context mContext;
    private String  picPath,ba1;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_profile);

        // 画面上のオブジェクト
        nameInputEditText = (EditText) findViewById(R.id.nameInputEditText); // EditTextオブジェクト
        profileImageView = (ImageView) findViewById(R.id.imageButton); // ImageButtonオブジェクト

        profileImageView.setDrawingCacheEnabled(true);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

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
        if (requestCode == 0) {

            if (resultCode != RESULT_OK) {
                if (mPictureUri != null) {
                    getContentResolver().delete(mPictureUri, null, null);
                    mPictureUri = null;
                }
                return;
            }

            // 画像を取得
            Uri result = (data == null) ? mPictureUri : data.getData();
            //ImageView button = (ImageView) findViewById(R.id.imageButton);
            profileImageView.setImageURI(result);

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
            String[] pojo = { MediaStore.MediaColumns.DATA };

            Cursor cursor = getApplicationContext().getContentResolver().query(mPictureUri, pojo, null, null, null);

            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
                cursor.moveToFirst();
                picPath = cursor.getString(columnIndex);
            }

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
            } else {
                Toast.makeText(this, "JPG, PNG, WEBP only", Toast.LENGTH_LONG).show();
            }
        }
    }
}
