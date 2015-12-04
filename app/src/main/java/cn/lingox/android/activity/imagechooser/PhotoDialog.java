package cn.lingox.android.activity.imagechooser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.lingox.android.R;
import cn.lingox.android.activity.PathCardImgDialog;
import cn.lingox.android.activity.imagechooser.crop.ClipImageLayout;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.utils.CompressImageUtil;
import cn.lingox.android.utils.FileUtil;

public class PhotoDialog extends Activity implements OnClickListener {
    // INCOMING INTENT EXTRAS
    public static final String REQUESTED_IMAGE = LingoXApplication.PACKAGE_NAME + ".REQUESTED_IMAGE";
    // RETURN INTENT EXTRAS
    public static final String SELECTED_IMAGE_LIST = LingoXApplication.PACKAGE_NAME + ".SELECTED_IMAGE_LIST";
    public static final String SELECTED_SINGLE_IMAGE = LingoXApplication.PACKAGE_NAME + ".SELECTED_SINGLE_IMAGE";
    // INCOMING REQUEST CODES
    public static final int REQUEST_AVATAR = 101;
    public static final int REQUEST_PHOTO = 102;
    public static final int REQUEST_CARD_IMAGE = 103;
    // OUTGOING REQUEST CODES
    static final int PHOTO_NEW = 201;
    static final int PHOTO_SELECT = 202;
    static final int PHOTO_SELECT_MULTIPLE = 203;
    static final int PHOTO_PRESET = 204;
    private static final String LOG_TAG = "PhotoDialog";
    private int requestedImageType = REQUEST_AVATAR;  // Avatar is default if no intent passed
    private Uri imageUri;
    private boolean sdCardMounted = false;
    private Bitmap croppedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(REQUESTED_IMAGE)) {
            switch (getIntent().getIntExtra(REQUESTED_IMAGE, REQUEST_AVATAR)) {
                case REQUEST_AVATAR:
                    requestedImageType = REQUEST_AVATAR;
                    break;
                case REQUEST_PHOTO:
                    requestedImageType = REQUEST_PHOTO;
                    break;
                case REQUEST_CARD_IMAGE:
                    requestedImageType = REQUEST_CARD_IMAGE;
                    break;
                default:
                    requestedImageType = REQUEST_AVATAR;
                    break;
            }
        }
        sdCardMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        initView();
    }

    /**
     * 初始化控件实例
     */
    private void initView() {
        setContentView(R.layout.dialog_image);
        //相机
        findViewById(R.id.camera).setOnClickListener(this);
        //本地图片
        findViewById(R.id.photo).setOnClickListener(this);
        //应用提供的图片
        TextView photoRecommend = (TextView) findViewById(R.id.photo_recommend);
        photoRecommend.setOnClickListener(this);
        photoRecommend.setVisibility(requestedImageType == REQUEST_CARD_IMAGE ? View.VISIBLE : View.GONE);

        TextView cancel = (TextView) findViewById(R.id.photo_cancel);
        cancel.setOnClickListener(this);

        // TODO Check if this is still required now that the layout is an activity instead of a dialog
        //设置页面的启动方式---从下往上
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = dm.widthPixels;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.camera:
                try {
                    imageUri = getOutputMediaFileUri();
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, PHOTO_NEW);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to take new photo: " + e.toString());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.photo:
                if (sdCardMounted) {
                    if (requestedImageType == REQUEST_PHOTO) {
                        intent = new Intent(this, AlbumListActivity.class);
                        intent.putExtra(AlbumListActivity.SELECT_MULTIPLE, true);
                        startActivityForResult(intent, PHOTO_SELECT_MULTIPLE);
                    } else {
                        intent = new Intent(this, AlbumListActivity.class);
                        intent.putExtra(AlbumListActivity.SELECT_MULTIPLE, false);
                        startActivityForResult(intent, PHOTO_SELECT);
                    }
                } else
                    Toast.makeText(this, getString(R.string.unable_sd), Toast.LENGTH_SHORT).show();
                break;
            case R.id.photo_recommend:
                startActivityForResult(new Intent(this, PathCardImgDialog.class), PHOTO_PRESET);
                break;
            case R.id.photo_cancel:
                setResult(111, new Intent(this, AddPhotosActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_NEW:
                if (resultCode == AlbumListActivity.RESULT_OK) {
                    if (ImageHelper.getInstance().shrinkImage(imageUri, ImageHelper.RESIZE_IMAGE_BEFORE_CROP)) {
                        startPhotoZoom(imageUri);
                    }
                }
                break;
            case PHOTO_SELECT:
                if (resultCode == AlbumListActivity.RESULT_OK) {
                    if (data.hasExtra(SELECTED_SINGLE_IMAGE)) {
                        try {
                            imageUri = getOutputMediaFileUri();
                            if (ImageHelper.getInstance().shrinkImage(
                                    data.getStringExtra(SELECTED_SINGLE_IMAGE),
                                    imageUri.getPath(), ImageHelper.RESIZE_IMAGE_BEFORE_CROP)) {
                                startPhotoZoom(imageUri);
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, getString(R.string.error_photo), Toast.LENGTH_LONG).show();
                            Log.e(LOG_TAG, "onActivityResult(): PHOTO_SELECT 1: " + e.toString());
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.error_select_photo), Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "onActivityResult(): PHOTO_SELECT 2: data did not have the expected intent");
                    }
                }
                break;
            case PHOTO_SELECT_MULTIPLE:
                if (resultCode == AlbumListActivity.RESULT_OK) {
                    if (data.hasExtra(SELECTED_IMAGE_LIST)) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
                break;
            case PHOTO_PRESET:
                if (resultCode == PathCardImgDialog.RESULT_OK) {
                    if (data.hasExtra(PathCardImgDialog.PRESET_URI)) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
                break;
        }
    }

    // TODO Add file name dependant on requested image type (Eg. if request card: "Activity_timestamp.jpg"
    private Uri getOutputMediaFileUri() throws Exception {
        if (!sdCardMounted)
            throw new Exception("Unable to create new image: SD card is not mounted");

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "LingoX");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG, "failed to create directory (possibly already exists)");
            }
        }
        // Create a media file name
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhMMss", Locale.CHINA);
        String timeStamp = format.format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        if (!mediaFile.createNewFile()) {
            Log.e(LOG_TAG, "File already exists (despite the name having a timestamp in)," +
                    " the file may be overwritten");
        }
//        Log.d(LOG_TAG, "getOutputMediaFile: " + mediaFile.toString());
        return Uri.fromFile(mediaFile);
    }

    /**
     * 裁切图片
     *
     * @param data 图片的uri
     */
    public void startPhotoZoom(final Uri data) {
        setContentView(R.layout.dialog_image_crop);
        TextView cropImageButton = (TextView) findViewById(R.id.image_crop_button);
        final TextView confirmButton = (TextView) findViewById(R.id.image_confirm_button);

        final ClipImageLayout cropImageView = (ClipImageLayout) findViewById(R.id.id_clipImageLayout);
        final ImageView croppedImageView = (ImageView) findViewById(R.id.image_cropped_view);
        try {
            cropImageView.setDrawable(new BitmapDrawable(MediaStore.Images.Media.getBitmap(getContentResolver(), data)));
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
        cropImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.clip();
                croppedImageView.setImageBitmap(croppedImage);
                croppedImageView.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.VISIBLE);
                cropImageView.setVisibility(View.GONE);
            }
        });
        confirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new CropImage(data).execute();
            }
        });
    }

    /**
     * 图片裁切异步任务
     */
    private class CropImage extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog pd = new ProgressDialog(PhotoDialog.this);
        private Uri photoUri;

        public CropImage(Uri photoUri) {
            this.photoUri = photoUri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage("Cropping Image...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //压缩图片
                CompressImageUtil.compressImage(croppedImage);
                FileUtil.saveImg(photoUri.getPath(), croppedImage, PhotoDialog.this);
                return true;
            } catch (Exception e) {
                publishProgress(null, "Cropping Image Failed: " + e.getMessage());
                Log.e(LOG_TAG, "Recovery failed: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null) {
                pd.setMessage(values[0]);
            }
            if (values[1] != null) {
                Toast.makeText(PhotoDialog.this, values[1], Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                Toast.makeText(PhotoDialog.this, getString(R.string.image_crop), Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                returnIntent.putExtra(SELECTED_SINGLE_IMAGE, photoUri);
                setResult(RESULT_OK, returnIntent);
            } else {
                Toast.makeText(PhotoDialog.this, getString(R.string.fail_image_crop), Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}