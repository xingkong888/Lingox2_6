package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class SearchPathTask extends AsyncTask<Void, String, Boolean> {
    private String country = "", province = "", city = "";
    private Callback callback;
    private ArrayList<Path> tempPathList = new ArrayList<>();

    public SearchPathTask(String country, String province, String city, Callback callback) {
        this.country = country;
        this.province = province;
        this.city = city;
        this.callback = callback;
    }

    // todo 建议加一个数据加载时的动画效果
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            tempPathList.addAll(ServerHelper.getInstance().getPathsByLocation(
                    country, province, city, 0, 0, null));

            if (!LingoXApplication.getInstance().getSkip()) {
                for (Path path : tempPathList) {
                    User tempUser = CacheHelper.getInstance().getUserInfo(path.getUserId());
                    if (tempUser == null)
                        CacheHelper.getInstance().addUserInfo(ServerHelper.getInstance().getUserInfo(path.getUserId()));
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Search", "getData() error:" + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            callback.onSuccess(tempPathList);
        } else {
            callback.onFail();
        }
    }

    /**
     * 回调接口
     */
    public interface Callback {
        //成功
        void onSuccess(ArrayList<Path> list);

        //失败
        void onFail();
    }

    /*
   原来的
    private String country = "", province = "", city = "";
    private int loaclOrTravel = 0;
    private HashMap<Integer, Integer> map;
    private ArrayList<String> postJson;

    //目前只支持local
    public SearchPathTask(String country, String province, String city, int i, int page, HashMap<Integer, Integer> map) {
        this.country = country;
        this.city = city;
        this.province = province;
//            loaclOrTravel = i;
        loaclOrTravel = 1;
        postJson = new ArrayList<>();
        this.map = new HashMap<>();
        this.map.putAll(map);
        if (page == 1) {
            progressBar.setVisibility(View.VISIBLE);
        }
        done.setClickable(false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            ArrayList<Path> tempPathList = new ArrayList<>();
            if (map.size() > 0) {
                Set key = activityTags.keySet();
                Object[] post = key.toArray();
                for (Object aPost : post) {
                    postJson.add(String.valueOf((int) aPost));
                }
            }
            tempPathList.addAll(ServerHelper.getInstance().getPathsByLocation(country, province, city, loaclOrTravel, page, postJson));

            if (!LingoXApplication.getInstance().getSkip()) {
                for (Path path : tempPathList) {
                    User tempUser = CacheHelper.getInstance().getUserInfo(path.getUserId());
                    if (tempUser == null)
                        CacheHelper.getInstance().addUserInfo(ServerHelper.getInstance().getUserInfo(path.getUserId()));
                }
            }
            pathList.addAll(tempPathList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Search", "getData() error:" + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        done.setClickable(true);
        if (success) {
            done.setText(getString(R.string.search));
            if (pathList.size() == 0) {
                //开始动画
                startAnim();
            }
        } else {
            discover.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.GONE);
        disAdapter.notifyDataSetChanged();
        listView.onRefreshComplete();
    }
*/
}