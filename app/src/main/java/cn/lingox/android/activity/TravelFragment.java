package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
    private static final int ADD_TRAVEL = 1101;//添加的请求码
    private static final int EDIT_TRAVEL = 1102;//修改的请求码

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
                refershView(4, null);
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
                    //添加新的 Travel
                    Intent intent = new Intent(getActivity(), TravelEditActivity.class);
                    startActivityForResult(intent, ADD_TRAVEL);
                }
                break;
        }
    }

    /**
     * 数据改变，刷新界面
     *
     * @param flg 1:添加 2:修改 3:删除 4:刷新适配器
     */
    private void refershView(int flg, TravelEntity travelEntity) {
        switch (flg) {
            case 1:
                travelDatas.add(travelEntity);
                break;
            case 2:
                travelDatas.add(clickPosition, travelEntity);
                break;
            case 3:
                travelDatas.remove(travelEntity);
                Log.d("星期", "delete>>>>" + travelDatas.size());
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
                case ADD_TRAVEL:
                    if (data.hasExtra(TravelEditActivity.TRAVEL_CREATE)) {
                        refershView(1, (TravelEntity) data.getParcelableExtra(TravelEditActivity.TRAVEL_CREATE));
                    }
                    break;
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