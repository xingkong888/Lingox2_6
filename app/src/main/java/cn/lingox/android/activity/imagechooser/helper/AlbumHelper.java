package cn.lingox.android.activity.imagechooser.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cn.lingox.android.activity.imagechooser.entity.ImageBucket;
import cn.lingox.android.activity.imagechooser.entity.ImageItem;

/**
 * 相册读取
 */
public class AlbumHelper {
    private static AlbumHelper instance;
   private final String TAG = getClass().getSimpleName();
    private Context context;
    private HashMap<String, String> thumbnailList = new HashMap<>();
    private HashMap<String, ImageBucket> bucketList = new HashMap<>();
    private boolean hasBuildImagesBucketList = false;
    private ContentResolver cr;

    public static AlbumHelper getHelper() {
        if (instance == null) {
            instance = new AlbumHelper();
        }
        return instance;
    }

    public void init(Context context) {
        if (this.context == null) {
            this.context = context;
            cr = context.getContentResolver();
        }
    }

    /**
     * 获取
     */
    private void getThumbnail() {
        String[] projection = {Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA};
        Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
        getThumbnailColumnData(cursor);
    }

    /**
     * 获取相册下图片的信息
     * @param cur Cursor
     */
    private void getThumbnailColumnData(Cursor cur) {
        if (cur.moveToFirst()) {
//            int _id;
            int image_id;
            String image_path;
//            int _idColumn = cur.getColumnIndex(Thumbnails._ID);
            int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
            int dataColumn = cur.getColumnIndex(Thumbnails.DATA);
            do {
                // Get the field values
//                _id = cur.getInt(_idColumn);
                image_id = cur.getInt(image_idColumn);
                image_path = cur.getString(dataColumn);

                thumbnailList.put("" + image_id, image_path);
            } while (cur.moveToNext());
        }
    }

    void buildImagesBucketList() {
        long startTime = System.currentTimeMillis();

        getThumbnail();

        String columns[] = new String[]{Media._ID, Media.BUCKET_ID,
                Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE,
                Media.SIZE, Media.BUCKET_DISPLAY_NAME};
        Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
        if (cur != null && cur.moveToFirst()) {
            int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
            int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
            int photoNameIndex = cur.getColumnIndexOrThrow(Media.DISPLAY_NAME);
            int photoTitleIndex = cur.getColumnIndexOrThrow(Media.TITLE);
            int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
            int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
            int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
            int picasaIdIndex = cur.getColumnIndexOrThrow(Media.PICASA_ID);
//            int totalNum = cur.getCount();//查询出来的数据总数
            //清除原HashMap中的数据，防止数据重复
            bucketList.clear();
            do {
                String _id = cur.getString(photoIDIndex);
                String name = cur.getString(photoNameIndex);
                String path = cur.getString(photoPathIndex);
                String title = cur.getString(photoTitleIndex);
                String size = cur.getString(photoSizeIndex);
                String bucketName = cur.getString(bucketDisplayNameIndex);
                String bucketId = cur.getString(bucketIdIndex);
                String picasaId = cur.getString(picasaIdIndex);

                Log.i(TAG, _id + ", bucketId: " + bucketId + ", picasaId: "
                        + picasaId + " name:" + name + " path:" + path
                        + " title: " + title + " size: " + size + " bucket: "
                        + bucketName + "---");

                ImageBucket bucket = bucketList.get(bucketId);
                //如果有新的图片文件夹，添加到HashMap集合中去
                if (bucket == null) {
                    bucket = new ImageBucket();
                    bucketList.put(bucketId, bucket);
                    bucket.imageList = new ArrayList<>();
                    bucket.bucketName = bucketName;
                }
                {//将图片加入到对应的文件夹下----未判断是否存在，重复添加
                    bucket.count++;
                    ImageItem imageItem = new ImageItem();
                    imageItem.imageId = _id;
                    imageItem.imagePath = path;
                    imageItem.thumbnailPath = thumbnailList.get(_id);
                    bucket.imageList.add(imageItem);
                }
            } while (cur.moveToNext());
        }

        for (Entry<String, ImageBucket> entry : bucketList.entrySet()) {
            ImageBucket bucket = entry.getValue();
            Log.d(TAG, entry.getKey() + ", " + bucket.bucketName + ", " + bucket.count + " ---------- ");
            for (int i = 0; i < bucket.imageList.size(); ++i) {
                ImageItem image = bucket.imageList.get(i);
                Log.d(TAG, "----- " + image.imageId + ", " + image.imagePath + ", " + image.thumbnailPath);
            }
        }
        hasBuildImagesBucketList = true;
        if (cur != null) {
            cur.close();
        }
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "use time: " + (endTime - startTime) + " ms");
    }

    /**
     * 获取本地图片路径
     *
     * @param refresh 是否重新获取 true：重新获取 false：不重新获取
     * @return 图片路径集合
     */
    public List<ImageBucket> getImagesBucketList(boolean refresh) {
        if (refresh || !hasBuildImagesBucketList) {
            buildImagesBucketList();
        }
        List<ImageBucket> tmpList = new ArrayList<>();
        for (Entry<String, ImageBucket> entry : bucketList.entrySet()) {
            tmpList.add(entry.getValue());
        }
        return tmpList;
    }
}
