package com.example.chesshelpservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;

public class MainActivity extends Activity {
    private MediaProjectionManager mManager;
    private ImageView mImage;
    private VirtualDisplay mDisplay;
    private MediaProjection mProtection;
    private volatile boolean mGetImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mImage = findViewById(R.id.image);
//        Intent intent = new Intent(MainActivity.this, CaptureService.class);
//        startForegroundService(intent);
    }

    public void onClick(View view) {
        mGetImage = true;
        if (mDisplay == null) {
            startActivityForResult(mManager.createScreenCaptureIntent(), 999);
        }
    }

    private void setupDisplay() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        mDisplay = mProtection.createVirtualDisplay("ytl_display", width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        Log.i("ytl", "setOnImageAvailableListener");
        imageReader.setOnImageAvailableListener(reader -> {
            Log.i("ytl", "on available");
//            if (!mGetImage) {
//                return;
//            }
            mGetImage = false;
            Image image = reader.acquireLatestImage();
            if (image != null) {
                processImage(image);
                image.close();
            }
        }, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("ytl", "code" + resultCode);

        Intent intent = new Intent(MainActivity.this, CaptureService.class);
        intent.putExtra("resultCode", resultCode);
        intent.putExtra("data", data);
        startForegroundService(intent);

//        mProtection = mManager.getMediaProjection(resultCode, data);
//        mProtection.registerCallback(new MediaProjection.Callback() {
//            @Override
//            public void onStop() {
//                super.onStop();
//                Log.i("ytl", "onStop");
//            }
//
//            @Override
//            public void onCapturedContentResize(int width, int height) {
//                super.onCapturedContentResize(width, height);
//                Log.i("ytl", "onCapturedContentResize " + width + "*" + height);
//            }
//
//            @Override
//            public void onCapturedContentVisibilityChanged(boolean isVisible) {
//                super.onCapturedContentVisibilityChanged(isVisible);
//                Log.i("ytl", "onCapturedContentVisibilityChanged " + isVisible);
//            }
//        }, null);
//
//        setupDisplay();
    }

    private void processImage(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Log.i("ytl", bitmap.getWidth() + " *" + bitmap.getHeight());
        mImage.setImageBitmap(bitmap);
    }

}
