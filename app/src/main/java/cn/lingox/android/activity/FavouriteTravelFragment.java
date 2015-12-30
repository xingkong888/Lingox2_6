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

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.TravelAdapter;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.task.GetFavouriteTravel;

/**
 * 展示收藏的travel数据
 */
public class FavouriteTravelFragment extends Fragment {
    public ArrayList<TravelEntity> travelDatas;
    private ImageView anim;
    private AnimationDrawable animationDrawable;
    private PullToRefreshListView mListView;
    private int clickPosition = 0;
    private TravelAdapter adapter;
    private int page = 1;//分页加载页码

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel, null);

        initView(view);
        refreshList();
        return view;
    }

    /**
     * 初始化控件
     *
     * @param view 容器布局
     */
    private void initView(View view) {
        anim = (ImageView) view.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();

        mListView = (PullToRefreshListView) view.findViewById(R.id.travel_listview);
        travelDatas = new ArrayList<>();
        page = 1;
        adapter = new TravelAdapter(getActivity(), travelDatas);
        mListView.setAdapter(adapter);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setRefreshing();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickPosition = position - 1;

                Intent intent = new Intent(getActivity(), TravelViewActivity.class);
                intent.putExtra(TravelViewActivity.TRAVEL_VIEW, travelDatas.get(position - 1));
                startActivity(intent);
            }
        });
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                page = 1;
                travelDatas.clear();
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
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
     * 下载
     */
    private void refreshList() {
        new GetFavouriteTravel(CacheHelper.getInstance().getSelfInfo().getId(),
                page, new GetFavouriteTravel.Callback() {
            @Override
            public void onSuccess(ArrayList<TravelEntity> list) {
                mListView.onRefreshComplete();
                travelDatas.addAll(list);
                adapter.notifyDataSetChanged();
                if (travelDatas.size() > 0) {
                    stopAnim();
                } else {
                    startAnim();
                }
            }

            @Override
            public void onFail() {
                mListView.onRefreshComplete();
                Toast.makeText(getActivity(), "Download fail", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            anim.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            anim.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }
}