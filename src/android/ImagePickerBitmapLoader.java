package com.fumi.imagePicker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import com.fenchtose.nocropper.CropMatrix;
import com.fenchtose.nocropper.CropperView;

public class ImagePickerBitmapLoader extends AsyncTask<String, Void, Bitmap> {
    private OnPostFinishedListener onPostFinishedListener;
    private CropperView cropperView;
    private CropMatrix matrix;

    public ImagePickerBitmapLoader() { }

    public ImagePickerBitmapLoader(CropperView cropperView, CropMatrix matrix) {
        this.cropperView = cropperView;
        this.matrix = matrix;
    }

    public void setOnPostFinishedListener(
            OnPostFinishedListener onPostFinishedListener) {
        this.onPostFinishedListener = onPostFinishedListener;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        Bitmap bitmap = BitmapFactory.decodeFile(strings[0]);
        bitmap = ImagePickerUtil.rotateBitmap(strings[0], bitmap);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (cropperView != null && bitmap != null) {
            cropperView.setImageBitmap(bitmap);
        }

        if (matrix != null)
            cropperView.setCropMatrix(matrix, true);


        if (onPostFinishedListener != null)
            onPostFinishedListener.onPostFinished(bitmap);

    }

    public interface OnPostFinishedListener {
        void onPostFinished(Bitmap bitmap);
    }
}