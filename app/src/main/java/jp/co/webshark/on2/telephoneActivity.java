package jp.co.webshark.on2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class telephoneActivity extends Activity {
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    private EditText phoneInputEditText;
    private TextView countryCodeView;
    private CheckBox acceptCheck;
    private AsyncPost telSender;
    private Spinner countrySelector;
    private String countryCode;
    private String[] countryCodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telephone);

        // 画面上のオブジェクト
        phoneInputEditText = (EditText) findViewById(R.id.idInputEditText); // EditTextオブジェクト
        acceptCheck = (CheckBox) findViewById(R.id.checkBox); // 利用規約チェック
        countrySelector = (Spinner) findViewById(R.id.countrySelector);
        countryCodeView = (TextView) findViewById(R.id.countryCodeTextView);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        setSpannableString(this.getWindow().getDecorView());

        setTelSender();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // アダプター用の表示リストを作成
        String[] countryNameList = getResources().getStringArray(R.array.country_name_list);
        for (String countryName : countryNameList) {
            adapter.add(countryName);
        }

        countryCodeList = getResources().getStringArray(R.array.country_code_list);

        // アダプター設定
        countrySelector.setAdapter(adapter);

        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        countrySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                // 選択されたアイテムを取得します
                countryCode = countryCodeList[(int)spinner.getSelectedItemPosition()];
                countryCodeView.setText("+"+countryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // デフォルト選択
        int defaultIndex = getResources().getInteger(R.integer.country_index);
        countrySelector.setSelection(defaultIndex);

        // 実験
        ImageView logo = (ImageView) findViewById(R.id.imageView2);
        registerForContextMenu(logo);

        //TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //String telnumber = tm.getLine1Number();
        //if(telnumber != null)
        //Toast.makeText(this, "Phone number: " + telnumber,
        //        Toast.LENGTH_LONG).show();
    }

    // 実験
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        //コンテキストメニューの設定
        menu.setHeaderTitle("Androidテストアカウントを選ぶ");
        //Menu.add(int groupId, int itemId, int order, CharSequence title)
        menu.add(0, 0, 0, "000-0000-1760で");
        menu.add(0, 1, 0, "000-0000-1761で");
        menu.add(0, 2, 0, "090-3997-8227で");
    }
    public boolean onContextItemSelected(MenuItem item) {

        AdapterViewCompat.AdapterContextMenuInfo info = (AdapterViewCompat.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;
        commonFucntion cf = new commonFucntion();

        switch (item.getItemId()) {
            case 0:
                cf.setUserID(getApplicationContext(), "329");
                intent = new Intent(getApplicationContext(),homeActivity.class);
                startActivity(intent);
                return true;
            case 1:
                cf.setUserID(getApplicationContext(), "330");
                intent = new Intent(getApplicationContext(),homeActivity.class);
                startActivity(intent);
                return true;
            case 2:
                cf.setUserID(getApplicationContext(), "4");
                intent = new Intent(getApplicationContext(),homeActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void setTelSender(){
        // 電話番号認証API通信のコールバック
        telSender = new AsyncPost(new AsyncCallback() {
            public void onPreExecute() {}
            public void onProgressUpdate(int progress) {}
            public void onCancelled() {}
            public void onPostExecute(String result) {
                // JSON文字列をユーザ情報クラスに変換して内部変数にプールする値を取り出しておく
                presetUserInfo(clsJson2Objects.setUserInfo(result));

                // resultから成否を確認してから
                Intent intent = new Intent(getApplicationContext(),verifyActivity.class);
                startActivity(intent);
            }
        });
    }

    private void presetUserInfo(clsUserInfo userInfo){
        onGlobal onGlobal = (onGlobal) this.getApplication();
        onGlobal.setShareData("user_id",userInfo.getId());
    }


    // 認証コード送信ボタン
    public void onButtonClick(View view){
        SpannableStringBuilder sp = (SpannableStringBuilder)phoneInputEditText.getText();
        String inputText = sp.toString();
        boolean isAccept = acceptCheck.isChecked();

        if(isAccept){
            if(inputText.isEmpty()){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                alertDialogBuilder.setMessage(getResources().getString(R.string.telAct_InputCheck));
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

                body.put("entity","sendSmsForValidTelephoneNumber");
                body.put("push_notify_key","xxxxx");
                body.put("device_type","2");
                body.put("telephone_number", inputText);
                body.put("country_number", countryCode);

                // API通信のPOST処理
                telSender.setParams(strURL,body);
                telSender.execute();
            }
        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setMessage(getResources().getString(R.string.telAct_AcceptErr));
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
        }
        //inputText.isEmpty()

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

    // 利用規約のリンク作成
    private void setSpannableString(View view) {

        String fullText = getResources().getString(R.string.telAct_eula_full_text);
        String linkText = getResources().getString(R.string.telAct_eula_link_text);

        // リンク化対象の文字列、リンク先 URL を指定する
        Map<String, String> map = new HashMap<String, String>();
        map.put(linkText, "");

        // SpannableString の取得
        SpannableString ss = createSpannableString(fullText, map);

        // SpannableString をセットし、リンクを有効化する
        TextView textView = (TextView) view.findViewById(R.id.eulaLinkTextView);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString createSpannableString(String message, Map<String, String> map) {

        SpannableString ss = new SpannableString(message);

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            int start = 0;
            int end = 0;

            // リンク化対象の文字列の start, end を算出する
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                break;
            }

            // SpannableString にクリックイベント、パラメータをセットする
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(getApplicationContext(),eulaActivity.class);
                    startActivity(intent);
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return ss;
    }

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
}
