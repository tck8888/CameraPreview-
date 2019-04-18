package com.acmeselect.camerapreview;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;


/**
 * <p>description:</p>
 * <p>created on: 2019/4/17</p>
 *
 * @author tck
 * @version 1.0
 */
public class CameraPreview extends FrameLayout implements SurfaceHolder.Callback {

    private String TAG = "CameraPreview";

    private Context context;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;

    private boolean isCameraPreview = false;
    private ImageView ivCover;

    public CameraPreview(@NonNull Context context) {
        this(context, null);
    }

    public CameraPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        surfaceView = new SurfaceView(context);
        addView(surfaceView, new FrameLayout.LayoutParams(-1, -1));

        surfaceView.getHolder().addCallback(this);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
        try {
            initCameraPreview();
            camera.setPreviewDisplay(holder);

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format,
                               int width,
                               int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void initCameraPreview() {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size bestSize = getBestSize(getWidth(), getHeight(), parameters.getSupportedPreviewSizes());
        if (bestSize == null) {
            parameters.setPreviewSize(getWidth(), getWidth());
        } else {
            parameters.setPreviewSize(bestSize.width, bestSize.height);
        }
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);

    }

    public void startPreview() {
        if (camera != null && holder != null) {
            isCameraPreview = true;
            camera.startPreview();
            camera.cancelAutoFocus();
        }
    }

    public void stopPreview() {
        if (camera != null) {
            if (isCameraPreview) {
                isCameraPreview = false;
                camera.stopPreview();
            }
        }
    }

    /**
     * 销毁摄像头
     */
    public void destroyCamera() {
        if (camera != null) {
            isCameraPreview = false;
            camera.stopPreview();
            camera.release();//释放相机
            camera = null;
        }
    }

    public void setCover(String imageUrl) {
        if (ivCover == null) {
            ivCover = new ImageView(context);
            ivCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addView(ivCover, new FrameLayout.LayoutParams(-1, -1));
        }
        //设置封面图

    }


    private Camera.Size getBestSize(Integer targetWidth,
                                    Integer targetHeight,
                                    List<Camera.Size> supportedPreviewSizes) {
        double targetRatio = targetHeight / (targetWidth + 0.0d);
        double minDiff = targetRatio;
        Camera.Size bestSize = null;
        for (Camera.Size size : supportedPreviewSizes) {
            if (size.width == targetHeight && size.height == targetWidth) {
                bestSize = size;
                break;
            }

            double supportedRatio = (size.width + 0.0d) / size.height;
            if (Math.abs(supportedRatio - targetRatio) < minDiff) {
                minDiff = Math.abs(supportedRatio - targetRatio);
                bestSize = size;
            }
        }
        Log.d(TAG, "targetWidth=" + targetWidth + "targetHeight=" + targetHeight + "targetRatio=" + targetRatio);
        if (bestSize != null) {
            Log.d(TAG, "bestSize.width=" + bestSize.width + "bestSize.height=" + bestSize.height);

        }
        return bestSize;
    }
}
