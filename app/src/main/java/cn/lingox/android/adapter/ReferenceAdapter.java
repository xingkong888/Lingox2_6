package cn.lingox.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.activity.ReferenceDialog;
import cn.lingox.android.activity.UserInfoActivity;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.helper.WriteReplayDialog;

public class ReferenceAdapter extends ArrayAdapter<Reference> {
    // Intent Extras
    public static final String INTENT_TARGET_USER_ID = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_ID";
    public static final String INTENT_TARGET_USER_NAME = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_NAME";
    public static final String INTENT_REFERENCE = LingoXApplication.PACKAGE_NAME + ".REFERENCE";
    public static final String INTENT_REQUEST_CODE = LingoXApplication.PACKAGE_NAME + ".REQUEST_CODE";
    static final int EDIT_REFERENCE = 2;
    // Data Elements
    private Activity context;
    private ArrayList<Reference> referenceList;
    private boolean ownReference = false;

    private boolean isSelf = false;//false 查看别人的评论  true 查看自己的评论

    private Handler handler;

    public ReferenceAdapter(Activity context, ArrayList<Reference> rList, String userId, Handler handler) {
        super(context, R.layout.row_reference, rList);
        this.context = context;
        this.referenceList = rList;
        this.handler = handler;
        isSelf = CacheHelper.getInstance().getSelfInfo().getId().contentEquals(userId);
    }

    @Override
    public int getCount() {
        return referenceList.size();
    }

    @Override
    public Reference getItem(int position) {
        return referenceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewHolder holder;

        final Reference reference = referenceList.get(position);

//        Log.d("星期", reference.toString());

        if (rowView == null) {
            rowView = LayoutInflater.from(context).inflate(
                    R.layout.row_reference, parent, false);
            holder = new ViewHolder();
            holder.layout = (RelativeLayout) rowView.findViewById(R.id.pdpdpd);
            holder.avatar = (ImageView) rowView.findViewById(R.id.avatar_reference);
            holder.replay = (ImageView) rowView.findViewById(R.id.refrence_replay);
            holder.name = (TextView) rowView.findViewById(R.id.name_reference);
            holder.replyName = (TextView) rowView.findViewById(R.id.name);
            holder.time = (TextView) rowView.findViewById(R.id.time_reference);
            holder.content = (TextView) rowView.findViewById(R.id.content_reference);
            holder.replayContent = (TextView) rowView.findViewById(R.id.refrence_replay_content);
            holder.editText = new EditText(context);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        final User user = CacheHelper.getInstance().getUserInfo(reference.getUserSrcId());
        if (user != null) {
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, holder.avatar,
                    user.getAvatar());
            holder.name.setText(user.getNickname());
            UIHelper.getInstance().textViewSetPossiblyNullString(holder.time, JsonHelper.getInstance().parseSailsJSDate(reference.getUpdatedAt(), 0));
        }
        holder.content.setText(TextUtils.isEmpty(reference.getContent()) ? reference.getTitle()
                : reference.getContent());


        holder.replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteReplayDialog.newInstance(handler, reference, context).show(context.getFragmentManager(), "");
            }
        });

        if (reference.getReply() != null) {
            holder.replyName.setText(
                    Html.fromHtml("<font color=\"#00838f\">"
                            + CacheHelper.getInstance().getSelfInfo().getNickname() + "</font>"
                            + " reply " +
                            "<font color=\"#00838f\">" + user.getNickname() + "</font>"));
            holder.replayContent.setVisibility(View.VISIBLE);
            holder.replayContent.setText(reference.getReply());
        } else {
            holder.replayContent.setVisibility(View.GONE);
            if (isSelf) {
                holder.replay.setVisibility(View.VISIBLE);
            } else {
                holder.replay.setVisibility(View.GONE);
            }
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ownReference =
                        CacheHelper.getInstance().getSelfInfo().getId().equals(reference.getUserSrcId());
                if (ownReference) {//自己对别人的评价
                    Intent intent = new Intent(context, ReferenceDialog.class);
                    intent.putExtra(INTENT_REFERENCE, reference);
                    intent.putExtra(INTENT_TARGET_USER_ID, reference.getUserSrcId());
                    intent.putExtra(INTENT_TARGET_USER_NAME, user.getNickname());
                    intent.putExtra(INTENT_REQUEST_CODE, EDIT_REFERENCE);
                    context.startActivityForResult(intent, EDIT_REFERENCE);
                } else {//别人对自己评价
                    Intent userInfoIntent = new Intent(context, UserInfoActivity.class);
                    userInfoIntent.putExtra(UserInfoActivity.INTENT_USER_ID, reference.getUserSrcId());
                    context.startActivity(userInfoIntent);
                }
            }
        });
        return rowView;
    }

    static class ViewHolder {
        ImageView avatar, replay;
        TextView name, time, replyName;
        TextView content, replayContent;
        RelativeLayout layout;
        EditText editText;
    }
}