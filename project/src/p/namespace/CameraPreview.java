package p.namespace;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public void Alert(String msg){	// TODO: refactor two Alerts into some separate class maybe
	    AlertDialog ad = new AlertDialog.Builder(getContext()).create();  
	    ad.setCancelable(false);   
	    ad.setMessage(msg);  
	    ad.setButton("OK", new DialogInterface.OnClickListener() {  
	         public void onClick(DialogInterface dialog, int which) {  
	             dialog.dismiss();                      
	         }  
	    });  
	    ad.show(); 
    }
    
    public CameraPreview(Context context, Camera camera) {
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
        	Alert("Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // this generally happens no earlier, then when quitting
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
          return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e){
        	Alert("Failed stoping preview: " + e.getMessage());
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
        	Alert("Error starting camera preview: " + e.getMessage());
        }
    }
}

