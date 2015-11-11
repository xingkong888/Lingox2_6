package cn.lingox.android.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.FileOutputStream;
import java.io.IOException;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.utils.RotateImageView;

public class ImageHelper {
    // Constants
    public static final int RESIZE_AVATAR = 1;
    public static final int RESIZE_PHOTO = 2;
    public static final int RESIZE_CARD_IMAGE = 3;
    public static final int RESIZE_IMAGE_BEFORE_CROP = 4;
    private static final String LOG_TAG = "ImageHelper";
    private static ImageHelper instance = null;
    private Context context;

    private ImageHelper() {
    }

    public static synchronized ImageHelper getInstance() {
        if (instance == null) {
            instance = new ImageHelper();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void loadAvatar(ImageView imageView, String uri) {
        if (isURIEmpty(uri)) {
            Log.d(LOG_TAG, "loadAvatar: uri was empty");
            imageView.setImageResource(R.drawable.default_avatar);
        } else {
            Picasso.with(imageView.getContext()).load(uri).placeholder(R.drawable.default_avatar).into(imageView);
        }
    }

    /**
     * 设置国旗
     *
     * @param imageView   显示控件
     * @param countryCode 国家代码
     * @param flag        1：倾斜的国旗 2：正常的国旗
     */
    public void loadFlag(ImageView imageView, String countryCode, int flag) {
        if (TextUtils.isEmpty(countryCode)) {
            imageView.setVisibility(View.GONE);
            Log.e(LOG_TAG, "Country1 code was empty");
            return;
        }
        try {
            imageView.setVisibility(View.VISIBLE);
            switch (flag) {
                case 1://倾斜的国旗
                    imageView.setImageBitmap(RotateImageView.rotateImage(context, countryCode));
                    break;
                case 2://正常的国旗
                    imageView.setImageResource(context.getResources().getIdentifier("flag_" + countryCode.toLowerCase(), "drawable", LingoXApplication.PACKAGE_NAME));
                    break;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception caught: " + e.toString());
        }
    }

    private boolean isURIEmpty(String uri) {
        return (TextUtils.isEmpty(uri) || uri.equals("-"));
    }

    // Shrinks the image in the given Uri to the same Uri
    public boolean shrinkImage(Uri imageUri, int type) {
        return shrinkImage(imageUri, imageUri, type);
    }

    public boolean shrinkImage(Uri imageUri, Uri newUri, int type) {
        return shrinkImage(imageUri.getPath(), newUri.getPath(), type);
    }

    // Shrinks the image in the given Uri to a new Uri
    public boolean shrinkImage(String imagePath, String newPath, int type) {
        FileOutputStream out = null;
        boolean failed = false;
        try {
            Bitmap bitmap = ImageHelper.getInstance().resizeBitmap(imagePath, type);
            out = new FileOutputStream(newPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
            failed = true;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.toString());
            }
        }
        return !failed;
    }

    public Bitmap resizeBitmap(String filePath) {
        return resizeBitmap(filePath, RESIZE_AVATAR);
    }

    public Bitmap resizeBitmap(String filePath, int type) {
        switch (type) {
            case RESIZE_AVATAR:
                return resizeBitmap(filePath, 800, 800);
            case RESIZE_PHOTO:
                return resizeBitmap(filePath, 800, 800);
            case RESIZE_CARD_IMAGE:
                return resizeBitmap(filePath, 800, 800);
            case RESIZE_IMAGE_BEFORE_CROP:
                return resizeBitmap(filePath, 800, 800);
            default:
                return resizeBitmap(filePath, 800, 800);
        }
    }

    private Bitmap resizeBitmap(String filePath, int maxWidth, int maxHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public int calculateInSampleSize(int width, int height, int maxWidth, int maxHeight) {
        int inSampleSize = 1;

        while (height > maxHeight || width > maxWidth) {
            inSampleSize = inSampleSize * 2;
            height = height / 2;
            width = width / 2;
        }
        return inSampleSize;
    }
}
