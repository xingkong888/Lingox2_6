package cn.lingox.android.helper;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;

// TODO make a better method for uploading images to server
//上传数据的网络请求
public class MsgSender {
    private static final String LOG_TAG = "MsgSender";
    private static final String APPVERSION = LingoXApplication.getInstance().getAppVersion();

    public static String postJsonToNet(String url, Map<String, String> params) {
        String jsonString;
        try {
            URL postURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) postURL.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type",
                    "application/x-www-form-urlencoded");
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            String paramStr = getParamString(params);

            if (null != paramStr) {
                out.write(paramStr);
            }
            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            jsonString = "";
            while ((lines = reader.readLine()) != null) {
                jsonString = jsonString + lines;
            }
            Log.d(LOG_TAG, jsonString);
            reader.close();
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedURLe: " + e.toString());
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.toString());
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Other Exception: " + e.toString());
            return null;
        }
        return jsonString;
    }

    /**
     * 上传json文件
     *
     * @param url 链接
     * @return 服务器返回数据
     */
    public static String postJsonToNet(String url) {
        String jsonString;
        try {
            URL postURL = new URL(url + "?" + StringConstant.verStr + "=" + APPVERSION);
            HttpURLConnection connection = (HttpURLConnection) postURL.openConnection();
//            connection.setConnectTimeout(time);设置连接主机超时（单位：毫秒）
//            connection.setReadTimeout(time);设置从主机读取数据超时（单位：毫秒）
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            jsonString = "";
            while ((lines = reader.readLine()) != null) {
                jsonString = jsonString + lines;
            }
//            Log.d(LOG_TAG, jsonString);
            reader.close();
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedURLe: " + e.toString());
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.toString());
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Other Exception: " + e.toString());
            return null;
        }
        return jsonString;
    }

    /**
     * 上传用户头像
     *
     * @param _url    链接
     * @param user_id 用户id
     * @param avatar  图片
     * @return 链接
     */
    public static String postAvatarToNet(String _url, String user_id, Bitmap avatar) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(_url + "?" + StringConstant.userIdStr + "=" + user_id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
            String newName = "avatar";
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " + "name=\"avatar\";filename=\"" + newName + "\"" + end);
            ds.writeBytes(end);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            avatar.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            InputStream isBm = new ByteArrayInputStream(baos.toByteArray());

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = isBm.read(buffer)) != -1) {
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            isBm.close();
            ds.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            String jsonString = "";
            while ((lines = reader.readLine()) != null)
                jsonString = jsonString + lines;
            reader.close();
            return jsonString;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Avatar upload failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * 上传用户相册图片
     *
     * @param _url        链接
     * @param user_id     用户id
     * @param description 图片描述
     * @param image       图片
     * @return 图片链接
     */
    public static String postPhotoToNet(String _url, String user_id, String description, Bitmap image) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(_url + "?"
                    + StringConstant.userIdStr + "=" + user_id + "&"
                    + StringConstant.photoDescription + "=" +
                    URLEncoder.encode(description, "UTF-8") + "&"
                    + StringConstant.verStr + "=" + APPVERSION
            );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
            String newName = "image";
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " + "name=\"image\";filename=\"" + newName + "\"" + end);
            ds.writeBytes(end);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            InputStream isBm = new ByteArrayInputStream(baos.toByteArray());

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = isBm.read(buffer)) != -1) {
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            isBm.close();
            ds.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            String jsonString = "";
            while ((lines = reader.readLine()) != null)
                jsonString = jsonString + lines;
            reader.close();

            return jsonString;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Photo upload failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从Map集合中将请求字段及数据取出
     *
     * @param params map集合
     * @return string
     */
    private static String getParamString(Map<String, String> params) {
        Object[] keys = params.keySet().toArray();
        if (keys.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = keys.length; i < j; i++) {
            try {
                sb.append(keys[i].toString())
                        .append("=")
                        .append(URLEncoder.encode(params.get(keys[i].toString()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "getParamString" + e.getMessage());
            }
            if (i != keys.length - 1) {
                sb.append("&");
            }
        }
//        Log.d(LOG_TAG, sb.toString());
        return sb.toString();
    }

    /**
     * 上传体验配图
     *
     * @param _url    链接
     * @param path_id 体验id
     * @param image   图片
     * @return 链接
     */
    public static String postPathImageToNet(String _url, String path_id, Bitmap image) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(_url + "?" + StringConstant.pathId + "=" + path_id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
            String newName = "pathImage";
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " + "name=\"pathImage\";filename=\"" + newName + "\"" + end);
            ds.writeBytes(end);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            InputStream isBm = new ByteArrayInputStream(baos.toByteArray());

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = isBm.read(buffer)) != -1) {
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            isBm.close();
            ds.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            String jsonString = "";
            while ((lines = reader.readLine()) != null)
                jsonString = jsonString + lines;
            reader.close();
            return jsonString;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Path image upload failed: " + e.getMessage());
            return null;
        }
    }
}