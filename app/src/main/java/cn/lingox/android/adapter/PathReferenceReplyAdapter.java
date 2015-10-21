package cn.lingox.android.adapter;

import android.app.Activity;
import android.os.Handler;
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
import cn.lingox.android.helper.WritePathReplayDialog;

public class PathReferenceReplyAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private ArrayList<HashMap<String, String>> groups;
    private ArrayList<ArrayList<HashMap<String, String>>> childs;
    private Handler handler;

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

            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        final HashMap<String, String> map = groups.get(groupPosition);
//        Log.d("星期", map.toString());
        Picasso.with(context)
                .load(CacheHelper.getInstance().getSelfInfo().getAvatar())
                .error(R.drawable.nearby_nopic_294dp)
                .into(groupViewHolder.avatar);
        groupViewHolder.content.setText(map.get("content"));
        groupViewHolder.name.setText(
                CacheHelper.getInstance().getUserInfo(map.get("user_id")).getNickname());
        groupViewHolder.replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WritePathReplayDialog.newInstance(handler, map.get("referenceId"),
                        CacheHelper.getInstance().getSelfInfo().getId(), context).
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

        childViewHolder.content.setText(map.get("content"));

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
        ImageView avatar, replay;
        TextView name, time;
        TextView content;
        RelativeLayout layout;
    }

    static class ChildViewHolder {
        TextView content;
    }
    //TODO 加一个获取user信息的异步任务

}
