package cn.lingox.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

import cn.lingox.android.app.LingoXApplication;

/**
 * Created by Administrator on 2015/6/23.
 */
public class RotateImageView {
    public static Bitmap rotateImage(Context context, String countryCode) {
        BitmapDrawable bd;
        int resource = context.getResources().getIdentifier("flag_" + countryCode.toLowerCase(), "drawable", LingoXApplication.PACKAGE_NAME);
//        Log.d("res==", "res" + resource);
        if (resource == 0) {
//            Log.d("res==", "" + context.getResources().getIdentifier("flag_cn", "drawable", LingoXApplication.PACKAGE_NAME));
            //TODO 设置默认国旗
            bd = (BitmapDrawable) context.getResources().getDrawable(context.getResources().getIdentifier("flag_cn", "drawable", LingoXApplication.PACKAGE_NAME));
        } else {
            bd = (BitmapDrawable) context.getResources().getDrawable(resource);
        }
        Bitmap bitmap = bd.getBitmap();
        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        matrix.postRotate(-45);

        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return newBitmap;
    }
}
