package cn.lingox.android.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.WritePathReplayDialog;

public class PathReferenceReplyAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private ArrayList<HashMap<String, String>> groups;
    private ArrayList<ArrayList<HashMap<String, String>>> childs;
    private Handler handler;
    private String userName = "", replyName = "";

    public PathReferenceReplyAdapter(Activity context, ArrayList<HashMap<String, String>> groups
            , ArrayList<ArrayList<HashMap<String, String>>> childs, Handler handler) {
        this.context = context;
        this.groups = groups;
        this.childs = childs;
        this.handler = handler;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_path_reference, null);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar_path_reference);
            groupViewHolder.content = (TextView) convertView.findViewById(R.id.content_path_reference);
            groupViewHolder.name = (TextView) convertView.findViewById(R.id.name_path_reference);
            groupViewHolder.time = (TextView) convertView.findViewById(R.id.time_path_reference);
            groupViewHolder.replay = (ImageView) convertView.findViewById(R.id.path_refrence_replay);
            groupViewHolder.delete = (ImageView) convertView.findViewById(R.id.path_refrence_delete);

            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        final HashMap<String, String> map = groups.get(groupPosition);

        userName = map.get("user_name");

//        Log.d("星期", map.toString());
        Picasso.with(context)
                .load(CacheHelper.getInstance().getUserInfo(map.get("user_id")).getAvatar())
                .error(R.drawable.nearby_nopic_294dp)
                .into(groupViewHolder.avatar);
        groupViewHolder.content.setText(map.get("content"));
        groupViewHolder.name.setText(
                CacheHelper.getInstance().getUserInfo(map.get("user_id")).getNickname());

        //判断是否显示回复图标
        //若当前用户是评论发起者，显示删除图标
        //若当前用户不是评论发起则，显示回复图标
        if (map.get("user_id").contentEquals(CacheHelper.getInstance().getSelfInfo().getId())) {
            groupViewHolder.replay.setVisibility(View.GONE);
            groupViewHolder.delete.setVisibility(View.VISIBLE);
        } else {
            groupViewHolder.replay.setVisibility(View.VISIBLE);
            groupViewHolder.delete.setVisibility(View.GONE);
        }

        groupViewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setMessage("是否删除？")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new DeletePathReference(map.get("referenceId")).execute();
                            }
                        })
                        .create().show();
            }
        });

        groupViewHolder.replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WritePathReplayDialog.newInstance(handler, map.get("referenceId"),
                        CacheHelper.getInstance().getSelfInfo().getId(),
                        CacheHelper.getInstance().getSelfInfo().getNickname(), context).
                        show(context.getFragmentManager(), "");
            }
        });

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_path_reply, null);
            childViewHolder = new ChildViewHolder();
            childViewHolder.content = (TextView) convertView.findViewById(R.id.show_reply_content);

            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        HashMap<String, String> map = (HashMap<String, String>) getChild(groupPosition, childPosition);

//        //回复者
        replyName = map.get("user_name");

        if (!userName.isEmpty() && !replyName.isEmpty()) {
            childViewHolder.content.setText(
                    Html.fromHtml("<font color=\"#00838f\">" + replyName + "</font>" + " reply "
                            + "<font color=\"#00838f\">" + userName + "</font>"
                            + " : " +
                            map.get("content")));
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childs.get(groupPosition).size();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childs.get(groupPosition).get(childPosition);
    }

    static class GroupViewHolder {
        ImageView avatar, replay, delete;
        TextView name, time;
        TextView content;
        RelativeLayout layout;
    }

    static class ChildViewHolder {
        TextView content;
    }

    private class DeletePathReference extends AsyncTask<Void, Void, Boolean> {
        private HashMap<String, String> map;
        private String referenceId;

        public DeletePathReference(String referenceId) {
            this.referenceId = referenceId;
        }

        @Override
        protected void onPreExecute() {
            map = new HashMap<>();
            map.put("referenceId", referenceId);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                ServerHelper.getInstance().deletePathReference(map);
                return true;
            } catch (Exception e) {
                Log.d("星期", "创建活动评论错误：" + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                //成功
            }
        }
    }
}
