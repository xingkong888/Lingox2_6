package cn.lingox.android.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lingox.android.R;
import cn.lingox.android.utils.ImageTask;

public class UIHelper {

    private static final String LOG_TAG = "UIHelper";
    private static UIHelper instance = null;

    private ExecutorService pool = Executors.newFixedThreadPool(5);

    private UIHelper() {
    }

    public static synchronized UIHelper getInstance() {
        if (instance == null)
            instance = new UIHelper();
        return instance;
    }

    public void textViewSetPossiblyNullString(TextView tv, String s) {
        if (s == null)
            tv.setText("");
        else
            tv.setText(s);
    }

    public void textViewSetPossiblyNullString(TextView tv, String s, int a) {
        if (s.equals(""))
            tv.setText("0");
        else
            tv.setText(s);
    }

    public void editTextSetPossiblyNullString(EditText et, String s) {
        if (s == null)
            et.setText("");
        else
            et.setText(s);
    }

    public void editBtnTextSetPossiblyNullString(Button btn, String s) {
        if (s == null)
            btn.setText("Select");
        else
            btn.setText(s);
    }

    public void imageViewSetPossiblyEmptyUrl(Context context, ImageView iv, String url, int placeholderResId) {
        if (!TextUtils.isEmpty(url))
            Picasso.with(context).load(url).placeholder(placeholderResId).into(iv);
    }

    public void imageViewSetPossiblyEmptyUrl(final ImageView iv, String url) {
        if (!TextUtils.isEmpty(url)) {
            new ImageTask(new ImageTask.Callback1() {
                @Override
                public void response(String url, Bitmap result) {
                    iv.setImageBitmap(result);
                }

                @Override
                public boolean isCancelled(String url) {
                    return false;
                }
            }).executeOnExecutor(pool, url);
        } else {
            iv.setImageResource(R.drawable.nearby_nopic_294dp);
        }
    }

    public void imageViewSetPossiblyEmptyUrl(Context context, final ImageView iv, String url) {
        if (!TextUtils.isEmpty(url)) {
            Picasso.with(context)
                    .load(url)
//                    .transform(new CropSquareTransformation())
                    .error(R.drawable.nearby_nopic_294dp)
                    .into(iv);
        } else {
            iv.setImageResource(R.drawable.nearby_nopic_294dp);
        }
    }

//     class CropSquareTransformation implements Transformation {
//        @Override
//        public Bitmap transform(Bitmap source) {
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                source.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
//                int options = 100;
//            Log.d("星期",source.getByteCount()+"");
//                while (baos.toByteArray().length / 1024 > 100 && options>0) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
//                    baos.reset();//重置baos即清空baos
//                    source.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
//                    options -= 10;//每次都减少10
////                    Log.d("星期",options+">>>");
////                    Log.d("星期",baos.toByteArray().length / 1024+">>>"+baos.toByteArray().length);
//                }
////            int size = Math.min(source.getWidth(), source.getHeight());
////            int x = (source.getWidth() - size) / 2;
////            int y = (source.getHeight() - size) / 2;
//
////            if (result != source) {
////                source.recycle();
////            }
//            return source;
//        }
//
//        @Override
//        public String key() {
//            return "square()";
//        }
//    }
}
