package cn.lingox.android.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.video.util.Utils;

public class RecorderVideoActivity extends BaseActivity implements
        OnClickListener, Callback, OnErrorListener, OnInfoListener {

    private final static String CLASS_LABEL = "RecordActivity";
    String localPath = "";// ¼�Ƶ���Ƶ·��
    Parameters cameraParameters = null;
    int defaultCameraId = -1, defaultScreenResolution = -1,
            cameraSelection = 0;
    int defaultVideoFrameRate = -1;
    MediaScannerConnection msc = null;
    private PowerManager.WakeLock mWakeLock;
    private ImageView btnStart;// ��ʼ¼�ư�ť
    private ImageView btnStop;// ֹͣ¼�ư�ť
    private MediaRecorder mediarecorder;// ¼����Ƶ����
    private SurfaceView surfaceview;// ��ʾ��Ƶ�Ŀؼ�
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    // Ԥ���Ŀ��
    private int previewWidth = 480;
    private int previewHeight = 480;

    @SuppressLint("NewApi")
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����ȫ��
        // ѡ��֧�ְ�͸��ģʽ������surfaceview��activity��ʹ��
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_recorder);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                CLASS_LABEL);
        mWakeLock.acquire();

        btnStart = (ImageView) findViewById(R.id.recorder_start);
        btnStop = (ImageView) findViewById(R.id.recorder_stop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
        SurfaceHolder holder = surfaceview.getHolder();// ȡ��holder
        holder.addCallback(this); // holder����ص��ӿ�
        // setType�������ã�Ҫ������.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void back(View view) {

        if (mediarecorder != null) {
            // ֹͣ¼��
            mediarecorder.stop();
            // �ͷ���Դ
            mediarecorder.release();
            mediarecorder = null;
        }
        try {
            mCamera.reconnect();
        } catch (IOException e) {
            Toast.makeText(this, "reconect fail", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock == null) {
            // ��ȡ������,������Ļ����
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                    CLASS_LABEL);
            mWakeLock.acquire();
        }
    }

    private void handleSurfaceChanged() {
        if (mCamera == null) {
            finish();
            return;
        }

        boolean hasSupportRate = false;
        List<Integer> supportedPreviewFrameRates = mCamera.getParameters()
                .getSupportedPreviewFrameRates();
        if (supportedPreviewFrameRates != null
                && supportedPreviewFrameRates.size() > 0) {
            Collections.sort(supportedPreviewFrameRates);
            for (int i = 0; i < supportedPreviewFrameRates.size(); i++) {
                int supportRate = supportedPreviewFrameRates.get(i);

                if (supportRate == 10) {
                    hasSupportRate = true;
                }

            }
            if (hasSupportRate) {
                defaultVideoFrameRate = 10;
            } else {
                defaultVideoFrameRate = supportedPreviewFrameRates.get(0);
            }

        }

        Log.d("RecorderVideoActivity", "supportedPreviewFrameRates"
                + supportedPreviewFrameRates);

        // ��ȡ����ͷ������֧�ֵķֱ���
        List<Camera.Size> resolutionList = Utils.getResolutionList(mCamera);
        if (resolutionList != null && resolutionList.size() > 0) {
            Collections.sort(resolutionList, new Utils.ResolutionComparator());
            Camera.Size previewSize = null;
            if (defaultScreenResolution == -1) {
                boolean hasSize = false;
                // �������ͷ֧��640*480����ôǿ����Ϊ640*480
                for (int i = 0; i < resolutionList.size(); i++) {
                    Size size = resolutionList.get(i);
                    if (size != null && size.width == 640 && size.height == 480) {
                        previewSize = size;
                        previewWidth = previewSize.width;
                        previewHeight = previewSize.height;
                        hasSize = true;
                        break;
                    }
                }
                // �����֧����Ϊ�м���Ǹ�
                if (!hasSize) {
                    int mediumResolution = resolutionList.size() / 2;
                    if (mediumResolution >= resolutionList.size())
                        mediumResolution = resolutionList.size() - 1;
                    previewSize = resolutionList.get(mediumResolution);
                    previewWidth = previewSize.width;
                    previewHeight = previewSize.height;

                }

            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recorder_start:
                mCamera.unlock();
                mediarecorder = new MediaRecorder();// ����mediarecorder����
                mediarecorder.reset();
                mediarecorder.setCamera(mCamera);
                mediarecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                // ����¼����ƵԴΪCamera�������
                mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                // ����¼����ɺ���Ƶ�ķ�װ��ʽTHREE_GPPΪ3gp.MPEG_4Ϊmp4
                mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                // ����¼�Ƶ���Ƶ����h263 h264
                mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                // ������Ƶ¼�Ƶķֱ��ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴�
                mediarecorder.setVideoSize(previewWidth, previewHeight);
                // // ����¼�Ƶ���Ƶ֡�ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴�
                if (defaultVideoFrameRate != -1) {
                    mediarecorder.setVideoFrameRate(defaultVideoFrameRate);
                }
                mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
                // ������Ƶ�ļ������·��
                localPath = PathUtil.getInstance().getVideoPath() + "/"
                        + System.currentTimeMillis() + ".mp4";
                mediarecorder.setOutputFile(localPath);
                mediarecorder.setOnErrorListener(this);
                mediarecorder.setOnInfoListener(this);
                try {
                    // ׼��¼��
                    mediarecorder.prepare();
                    // ��ʼ¼��
                    mediarecorder.start();
                    Toast.makeText(this, "¼��ʼ", Toast.LENGTH_SHORT).show();
                    btnStart.setVisibility(View.INVISIBLE);
                    btnStop.setVisibility(View.VISIBLE);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.recorder_stop:

                if (mediarecorder != null) {
                    // ֹͣ¼��
                    mediarecorder.stop();
                    // �ͷ���Դ
                    mediarecorder.release();
                    mediarecorder = null;
                }
                try {
                    mCamera.reconnect();
                } catch (IOException e) {
                    Toast.makeText(this, "reconect fail", Toast.LENGTH_SHORT).show();
                }
                btnStart.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.INVISIBLE);

                new AlertDialog.Builder(this)
                        .setMessage("�Ƿ��ͣ�")
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        arg0.dismiss();
                                        sendVideo(null);

                                    }
                                }).setNegativeButton(R.string.cancel, null).show();

                break;

            default:
                break;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // ��holder�����holderΪ��ʼ��oncreat����ȡ�õ�holder����������surfaceHolder
        surfaceHolder = holder;

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // ��holder�����holderΪ��ʼ��oncreat����ȡ�õ�holder����������surfaceHolder
        surfaceHolder = holder;
        try {
            initpreview();
        } catch (Exception e) {
            showFailDialog();
            return;
        }
        handleSurfaceChanged();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // surfaceDestroyed��ʱ��ͬʱ��������Ϊnull
        surfaceview = null;
        surfaceHolder = null;
        mediarecorder = null;
        releaseCamera();
    }

    protected void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
        }
    }

    @SuppressLint("NewApi")
    protected void initpreview() throws Exception {
        try {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                int numberOfCameras = Camera.getNumberOfCameras();
                CameraInfo cameraInfo = new CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == cameraSelection) {
                        defaultCameraId = i;
                    }
                }

            }
            if (mCamera != null) {
                mCamera.stopPreview();
            }

            mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
            mCamera.setPreviewDisplay(surfaceHolder);
            setCameraDisplayOrientation(this, CameraInfo.CAMERA_FACING_BACK,
                    mCamera);
            mCamera.startPreview();
        } catch (Exception e) {
            EMLog.e("###", e.getMessage());
            throw new Exception(e.getMessage());
        }

    }

    public void sendVideo(View view) {
        if (TextUtils.isEmpty(localPath)) {
            EMLog.e("Recorder", "recorder fail please try again!");
            return;
        }

        msc = new MediaScannerConnection(this,
                new MediaScannerConnectionClient() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d("RecorderVideoActivity", "scanner completed");
                        msc.disconnect();
                        setResult(RESULT_OK, getIntent().putExtra("uri", uri));
                        finish();
                    }

                    @Override
                    public void onMediaScannerConnected() {
                        msc.scanFile(localPath, "video/*");
                    }
                });
        msc.connect();

    }

    @Override
    public void onInfo(MediaRecorder arg0, int arg1, int arg2) {

    }

    @Override
    public void onError(MediaRecorder arg0, int arg1, int arg2) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }

    }

    @Override
    public void onBackPressed() {
        back(null);
    }

    private void showFailDialog() {
        new AlertDialog.Builder(this)
                .setTitle("��ʾ")
                .setMessage("���豸ʧ�ܣ�")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                finish();

                            }
                        }).setCancelable(false).show();

    }
}
