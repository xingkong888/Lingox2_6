package cn.lingox.android.activity;

import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.lingox.android.R;
import cn.lingox.android.adapter.NearbyAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;

public class NearByFragment extends Fragment {
    public static final int VIEW_USER = 102;
    private static final String LOG_TAG = "NearByFragment";
    // Constants
    private static final int SEARCH_TYPE_LOCATION = 1;
    // Data elements
    private ArrayList<User> searchList = new ArrayList<>();
    private Map<String, String> params = new HashMap<>();
    //是否为正常登录 true 跳过 false正常登录
    private boolean isSkip = LingoXApplication.getInstance().getSkip();

    private PullToRefreshListView listView;
    private NearbyAdapter adapter;
    private int page = 0;
    private ImageView img;
    private AnimationDrawable animationDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        initView(view);
        new SearchUser().execute();
        return view;
    }

    private void initView(View view) {
        img = (ImageView) view.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) img.getBackground();

        listView = (PullToRefreshListView) view.findViewById(R.id.nearby_listview);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        adapter = new NearbyAdapter(getActivity(), searchList);
        listView.setAdapter(adapter);
        listView.setRefreshing(true);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                new SearchUser().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载更多
                page++;
                defaultSearch();
            }
        });
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            img.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            img.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }

    private void defaultSearch() {
        params.clear();
        if (!isSkip) {
            params.put(StringConstant.locStr, JsonHelper.getInstance().getLocationStr(CacheHelper.getInstance()
                    .getSelfInfo().getLoc()));
            new Search(params, SEARCH_TYPE_LOCATION).execute();
        } else {
            params.put(StringConstant.locStr, "[116.30485,40.101113]");
            new Search(params, SEARCH_TYPE_LOCATION).execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("NearByFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("NearByFragment");
    }

    private class Search extends AsyncTask<Void, String, Boolean> {
        public Map<String, String> localParams;
        private int searchType;

        public Search(Map<String, String> p, int searchType) {
            this.localParams = p;
            this.searchType = searchType;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (!isSkip) {
                    searchList.addAll(ServerHelper.getInstance().searchUser(
                            CacheHelper.getInstance().getSelfInfo().getId(),
                            searchType, localParams, page));
                } else {
                    searchList.addAll(ServerHelper.getInstance().searchUser(
                            "54fd1c335c7b29ad6b18e07f",
                            searchType, localParams, page));
                }
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "search()" + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (searchList.size() <= 0) {
                    startAnim();
                } else {
                    stopAnim();
                }
                adapter.notifyDataSetChanged();
            }
            listView.onRefreshComplete();
        }
    }

    private class SearchUser extends AsyncTask<Void, String, Boolean> {
        @Override
        protected void onPreExecute() {
            searchList.clear();
            adapter.notifyDataSetChanged();
            if (listView.getCurrentMode() == PullToRefreshBase.Mode.PULL_FROM_START) {
                listView.setMode(PullToRefreshBase.Mode.DISABLED);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                searchList.addAll(ServerHelper.getInstance().searchUserDefault());
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "search()" + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (searchList.size() <= 0) {
                    startAnim();
                } else {
                    stopAnim();
                }
                adapter.notifyDataSetChanged();
            }
            if (listView.getCurrentMode() == PullToRefreshBase.Mode.DISABLED) {
                listView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            listView.onRefreshComplete();
        }
    }
}
