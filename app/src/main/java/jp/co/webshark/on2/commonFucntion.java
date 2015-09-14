package jp.co.webshark.on2;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.ContactsContract;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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
        BufferedOutputStream out = null;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        try {
            bao.flush();
            byte[] ba = bao.toByteArray();
            bao.close();

            FileOutputStream file = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            out = new BufferedOutputStream(file);
            for(int i=0; i<ba.length; i++) {
                out.write(ba[i]);
            }
            out.flush();
            file.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
    }

    public static Bitmap loadBitmapCache(Context context, String fileName){
        //BufferedInputStream bis = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(context.openFileInput(fileName));

            // 画像サイズ情報を取得する
            BitmapFactory.Options imageOptions = new BitmapFactory.Options();
            imageOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bis, null, imageOptions);

            bis.close();

            // もし、画像が大きかったら縮小して読み込む
            //  今回はimageSizeMaxの大きさに合わせる
            Bitmap bitmap;
            int imageSizeMax = 100;
            bis = new BufferedInputStream(context.openFileInput(fileName));
            float imageScaleWidth = (float)imageOptions.outWidth / imageSizeMax;
            float imageScaleHeight = (float)imageOptions.outHeight / imageSizeMax;

            // もしも、縮小できるサイズならば、縮小して読み込む
            if (imageScaleWidth > 2 && imageScaleHeight > 2) {
                BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();

                // 縦横、小さい方に縮小するスケールを合わせる
                int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));

                // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
                for (int i = 2; i <= imageScale; i *= 2) {
                    imageOptions2.inSampleSize = i;
                }

                bitmap = BitmapFactory.decodeStream(bis, null, imageOptions2);
            } else {
                bitmap = BitmapFactory.decodeStream(bis);
            }

            bis.close();

            //return BitmapFactory.decodeStream(bis);
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    public static HashMap getLocalAddressList(Context context){

        String[] Projection = { "display_name", ContactsContract.CommonDataKinds.Phone.DATA };
        HashMap returnMap = new HashMap();

        Cursor managedQuery = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,Projection, null, null, null);
        while (managedQuery.moveToNext()) {
            // japan-only
            if( managedQuery.getString(1).substring(0,3).equals("090")
                    || managedQuery.getString(1).substring(0,3).equals("080")){
                clsFriendInfo cfl = new clsFriendInfo();
                cfl.setName(managedQuery.getString(0));
                cfl.setTelephoneNumber(managedQuery.getString(1).replace("-", ""));
                returnMap.put(cfl.getTelephoneNumber(),cfl);
            }
        }

        return returnMap;
    }
    */

    public void setMyAddress(Context context){
        try{
            onGlobal onGlobal = (onGlobal) context.getApplicationContext();
            onGlobal.setShareData("myAddress",null);
            onGlobal.setShareData("myAddress",getAddressList(context));
        }catch (Exception e){}
    }

    public static String getArrayTelJsonList(Context context){
        try{
            onGlobal onGlobal = (onGlobal) context.getApplicationContext();
            ArrayList<clsFriendInfo> addressList = (ArrayList<clsFriendInfo>) onGlobal.getShareData("myAddress");
            String result = "";

            for( int i = 0 ; i < addressList.size() ; i++ ){
                ArrayList<String> phoneList = addressList.get(i).getArrPhone();
                for( String phoneNumber : phoneList ){
                    if( result.equals("") ){
                        result = phoneNumber;
                    }else{
                        result = result + "\",\"" + phoneNumber;
                    }
                }

            }

            result = "[\""+result+"\"]";
            return result;
        }catch (Exception e){
            return "";
        }
    }

    private ArrayList<clsFriendInfo> getAddressList(Context context){
        //Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        ContentResolver contentResolver = context.getContentResolver();
        // Cursor c = contentResolver.query(intent.getData(), null, null, null, null);
        String[] proj = null;
        String selection = null;
        String[] args = null;
        String sort = ContactsContract.CommonDataKinds.Contactables.PHONETIC_NAME;

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, proj, selection, args, sort);

        ArrayList<clsFriendInfo> inviteList = new ArrayList<clsFriendInfo>();

        if (cursor.moveToFirst()) {
            String id = null;
            String name = null;

            do {
                id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name = cursor.getString(cursor.getColumnIndex("display_name"));

                clsFriendInfo cfl = new clsFriendInfo();
                cfl.setName(name);  // 名前をセット

                // 電話番号とメールアドレスをリスト化して取得
                cfl.setArrPhone(getPhoneNumber(context, id));
                cfl.setArrEMail(getEmail(context, id));

                // 表示配列に格納
                if( !cfl.getArrPhone().isEmpty() ){
                    inviteList.add(cfl);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return inviteList;
    }

    // IDから電話番号を取得
    private ArrayList<String> getPhoneNumber(Context context, String id) {
        ArrayList<String> result = new ArrayList<String>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                , null
                , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id
                , null
                , ContactsContract.CommonDataKinds.Phone.NUMBER
        );
        if (cursor.moveToFirst()) {
            String lastInsert = "";
            do {
                String cursorData = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "");
                if( !lastInsert.equals(cursorData) ){
                    result.add(cursorData);
                    lastInsert = cursorData;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    // IDからメールアドレスを取得
    private ArrayList<String> getEmail(Context context, String id) {
        ArrayList<String> result = new ArrayList<String>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI
                , null
                , ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + id
                , null
                , null
        );
        if (cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
}
