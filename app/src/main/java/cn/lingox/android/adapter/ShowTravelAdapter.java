package cn.lingox.android.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.helper.JsonHelper;

public class ShowTravelAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Travel> datas;
    private Handler handler;
    private boolean isSelf = false;

    public ShowTravelAdapter(Activity context, ArrayList<Travel> cList, Handler handler, boolean isSelf) {
        this.context = context;
        datas = cList;
        this.handler = handler;
        this.isSelf = isSelf;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_travel_show_item, null);
            viewHolder = new ViewHolder();

            viewHolder.editAndDel = (RelativeLayout) convertView.findViewById(R.id.travel_editanddel);
            if (isSelf) {
                viewHolder.editAndDel.setVisibility(View.VISIBLE);
            }

            viewHolder.delete = (ImageView) convertView.findViewById(R.id.show_travel_delete);
            viewHolder.edit = (ImageView) convertView.findViewById(R.id.show_travel_edit);

            viewHolder.endTime = (TextView) convertView.findViewById(R.id.show_end_time_info);
            viewHolder.startTime = (TextView) convertView.findViewById(R.id.show_start_time_info);
            viewHolder.location = (TextView) convertView.findViewById(R.id.show_travel_location);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Travel travel = datas.get(position);
        viewHolder.location.setText(travel.getLocation());
        viewHolder.startTime.setText(JsonHelper.getInstance().parseTimestamp(travel.getStartTime(), 2));
        viewHolder.endTime.setText(JsonHelper.getInstance().parseTimestamp(travel.getEndTime(), 2));

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Sure to delete?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Message msg = new Message();
                                msg.arg1 = position;
                                msg.what = 0;
                                handler.sendMessage(msg);
                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            }
        });

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = position;
                handler.sendMessage(msg);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView location, startTime, endTime;
        ImageView delete, edit;
        RelativeLayout editAndDel;
    }
}
