package cn.lingox.android.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.Photo;

public class UserPhotosAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<Photo> photoList;

    public UserPhotosAdapter(Activity context, ArrayList<Photo> pList) {
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
        ViewHolder holder;

        Photo photo = photoList.get(position);

        if (gridView == null) {
            gridView = LayoutInflater.from(context).inflate(
                    R.layout.item_published_grida, null);
            holder = new ViewHolder();
            holder.photo = (ImageView) gridView.findViewById(R.id.item_grida_image);
            holder.photo.setFocusableInTouchMode(true);
            holder.photo.setFocusable(false);
            gridView.setTag(holder);
        } else {
            holder = (ViewHolder) gridView.getTag();
        }
        Picasso.with(context).load(photo.getUrl()).into(holder.photo);
        return gridView;
    }

    static class ViewHolder {
        ImageView photo;
    }
}
