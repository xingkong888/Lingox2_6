package cn.lingox.android.helper;

import android.os.Handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import java.io.InputStream;

public class NetUtil {

    private static Handler mHandler = new Handler();

    public static void download(final String url, final Callback callback) {
        if (callback == null) {
            throw new RuntimeException("AUtils.Callback is not null!");
        }

        new Thread() {
            @Override
            public void run() {
                try {

                    HttpClient client = new DefaultHttpClient();
                    HttpGet get = new HttpGet(url);
                    HttpResponse resp = client.execute(get);
                    if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                        ByteArrayBuffer bytesBuffer = new ByteArrayBuffer(0);

                        InputStream is = resp.getEntity().getContent();
                        byte[] buffer = new byte[10 * 1024]; // 10k
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {
                            bytesBuffer.append(buffer, 0, len);

                            if (callback.isCanceled(url)) {
                                return;
                            }
                        }
                        final byte[] bytes = bytesBuffer.toByteArray();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 将数据回传给主线程
                                callback.response(url, bytes);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public interface Callback {
        void response(String url, byte[] data);

        boolean isCanceled(String url);
    }

}
