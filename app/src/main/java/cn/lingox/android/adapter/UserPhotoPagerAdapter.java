package cn.lingox.android.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.lingox.android.entity.Photo;

public class UserPhotoPagerAdapter extends PagerAdapter {

    private ArrayList<Photo> photoList;
    private Activity context;

    public UserPhotoPagerAdapter(Activity context, ArrayList<Photo> pList) {
        this.photoList = pList;
        this.context = context;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView img = new ImageView(context);
        img.setBackgroundColor(0xff000000);
        Picasso.with(context).load(photoList.get(position).getUrl()).into(img);
        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        container.addView(img);
        return img;
    }

    @Override
    public int getCount() {
        return photoList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
