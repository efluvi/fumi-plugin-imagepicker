package com.fumi.imagePicker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class ImagePicker extends CordovaPlugin {

    private static final CallbackContext PUBLIC_CALLBACKS = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if (action.equals("showPickerActivity")) {
            this.showPickerActivity(callbackContext);
            return true;
        } else if (action.equals("deleteFiles")) {
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            Intent intent= new Intent(cordova.getContext(), PickerActivity.class);
//            intent.putExtra("..", false);
            cordova.setActivityResultCallback(this);

            cordova.startActivityForResult(this, intent, 0);
        });
        
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void showPickerActivity(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(() -> {
            Intent intent= new Intent(cordova.getContext(), PickerActivity.class);
            cordova.setActivityResultCallback(this);

            cordova.startActivityForResult(this, intent, 0);
        });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(resultCode == cordova.getActivity().RESULT_OK){
            Bundle extras = data.getExtras();// Get data sent by the Intent
            String information = extras.getString("data"); // data parameter will be send from the other activity.
            PluginResult resultDo = new PluginResult(PluginResult.Status.OK, "this value will be sent to cordova");
            resultDo.setKeepCallback(true);
//            PUBLIC_CALLBACKS.sendPluginResult(resultDo);
            return;
        } else if(resultCode == cordova.getActivity().RESULT_CANCELED){
            PluginResult resultDo = new PluginResult(PluginResult.Status.OK, "canceled action, process this in javascript");
            resultDo.setKeepCallback(true);
//            PUBLIC_CALLBACKS.sendPluginResult(resultDo);
            return;
        }
        // Handle other results if exists.
        super.onActivityResult(requestCode, resultCode, data);
    }
}
