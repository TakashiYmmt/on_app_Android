package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import jp.co.webshark.on2.customViews.HttpImageView;

public class profileEditQrResult extends Activity {
    private HttpImageView friendImageView;
    private TextView friendNameTextView;
    private Button addButton;
    private AsyncPost qrProfileGetter;
    private AsyncPost qrFriendSetter;
    private String qrResult;
    private String friendName;
    private String friendImageUrl;
    private String timeLag;
    private String friendId;
    private String friendUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_qr_result);

        friendImageView = (HttpImageView)findViewById(R.id.profile_image);
        friendNameTextView = (TextView)findViewById(R.id.nameTextView);
        addButton = (Button)findViewById(R.id.addQrFriendButton);

        Bitmap bm = ((BitmapDrawable)friendImageView.getDrawable()).getBitmap();
        BitmapTrim bitmapTrim = new BitmapTrim(bm.getWidth(), bm.getHeight());
        bitmapTrim.setAntiAlias(true);
        bitmapTrim.setTrimCircle(bm.getWidth() / 2, bm.getWidth() / 2, bm.getWidth() / 2);
        bitmapTrim.drawBitmap(bm, 0, 0);
        bm = bitmapTrim.getBitmap();
        friendImageView.setImageBitmap(bm);
        bm = null;

        Intent i = getIntent();
        qrResult = i.getStringExtra("qrResult");

    }

    @Override
    public void onResume(){
        super.onResume();

        // コールバックの初期化
        this.setQrProfileGetter();
        this.setQrFriendSetter();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getQrUserInfo();
    }

    // APIコールバック定義
    private void setQrProfileGetter(){
        // プロフィール取得用API通信のコールバック
        qrProfileGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列から必要な文字列を取り出しておく
                friendName = clsJson2Objects.getElement(result,"name");
                friendImageUrl = clsJson2Objects.getElement(result,"image_url");
                timeLag = clsJson2Objects.getElement(result,"time_lag");
                friendId = clsJson2Objects.getElement(result,"friend_id");
                friendUserId = clsJson2Objects.getElement(result,"friend_user_id");

                drawQrResult();
                setQrProfileGetter();
            }
        });
    }
    private void setQrFriendSetter(){
        // プロフィール取得用API通信のコールバック
        qrFriendSetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // 成功していたらhome画面に戻る
                if( clsJson2Objects.isOK(result) ){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.profEditAct_QrAddComplete), Toast.LENGTH_LONG).show();
                    // 一方通行で開くだけ
                    Intent intent = new Intent(getApplicationContext(),homeActivity.class);
                    startActivity(intent);
                }else{
                    setQrFriendSetter();
                }

            }
        });
    }

    private void drawQrResult(){
        // 有効データチェック
        if( friendName.length() > 0 ){

            friendNameTextView.setText(friendName);
            friendImageView.setImageUrl(friendImageUrl, getResources().getDimensionPixelSize(R.dimen.pe_qr_result_image), this.getApplicationContext(),true);
            addButton.setEnabled(true);

            // リレーションチェック
            if( friendId.length() > 0 ){
                addButton.setText(getResources().getString(R.string.profEditAct_QrRegisted));
                addButton.setEnabled(false);

            // 期限チェック
            }else if( Integer.parseInt(timeLag) > 18000 ) {
                addButton.setText(getResources().getString(R.string.profEditAct_QrLimitOver));
                addButton.setEnabled(false);

            // 追加可能
            }else{
                addButton.setText(getResources().getString(R.string.profEditAct_QrDefault));
                addButton.setEnabled(true);
            }

        }else{
            friendNameTextView.setText("---");
            addButton.setText(getResources().getString(R.string.profEditAct_QrIllegal));
            addButton.setEnabled(false);
        }

    }

    private void getQrUserInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getFriendInfoFromQR");
        body.put("qr_friend_key", qrResult);
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        qrProfileGetter.setParams(strURL, body);
        qrProfileGetter.execute();
    }

    private void addFriend(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "createFriend");
        body.put("friend_user_id", friendUserId);
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        qrFriendSetter.setParams(strURL, body);
        qrFriendSetter.execute();
    }

    // 戻るリンク
    public void profileQrResultClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate",false);
        finish();
    }

    // 追加ボタン
    public void addQrFriend(View view) {
        this.addFriend();
    }
}
