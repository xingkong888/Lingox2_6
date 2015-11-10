package cn.lingox.android.activity.imagechooser.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.Photo;

public class AddPhotosAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<Photo> photoList;

    private boolean isScroll = false;

    public AddPhotosAdapter(Activity context, ArrayList<Photo> pList) {
        this.context = context;
        this.photoList = pList;
    }

    @Override
    public int getCount() {
        return photoList.size();
    }

    @Override
    public Photo getItem(int position) {
        return photoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View gridView = convertView;
        final ViewHolder holder;
        final Photo photo = photoList.get(position);
        if (gridView == null) {
            gridView = LayoutInflater.from(context).inflate(R.layout.row_pic_item, null, false);
            holder = new ViewHolder();
            holder.photo = (ImageView) gridView.findViewById(R.id.item_grida_image);
            holder.description = (EditText) gridView.findViewById(R.id.et_description);
            holder.delete = (Button) gridView.findViewById(R.id.item_delete);
            gridView.setTag(holder);
        } else {
            holder = (ViewHolder) gridView.getTag();
        }

        if (holder.textWatcher != null)
            holder.description.removeTextChangedListener(holder.textWatcher);

        holder.textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                photo.setDescription(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        holder.description.addTextChangedListener(holder.textWatcher);
        holder.description.setText(photo.getDescription());
        if (!isScroll) {
            holder.photo.setImageBitmap(getImageThumbnail
                    (photo.getUrl(), 90, 90));
        }
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoList.remove(position);
                notifyDataSetChanged();
            }
        });
        return gridView;
    }

    /**
     * 标识listview是否在滚动
     *
     * @param srocll false 标识空闲， true 标识滚动
     */
    public void isScroll(boolean srocll) {
        this.isScroll = srocll;
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     * 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    private Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
    static class ViewHolder {
        ImageView photo;
        EditText description;
        Button delete;
        TextWatcher textWatcher;
    }
}
