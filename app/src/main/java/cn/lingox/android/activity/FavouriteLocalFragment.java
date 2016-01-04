package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
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
import cn.lingox.android.adapter.FavouriteLocalAdapter;
import cn.lingox.android.entity.Path;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.task.GetFavouriteLocal;

/**
 * 展示收藏的local数据
 */
public class FavouriteLocalFragment extends Fragment {
    // Data Elements
    private ArrayList<Path> pathList;
    // UI Elements
    private PullToRefreshListView listView;
    private FavouriteLocalAdapter adapter;
    private int page = 1;
    private ImageView img;
    private AnimationDrawable animationDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local, container, false);

        initView(view);
        refreshList();
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
        pathList = new ArrayList<>();
        adapter = new FavouriteLocalAdapter(getActivity(), pathList);
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

                Intent intent = new Intent(getActivity(), LocalViewActivity.class);
                intent.putExtra(LocalViewActivity.PATH_TO_VIEW, pathList.get(position - 1));
                startActivity(intent);
            }
        });
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                page = 1;
                pathList.clear();
                adapter.notifyDataSetChanged();
                refreshList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载更多
                page++;
                refreshList();
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
    private void refreshList() {
        new GetFavouriteLocal(CacheHelper.getInstance().getSelfInfo().getId(),
                page, new GetFavouriteLocal.Callback() {
            @Override
            public void onSuccess(ArrayList<Path> list) {
                if (list.size() <= 0) {
                    startAnim();
                }
                pathList.addAll(list);
                listView.onRefreshComplete();
            }

            @Override
            public void onFail() {
                listView.onRefreshComplete();
                startAnim();
                Toast.makeText(getActivity(), "Download fail", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }
}