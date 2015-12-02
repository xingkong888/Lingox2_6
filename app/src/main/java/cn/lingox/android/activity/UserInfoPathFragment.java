package cn.lingox.android.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.lingox.android.R;
import cn.lingox.android.widget.MyViewPager;

/**
 * 显示在个人信息中的活动
 */
public class UserInfoPathFragment extends Fragment implements View.OnClickListener {

    private Button local, travel;
    private UserInfoLocalFragment localFragment;
    private UserInfoTravelFragment travelFragment;
    private MyViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_path, container, false);
        initView(view);

        return view;
    }

    /**
     * 实例化控件
     *
     * @param view 布局
     */
    private void initView(View view) {
//        view.findViewById(R.id.xxx).setVisibility(View.GONE);
        viewPager = (MyViewPager) view.findViewById(R.id.path_view_pager);
        viewPager.setScrollable(false);
        localFragment = new UserInfoLocalFragment();
        travelFragment = new UserInfoTravelFragment();
        FragmentAdapter adapter = new FragmentAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        // (Number of fragments - 1) This prevents the edge tabs being recreated
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);
        //本地人
        local = (Button) view.findViewById(R.id.path_local);
        local.setOnClickListener(this);
        //旅行者
        travel = (Button) view.findViewById(R.id.path_travel);
        travel.setOnClickListener(this);

        if (getArguments() != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("USER", getArguments().getParcelable("USER"));
            localFragment.setArguments(bundle);
            travelFragment.setArguments(bundle);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.path_local:
                viewPager.setCurrentItem(0);
                local.setBackgroundColor(Color.WHITE);
                travel.setBackgroundColor(getResources().getColor(R.color.three_c7));
                local.setTextColor(Color.BLACK);
                travel.setTextColor(Color.rgb(153, 153, 153));
                break;
            case R.id.path_travel:
                viewPager.setCurrentItem(1);
                travel.setBackgroundColor(Color.WHITE);
                local.setBackgroundColor(getResources().getColor(R.color.three_c7));
                travel.setTextColor(Color.BLACK);
                local.setTextColor(Color.rgb(153, 153, 153));
                break;
        }
    }

    /**
     * Viewpager的适配器
     */
    private class FragmentAdapter extends FragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return localFragment;
                case 1:
                    return travelFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}