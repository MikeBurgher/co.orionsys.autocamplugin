/*
	    Copyright 2015 Mike Burgher.

		Licensed under the Apache License, Version 2.0 (the "License");
		you may not use this file except in compliance with the License.
		You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

		Unless required by applicable law or agreed to in writing, software
		distributed under the License is distributed on an "AS IS" BASIS,
		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
		See the License for the specific language governing permissions and
   		limitations under the License.   			
 */

package co.orionsys.autocamplugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

/**
 * Camera Activity Class. Configures Android camera to take picture and show it.
 */
public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";

	private Camera mCamera;
	private ForegroundCameraPreview mPreview;
    private FrameLayout mFrameLayout;
    private int nCamID;
    private int af;
    private int cameraCount = 0;
    private Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getResources().getIdentifier("autocamplugin", "layout", getPackageName()));

		// Select the camera, default to primary
		
		nCamID = (int) getIntent().getIntExtra("cameraDirection",-1);
		af = (int) getIntent().getIntExtra("sourceType",-1);
	    cameraCount = Camera.getNumberOfCameras();
	    if (nCamID == 1) {
	    	for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
	            Camera.getCameraInfo(camIdx, cameraInfo);
	            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	            	nCamID = camIdx;
	            }
	    	}
	    } else if (nCamID == 0) {
		    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
		        Camera.getCameraInfo(camIdx, cameraInfo);
		        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
		           nCamID = camIdx;
		         }
		   }
		} else {
			nCamID = 0;
		}
						
		// Create an instance of Camera
	    
		mCamera = getCameraInstance(nCamID);

		if (mCamera == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		Camera.Parameters params = mCamera.getParameters();
	    List<Camera.Size> sizes = params.getSupportedPictureSizes();
	    
	    int w = 0, h = 0;
	    for(Camera.Size s : sizes){
	    	// If larger, take it
	    	if (s.width * s.height > w * h) {
	    		w = s.width;
	    		h = s.height;
	    	}
	    }

	    params.setPictureSize(w, h);
	    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);   
	    mCamera.setParameters(params);
	    
		// Create a Preview and set it as the content of activity.

		mPreview = new ForegroundCameraPreview(this, mCamera);
		mFrameLayout = (FrameLayout) findViewById(getResources().getIdentifier("camera_preview", "id", getPackageName()));
		mFrameLayout.addView(mPreview);
	}

	@Override
	protected void onDestroy() {
		if (mCamera != null) {
			try {
			    mCamera.stopPreview();
	        	mCamera.setPreviewCallback(null); 
			} catch (Exception e) {
				Log.d(TAG, "Exception stopping camera: " + e.getMessage());
			}
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
		super.onDestroy();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(int nnCam) {
		Camera c = null;
		
		try {
			c = Camera.open(nnCam); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.d(TAG, "Camera not available: " + e.getMessage());
		}
		return c; // returns null if camera is unavailable
	}

	private PictureCallback mPicture = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {

			Uri fileUri = (Uri) getIntent().getExtras().get(
					MediaStore.EXTRA_OUTPUT);

			File pictureFile = new File(fileUri.getPath());

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
			setResult(RESULT_OK);
			finish();
		}
	};
	
	public class ForegroundCameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	    private SurfaceHolder mHolder;
	    private Camera mCamera;
	    private static final String TAG = "CameraPreview";

	    public ForegroundCameraPreview(Context context, Camera camera) {
	        super(context);
	        mCamera = camera;

	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        try {
	            mCamera.setPreviewDisplay(holder);
	            mCamera.startPreview();
	        } catch (IOException e) {
	            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	    }
	    

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

	        if (mHolder.getSurface() == null){
	          return;
	        }

	        try {
	            mCamera.stopPreview();
	        } catch (Exception e){
	        }

	        try {
	            mCamera.setPreviewDisplay(mHolder);
	            mCamera.startPreview();

	        } catch (Exception e){
	            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
	        }

	        if (af == 5) {
	        	mCamera.takePicture(null, null, mPicture);
	        } else {
			mCamera.autoFocus(new AutoFocusCallback() {
				
				public void onAutoFocus(boolean success, Camera camera) {
					mCamera.takePicture(null, null, mPicture);
				}
			});
	        }
	    }
	}
}