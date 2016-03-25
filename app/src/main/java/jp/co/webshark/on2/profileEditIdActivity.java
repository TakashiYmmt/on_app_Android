package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class profileEditIdActivity extends commonActivity {
    private String profileId;
    //private AsyncPost profileSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_id);

        Intent i = getIntent();
        profileId = i.getStringExtra("userId");
        EditText idEditView = (EditText) findViewById(R.id.idInputEditText);
        idEditView.setText(profileId);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void saveButtonClick(View view) {

        String strURL = getResources().getString(R.string.api_url);
        HashMap<String, String> body = new HashMap<String, String>();
        int user_id = commonFucntion.getUserID(this.getApplicationContext());

        EditText idEditView = (EditText) findViewById(R.id.idInputEditText);
        profileId = idEditView.getText().toString();

        //body.put("entity", "updateUserinfoSingleColumn");
        //body.put("column_name", "profile_id");
        //body.put("column_value", userId);
        //body.put("user_id", String.valueOf(user_id));

        body.put("entity", "updateProfileID");
        body.put("profile_id", profileId);
        body.put("user_id", String.valueOf(user_id));

        // プロフィール取得用API通信のコールバック
        AsyncPost profileSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                if(!isDestroy){
                    String strResult = "";
                    try{
                        JSONObject json = new JSONObject(result);
                        strResult = json.getString("result");
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if( strResult.equals("0") ){
                        // アクティビティを終了
                        Intent intent = new Intent();
                        setResult(RESULT_CANCELED, intent);
                        intent.putExtra("isUpdate", true);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.profEditAct_profIdNotUnique), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        // API通信のPOST処理
        profileSender.setParams(strURL, body);
        profileSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // 戻るリンク
    public void profileIdEditClose(View view) {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate",false);
        finish();
    }
}