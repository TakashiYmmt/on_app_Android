package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.util.HashMap;

public class profileEditNameActivity extends Activity {
    private String userName;
    private AsyncPost profileSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_name);

        Intent i = getIntent();
        userName = i.getStringExtra("userName");
        EditText nameEditView = (EditText) findViewById(R.id.idInputEditText);
        nameEditView.setText(userName);
    }
    @Override
    public void onResume(){
        super.onResume();

        // コールバックの初期化
        this.setProfileSender();
    }
    private void setProfileSender(){
        // プロフィール取得用API通信のコールバック
        profileSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                setProfileSender();

                // アクティビティを終了
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                intent.putExtra("isUpdate",true);
                finish();
            }
        });
    }

    public void saveButtonClick(View view){

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String,String> body = new HashMap<String,String>();
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        EditText nameEditView = (EditText) findViewById(R.id.idInputEditText);
        userName = nameEditView.getText().toString();

        body.put("entity","updateUserinfoSingleColumn");
        body.put("column_name", "name");
        body.put("column_value",userName);
        body.put("user_id", String.valueOf(user_id));

        // API通信のPOST処理
        profileSender.setParams(strURL, body);
        profileSender.execute();
    }
    // 戻るリンク
    public void profileNameEditClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate",false);
        finish();
    }
}
