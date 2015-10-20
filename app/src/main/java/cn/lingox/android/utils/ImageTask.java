package cn.lingox.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import java.io.InputStream;

import cn.lingox.android.app.LingoXApplication;

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
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    bytesBuffer.append(buffer, 0, len);
                }
                final byte[] bytes = bytesBuffer.toByteArray();

                if (bytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                            bytes.length);
//
                    bmpWidth = bitmap.getWidth();
                    bmpHeight = bitmap.getHeight();
                    //设置图片放大比例
                    double scale = LingoXApplication.getInstance().getWidth() / (double) bmpWidth;
                    //计算出这次要缩小的比例
                    float scaleWidth = (float) (1 * scale);
                    float scaleHeight = (float) (1 * scale);
                    //产生ReSize之后的bmp对象
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
                    bitmap1 = Bitmap.createBitmap(bitmap, 0, (int) (bmpHeight * 0.25)
                            , bmpWidth, bmpHeight / 2);
                    // 将图片保存到本地
                    FileUtil.saveImg(url, bitmap1);
                    ImageCache.getInstance().put(url, bitmap1);

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
