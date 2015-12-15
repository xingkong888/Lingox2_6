package cn.lingox.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

import cn.lingox.android.app.LingoXApplication;

/**
 *倾斜图片----用于显示国旗
 */
public class RotateImageView {
    public static Bitmap rotateImage(Context context, String countryCode) {
        BitmapDrawable bd;
        int resource = context.getResources().getIdentifier("flag_" + countryCode.toLowerCase(), "drawable", LingoXApplication.PACKAGE_NAME);
        bd = (resource == 0) ?
                (BitmapDrawable) context.getResources().getDrawable
                        (context.getResources().getIdentifier
                                ("flag_cn", "drawable", LingoXApplication.PACKAGE_NAME))
                :(BitmapDrawable) context.getResources().getDrawable(resource);

        Bitmap bitmap = (bd != null) ? bd.getBitmap() : null;
        Matrix matrix = new Matrix();
        matrix.postRotate(-45);

        return Bitmap.createBitmap(bitmap, 0, 0,
                (bitmap!=null)?bitmap.getWidth():DpToPx.dip2px(context, 50),
                (bitmap!=null)?bitmap.getHeight():DpToPx.dip2px(context,50),
                matrix, true);
    }
}
