package org.getid.facerecognition;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AndroidCamera implements Camera.PreviewCallback{
    private static final String TAG = "AndroidCamera";

    private Camera mCamera;
    private int imageFormat;

    private byte[] FrameData = null;
    private int PreviewSizeWidth = 640;
    private int PreviewSizeHeight = 480;
    private Camera.Size actualSize;
    private boolean bProcessing = false;
    private SurfaceTexture surfaceTexture = new SurfaceTexture(10);
    Handler mHandler = new Handler(Looper.getMainLooper());

    public static Camera getCameraInstance() {
        Camera c = null;
        int cameraId = -1;
        //Get Front CameraID
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i <= numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        Log.d(TAG, "Get Front Camera ID = " + cameraId);    
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance

            //Get Support
            Camera.Parameters params = c.getParameters();
            // List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
            // int length = pictureSizes.size();
            // for (int i = 0; i < length; i++) {
            //     Log.e("SupportedPictureSizes","SupportedPictureSizes : " + pictureSizes.get(i).width + "x" + pictureSizes.get(i).height);
            // }

            // List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
            // length = previewSizes.size();
            // for (int i = 0; i < length; i++) {
            //     Log.e("SupportedPreviewSizes","SupportedPreviewSizes : " + previewSizes.get(i).width + "x" + previewSizes.get(i).height);
            // }
            for(int i =0; i< c.getParameters().getSupportedPreviewSizes().size(); i++){
                Camera.Size size = c.getParameters().getSupportedPreviewSizes().get(i);
                Log.d(TAG, "supported preview size: " + size.width + " " + size.height);
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, "Open Camera failed ID = " + cameraId);   
        }
        return c; // returns null if camera is unavailable

    }


    public AndroidCamera() {
        //super(context);
        Log.d(TAG, "Begin InitJNI From Java");
        Log.d(TAG, "AndroidCamera ptr == %s" + this);
        InitJNI();
    }

    public void startPreview() {
        Log.d(TAG, "Try to start preview");

        if (mCamera != null) {
            Log.w(TAG, "Already connected to camera! Skipping..");
            return;
        }

        mCamera = getCameraInstance();
        if (mCamera == null) {
            Log.e(TAG, "Cannot connect to camera");
            return;
        }

        Camera.Parameters parameters;
        parameters = mCamera.getParameters();
        parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
        imageFormat = parameters.getPreviewFormat();
        mCamera.setParameters(parameters);

        actualSize = parameters.getPreviewSize();
        Log.i(TAG, "Actual preview size: " + actualSize.width + "x" + actualSize.height);
        
        /* 
        This is outdated version. See for: 
        int[] textures = new int[1];  
            GLES20.glGenTextures(1, textures, 0);  
            mTextureID = textures[0];  
            GLES20.glBindTexture(android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID); 

		    surfaceTexture = new SurfaceTexture(mTextureID);
			mCamera.setPreviewTexture(surfaceTexture);
        */

        /* Workaround for API > 10. It needs some preview destination */
        if (Build.VERSION.SDK_INT > 10) {
            
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }

    public void stopPreview() {
        Log.d(TAG, "Try to stop preview");
        if (mCamera == null) {
            Log.w(TAG, "Already disconnected");
            return;
        }
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


        @Override
        public void onPreviewFrame(byte[] arg0, Camera arg1)
        {
            //Log.d(TAG, "Got frame");

            // At preview mode, the frame data will push to here.
            if (imageFormat == ImageFormat.NV21)
            {
                //We only accept the NV21(YUV420) format.
                if ( !bProcessing )
                {
                    FrameData = arg0;
                    mHandler.post(DoImageProcessing);
                }
            } else {
                Log.e(TAG, "Not my format of frame");
            }
        }


        private Runnable DoImageProcessing = new Runnable()
        {
            public void run()
            {
                bProcessing = true;
                FrameProcessing(actualSize.width, actualSize.height, FrameData);
                bProcessing = false;
            }
        };

    public void SetPreviewSize(int width, int height) {
        Log.i(TAG, "SET PREVIEW SIZE: " + width + " x " + height);
        PreviewSizeWidth = width;
        PreviewSizeHeight = height;
        stopPreview();
        startPreview();
    }

    public native boolean InitJNI();
    public native boolean FrameProcessing(int width, int height, byte[] NV21FrameData);

}
