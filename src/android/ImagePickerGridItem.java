package com.fumi.imagePicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ImagePickerGridItem extends ConstraintLayout {

    private ImageView imageView;
    private TextView selector;

    public ImagePickerGridItem(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item, this);
        imageView = findViewById(R.id.imageView);
        selector = findViewById(R.id.selector);
    }

    public void setScaleType(ImageView.ScaleType type){
        this.imageView.setScaleType(type);

    }

    public ImageView getImageView() {
        return this.imageView;
    }

    public View getSelector() {
        return this.selector;
    }

    public void setData(int position, int selected){
//        if (position == selected )
//            imageView.setAlpha(0.4f);
//        else
//            imageView.setAlpha(1.0f);
    }

    public void setMultiSelect(int position, ArrayList<Integer> selected){
        if (selected.contains(position)) {
            this.selector.setSelected(true);
            this.selector.setText(String.valueOf(selected.indexOf(new Integer(position))+1));
        } else {
            this.selector.setSelected(false);
            this.selector.setText("");
        }

    }
}
