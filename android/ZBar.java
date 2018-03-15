package org.cloudsky.cordovaPlugins;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.provider.MediaStore;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.cloudsky.cordovaPlugins.ZBarScannerActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;

public class ZBar extends CordovaPlugin {

    // Configuration ---------------------------------------------------

    private static int SCAN_CODE = 1;
    private static  int PHOTO_SELECT = 2;


    // State -----------------------------------------------------------

    private boolean isInProgress = false;
    private CallbackContext scanCallbackContext;


    // Plugin API ------------------------------------------------------

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext)
    throws JSONException
    {
        if(action.equals("scan")) {
            if(isInProgress) {
                callbackContext.error("A scan is already in progress!");
            } else {
                isInProgress = true;
                scanCallbackContext = callbackContext;
                JSONObject params = args.optJSONObject(0);

                Context appCtx = cordova.getActivity().getApplicationContext();
                Intent scanIntent = new Intent(appCtx, ZBarScannerActivity.class);
                scanIntent.putExtra(ZBarScannerActivity.EXTRA_PARAMS, params.toString());
                cordova.startActivityForResult(this, scanIntent, SCAN_CODE);
            }
            return true;
        }else{
            return false;
        }
    }


    // External results handler ----------------------------------------

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent result)
    {
        if(requestCode == SCAN_CODE) {
            boolean libraryFLAG = false;
            switch(resultCode) {
                case Activity.RESULT_OK:
                    JSONObject callBackObj = new JSONObject();
                    try {
                        callBackObj.put("value",result.getStringExtra(ZBarScannerActivity.EXTRA_QRVALUE));
                        callBackObj.put("format",result.getStringExtra(ZBarScannerActivity.EXTRA_CODEFORMAT));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    scanCallbackContext.success(callBackObj);
                    break;
                case Activity.RESULT_CANCELED:
                    scanCallbackContext.error("cancelled");
                    break;
                case ZBarScannerActivity.RESULT_ERROR:
                    scanCallbackContext.error("Scan failed due to an error");
                    break;
                case ZBarScannerActivity.PHOTO_REQUEST_GALLERY:
                    Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT) ; //"android.intent.action.GET_CONTENT"
                    innerIntent.setType( "image/*"); //查看类型 String IMAGE_UNSPECIFIED = "image/*";
                    innerIntent.addCategory(Intent. CATEGORY_OPENABLE );
                    cordova.startActivityForResult(this,Intent. createChooser(innerIntent, null) , PHOTO_SELECT);
                    libraryFLAG=true;
                    break;
                default:
                    scanCallbackContext.error("Unknown error");
            }
            if(!libraryFLAG){
                isInProgress = false;
                scanCallbackContext = null;
            }
        }else if (requestCode == PHOTO_SELECT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri intentData = result.getData();
                Image image = getImgFormLibrary(intentData);
                String scanedText = scanLibImg(image);
                if(image==null){
                    scanCallbackContext.error("无法打开图片");
                }else {
                    scanedText = scanLibImg(image);
                    if(scanedText == null){
                        scanCallbackContext.error("无法识别图片中的二维码");
                    }else{
                        scanCallbackContext.success(scanedText);
                    }
                }
            }else if(resultCode == Activity.RESULT_CANCELED){
                scanCallbackContext.error("cancelled");
            }else{
                scanCallbackContext.error("Scan failed due to an error");
            }
            isInProgress = false;
            scanCallbackContext = null;
        }
    }

    private Image getImgFormLibrary(Uri uri){//获取图像文件,并转成Image格式
        ContentResolver cr = this.cordova.getActivity().getContentResolver();
        try {
//            InputStream inStream = cr.openInputStream(uri);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr,uri);

            Image barcode = RBGToYUV.getYUVByBitmap(bitmap);
//            outStream.close();
//            inStream.close();
            return barcode;
        } catch (Exception e) {
            return null;
        }
    }

    private String scanLibImg(Image image){
        //set Scanner
        ImageScanner scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        // Set the config for barcode formats
        for(ZBarcodeFormat format : getFormats()) {
            scanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
        //scanner.setConfig(ZBarcodeFormat.QRCODE.getId(),Config.ENABLE,1);

        if (scanner.scanImage(image) != 0) {
            String qrValue = "";

            SymbolSet syms = scanner.getResults();
            for (Symbol sym : syms) {
                qrValue = sym.getData();
            }
            return qrValue;
        }else{
            return null;
        }
    }

    private Collection<ZBarcodeFormat> getFormats() {
        return ZBarcodeFormat.ALL_FORMATS;
    }
}


