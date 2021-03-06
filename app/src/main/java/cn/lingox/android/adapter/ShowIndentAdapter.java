package cn.lingox.android.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.activity.LocalViewActivity;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Indent;
import cn.lingox.android.helper.ServerHelper;

/**
 * 展示申请单的适配器
 */
public class ShowIndentAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Indent> datas;
    private String title;

    public ShowIndentAdapter(Context context, ArrayList<Indent> datas) {
        this.context = context;
        this.datas = datas;
        title = "";
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_show_indent, null);
            viewHolder = new ViewHolder();
            convertView.setVisibility(View.VISIBLE);
            viewHolder.declineAndAccept = (LinearLayout) convertView.findViewById(R.id.layout_decline_accept);
            viewHolder.accept = (TextView) convertView.findViewById(R.id.accept);
            viewHolder.decline = (TextView) convertView.findViewById(R.id.decline);
            viewHolder.cancel = (TextView) convertView.findViewById(R.id.self_cancel);
            viewHolder.pathTitle = (TextView) convertView.findViewById(R.id.show_one_path_title);
            viewHolder.timeAndNum = (TextView) convertView.findViewById(R.id.show_one_start_time);
            viewHolder.state = (TextView) convertView.findViewById(R.id.show_one_state);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Indent indent = datas.get(position);
        LingoXApplication.getInstance().setIndent(viewHolder.cancel, viewHolder.declineAndAccept,
                viewHolder.state, viewHolder.timeAndNum, viewHolder.pathTitle, indent);
        setClick(viewHolder.cancel, viewHolder.accept, viewHolder.decline, viewHolder.pathTitle, indent);
        return convertView;
    }

    /**
     * 设置点击事件
     *
     * @param view1  取消
     * @param view2  同意
     * @param view3  拒绝
     * @param view4  活动标题
     * @param indent 申请实例
     */
    private void setClick(final TextView view1, final TextView view2,
                          final TextView view3, TextView view4, final Indent indent) {
        title = indent.getPathTitle();
        //取消
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edit = new EditText(context);
                new AlertDialog.Builder(context)
                        .setMessage("Do you really want to cancel your request?")
                        .setView(edit)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setIndent(indent, "2", edit.getText().toString());
                            }
                        })
                        .setNegativeButton("NO", null)
                        .create().show();
            }
        });
        //同意
        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIndent(indent, "3", "");
            }
        });
        //拒绝
        view3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIndent(indent, "4", "");
            }
        });
        //标题
        view4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LocalViewActivity.class);
                intent.putExtra(LocalViewActivity.PATH_TO_VIEW_ID, indent.getPathId());
                context.startActivity(intent);
            }
        });
    }

    private void setIndent(Indent indent, String state, String reason) {
        HashMap<String, String> map = new HashMap<>();
        map.clear();
        map.put("id", indent.getId());
        map.put("state", state);
        if (!"".equals(reason)) {
            map.put("reason", reason);
        }
        new EditIndent(indent, map).execute();
    }

    static class ViewHolder {
        TextView state, timeAndNum, pathTitle, cancel, decline, accept;
        LinearLayout layout, declineAndAccept;
    }

    private class EditIndent extends AsyncTask<Void, Void, Boolean> {
        private Indent indent;
        private ProgressDialog pd;
        private HashMap<String, String> map;

        public EditIndent(Indent indent, HashMap<String, String> map) {
            this.indent = indent;
            this.map = map;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setMessage("Is submitted");
            pd.show();
            datas.remove(indent);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                indent = ServerHelper.getInstance().editApplication(map);
                indent.setPathTitle(title);
                datas.add(indent);
                Collections.reverse(datas);
                return true;
            } catch (Exception e) {
                Log.e("ShowIndentAdapter", e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
            }
            pd.dismiss();
        }
    }
}
