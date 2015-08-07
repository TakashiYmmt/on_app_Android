package jp.co.webshark.on2;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by takashi on 2015/07/28.
 */
public class AsyncDraw extends AsyncTask<Void, Void, String> {

    private AsyncCallback _asyncCallback = null;
    private ImageView baseView;
    private ImageView efectView;
    private long waitTime;

    public AsyncDraw(AsyncCallback asyncCallback) {
        this._asyncCallback = asyncCallback;
    }

    public void setParams(ImageView baseView, ImageView efectView, long waitTime){
        this.baseView = baseView;
        this.efectView = efectView;
        this.waitTime = waitTime;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this._asyncCallback.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        String returnString = "";
        try {
            baseView.setVisibility(View.INVISIBLE);
            efectView.setVisibility(View.VISIBLE);
            Thread.sleep(waitTime);
            efectView.setVisibility(View.INVISIBLE);
            baseView.setVisibility(View.VISIBLE);
            //Log.v("log_tag", "In the try Loop" + st);

        } catch (Exception e) {
            //Log.v("log_tag", "Error in http connection " + e.toString());
        }
        return returnString;
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        this._asyncCallback.onPostExecute(result);
    }
}
