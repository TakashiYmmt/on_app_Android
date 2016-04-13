package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.util.HashMap;

public class profileEditNameActivity extends commonActivity {
    private String userName;
    //private AsyncPost profileSender;

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

        // プロフィール取得用API通信のコールバック
        AsyncPost profileSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // アクティビティを終了
                if(!isDestroy){
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);
                    intent.putExtra("isUpdate",true);
                    finish();
                }

            }
        });
        // API通信のPOST処理
        profileSender.setParams(strURL, body);
        profileSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
