package cn.lingox.android.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.WritePathReplayDialog;
import cn.lingox.android.task.GetUser;

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
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final GroupViewHolder groupViewHolder;
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

        User user = CacheHelper.getInstance().getUserInfo(map.get("user_id"));
        if (user == null) {
            new GetUser(groups.get(groupPosition).get("user_id"), new GetUser.Callback() {
                @Override
                public void onSuccess(User user) {
                    CacheHelper.getInstance().addUserInfo(user);
                    Picasso.with(context)
                            .load(user.getAvatar())
                            .error(R.drawable.nearby_nopic_294dp)
                            .into(groupViewHolder.avatar);
                }

                @Override
                public void onFail() {

                }
            }).execute();
        } else {
            Picasso.with(context)
                    .load(CacheHelper.getInstance().getUserInfo(map.get("user_id")).getAvatar())
                    .error(R.drawable.nearby_nopic_294dp)
                    .into(groupViewHolder.avatar);
        }

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
                                new DeletePathReference(map.get("referenceId"), groupPosition).execute();
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
        final ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_path_reply, null);
            childViewHolder = new ChildViewHolder();
            childViewHolder.content = (TextView) convertView.findViewById(R.id.show_reply_content);
            childViewHolder.userName = (TextView) convertView.findViewById(R.id.path_reference_name);
            childViewHolder.replyName = (TextView) convertView.findViewById(R.id.path_reference_reply_name);

            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        HashMap<String, String> map = (HashMap<String, String>) getChild(groupPosition, childPosition);
//评论发起者
        User user = CacheHelper.getInstance().getUserInfo(groups.get(groupPosition).get("user_id"));
        if (user == null) {
            new GetUser(groups.get(groupPosition).get("user_id"), new GetUser.Callback() {
                @Override
                public void onSuccess(User user) {
                    CacheHelper.getInstance().addUserInfo(user);
                    childViewHolder.userName.setText(user.getNickname());
                }

                @Override
                public void onFail() {

                }
            }).execute();
        } else {
            childViewHolder.userName.setText(user.getNickname());
        }
        //回复者
        final User replyUser = CacheHelper.getInstance().getUserInfo(map.get("user_id"));
        if (replyUser == null) {
            new GetUser(map.get("user_id"), new GetUser.Callback() {
                @Override
                public void onSuccess(User user) {
                    CacheHelper.getInstance().addUserInfo(user);
                    childViewHolder.replyName.setText(user.getNickname());
                }

                @Override
                public void onFail() {

                }
            }).execute();
        } else {
            childViewHolder.replyName.setText(user.getNickname());
        }

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
        ImageView avatar, replay, delete;
        TextView name, time;
        TextView content;
        RelativeLayout layout;
    }

    static class ChildViewHolder {
        TextView content, userName, replyName;
    }

    private class DeletePathReference extends AsyncTask<Void, Void, Boolean> {
        private HashMap<String, String> map;
        private String referenceId;
        private int position;

        public DeletePathReference(String referenceId, int position) {
            this.referenceId = referenceId;
            this.position = position;
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
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                //成功
                groups.remove(position);
                childs.remove(position);
                notifyDataSetChanged();
            }
        }
    }
}
