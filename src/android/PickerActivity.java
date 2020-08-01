package com.fumi.imagePicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.fenchtose.nocropper.CropInfo;
import com.fenchtose.nocropper.CropMatrix;
import com.fenchtose.nocropper.CropResult;
import com.fenchtose.nocropper.Cropper;
import com.fenchtose.nocropper.CropperCallback;
import com.fenchtose.nocropper.CropperView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


public class PickerActivity extends Activity {

    private static String LOG = "log";
    private ArrayList<Pair<String, String>> imageInfo;

    private int loadMoreCount = 0;
    private int loadMoreBulkAmount = 120;
    private int uploadedCount = 0;

    CropperView cropperView;
    ImageView snapImageView;
    ImageView rotateImageView;
    ImageView multiImageView;
    ProgressBar progressBar;

    ImageAdapter adapter;

    private Bitmap changedBitmap;

    private boolean isMultiMode = false;

    private boolean isSnappedToCenter = false;
    private int selectedPosition = 0;
    private ArrayList<Integer> selectedPositions = new ArrayList<>();
    private ArrayList<CropMatrix> selectedImagesState = new ArrayList<>();
    private ArrayList<CropResult> selectedImagesInfo = new ArrayList<>();

    ImagePickerGridView imagePickerGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity);

        //Check Permission
        this.checkPermission();

        cropperView = findViewById(R.id.image_crop_view);
        snapImageView = findViewById(R.id.snap_button);
        rotateImageView = findViewById(R.id.rotate_button);
        multiImageView = findViewById(R.id.multi_button);
        imagePickerGridView = findViewById(R.id.gallery_grid_view);
        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void onResume() {
        super.onResume();

        selectedPositions.clear();
        selectedImagesState.clear();
        selectedImagesInfo.clear();
        uploadedCount = 0;

        adapter = new ImageAdapter(this);
        imageInfo = new ArrayList();
        imageInfo = getImagesPath();

        if (imageInfo.isEmpty())
            return;

        //첫 로딩
        ImagePickerBitmapLoader firstLoading = new ImagePickerBitmapLoader(cropperView, null);
        firstLoading.setOnPostFinishedListener((bitmap) -> {
            changedBitmap = bitmap;
        });
        firstLoading.execute(imageInfo.get(0).first);


        //GridView Loading
        imagePickerGridView.setAdapter(adapter);
        imagePickerGridView.setOnItemClickListener((adapter, view, position, id) -> {
            setSelection(view, position);

            int index = selectedPositions.indexOf(new Integer(selectedPosition));
            CropMatrix matrix = ((index != -1) && (index< selectedImagesState.size())) ? selectedImagesState.get(index) : null;

            ImagePickerBitmapLoader task = new ImagePickerBitmapLoader(cropperView, matrix);
            task.setOnPostFinishedListener((bitmap) -> {
                changedBitmap = bitmap;
            });
            task.execute(imageInfo.get(selectedPosition).first);
        });

        imagePickerGridView.setOnBottomReachedListener(() -> {
            if(imageInfo.size() <= loadMoreBulkAmount*(loadMoreCount+1)){
                loadMoreCount++;
                imageInfo.addAll(getImagesPath());
                adapter.notifyDataSetChanged();
            }
        });

        imagePickerGridView.setLongClickable(true);
        imagePickerGridView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!isMultiMode) {
                multiMode();
            }
            return isMultiMode;
        });

        //Button Event
        snapImageView.setOnClickListener(v -> snapImage());
        rotateImageView.setOnClickListener(v -> rotateImage());
        multiImageView.setOnClickListener(v -> multiMode());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater  inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.next_btn) {
            progressBar.setVisibility(View.VISIBLE);
            uploadImages();
            return true;
        } else if (itemId == R.id.test_btn) {
            Intent intent1 = new Intent(this, ViewerActivity.class);
            startActivity(intent1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    1052);
        }
    }

    private ArrayList<Pair<String, String>> getImagesPath() {
        int column_index_data;
        String absolutePathOfImage;

        ArrayList<Pair<String,String>> listOfAllImageIndex = new ArrayList();
        String imageIndex;

        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns._ID
        };

        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = getContentResolver().query(imageUri,
                projection,
                null,
                null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
        );


        column_index_data = cur.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int column_index_id = cur.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);

        cur.moveToPosition(loadMoreCount*loadMoreBulkAmount - 1);
        while (cur.moveToNext()) {
            int x = cur.getPosition();
            if(cur.getPosition() >= ((loadMoreCount + 1) * loadMoreBulkAmount))
                break;

            absolutePathOfImage = cur.getString(column_index_data);
            imageIndex = cur.getString(column_index_id);

            Pair<String, String> info = Pair.create(absolutePathOfImage, imageIndex);

            listOfAllImageIndex.add(info);
        }
        return listOfAllImageIndex;
    }



    private class ImageAdapter extends BaseAdapter {

        private Activity context;


        public ImageAdapter(Activity localContext) {
            context = localContext;
        }


        public int getCount() {
            return imageInfo.size();
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

            gridView.getSelector().setVisibility(isMultiMode ? View.VISIBLE : View.INVISIBLE);;
            gridView.setData(position, selectedPosition);
            gridView.setMultiSelect(position, selectedPositions);

            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                    getContentResolver(), Long.parseLong(imageInfo.get(position).second),
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    (BitmapFactory.Options) null );

            Glide.with(context)
                    .load(bitmap)
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .centerCrop()
                    .into(gridView.getImageView());

            return gridView;
        }
    }

    private void rotateImage() {
        if (changedBitmap == null) {
            return;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        int width = changedBitmap.getWidth();
        int height = changedBitmap.getHeight();
        changedBitmap = Bitmap.createBitmap(changedBitmap, 0, 0, width, height, matrix, true);


        cropperView.setImageBitmap(changedBitmap);
    }

    private void snapImage() {
        if (isSnappedToCenter) {
            cropperView.cropToCenter();
        } else {
            cropperView.fitToCenter();
        }

        isSnappedToCenter = !isSnappedToCenter;
    }

    private void multiMode() {
        // multi mode가 아닌 상태에서 click
        if (isMultiMode) {
            selectedPositions.clear();
            selectedImagesState.clear();
            selectedImagesState.clear();
        }

        isMultiMode = !isMultiMode;
        adapter.notifyDataSetChanged();

        multiImageView.setSelected(isMultiMode);
    }

    private void setSelection(View view, int position) {
        if (isMultiMode) {
            if (selectedPositions.contains(position)) {
                if (selectedPosition == position) {
                    removeCropState();
                    selectedPositions.remove(new Integer(position));

                    selectedPosition = selectedPositions.isEmpty() ? position : selectedPositions.get(selectedPositions.size() - 1);
                } else {
                    setCropState();
                    selectedPosition = position;
                }
            } else {
                if (selectedPositions.size() < 10) {
                    selectedPositions.add(position);
                    addCropState();
                    selectedPosition = position;
                }
            }

        } else {
            selectedPosition = position;

        }

        adapter.notifyDataSetChanged();
    }

    private void removeCropState() {
        int index = selectedPositions.indexOf(selectedPosition);
        if (selectedImagesState.size() > index) {
            selectedImagesState.remove(index);
            selectedImagesInfo.remove(index);
        }
    }

    private void setCropState() {
        int index = selectedPositions.indexOf(selectedPosition);
        if (selectedImagesState.size() > index) {
            selectedImagesState.set(index, cropperView.getCropMatrix());
            selectedImagesInfo.set(index, cropperView.getCropInfo());
        }
        else {
            selectedImagesState.add(cropperView.getCropMatrix());
            selectedImagesInfo.add(cropperView.getCropInfo());
        }
    }

    private void addCropState() {
        int index = selectedPositions.indexOf(selectedPosition);
        if (selectedImagesState.size() == index && index != -1) {
            selectedImagesState.add(cropperView.getCropMatrix());
            selectedImagesInfo.add(cropperView.getCropInfo());
        }
    }

    private void uploadImages() {
        if (isMultiMode)
            uploadMultipleImages();
        else
            uploadOneImage();
    }

    private void uploadMultipleImages() {

        if (selectedPositions.size()-1 == selectedImagesInfo.size())
            selectedImagesInfo.add(cropperView.getCropInfo());

        for (int i=0; i<selectedImagesInfo.size(); i++){
            ImagePickerBitmapLoader task = new ImagePickerBitmapLoader();
            int finalI = i;
            task.setOnPostFinishedListener((bitmap) -> {
                Cropper cropper = new Cropper(selectedImagesInfo.get(finalI).getCropInfo(), bitmap);
                cropper.crop(cropCallbaack());
            });
            task.execute(imageInfo.get(selectedPositions.get(i)).first);
        }
    }

    private void uploadOneImage() {
        CropInfo cropInfo = cropperView.getCropInfo().getCropInfo();
        ImagePickerBitmapLoader task = new ImagePickerBitmapLoader();
        task.setOnPostFinishedListener((bitmap) -> {
            Cropper cropper = new Cropper(cropInfo, bitmap);
            cropper.crop(cropCallbaack());
        });
        task.execute(imageInfo.get(selectedPosition).first);
    }

    public CropperCallback cropCallbaack() {
        return new CropperCallback() {
            @Override
            public void onCropped(Bitmap bitmap) {
                try {
                    String filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + Long.toString(Calendar.getInstance().getTimeInMillis()) + ".png";
                    ImagePickerUtil.writeBitmapToFile(bitmap, new File(filePath), 100);
                    uploadedCount++;
                    finishActivity();
                } catch( IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError() {
                //need to delete uploaded tmpFile;
                finish();
            }
        };
    }

    public void finishActivity() {
        if ( selectedPositions.size() != uploadedCount || selectedPositions.size() == 0)
            return;
        Intent intent = new Intent();
//        intent.putExtra("response", new Response("success", "", uploadedCount, ));

        setResult(RESULT_OK, intent);
        finish();
    }

//    public class Response{
//        private String result;
//        private String message;
//        private int count;
//        private String[] path;
//
//        public Response(String result, String message, int count, String[] path){
//            this.result = result;
//            this.message = message;
//            this.count = count;
//            this.path = path;
//        }
//    }
}