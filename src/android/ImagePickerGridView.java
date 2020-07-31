package com.fumi.imagePicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.GridView;

public class ImagePickerGridView extends GridView implements AbsListView.OnScrollListener {
    private OnBottomReachedListener bottomReachedListener;
    private int offset = 0;

    public ImagePickerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        defineScrolling();
    }

    private void defineScrolling() {
        this.setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int position = firstVisibleItem+visibleItemCount;
        int limit = totalItemCount - this.offset;

        if (position >= limit && totalItemCount > 0) {
            if (bottomReachedListener != null ) {
                bottomReachedListener.onBottomReached();
            }
        }
    }

    public OnBottomReachedListener getOnBottomReachedListener() {
        return bottomReachedListener;
    }

    public void setOnBottomReachedListener(
            OnBottomReachedListener onBottomReachedListener) {
        this.bottomReachedListener = onBottomReachedListener;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public interface OnBottomReachedListener {
        void onBottomReached();
    }
}
