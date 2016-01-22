package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.LocalAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.task.SearchPathTask;

/**
 * 活动展示
 */
public class LocalFragment extends Fragment implements OnClickListener {
    // Request Codes
    public static final int EDIT_PATH = 102;
    static final String LOG_TAG = "LocalFragment";
    String[] select = new String[]{"Beijing", "Shanghai", "Guangzhou"};
    // Data Elements
    private ArrayList<Path> pathList;
    // UI Elements
    private PullToRefreshListView listView;
    private LocalAdapter adapter;
    private int page = 1;
    private ImageView img, refresh;
    private AnimationDrawable animationDrawable;
    private int clickPosition = -1;
    private int position = 0;//表示获取所有数据还是地区数据
    /**
     * local搜索的回调接口
     */
    private SearchPathTask.Callback localCallback = new SearchPathTask.Callback() {
        @Override
        public void onSuccess(ArrayList<Path> list) {
            refershPath(list);
            listView.onRefreshComplete();
        }

        @Override
        public void onFail() {
            listView.onRefreshComplete();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local, container, false);

        initView(view);
        getSelect(0);
        return view;
    }

    /**
     * 实例化控件
     *
     * @param v 父容器
     */
    private void initView(View v) {
        img = (ImageView) v.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) img.getBackground();
        refresh = (ImageView) v.findViewById(R.id.refresh_view);
        refresh.setOnClickListener(this);
        pathList = new ArrayList<>();
        adapter = new LocalAdapter(getActivity(), pathList);
        listView = (PullToRefreshListView) v.findViewById(R.id.path_pto_listview);
        listView.setAdapter(adapter);
        listView.setRefreshing();
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = new HashMap<>();
                map.put("discover", pathList.get(position - 1).getTitle());
                MobclickAgent.onEvent(getActivity(), "click_discover", map);

                clickPosition = position - 1;

                Intent intent = new Intent(getActivity(), LocalViewActivity.class);
                intent.putExtra(LocalViewActivity.PATH_TO_VIEW, pathList.get(position - 1));
                startActivityForResult(intent, LocalFragment.EDIT_PATH);
            }
        });
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                page = 1;
                pathList.clear();
                adapter.notifyDataSetChanged();
                getSelect(position);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载更多
                page++;
                getSelect(position);
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    //闲置状态，加载数据
                    adapter.setLoading(false);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.setLoading(true);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    /**
     * 开始动画
     */
    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            img.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    /**
     * 结束动画
     */
    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            img.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }

    /**
     * 调用path下载异步任务
     */
    public void refreshList(int position) {
        if (this.position != position) {
            this.position = position;
            pathList.clear();
            page = 1;
            listView.setRefreshing();
            //搜索地区
            getSelect(position);
        }
    }

    /**
     * 获取所有数据
     * 不要这个功能，直接获取某地的数据
     */
    private void getAll() {
        new GetPaths().execute();
    }

    //获取搜索数据
    private void getSelect(int position) {
        if (position == 2) {
            new SearchPathTask("", "", select[position], localCallback, page, getActivity()).execute();
        } else {
            new SearchPathTask("", select[position], "", localCallback, page, getActivity()).execute();
        }
    }

    /**
     * 添加一个新建的path
     *
     * @param path 新建的path实例
     */
    public void addPath(Path path) {
        pathList.add(0, path);
        adapter.notifyDataSetChanged();
    }

    /**
     * 通过搜索地点获取到的数据
     *
     * @param list “”
     */
    public void refershPath(ArrayList<Path> list) {
        //如果数据为空，则显示动画，并Toast
        pathList.addAll(list);
        if (pathList.size() <= 0) {
            //数据为空
            startAnim();
            Toast.makeText(getActivity(), "Temporarily no data.", Toast.LENGTH_SHORT).show();
        } else {
            stopAnim();
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除path
     *
     * @param path 删除的path实例
     */
    public void removePath(Path path) {
        pathList.remove(path);
        adapter.notifyDataSetChanged();
    }

    // If this becomes problematic just replace the view with a newly generated one from getPathView()
    public void modifyPath(Path path) {
        pathList.set(clickPosition, path);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_view:
                getSelect(position);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("LocalFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("LocalFragment");
    }

    private class GetPaths extends AsyncTask<Void, String, Boolean> {
        ArrayList<Path> tempPathList;

        @Override
        protected void onPreExecute() {
            tempPathList = new ArrayList<>();
            if (listView.getCurrentMode() == PullToRefreshBase.Mode.PULL_FROM_START) {
                listView.setMode(PullToRefreshBase.Mode.DISABLED);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                tempPathList.addAll(ServerHelper.getInstance().getAllPaths(page));
                if (!LingoXApplication.getInstance().getSkip()) {
                    for (Path path : tempPathList) {
                        User tempUser = CacheHelper.getInstance().getUserInfo(path.getUserId());
                        if (tempUser == null) {
                            CacheHelper.getInstance().addUserInfo(ServerHelper.getInstance().getUserInfo(path.getUserId()));
                        }
                    }
                }
                // 将数据添加到集合中
                pathList.addAll(tempPathList);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "getData() error:" + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (listView.getCurrentMode() == PullToRefreshBase.Mode.DISABLED) {
                listView.setMode(PullToRefreshBase.Mode.BOTH);
            }
            listView.onRefreshComplete();
            if (success) {
                if (pathList.size() != 0 && tempPathList.size() == 0) {
                    Toast.makeText(getActivity(), "No more data", Toast.LENGTH_SHORT).show();
                }
                refresh.setVisibility(View.GONE);
                try {
                    if (pathList.size() > 0) {
                        stopAnim();
                    } else {
                        startAnim();
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "GetPaths.onPostExecute: " + e.toString());
                }
            } else {
                refresh.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
            }
        }
    }
}