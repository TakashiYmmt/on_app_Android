package jp.co.webshark.on2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.UrlImageView;

public class profileEditActivity extends commonActivity {
    private RelativeLayout mainLayout;
    //private AsyncPost profileSender;
    //private AsyncPost profileGetter;
    private ImageView profileImageView;
    private Uri mPictureUri;
    private String userName;
    private String userProfileComment;
    private String picPath;
    private clsUserInfo userInfo;
    private boolean drawBitmap;
    private TextView turnButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

        profileImageView = (ImageView) findViewById(R.id.profile_image);
        turnButton = (TextView) findViewById(R.id.turn_button);

        // FB認証チェック
        AccessToken accessToken = commonFucntion.checkFbLogin(getApplicationContext());
        if (accessToken != null) {
            ((TextView) findViewById(R.id.fb_login)).setText("連携しています");
        } else {
            ((TextView) findViewById(R.id.fb_login)).setText("facebookと連携する");
        }

        drawBitmap = false;
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getUserInfo();

    }

    private void getUserInfo() {
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String, String> body = new HashMap<String, String>();

        body.put("entity", "getUserInfo");
        body.put("device_type", "2");
        body.put("user_id", String.valueOf(user_id));

        // プロフィール取得用API通信のコールバック
        AsyncPost profileGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {
            }

            public void onProgressUpdate(int progress) {
            }

            public void onCancelled() {
            }

            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if (!isDestroy) {
                    userInfo = clsJson2Objects.setUserInfo(result);
                    drawUserInfo();
                }

            }
        });
        // API通信のPOST処理
        profileGetter.setParams(strURL, body);
        profileGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawUserInfo() {
        HttpImageView profImage = (HttpImageView) findViewById(R.id.profile_image);
        TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
        TextView idTextView = (TextView) findViewById(R.id.idTextView);

        profImage.setImageUrl(userInfo.getImageURL(), getResources().getDimensionPixelSize(R.dimen.profile_edit_image_height), getApplicationContext(), true);
        nameTextView.setText(userInfo.getName());
        idTextView.setText(userInfo.getProfileId());

        userName = userInfo.getName();
        userProfileComment = userInfo.getComment();
    }


    public void openLibrary(View view) {
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
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{i2});

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

        if (data == null && requestCode == 0 && resultCode == 0) {
            return;
        }

        if (data != null) {
            boolean isUpdate = data.getBooleanExtra("isUpdate", false);
            if (isUpdate) {
                Toast.makeText(this, getResources().getString(R.string.profEditAct_profUpdate), Toast.LENGTH_LONG).show();
                return;
            }
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


                if (data == null) {
                    istream = getContentResolver().openInputStream(mPictureUri);
                } else {
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

                if (data == null) {
                    //File file = new File(mPictureUri.toString());
                    //istream = new FileInputStream(file);
                    istream = getContentResolver().openInputStream(mPictureUri);
                } else {
                    istream = getContentResolver().openInputStream(data.getData());
                }

                float imageScaleWidth = (float) imageOptions.outWidth / imageSizeMax;
                float imageScaleHeight = (float) imageOptions.outHeight / imageSizeMax;

                // もしも、縮小できるサイズならば、縮小して読み込む
                if (imageScaleWidth > 2 && imageScaleHeight > 2) {
                    BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();

                    // 縦横、小さい方に縮小するスケールを合わせる
                    int imageScale = (int) Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));

                    // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
                    for (int i = 2; i <= imageScale; i *= 2) {
                        imageOptions2.inSampleSize = i;
                    }

                    bitmap = BitmapFactory.decodeStream(istream, null, imageOptions2);
                } else {
                    bitmap = BitmapFactory.decodeStream(istream);
                }
                istream.close();

                profileImageView.setImageBitmap(bitmap);
                drawBitmap = true;

            } catch (IOException e) {
                e.printStackTrace();
            }

            // サイズ調整
            ViewGroup.LayoutParams params = profileImageView.getLayoutParams();
            // 縦幅に合わせる
            params.height = params.width;
            profileImageView.setLayoutParams(params);

            //mPictureUri = null;
            sendProfileImage();
        }
    }

    public void rollImage(View view) {
        Bitmap base = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
        Matrix mat = new Matrix();
        mat.postRotate(90);
        Bitmap result = Bitmap.createBitmap(base, 0, 0, base.getWidth(), base.getHeight(), mat, true);
        profileImageView.setImageBitmap(result);
        drawBitmap = true;
        this.sendProfileImage();

    }

    private void sendProfileImage() {

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String, String> body = new HashMap<String, String>();
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        body.put("entity", "updateUserinfo");
        body.put("name", userName);
        body.put("profile_comment", userProfileComment);
        body.put("user_id", String.valueOf(user_id));

        if (drawBitmap) {
            Bitmap bm = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            if (picPath == null) {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
            } else {
                if (picPath.endsWith(".jpg") || picPath.endsWith(".JPG")) {

                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                } else if (picPath.endsWith(".png") || picPath.endsWith(".PNG")) {

                    bm.compress(Bitmap.CompressFormat.PNG, 100, bao);

                } else {
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                }
            }
            byte[] ba = bao.toByteArray();

            // プロフィール取得用API通信のコールバック
            AsyncPost profileSender = new AsyncPost(new AsyncCallback() {
                public void onPreExecute() {
                    turnButton.setText("保存しました");
                    turnButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_active_green));
                    turnButton.setClickable(false);
                    turnButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.profile_turncheck_android, 0, 0, 0);
                    turnButton.setBackgroundResource(R.color.bg_white);
                }

                public void onProgressUpdate(int progress) {
                }

                public void onCancelled() {
                }

                public void onPostExecute(String result) {
                    saveEffect();
                }
            });
            // API通信のPOST処理
            profileSender.setParams(strURL, body, "image.jpg", ba);
            profileSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            drawBitmap = false;
        } else {
            Toast.makeText(this, "JPG, PNG only", Toast.LENGTH_LONG).show();
        }
    }

    public void saveEffect() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        Context context = getBaseContext();
        turnButton.setText("画像を回転する");
        turnButton.setTextColor(ContextCompat.getColor(context, R.color.color_text_gray2));
        turnButton.setClickable(true);
        turnButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.profile_turn_android, 0, 0, 0);
        turnButton.setBackgroundResource(R.drawable.turn_button);
    }

    // 戻るリンク
    public void profileEditClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        //finish();

        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(), homeActivity.class);
        intent.putExtra("userName", userInfo.getName());
        startActivityForResult(intent, 0);
    }

    // FBにログイン
    public void fbLogin(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(), fbLoginActivity.class);
        intent.putExtra("userName", userInfo.getName());
        startActivityForResult(intent, 0);
    }

    // 名前を変更
    public void editName(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(), profileEditNameActivity.class);
        intent.putExtra("userName", userInfo.getName());
        startActivityForResult(intent, 0);
    }

    // IDを変更
    public void editId(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(), profileEditIdActivity.class);
        intent.putExtra("userId", userInfo.getProfileId());
        startActivityForResult(intent, 0);
    }

    // QRを読み取り
    public void readQr(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(), profileEditReadQrActivity.class);
        startActivityForResult(intent, 0);
    }

    // 自分のQRを表示
    public void drawQr(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(), profileEditDrawQrActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "profileEdit Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://jp.co.webshark.on2/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "profileEdit Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://jp.co.webshark.on2/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
