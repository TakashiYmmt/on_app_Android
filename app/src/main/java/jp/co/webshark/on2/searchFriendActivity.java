package jp.co.webshark.on2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import jp.co.webshark.on2.customViews.DetectableKeyboardEventLayout;
import jp.co.webshark.on2.customViews.HttpImageView;

public class searchFriendActivity extends commonActivity {

    private RelativeLayout mainLayout;
    private InputMethodManager inputMethodManager;
    private boolean openKeyBoard;
    //private AsyncPost profileGetter;
    //private AsyncPost friendGetter;
    //private AsyncPost friendSetter;
    private clsUserInfo userInfo;
    private HttpImageView imageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private Button addButtonView;
    private EditText searchBox;
    private clsUserInfo searchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);

        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // 画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        // 画面要素の初期化
        imageView = (HttpImageView)findViewById(R.id.profile_image);
        nameTextView = (TextView)findViewById(R.id.nameTextView);
        messageTextView = (TextView)findViewById(R.id.messageTextView);
        addButtonView = (Button)findViewById(R.id.actButton);
        searchBox = (EditText)findViewById(R.id.idSearchEdit);

        imageView.setVisibility(View.INVISIBLE);
        nameTextView.setVisibility(View.INVISIBLE);
        messageTextView.setVisibility(View.INVISIBLE);
        addButtonView.setVisibility(View.INVISIBLE);

        DetectableKeyboardEventLayout root = (DetectableKeyboardEventLayout)findViewById(R.id.searchBody);
        root.setKeyboardListener(new DetectableKeyboardEventLayout.KeyboardListener() {

            @Override
            public void onKeyboardShown() {
                //Log.d(TAG, "keyboard shown");
                openKeyBoard = true;
            }

            @Override
            public void onKeyboardHidden() {
                if (openKeyBoard) {
                    searchFriend();
                    openKeyBoard = false;
                }
            }
        });

    }
    @Override
    public void onResume(){
        super.onResume();

        // 画面初期化時にAPIから取得・描画する分はここで
        this.getUserInfo();

        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();
    }

    private void getUserInfo(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getUserInfo");
        body.put("device_type", "2");
        body.put("user_id", String.valueOf(user_id));

        // プロフィール取得用API通信のコールバック
        AsyncPost profileGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    userInfo = clsJson2Objects.setUserInfo(result);
                    drawUserInfo();
                }

            }
        });
        // API通信のPOST処理
        profileGetter.setParams(strURL, body);
        profileGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void searchFriend(){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "getFriendInfoFromID");
        body.put("profile_id", searchBox.getText().toString());
        body.put("user_id", String.valueOf(user_id));

        // 検索結果取得用API通信のコールバック
        AsyncPost friendGetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    searchResult = clsJson2Objects.setSearchResult(result);
                    drawResult();
                }

            }
        });
        // API通信のPOST処理
        friendGetter.setParams(strURL, body);
        friendGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void addNewFriend(String friendUserId){
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();

        body.put("entity", "createFriend");
        body.put("friend_user_id", friendUserId);
        body.put("user_id", String.valueOf(user_id));

        // 友達登録用API通信のコールバック
        AsyncPost friendSetter = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して画面書き換えをコールする
                if(!isDestroy){
                    if( clsJson2Objects.isOK(result) ){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(searchFriendActivity.this);
                        alertDialogBuilder.setMessage("友達に追加しました。");
                        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getApplicationContext(), homeActivity.class);
                                        startActivityForResult(intent, 0);

                                        dialog.dismiss();
                                        dialog = null;
                                        finish();

                                    }
                                });
                        alertDialogBuilder.setCancelable(false);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }else{
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(searchFriendActivity.this);
                        alertDialogBuilder.setMessage("追加に失敗しました。");
                        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getApplicationContext(), homeActivity.class);
                                        startActivityForResult(intent, 0);

                                        dialog.dismiss();
                                        dialog = null;
                                        finish();

                                    }
                                });
                        alertDialogBuilder.setCancelable(false);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }

            }
        });
        // API通信のPOST処理
        friendSetter.setParams(strURL, body);
        friendSetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void drawUserInfo(){
        TextView idTextView = (TextView) findViewById(R.id.idTextView);
        idTextView.setText(userInfo.getProfileId());
    }

    private void drawResult(){

        imageView.setVisibility(View.INVISIBLE);
        nameTextView.setVisibility(View.INVISIBLE);
        messageTextView.setVisibility(View.INVISIBLE);
        addButtonView.setVisibility(View.INVISIBLE);

        if( searchResult.getId() == null ){
            messageTextView.setText("見つかりませんでした。");
            imageView.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
            messageTextView.setVisibility(View.GONE);
            messageTextView.setVisibility(View.VISIBLE);

        }else if( searchResult.getFriendFlag().equals("0") ){
            addButtonView.setText("追加");
            imageView.setImageUrl(searchResult.getImageURL(), getResources().getDimensionPixelSize(R.dimen.profile_edit_image_height), getApplicationContext(), true);
            nameTextView.setText(searchResult.getName());
            imageView.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
            addButtonView.setVisibility(View.VISIBLE);

        }else if( searchResult.getFriendFlag().equals("1") ){
            messageTextView.setText("もう友達になっています。");
            addButtonView.setText("ホームへ戻る");
            imageView.setImageUrl(searchResult.getImageURL(), getResources().getDimensionPixelSize(R.dimen.profile_edit_image_height), getApplicationContext(), true);
            nameTextView.setText(searchResult.getName());
            imageView.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
            addButtonView.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.VISIBLE);

        }else if( searchResult.getFriendFlag().equals("2") ){
            messageTextView.setText("あなた自身です。");
            addButtonView.setText("ホームへ戻る");
            imageView.setImageUrl(searchResult.getImageURL(), getResources().getDimensionPixelSize(R.dimen.profile_edit_image_height), getApplicationContext(), true);
            nameTextView.setText(searchResult.getName());
            imageView.setVisibility(View.VISIBLE);
            nameTextView.setVisibility(View.VISIBLE);
            addButtonView.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.VISIBLE);
        }
    }

    // 戻るリンク
    public void searchFriendClose(View view) {
        // 一方通行で開くだけ
        Intent intent = new Intent(getApplicationContext(),homeActivity.class);
        startActivityForResult(intent, 0);
    }

    // 友達に追加・ホームに戻るボタン
    public void addFriend(View view) {

        if( searchResult.getFriendFlag().equals("1") || searchResult.getFriendFlag().equals("2") ){
            // 一方通行で開くだけ
            Intent intent = new Intent(getApplicationContext(),homeActivity.class);
            startActivityForResult(intent, 0);
        }else if( searchResult.getFriendFlag().equals("0") ){
            addNewFriend(searchResult.getId());
        }
    }
    /**
     * EditText編集時に背景をタップしたらキーボードを閉じるようにするタッチイベントの処理
     */

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    // ダイアログ表示など特定の処理を行いたい場合はここに記述
                    // 親クラスのdispatchKeyEvent()を呼び出さずにtrueを返す
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

        // キーボードが出ていた時はイベントをカット
        if( openKeyBoard ){
            return false;
        }else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        mainLayout.requestFocus();

        return false;
    }
}
