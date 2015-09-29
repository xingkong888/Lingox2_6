//package cn.lingox.android.activity;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler.Callback;
//import android.os.Message;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.HashMap;
//
//import cn.lingox.android.R;
//import cn.lingox.android.constants.StringConstant;
//import cn.lingox.android.entity.Path;
//import cn.sharesdk.facebook.Facebook;
//import cn.sharesdk.framework.Platform;
//import cn.sharesdk.framework.PlatformActionListener;
//import cn.sharesdk.framework.ShareSDK;
//import cn.sharesdk.framework.utils.UIHandler;
//import cn.sharesdk.instagram.Instagram;
//import cn.sharesdk.sina.weibo.SinaWeibo;
//import cn.sharesdk.twitter.Twitter;
//import cn.sharesdk.wechat.friends.Wechat;
//import cn.sharesdk.wechat.moments.WechatMoments;
//
//public class ShareActivity extends BaseActivity implements OnClickListener, PlatformActionListener, Callback {
//    private static final String LOG_TAG = "ShareActivity";
//    private static final String FILE_NAME = "/app_icon.jpg";
//    private static String appIconImagePath;
//    private ImageView avatar, wechatImg, momentsImg, weiboImg, facebookImg, twitterImg, instagramImg;
//    private TextView title, wechat, moments, weibo, facebook, twitter, instagram, share;
//    private EditText say;
//    private int shareNum = 0;
//    private Path path;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_share);
//
//        path = getIntent().getParcelableExtra("PathId");
//
//        new CreateAppIconFile().execute();
//
//        initView();
//    }
//
//    private void initView() {
//
//        ShareSDK.initSDK(this);
//
//        share = (TextView) findViewById(R.id.share);
//        instagram = (TextView) findViewById(R.id.share_instagram);
//        twitter = (TextView) findViewById(R.id.share_twitter);
//        facebook = (TextView) findViewById(R.id.share_facebook);
//        weibo = (TextView) findViewById(R.id.share_weibo);
//        moments = (TextView) findViewById(R.id.share_moments);
//        wechat = (TextView) findViewById(R.id.share_wechat);
//        say = (EditText) findViewById(R.id.share_say);
//        title = (TextView) findViewById(R.id.share_title);
//        avatar = (ImageView) findViewById(R.id.share_avatar);
//
//        instagramImg = (ImageView) findViewById(R.id.share_instagram_img);
//        twitterImg = (ImageView) findViewById(R.id.share_twitter_img);
//        facebookImg = (ImageView) findViewById(R.id.share_facebook_img);
//        weiboImg = (ImageView) findViewById(R.id.share_weibo_img);
//        momentsImg = (ImageView) findViewById(R.id.share_moments_img);
//        wechatImg = (ImageView) findViewById(R.id.share_wechat_img);
//
//        wechatImg.setOnClickListener(this);
//        momentsImg.setOnClickListener(this);
//        weiboImg.setOnClickListener(this);
//        facebookImg.setOnClickListener(this);
//        twitterImg.setOnClickListener(this);
//        instagramImg.setOnClickListener(this);
//        share.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.share_wechat:
//            case R.id.share_wechat_img:
//                shareNum = 1;
//                wechatImg.setImageDrawable(getResources().getDrawable(R.drawable.share_wechat_open_39dp));
//                momentsImg.setImageDrawable(getResources().getDrawable(R.drawable.share_moments_off_39dp));
//                weiboImg.setImageDrawable(getResources().getDrawable(R.drawable.share_weibo_off_39dp));
//                facebookImg.setImageDrawable(getResources().getDrawable(R.drawable.share_facebook_off_39dp));
//                twitterImg.setImageDrawable(getResources().getDrawable(R.drawable.share_twitter_off_39dp));
//                instagramImg.setImageDrawable(getResources().getDrawable(R.drawable.share_instagram_off_39dp));
//                break;
//            case R.id.share_moments:
//            case R.id.share_moments_img:
//                shareNum = 2;
//                wechatImg.setImageDrawable(getResources().getDrawable(R.drawable.share_wechat_off_39dp));
//                momentsImg.setImageDrawable(getResources().getDrawable(R.drawable.share_moments_open_39dp));
//                weiboImg.setImageDrawable(getResources().getDrawable(R.drawable.share_weibo_off_39dp));
//                facebookImg.setImageDrawable(getResources().getDrawable(R.drawable.share_facebook_off_39dp));
//                twitterImg.setImageDrawable(getResources().getDrawable(R.drawable.share_twitter_off_39dp));
//                instagramImg.setImageDrawable(getResources().getDrawable(R.drawable.share_instagram_off_39dp));
//                break;
//            case R.id.share_weibo_img:
//            case R.id.share_weibo:
//                shareNum = 3;
//                wechatImg.setImageDrawable(getResources().getDrawable(R.drawable.share_wechat_off_39dp));
//                momentsImg.setImageDrawable(getResources().getDrawable(R.drawable.share_moments_off_39dp));
//                weiboImg.setImageDrawable(getResources().getDrawable(R.drawable.share_weibo_open_39dp));
//                facebookImg.setImageDrawable(getResources().getDrawable(R.drawable.share_facebook_off_39dp));
//                twitterImg.setImageDrawable(getResources().getDrawable(R.drawable.share_twitter_off_39dp));
//                instagramImg.setImageDrawable(getResources().getDrawable(R.drawable.share_instagram_off_39dp));
//                break;
//            case R.id.share_facebook_img:
//            case R.id.share_facebook:
//                shareNum = 4;
//                wechatImg.setImageDrawable(getResources().getDrawable(R.drawable.share_wechat_off_39dp));
//                momentsImg.setImageDrawable(getResources().getDrawable(R.drawable.share_moments_off_39dp));
//                weiboImg.setImageDrawable(getResources().getDrawable(R.drawable.share_weibo_off_39dp));
//                facebookImg.setImageDrawable(getResources().getDrawable(R.drawable.share_facebook_open_39dp));
//                twitterImg.setImageDrawable(getResources().getDrawable(R.drawable.share_twitter_off_39dp));
//                instagramImg.setImageDrawable(getResources().getDrawable(R.drawable.share_instagram_off_39dp));
//                break;
//            case R.id.share_twitter_img:
//            case R.id.share_twitter:
//                shareNum = 5;
//                wechatImg.setImageDrawable(getResources().getDrawable(R.drawable.share_wechat_off_39dp));
//                momentsImg.setImageDrawable(getResources().getDrawable(R.drawable.share_moments_off_39dp));
//                weiboImg.setImageDrawable(getResources().getDrawable(R.drawable.share_weibo_off_39dp));
//                facebookImg.setImageDrawable(getResources().getDrawable(R.drawable.share_facebook_off_39dp));
//                twitterImg.setImageDrawable(getResources().getDrawable(R.drawable.share_twitter_open_39dp));
//                instagramImg.setImageDrawable(getResources().getDrawable(R.drawable.share_instagram_off_39dp));
//                break;
//            case R.id.share_instagram_img:
//            case R.id.share_instagram:
//                shareNum = 6;
//                wechatImg.setImageDrawable(getResources().getDrawable(R.drawable.share_wechat_off_39dp));
//                momentsImg.setImageDrawable(getResources().getDrawable(R.drawable.share_moments_off_39dp));
//                weiboImg.setImageDrawable(getResources().getDrawable(R.drawable.share_weibo_off_39dp));
//                facebookImg.setImageDrawable(getResources().getDrawable(R.drawable.share_facebook_off_39dp));
//                twitterImg.setImageDrawable(getResources().getDrawable(R.drawable.share_twitter_off_39dp));
//                instagramImg.setImageDrawable(getResources().getDrawable(R.drawable.share_instagram_open_39dp));
//                break;
//            case R.id.share:
//                showShare();
//                break;
//        }
//    }
//
//    //TODO 微信朋友圈
//    private void momentsShare() {
//        WechatMoments.ShareParams sp = new WechatMoments.ShareParams();
//        sp.setShareType(Platform.SHARE_IMAGE);
//        sp.setTitle(path.getTitle());
//        sp.setTitleUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId()); // 标题的超链接
//        sp.setText(path.getText());
//        sp.setUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//        sp.setImageUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//        sp.setImagePath(appIconImagePath);
//
//        Platform moments1 = ShareSDK.getPlatform(WechatMoments.NAME);
//        // 执行图文分享
//        moments1.share(sp);
//    }
//
//    //TODO 微信
//    private void wechatShare() {
//        Wechat.ShareParams sp = new Wechat.ShareParams();
//        sp.setShareType(Platform.SHARE_IMAGE);
//        sp.setTitle(path.getTitle());
//        sp.setTitleUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId()); // 标题的超链接
//        sp.setText(path.getText());
//        sp.setUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//        sp.setImageUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//        sp.setImagePath(appIconImagePath);
//        Platform wechat1 = ShareSDK.getPlatform(Wechat.NAME);
//        // 执行图文分享
//        wechat1.share(sp);
//    }
//
//    //TODO 新浪微博
//    private void weiboShare() {
//        SinaWeibo.ShareParams sp = new SinaWeibo.ShareParams();
//        sp.setTitle(path.getTitle());
//        sp.setTitleUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId()); // 标题的超链接
//        sp.setText(path.getText());
//        sp.setImageUrl(appIconImagePath);
//        sp.setUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//
//        Platform weibo1 = ShareSDK.getPlatform(SinaWeibo.NAME);
//        // 执行图文分享
//        weibo1.share(sp);
//    }
//
//    //TODO facebook
//    private void fbShare() {
//        Facebook.ShareParams sp = new Facebook.ShareParams();
////        sp.setTitle(path.getTitle());
////        sp.setTitleUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId()); // 标题的超链接
//        sp.setText(path.getText());
////        sp.setImageUrl(appIconImagePath);
//        // sp.setUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//
//        Platform fb = ShareSDK.getPlatform(Facebook.NAME);
//        // 执行图文分享
//        fb.share(sp);
//    }
//
//    //TODO twitter
//    private void twShare() {
//        Twitter.ShareParams sp = new Twitter.ShareParams();
//        sp.setTitle(path.getTitle());
////        sp.setTitleUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId()); // 标题的超链接
//        sp.setText(path.getText());
////        sp.setImageUrl(appIconImagePath);
//        // sp.setUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//
//        Platform tw = ShareSDK.getPlatform(Twitter.NAME);
//        // 执行图文分享
//        tw.share(sp);
//    }
//
//    //TODO instagram
//    private void inShare() {
//        Instagram.ShareParams sp = new Instagram.ShareParams();
////        sp.setTitle(path.getTitle());
////        sp.setTitleUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId()); // 标题的超链接
////        sp.setText(path.getText());
//        sp.setImageUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
////        sp.setUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
//
//        Platform in = ShareSDK.getPlatform(Instagram.NAME);
//        // 执行图文分享
//        in.share(sp);
//    }
//
//    // TODO 分享展示
//    private void showShare() {
//        switch (shareNum) {
//            case 1://微信
//                wechatShare();
//                break;
//            case 2://微信朋友圈
//                momentsShare();
//                break;
//            case 3://新浪微博
//                weiboShare();
//                break;
//            case 4://Facebook
//                fbShare();
//                break;
//            case 5://twitter
//                twShare();
//                break;
//            case 6://instagram
//                inShare();
//                break;
//        }
//    }
//
//    @Override
//    public void onCancel(Platform arg0, int arg1) {
//        new Thread() {
//            @Override
//            public void run() {
//                Message msg = new Message();
//                msg.what = 0;
//                UIHandler.sendMessage(msg, ShareActivity.this);
//            }
//        }.start();
//    }
//
//    @Override
//    public void onComplete(final Platform plat, final int action,
//                           HashMap<String, Object> res) {
//        new Thread() {
//            @Override
//            public void run() {
//                Message msg = new Message();
//                msg.arg1 = 1;
//                msg.arg2 = action;
//                msg.obj = plat;
//                UIHandler.sendMessage(msg, ShareActivity.this);
//            }
//        }.start();
//    }
//
//    @Override
//    public void onError(Platform arg0, int arg1, Throwable arg2) {
//        new Thread() {
//            @Override
//            public void run() {
//                Message msg = new Message();
//                msg.what = 1;
//                UIHandler.sendMessage(msg, ShareActivity.this);
//            }
//        }.start();
//    }
//
//    @Override
//    public boolean handleMessage(Message msg) {
//        int what = msg.what;
//        if (what == 1) {
//            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
//        }
//        return false;
//    }
//
//    private class CreateAppIconFile extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            if (Environment.MEDIA_MOUNTED.equals(Environment
//                    .getExternalStorageState())
//                    && Environment.getExternalStorageDirectory().exists()) {
//                appIconImagePath = Environment.getExternalStorageDirectory().
//                        getAbsolutePath() + FILE_NAME;
//            } else {
//                appIconImagePath = getApplication().getFilesDir().getAbsolutePath()
//                        + FILE_NAME;
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                File file = new File(appIconImagePath);
//                if (!file.exists()) {
//                    //TODO
//                    file.createNewFile();
//                    Bitmap pic = BitmapFactory.decodeResource(getResources(),
//                            R.drawable.app_share_icon);
//                    FileOutputStream fos = new FileOutputStream(file);
//                    pic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                    fos.flush();
//                    fos.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                appIconImagePath = null;
//                Log.e(LOG_TAG, e.toString());
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            Log.d(LOG_TAG, "Finished creating app icon file");
//        }
//
//    }
//}