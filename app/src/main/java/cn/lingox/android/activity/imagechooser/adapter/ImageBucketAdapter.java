package cn.lingox.android.activity.imagechooser.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.entity.ImageBucket;
import cn.lingox.android.activity.imagechooser.helper.BitmapCache;
import cn.lingox.android.activity.imagechooser.helper.BitmapCache.ImageCallback;

public class ImageBucketAdapter extends BaseAdapter {
    private static final String LOG_TAG = "ImageBucketAdapter";

    private ImageCallback callback = new ImageCallback() {
        @Override
        public void imageLoad(ImageView imageView, Bitmap bitmap,
                              Object... params) {
            if (imageView != null && bitmap != null) {
                String url = (String) params[0];
                if (url != null && url.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.e(LOG_TAG, "callback, bmp not match");
                }
            } else {
                Log.e(LOG_TAG, "callback, bmp null");
            }
        }
    };
    private Activity act;
    private List<ImageBucket> dataList;
    private BitmapCache cache;

    public ImageBucketAdapter(Activity act, List<ImageBucket> list) {
        this.act = act;
        dataList = list;
        cache = new BitmapCache();
    }

    @Override
    public int getCount() {
        int count = 0;
        if (dataList != null) {
            count = dataList.size();
        }
        return count;
    }

    @Override
    public Object getItem(int pos) {
        if (dataList != null) {
            return dataList.get(pos);
        }
        return null;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        Holder holder;
        if (arg1 == null) {
            holder = new Holder();
            arg1 = View.inflate(act, R.layout.item_image_bucket, null);
            holder.iv = (ImageView) arg1.findViewById(R.id.image);
            holder.selected = (ImageView) arg1.findViewById(R.id.isselected);
            holder.name = (TextView) arg1.findViewById(R.id.name);
            holder.count = (TextView) arg1.findViewById(R.id.count);
            arg1.setTag(holder);
        } else {
            holder = (Holder) arg1.getTag();
        }
        ImageBucket item = dataList.get(arg0);
        holder.count.setText("" + item.count);
        holder.name.setText(item.bucketName);
        holder.selected.setVisibility(View.GONE);
        if (item.imageList != null && item.imageList.size() > 0) {
            String thumbPath = item.imageList.get(0).thumbnailPath;
            String sourcePath = item.imageList.get(0).imagePath;
            holder.iv.setTag(sourcePath);
            cache.displayBmp(holder.iv, thumbPath, sourcePath, callback);
        } else {
            holder.iv.setImageBitmap(null);
            Log.e(LOG_TAG, "no images in bucket " + item.bucketName);
        }
        return arg1;
    }

    private class Holder {
        private ImageView iv;
        private ImageView selected;
        private TextView name;
        private TextView count;
    }

}
