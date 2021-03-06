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

import cn.lingox.android.R;
import cn.lingox.android.adapter.TravelAdapter;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.task.GetAllTravelEntity;
import cn.lingox.android.task.SearchTravelEntity;

/**
 * 展示travel数据
 */
public class TravelFragment extends Fragment {
    private static final int EDIT_TRAVEL = 1102;//修改的请求码
    public ArrayList<TravelEntity> travelDatas;
    String[] select = new String[]{"Beijing", "Shanghai", "Guangzhou"};
    private ImageView anim;
    private AnimationDrawable animationDrawable;
    private PullToRefreshListView mListView;
    private int clickPosition = 0;
    private TravelAdapter adapter;
    private int page = 1;//分页加载页码
    private int position = 0;//表示获取全部数据还是获取地区数据
    /**
     * travel搜索的回调接口
     */
    private SearchTravelEntity.Callback travelCallback = new SearchTravelEntity.Callback() {
        @Override
        public void onSuccess(ArrayList<TravelEntity> list) {
            travelDatas.addAll(list);
            if (travelDatas.size() <= 0) {
                startAnim();
            } else {
                stopAnim();
            }
            mListView.onRefreshComplete();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onFail() {
            mListView.onRefreshComplete();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel, null);

        initView(view);
        getSelect(0);
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
        mListView.setRefreshing();
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickPosition = position - 1;

                Intent intent = new Intent(getActivity(), TravelViewActivity.class);
                intent.putExtra(TravelViewActivity.TRAVEL_VIEW, travelDatas.get(position - 1));
                startActivityForResult(intent, TravelFragment.EDIT_TRAVEL);
            }
        });
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                page = 1;
                travelDatas.clear();
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
    public void refreshList(int position) {
        if (this.position != position) {
            this.position = position;
            travelDatas.clear();
            page = 1;
            mListView.setRefreshing();
        }
    }

    /**
     * 获取所有数据
     * 不要这个功能，直接获取某地的数据
     */
    private void getAll() {
        mListView.setRefreshing();
        new GetAllTravelEntity(page, new GetAllTravelEntity.Callback() {
            @Override
            public void onSuccess(ArrayList<TravelEntity> list) {
                travelDatas.addAll(list);
                refershView(4, null);
                mListView.onRefreshComplete();
            }

            @Override
            public void onFail() {
                mListView.onRefreshComplete();
                Toast.makeText(getActivity(), "Download fail", Toast.LENGTH_SHORT).show();
            }
        }, getActivity()).execute();
    }

    //获取搜索结果
    private void getSelect(int position) {
        if (position == 2) {
            new SearchTravelEntity("", "", select[position], page, travelCallback, getActivity()).execute();
        } else {
            new SearchTravelEntity("", select[position], "", page, travelCallback, getActivity()).execute();
        }
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

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("TravelFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("TravelFragment");
    }

    /**
     * 为MainActivity提供的公共方法
     *
     * @param travelEntity 实例
     */
    public void refershView(TravelEntity travelEntity) {
        refershView(1, travelEntity);
    }

    /**
     * 数据更新，界面更新
     *
     * @param flg          1:添加 2:修改 3:删除 4:刷新适配器
     * @param travelEntity 实例
     */
    private void refershView(int flg, TravelEntity travelEntity) {
        switch (flg) {
            case 1:
                travelDatas.add(0, travelEntity);
                break;
            case 2:
                travelDatas.remove(clickPosition);
                travelDatas.add(clickPosition, travelEntity);
                break;
            case 3:
                travelDatas.remove(clickPosition);
                break;
        }
        adapter.notifyDataSetChanged();
        if (travelDatas.size() > 0) {
            stopAnim();
        } else {
            startAnim();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case EDIT_TRAVEL:
                    if (data.hasExtra(TravelViewActivity.EDIT)) {
                        refershView(2, (TravelEntity) data.getParcelableExtra(TravelViewActivity.EDIT));
                    } else if (data.hasExtra(TravelViewActivity.DELETE)) {
                        refershView(3, (TravelEntity) data.getParcelableExtra(TravelViewActivity.DELETE));
                    }
                    break;
            }
        }
    }
}
/*为活动申请群聊id
     new Thread(new Runnable() {
    @Override
    public void run() {
        try {
            EMGroupManager.getInstance().createPublicGroup(path.getTitle().equals("") ? CacheHelper.getInstance().getSelfInfo().getNickname() :
                    path.getTitle(), path.getText(), null, true);
        } catch (EaseMobException e) {
            e.printStackTrace();
        }
    }
}).start();*/