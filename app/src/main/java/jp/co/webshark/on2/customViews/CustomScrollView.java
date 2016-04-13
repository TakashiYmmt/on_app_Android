package jp.co.webshark.on2.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by takashi on 2016/04/08.
 */
public class CustomScrollView extends ScrollView{
    public interface ScrollToBottomListener {
        void onScrollToBottom(CustomScrollView scrollView);
    }

    public interface ScrollToTopListener {
        void onScrollToTop(CustomScrollView scrollView);
    }

    private ScrollToBottomListener scrollToBottomListener;
    private ScrollToTopListener scrollToTopListener;
    private int scrollBottomMargin = 0;
    private int scrollTopMargin = 0;
    private boolean finished = true;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defs) {
        super(context, attrs, defs);
    }

    public void setScrollToBottomListener(ScrollToBottomListener listener) {
        this.scrollToBottomListener = listener;
    }

    public void setScrollToTopListener(ScrollToTopListener listener) {
        this.scrollToTopListener = listener;
    }

    public void setScrollBottomMargin(int value) {
        this.scrollBottomMargin = value;
    }

    public void setScrollTopMargin(int value) {
        this.scrollTopMargin = value;
    }
    public void eventFinish(){
        finished = true;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(finished){
            View content = getChildAt(0);
            if (scrollToBottomListener == null) return;
            if (scrollToTopListener == null) return;
            if (content == null) return;
            if (y + this.getHeight() >= content.getHeight() - scrollBottomMargin) {
                finished = false;
                scrollToBottomListener.onScrollToBottom(this);
            }else if( y == 0 ){
                finished = false;
                scrollToTopListener.onScrollToTop(this);
            }
        }
    }
}
