package jp.co.webshark.on2;

/**
 * Created by takashi on 2015/06/25.
 * 非同期通信のコールバックインタフェース
 */
public interface AsyncCallback {

    void onPreExecute();
    void onPostExecute(String result);
    void onProgressUpdate(int progress);
    void onCancelled();

}

