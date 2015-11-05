package cn.lingox.android.helper;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;

// TODO make a better method for uploading images to server
public class MsgSender {
    private static final String LOG_TAG = "MsgSender";
    private static final String APPVERSION = LingoXApplication.getInstance().getAppVersion();
    public static HttpClient httpClient = new DefaultHttpClient();

    public static String postJsonToNet(String url, Map<String, String> params) {
        String jsonString;
        try {
            URL postURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) postURL
                    .openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type",
                    "application/x-www-form-urlencoded");
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream());
            String paramStr = getParamString(params);

            if (null != paramStr)
                out.write(paramStr);
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

    public static String postJsonToNet(String url) {
        String jsonString;
        try {
            URL postURL = new URL(url + "?"
                    + StringConstant.verStr + "=" + APPVERSION
            );
            HttpURLConnection connection = (HttpURLConnection) postURL
                    .openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type",
                    "application/x-www-form-urlencoded");
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream());
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

    public static String getTransation(final String url)
            throws InterruptedException, ExecutionException {
        FutureTask<String> task = new FutureTask<String>(
                new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        // 创建HttpGet对象
                        HttpGet get = new HttpGet(url);
                        // 发送get请求
                        HttpResponse httpResponse = httpClient.execute(get);
                        // 如果服务器成功返回响应
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            // 获取服务器响应的字符串
                            return EntityUtils.toString(httpResponse
                                    .getEntity());
                        }
                        return null;
                    }
                });
        new Thread(task).start();
        String s = null;
        try {
            JSONObject jsonObject = new JSONObject(task.get());
            s = jsonObject.getJSONArray("translation").getString(0).replace(",", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String postAvatarToNet(String _url, String user_id, Bitmap avatar) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(_url + "?" + StringConstant.userIdStr + "=" + user_id);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
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

    public static String postPhotoToNet(
            String _url, String user_id, String description, Bitmap image) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(_url + "?"
                    + StringConstant.userIdStr + "=" + user_id + "&"
                    + StringConstant.photoDescription + "=" +
                    URLEncoder.encode(description, "UTF-8")
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

    private static String getParamString(Map<String, String> params) {
        Object[] keys = params.keySet().toArray();
        if (keys.length == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            try {
                sb.append(keys[i].toString()).append("=").append(URLEncoder.encode(params.get(keys[i].toString()),
                        "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "getParamString" + e.getMessage());
            }
            if (i != keys.length - 1)
                sb.append("&");
        }
        Log.d(LOG_TAG, sb.toString());
        return sb.toString();
    }

    public static String postPathImageToNet(String _url, String path_id, Bitmap image) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            URL url = new URL(_url + "?" + StringConstant.pathId + "=" + path_id
            );
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
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