//package cn.lingox.android.utils;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Paint;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
//import cn.lingox.android.R;
//import cn.lingox.android.app.LingoXApplication;
//import cn.lingox.android.entity.Comment;
//import cn.lingox.android.helper.CacheHelper;
//import cn.lingox.android.helper.JsonHelper;
//
///**
// * 创建评论控件
// */
//public class CreateCommentView {
//    private void removeComment(int position) {
//        path.removeComment(commentsList.get(position));
//        commentsList.remove(position);
//        if (commentsList.size() <= 0) {
//            commitLayout.setVisibility(View.GONE);
//        }
//        pathCommentsNum.setText(String.valueOf(commentsList.size()));
//        commentsListView.removeViewAt(position);
//    }
//
//    private void addComment(Comment comment) {
//        path.addComment(comment);
//        commentsList.add(comment);
//        pathCommentsNum.setText(String.valueOf(commentsList.size()));
//        commentsListView.addView(getCommentView(commentsList.size() - 1));
//        commentEditText.clearFocus();
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
//    }
//
//    private void loadComments() {
//        commentsListView.removeAllViews();
//        for (int i = 0, j = commentsList.size(); i < j; i++) {
//            commentsListView.addView(getCommentView(i));
//        }
//    }
//
//    private View getCommentView(final int position) {
//        View rowView = getLayoutInflater().inflate(R.layout.row_path_comment, null);
//        final Comment comment = commentsList.get(position);
//
//        ImageView userAvatar = (ImageView) rowView.findViewById(R.id.comment_user_avatar);
//        if (!LingoXApplication.getInstance().getSkip()) {
//            if (CacheHelper.getInstance().getSelfInfo().getId().contentEquals(comment.getUserId())) {
//                ImageView delete = (ImageView) rowView.findViewById(R.id.path_del);
//                delete.setVisibility(View.VISIBLE);
//            } else {
//                ImageView replay = (ImageView) rowView.findViewById(R.id.path_replay);
//                replay.setVisibility(View.VISIBLE);
//            }
//        }
//        TextView userNickname = (TextView) rowView.findViewById(R.id.comment_user_nickname);
//        TextView commentText = (TextView) rowView.findViewById(R.id.comment_text);
//        TextView commentDateTime = (TextView) rowView.findViewById(R.id.comment_date_time);
//        TextView replyTarName = (TextView) rowView.findViewById(R.id.reply_tar_name);
//
//        uiHelper.textViewSetPossiblyNullString(commentText, comment.getText());
//        uiHelper.textViewSetPossiblyNullString(commentDateTime,
//                JsonHelper.getInstance().parseSailsJSDate(comment.getCreatedAt()));
//        new LoadCommentUser(userNickname, userAvatar, comment.getUserId()).execute();
//        if (!comment.getUser_tar().isEmpty()) {
//            new LoadReplyUser(comment.getUser_tar(), replyTarName).execute();
//        }
//        if (!LingoXApplication.getInstance().getSkip()) {
//            rowView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (comment.getUserId().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
//                        CommentDialog commentDialog = new CommentDialog(comment);
//                        commentDialog.setCanceledOnTouchOutside(true);
//                        commentDialog.show();
//                    } else {
//                        replyOthers(comment);
//                    }
//                }
//            });
//        }
//        return rowView;
//    }
//
//    private void replyOthers(final Comment comment) {
//        replyUser = CacheHelper.getInstance().getUserInfo(comment.getUserId());
//        commentEditText.setHint((getString(R.string.reply_comment)) + " " + replyUser.getNickname() + ":");
//        replyEveryOne = false;
//    }
//
//}