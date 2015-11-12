package cn.lingox.android.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import cn.lingox.android.R;
import cn.lingox.android.utils.DpToPx;

public class UIHelper {
    private static UIHelper instance = null;

    private UIHelper() {
    }

    public static synchronized UIHelper getInstance() {
        if (instance == null) {
            instance = new UIHelper();
        }
        return instance;
    }

    public void textViewSetPossiblyNullString(TextView tv, String s) {
        if (s == null) {
            tv.setText("");
        } else if ("".equals(s)) {
            tv.setText("0");
        } else {
            tv.setText(s);
        }
    }

    /**
     * 处理用户头像图片问题
     *
     * @param context 上下文
     * @param iv      控件
     * @param url     图片URL
     * @param flag    标识 “crop”--裁切，“original”--原图，“circular”---压缩
     */
    public void imageViewSetPossiblyEmptyUrl(Context context, ImageView iv, String url, String flag) {
        if (!TextUtils.isEmpty(url)) {
            RequestCreator rc = Picasso.with(context).load(url).error(R.drawable.default_avatar);
            switch (flag) {
                case "crop"://裁切
                    rc.transform(new CropSquareTransformation());
                    break;
                case "original"://原图
                    rc.placeholder(null);
                    break;
                case "circular"://压缩---将图片压缩到58dpX58dp
                    int size = DpToPx.dip2px(context, 85);
                    rc.resize(size, size);
                    rc.placeholder(R.drawable.default_avatar);
                    break;
            }
            rc.into(iv);
        } else {
            iv.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
        }
    }

    private class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap result = Bitmap.createBitmap(source, 0, (int) (source.getHeight() * 0.25),
                    source.getWidth(), (int) (source.getHeight() * 0.5));
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "square()";
        }
    }
}
