package cn.lingox.android.activity.imagechooser.adapter;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.Photo;

public class AddPhotosAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<Photo> photoList;

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
            gridView = LayoutInflater.from(context).inflate(R.layout.row_pic_item, parent, false);
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

        Picasso.with(context).load("file://" + photo.getUrl()).into(holder.photo);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoList.remove(position);
                notifyDataSetChanged();
            }
        });

        return gridView;
    }

    static class ViewHolder {
        ImageView photo;
        EditText description;
        Button delete;
        TextWatcher textWatcher;
    }
}
