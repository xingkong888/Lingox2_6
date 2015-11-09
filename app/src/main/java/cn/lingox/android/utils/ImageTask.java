package cn.lingox.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import java.io.InputStream;

public class ImageTask extends AsyncTask<String, Void, Bitmap> {
    private String url;
    private int bmpWidth = 0, bmpHeight = 0;
    private Bitmap resizeBmp = null, bitmap1 = null;
    private Callback1 callback;

    public ImageTask(Callback1 callback) {
        this.callback = callback;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        url = params[0];
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(params[0]);
            HttpResponse resp = client.execute(get);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                ByteArrayBuffer bytesBuffer = new ByteArrayBuffer(0);

                InputStream is = resp.getEntity().getContent();
                byte[] buffer = new byte[10 * 1024]; // 10k
                int len;
                while ((len = is.read(buffer)) != -1) {
                    bytesBuffer.append(buffer, 0, len);
                }
                final byte[] bytes = bytesBuffer.toByteArray();

                if (bytes != null) {
/*     采用ARGB_4444模式，图片失真率太高
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inPreferredConfig= Bitmap.Config.ARGB_4444;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                            bytes.length,options);
                            */
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                            bytes.length);
                    bmpWidth = bitmap.getWidth();
                    bmpHeight = bitmap.getHeight();
                    bitmap1 = Bitmap.createBitmap(bitmap, 0, (int) (bmpHeight * 0.25), bmpWidth, bmpHeight / 2);
                    // 将图片保存到本地
                    if (bitmap1 != null) {
                        bitmap.recycle();
//                        FileUtil.saveImg(url, bitmap1,);
                        ImageCache.getInstance().put(url, bitmap1);
                    }
                    return bitmap1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null)
            callback.response(url, result);
    }

    public interface Callback1 {
        void response(String url, Bitmap result); // 回传数据

        boolean isCancelled(String url); // 是否取消下载图片任务
    }
}
