package com.fumi.imagePicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * This class echoes a string called from JavaScript.
 */
public class ImagePicker extends CordovaPlugin {

    private static final CallbackContext PUBLIC_CALLBACKS = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("showPickerActivity")) {
            this.showPickerActivity(callbackContext, args);
            return true;
        } else if (action.equals("deleteFiles")) {
            this.deleteFiles(callbackContext);
            return true;
        }
        return false;
    }

    private void showPickerActivity(CallbackContext callbackContext, JSONArray args) {
        cordova.getActivity().runOnUiThread(() -> {
            Intent intent= new Intent(cordova.getContext(), PickerActivity.class);
            try {
                final JSONObject params = args.getJSONObject(0);
                if (params.has("mode")) {
                    intent.putExtra("mode", params.getString("mode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cordova.setActivityResultCallback(this);
            cordova.startActivityForResult(this, intent, 0);
        });
    }

    private void deleteFiles(CallbackContext callbackContext) {
        File files[] = cordova.getActivity().getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES).listFiles();

        for(int i = 0; i<files.length; i++){
            files[i].delete();
        }
        callbackContext.success("success");
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(resultCode == cordova.getActivity().RESULT_OK){
            Bundle extras = data.getExtras();// Get data sent by the Intent
            String result = extras.getString("result"); // data parameter will be send from the other activity.
            PluginResult resultDo = new PluginResult(PluginResult.Status.OK, result);
            resultDo.setKeepCallback(true);
            return;
        } else if(resultCode == cordova.getActivity().RESULT_CANCELED){
            PluginResult resultDo = new PluginResult(PluginResult.Status.OK, "canceled action, process this in javascript");
            resultDo.setKeepCallback(true);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}