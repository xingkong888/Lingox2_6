package cn.lingox.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @version : 1.0
 */
public class FileUtil {

    public static final String CACHE_DIR = Environment.getExternalStorageDirectory()
            + "/myimages/";
    public static String SDPATH = Environment.getExternalStorageDirectory()
            + "/formats/";

    public static boolean isMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    public static void saveImg(String url, Bitmap bitmap, Context context) {
        if (!isMounted()) {
            throw new RuntimeException("内存卡不存在，请插入内存卡");
        } else {
            try {
                File dir = new File(CACHE_DIR);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File imgFile = new File(dir, getImgName(url));
                if (imgFile.exists()) {
                    imgFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(imgFile);
                bitmap.compress(CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteDir() {
        File dir = new File(CACHE_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDir(); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }

    public static Bitmap getImg(String url, Context context) {
        if (!isMounted()) {
            throw new RuntimeException("内存卡不存在，请插入内存卡");
        } else {
            Bitmap bitmap = null;
            File imgFile = new File(CACHE_DIR, getImgName(url));
            if (imgFile.exists()) {
                bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
            return bitmap;
        }
    }

    public static void getFileSize() throws Exception {
        if (!isMounted()) {
            throw new RuntimeException("内存卡不存在，请插入内存卡");
        }
        long size = 0;
        File f = new File(CACHE_DIR);
        File flist[] = f.listFiles();
        for (File file : flist) {
            size = size + file.length();
        }
        if (size > 1024 * 1024 * 1024) {//1M
            deleteDir();
        }
    }

    public static String getImgName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
