package com.ets.sdoddapaneni.videosample1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private Button capture, switchCamera, photoBtn;
    private Context myContext;
    private boolean cameraFront = false;
    private TextView countDownTextView, countDownTimer;
    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;
    AlertDialog alertDialog1;
    CharSequence[] values = {" Low ", " High "};
    String qualityValue;
    private ImageView redimg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        myContext = this;
        initialize();
    }


    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            // if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            mPreview.refreshCamera(mCamera);
        }
    }

    public void initialize() {

        FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);

        countDownTextView = (TextView) findViewById(R.id.countDownTextView);
        countDownTimer = (TextView) findViewById(R.id.countDownTimer20);
        redimg = (ImageView) findViewById(R.id.red_image);

        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        capture = (Button) findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
        switchCamera.setOnClickListener(switchCameraListener);

        photoBtn = (Button) findViewById(R.id.button_photo);
        photoBtn.setOnClickListener(photoListener);

        rawCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        /* Handles data for jpeg picture */
        shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };
        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {

                File photo = new File(Environment.getExternalStorageDirectory(), "Photo_" + System.currentTimeMillis() + ".jpg");
                if (photo.exists()) {
                    photo.delete();
                }
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(photo.getPath());
                    outStream.write(data);
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Photo captured!", Toast.LENGTH_LONG).show();
            }
        };
    }

    OnClickListener switchCameraListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    // release the old camera instance
                    // switch camera, from the front and the back and vice versa

                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    OnClickListener photoListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        }
    };

    public void chooseCamera() {
        // if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    boolean recording = false;
    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (recording) {
                cdt1.cancel();
                cdt2.cancel();
                redimg.setVisibility(View.INVISIBLE);
                redimg.clearAnimation();
                countDownTimer.setVisibility(View.INVISIBLE);
                countDownTimer.clearAnimation();
                // stop recording and release camera
                mediaRecorder.stop(); // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                Toast.makeText(MainActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
                recording = false;
                switchCamera.setVisibility(View.VISIBLE);
                switchCamera.setEnabled(true);
                capture.setText("Record");
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select a Quality");
                builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                qualityValue = "1";
                                break;
                            case 1:
                                qualityValue = "2";
                                break;
                        }
                        alertDialog1.dismiss();
                        cdt1.cancel();
                        cdt1.start();
                    }
                });
                alertDialog1 = builder.create();
                alertDialog1.show();
            }
        }
    };

    CountDownTimer cdt1 = new CountDownTimer(4000, 1000) {

        public void onTick(long millisUntilFinished) {
            capture.setEnabled(false);
            switchCamera.setEnabled(false);
            countDownTextView.setVisibility(View.VISIBLE);
            countDownTextView.setText("" + millisUntilFinished / 1000);
            //here you can have your logic to set text to edittext
        }

        public void onFinish() {

            countDownTextView.setVisibility(View.INVISIBLE);
            if (!prepareMediaRecorder()) {
                Toast.makeText(MainActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                finish();
            }
            // work on UiThread for better performance
            runOnUiThread(new Runnable() {
                public void run() {
                    // If there are stories, add them to the table
                    try {
                        mediaRecorder.start();
                    } catch (final Exception ex) {
                        // Log.i("---","Exception in thread");
                    }
                }
            });
            capture.setEnabled(true);
            switchCamera.setVisibility(View.INVISIBLE);
            capture.setText("Stop");
            recording = true;
            cdt2.cancel();
            cdt2.start();
        }
    };

    CountDownTimer cdt2 = new CountDownTimer(21000, 1000) {

        public void onTick(long millisUntilFinished) {
            countDownTimer.setVisibility(View.VISIBLE);

            redimg.setVisibility(View.VISIBLE);
            Animation animcir = new AlphaAnimation(0.0f, 1.0f);
            animcir.setDuration(1000); //You can manage the blinking time with this parameter
            animcir.setRepeatMode(Animation.REVERSE);
            animcir.setRepeatCount(Animation.INFINITE);
            redimg.startAnimation(animcir);

            if ((millisUntilFinished / 1000) % 60 < 10)
                countDownTimer.setText("00:0" + millisUntilFinished / 1000 + " / 00:20");
            else
                countDownTimer.setText("00:" + millisUntilFinished / 1000 + " / 00:20");

            if ((millisUntilFinished / 1000) % 60 < 6) {
                Animation anim1 = new AlphaAnimation(0.0f, 1.0f);
                anim1.setDuration(1000); //You can manage the blinking time with this parameter
                anim1.setStartOffset(20);
                anim1.setRepeatMode(Animation.REVERSE);
                anim1.setRepeatCount(Animation.INFINITE);
                countDownTimer.startAnimation(anim1);
            }
        }

        public void onFinish() {
            cdt1.cancel();
            cdt2.cancel();
            redimg.setVisibility(View.INVISIBLE);
            redimg.clearAnimation();
            countDownTimer.setVisibility(View.INVISIBLE);
            countDownTimer.clearAnimation();
            // stop recording and release camera
            mediaRecorder.stop(); // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            Toast.makeText(MainActivity.this, "Video captured!", Toast.LENGTH_LONG).show();
            recording = false;
            switchCamera.setVisibility(View.VISIBLE);
            switchCamera.setEnabled(true);
            capture.setText("Stop");
        }
    };

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        switch (qualityValue) {
            case "1":
                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
                break;
            case "2":
                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                break;
            default:
                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
                break;
        }

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0) {
            mediaRecorder.setOrientationHint(90);
        }
        if (display.getRotation() == Surface.ROTATION_90) {
            mediaRecorder.setOrientationHint(0);
        }
        if (display.getRotation() == Surface.ROTATION_180) {
            mediaRecorder.setOrientationHint(270);
        }
        if (display.getRotation() == Surface.ROTATION_270) {
            mediaRecorder.setOrientationHint(180);
        }

        File newFile = null;
        try {
            newFile = File.createTempFile("videocapture", ".mp4", Environment.getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (newFile != null) {
            mediaRecorder.setOutputFile(newFile.getAbsolutePath());
        }

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}