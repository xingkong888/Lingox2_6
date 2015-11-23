package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.TravelAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.task.GetAllTravelEntity;
import cn.lingox.android.utils.SkipDialog;

/**
 * 展示travel数据
 */
public class TravelFragment extends Fragment implements View.OnClickListener {
    private static final int SHOW_TRAVEL = 1101;//跳转到TravelViewActivity的请求码

    private static TravelFragment fragment;
    private ImageView anim;
    private AnimationDrawable animationDrawable;
    private PullToRefreshListView mListView;
    private int clickPosition = 0;
    private TravelAdapter adapter;
    private ArrayList<TravelEntity> travelDatas;

    private int page = 1;//分页加载页码

    public static synchronized TravelFragment newInstance() {
        if (fragment == null) {
            fragment = new TravelFragment();
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel, null);

        anim = (ImageView) view.findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();

        ImageView add = (ImageView) view.findViewById(R.id.iv_add_travel);
        add.setOnClickListener(this);

        mListView = (PullToRefreshListView) view.findViewById(R.id.travel_listview);
        travelDatas = new ArrayList<>();
        adapter = new TravelAdapter(getActivity(), travelDatas);
        mListView.setAdapter(adapter);
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setRefreshing(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickPosition = position - 1;

                Intent intent = new Intent(getActivity(), TravelViewActivity.class);
                intent.putExtra(TravelViewActivity.TRAVEL_VIEW, travelDatas.get(position - 1));
                startActivityForResult(intent, TravelFragment.SHOW_TRAVEL);
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
        refreshList();
        return view;
    }

    /**
     * 下载
     */
    private void refreshList() {
        new GetAllTravelEntity(page, new GetAllTravelEntity.Callback() {
            @Override
            public void onSuccess(ArrayList<TravelEntity> list) {
                travelDatas.addAll(list);
                if (travelDatas.size() > 0) {
                    stopAnim();
                } else {
                    startAnim();
                }
                adapter.notifyDataSetChanged();
                mListView.onRefreshComplete();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_travel:
                if (LingoXApplication.getInstance().getSkip()) {
                    SkipDialog.getDialog(getActivity()).show();
                } else {
                    //添加新的 TravelEditActivity
                    Intent intent = new Intent(getActivity(), TravelEditActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }
}