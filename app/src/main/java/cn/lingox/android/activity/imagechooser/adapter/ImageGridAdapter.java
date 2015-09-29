package cn.lingox.android.activity.imagechooser.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.entity.ImageItem;
import cn.lingox.android.activity.imagechooser.helper.BitmapCache;

public class ImageGridAdapter extends BaseAdapter {
    private static final String LOG_TAG = "ImageGridAdapter";
    public Map<String, String> map = new HashMap<>();
    private BitmapCache.ImageCallback callback = new BitmapCache.ImageCallback() {
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
    private List<ImageItem> dataList;
    private BitmapCache cache;
    private int selectTotal = 0;
    private boolean selectMultiple;
    private SelectionListener selectionListener;

    public ImageGridAdapter(Activity act, List<ImageItem> list, boolean selectMultiple, SelectionListener selectionListener) {
        this.act = act;
        dataList = list;
        cache = new BitmapCache();
        this.selectMultiple = selectMultiple;
        this.selectionListener = selectionListener;
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
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = View.inflate(act, R.layout.item_image_grid, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.image);
            holder.selected = (ImageView) convertView
                    .findViewById(R.id.isselected);
            holder.text = (TextView) convertView
                    .findViewById(R.id.item_image_grid_text);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        final ImageItem item = dataList.get(position);
        holder.iv.setTag(item.imagePath);
        cache.displayBmp(holder.iv, item.thumbnailPath, item.imagePath,
                callback);
        holder.selected.setImageResource(R.drawable.icon_data_select);
        if (item.isSelected) {
            holder.selected.setVisibility(View.VISIBLE);
            holder.text.setBackgroundResource(R.drawable.bgd_relatly_line);
        } else {
            holder.selected.setVisibility(View.INVISIBLE);
            holder.text.setBackgroundColor(0x00000000);
        }

        return convertView;
    }

    public interface SelectionListener {
        void onSingleItemSelected(String path);

        void onMultiItemSelected();
    }

    static class Holder {
        ImageView iv, selected;
        TextView text;
    }
}
