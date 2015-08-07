package jp.co.webshark.on2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class eulaActivity extends Activity {
//public class eulaActivity extends ActionBarActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eula);

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("http://210.140.165.80/on/eula.html");

        setSpannableString(this.getWindow().getDecorView());

    }

    // 閉じるリンク作成
    private void setSpannableString(View view) {

        String fullText = getResources().getString(R.string.close);
        String linkText = getResources().getString(R.string.close);

        // リンク化対象の文字列、リンク先 URL を指定する
        Map<String, String> map = new HashMap<String, String>();
        map.put(linkText, "");

        // SpannableString の取得
        SpannableString ss = createSpannableString(fullText, map);

        // SpannableString をセットし、リンクを有効化する
        TextView textView = (TextView) view.findViewById(R.id.linkTextView);
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
                    close();
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(Color.parseColor(getResources().getString(R.string.color_code_link_green)));
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return ss;
    }

    private void close() {
        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        finish();
    }
}
