package fastarrow.com.example.guyb.fastarrow;

/**
 * Created by guyb on 03/09/18.
 */
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.SurfaceHolder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera = null;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private Resources rRes;
    public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight) {
        Log.d("DEBUG", "Height: " + PreviewlayoutHeight + " Width:" + PreviewlayoutWidth);
        PreviewSizeWidth = PreviewlayoutWidth;
        PreviewSizeHeight = PreviewlayoutHeight;
    }
    public CameraPreview(Resources res) {
        rRes = res;
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        // At preview mode, the frame data will push to here.
        // But we do not want these data.
    }
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        setOrientation();
        Parameters parameters;

        parameters = mCamera.getParameters();
        if (rRes != null) {
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, rRes.getDisplayMetrics().widthPixels, rRes.getDisplayMetrics().heightPixels);
            Log.e("sizes", "Height: " + optimalSize.width + " Width:" + optimalSize.height);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        }
        else if (PreviewSizeHeight != 0 && PreviewSizeWidth != 0) {
            // Set the camera preview size
            parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
            // Set the take picture size, you can set the large size of the camera supported.
            parameters.setPictureSize(PreviewSizeWidth, PreviewSizeHeight);
        }
        // Turn on the camera flash.
        String NowFlashMode = parameters.getFlashMode();
        if (NowFlashMode != null)
            parameters.setFlashMode(Parameters.FLASH_MODE_ON);
        // Set the auto-focus.
        String NowFocusMode = parameters.getFocusMode();
        if (NowFocusMode != null)
            parameters.setFocusMode("auto");
        try {
            mCamera.setParameters(parameters);

        }
        catch (Exception e){
            Log.d("DEBUG", "----------------\n" + e.getMessage() + "\n-------------");
        }
        if (mCamera == null)
        {
            Log.v("check", "Camera is null!");
        }
        mCamera.startPreview();
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                    Log.e("verbose", "Opend camera :\n" + cam.toString());
                    return cam;
                } catch (RuntimeException e) {
                    Log.e("DEBUG", "------------Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }
    private void setOrientation()
    {
        if (rRes != null)
        {
            if (rRes.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                mCamera.setDisplayOrientation(0);
            }
            if (rRes.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                mCamera.setDisplayOrientation(0);
            }
        }
        else {
            mCamera.setDisplayOrientation(90);
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        mCamera = openFrontFacingCameraGingerbread();
        setOrientation();
        try {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}