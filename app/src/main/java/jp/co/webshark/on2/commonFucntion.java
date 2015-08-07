package jp.co.webshark.on2;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by takashi on 2015/06/18.
 */
public class commonFucntion extends Application{

    public static void sleep(long millis) throws InterruptedException{
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
    public static void setUserID(Context context, String userID){

        OutputStream out;
        try {
            out = context.openFileOutput("user.info", MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,"UTF-8"));

            //上書きする
            writer.println(userID);
            writer.close();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }
    public static int getUserID(Context context){

        InputStream in;
        String lineBuffer;

        try {
            in = context.openFileInput("user.info");

            File f1  = new File("user.info");
            if( f1.exists() ){
                return -1;

            }
            BufferedReader reader= new BufferedReader(new InputStreamReader(in,"UTF-8"));
            while( (lineBuffer = reader.readLine()) != null ){
                //Log.d("FileAccess", lineBuffer);
                try {
                    //Integer.parseInt(lineBuffer);
                    return Integer.parseInt(lineBuffer);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            //e.printStackTrace();
            return -1;
        }
        return -1;
    }

    public static void createBitmapCache(Context context, Bitmap bitmap, String fileName){

        // 保存処理開始
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);

            // jpegで保存
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // 保存処理終了
            fos.close();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

}
