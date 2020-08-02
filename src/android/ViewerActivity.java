package com.fumi.imagePicker;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class ViewerActivity extends Activity {

    ImagePickerGridView imagePickerGridView;
    ImageAdapter adapter;
    File[] files;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("picker_activity", "layout", getPackageName()));


        imagePickerGridView = findViewById(getResources().getIdentifier("gallery_grid_view", "id", getPackageName()));
    }

    @Override
    public void onResume() {
        super.onResume();

        files = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        adapter = new ImageAdapter(this);
        imagePickerGridView.setAdapter(adapter);
    }

    private class ImageAdapter extends BaseAdapter {

        private Activity context;


        public ImageAdapter(Activity localContext) {
            context = localContext;
        }


        public int getCount() {
            return files.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            ImagePickerGridItem gridView;
            if (convertView == null) {
                gridView = new ImagePickerGridItem(context);
                gridView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                gridView.setLayoutParams(new GridView.LayoutParams(270, 270));

            } else {
                gridView = (ImagePickerGridItem) convertView;
            }

            Glide.with(context)
                    .load(files[position])
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .centerCrop()
                    .into(gridView.getImageView());

            return gridView;
        }
    }
}
