package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.HashMap;


public class verifyActivity extends Activity {
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    private EditText verifyInputEditText;
    private AsyncPost profileSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        // 画面上のオブジェクト
        verifyInputEditText = (EditText) findViewById(R.id.verifyInputEditText); // EditTextオブジェクト

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // 認証API通信のコールバック
        setProfileSender();
    }

    private void setProfileSender(){
        // 認証API通信のコールバック
        profileSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // resultから成否を確認してから
                if(clsJson2Objects.isOK(result)){
                    // プロフィール画面へ遷移
                    Intent intent = new Intent(getApplicationContext(),registProfileActivity.class);
                    startActivity(intent);
                }else {
                    // 認証失敗
                    verifyError();
                }
            }
        });
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

    // 認証コード確認ボタン
    public void onButtonClick(View view){
        SpannableStringBuilder sp = (SpannableStringBuilder)verifyInputEditText.getText();
        String inputText = sp.toString();

        if(inputText.isEmpty()){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setMessage(getResources().getString(R.string.verifyAct_InputCheck));
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
            onGlobal global = (onGlobal)getApplication();
            String strURL = getResources().getString(R.string.api_url);
            HashMap<String,String> body = new HashMap<String,String>();

            body.put("entity","checkValidateKey");
            body.put("user_id",(String)global.getShareData("user_id"));
            body.put("validate_key", inputText);

            // API通信のPOST処理
            profileSender.setParams(strURL,body);
            profileSender.execute();

        }
    }

    // 認証失敗メッセージ
    private void verifyError(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(getResources().getString(R.string.verifyAct_VerifyError));
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

        // 認証API通信のコールバック
        setProfileSender();
    }
}
