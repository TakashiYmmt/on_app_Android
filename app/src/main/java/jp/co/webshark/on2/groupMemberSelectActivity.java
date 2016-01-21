package jp.co.webshark.on2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import jp.co.webshark.on2.customViews.HttpImageView;
import jp.co.webshark.on2.customViews.UrlImageView;

public class groupMemberSelectActivity extends Activity {
    ArrayList<clsFriendInfo> groupMember;
    private ListView listView;
    private ScrollView scrollView;
    private InputMethodManager inputMethodManager;
    private RelativeLayout mainLayout;
    GroupMemberAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_select);

        //画面全体のレイアウト
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        listView = (ListView) findViewById(R.id.listView1);
        scrollView = (ScrollView) findViewById(R.id.scroll_body);

        onGlobal onGlobal = (onGlobal) this.getApplication();
        groupMember = (ArrayList<clsFriendInfo>)onGlobal.getShareData("groupMember");
        onGlobal.shareDataRemove("groupMember");
        drawGroupMemberInfo();
    }

    private void drawGroupMemberInfo(){

        adapter = new GroupMemberAdapter(groupMemberSelectActivity.this);
        int cellHeight = getResources().getDimensionPixelSize(R.dimen.group_cell_height);

        //this.groupMember = list;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // 実際のListViewに反映する
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        //params.height = list.size() * params.height;
        params.height = groupMember.size() * cellHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();

        adapter.notifyDataSetChanged();
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);

    }

    // リスト部の配列連結アダプタ
    private class GroupMemberAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater = null;

        public GroupMemberAdapter(Context context){
            this.context = context;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return groupMember.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.group_view_cell01,parent,false);

            HttpImageView profImage = (HttpImageView)convertView.findViewById(R.id.member_image);
            profImage.setImageUrl(groupMember.get(position).getImageURL(), parent.getResources().getDimensionPixelSize(R.dimen.group_cell_height), parent.getContext(),true);
            ((TextView)convertView.findViewById(R.id.cell_member_name)).setText(groupMember.get(position).getName());
            ((TextView)convertView.findViewById(R.id.cellHiddenIndex)).setText(Integer.toString(position));

            ImageView selectButtonImage = (ImageView)convertView.findViewById(R.id.cell_switch_button);
            if(groupMember.get(position).getSelected()){
                selectButtonImage.setImageResource(R.drawable.setup_check);
            }else{
                selectButtonImage.setImageResource(R.drawable.setup_add);
            }

            return convertView;
        }
    }


    // メンバー選択ボタン
    public void memberOnOff(View view){

        RelativeLayout cell = (RelativeLayout)view.getParent();
        String deleteIndex = "";
        ImageView switchButton = null;
        for (int i = 0 ; i < cell.getChildCount() ; i++) {
            View childview = cell.getChildAt(i);
            if (childview instanceof TextView) {
                if( i == 0 ){
                    TextView hiddenText = (TextView)childview;
                    deleteIndex = hiddenText.getText().toString();
                    groupMember.get(Integer.parseInt(deleteIndex)).setSelected(!groupMember.get(Integer.parseInt(deleteIndex)).getSelected());
                    //break;
                }
            }else if( childview instanceof ImageView ){
                if( i == 3 ) {
                    switchButton = (ImageView)childview;
                }
            }
        }

        if( switchButton != null ){
            if( groupMember.get(Integer.parseInt(deleteIndex)).getSelected() ){
                switchButton.setImageResource(R.drawable.setup_check);
            }else{
                switchButton.setImageResource(R.drawable.setup_add);
            }
        }
        // 再描画
        //adapter.notifyDataSetChanged();
    }

    // 完了ボタン
    public void completeSelect(View view) {
        // 選択済リストだけを形成してグローバルに持たせる
        ArrayList<clsFriendInfo> returnArray = new ArrayList<clsFriendInfo>();
        for( int i = 0 ; i < groupMember.size() ; i++ ){
            if( groupMember.get(i).getSelected() ){
                returnArray.add(groupMember.get(i));
            }
        }
        onGlobal onGlobal = (onGlobal) this.getApplication();
        onGlobal.setShareData("groupMember", returnArray);

        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate", true);
        finish();
    }

    // 戻るリンク
    public void backGroupEdit(View view) {

        // アクティビティを終了させる事により、一つ前のアクティビティへ戻る事が出来る。
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        intent.putExtra("isUpdate", false);
        finish();

    }


}
