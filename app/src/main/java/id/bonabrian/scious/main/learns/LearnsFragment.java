package id.bonabrian.scious.main.learns;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class LearnsFragment extends Fragment {
    @BindView(R.id.learns_view_pager)
    ViewPager viewPager;
    @BindView(R.id.learns_tab_layout)
    TabLayout tabLayout;

    LearnsTabPagerAdapter tabPagerAdapter;

    public LearnsFragment() {
        // empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learns, container, false);
        ButterKnife.bind(this, view);
        viewPager.setOffscreenPageLimit(2);
        tabPagerAdapter = new LearnsTabPagerAdapter(getChildFragmentManager(), getContext());
        viewPager.setAdapter(tabPagerAdapter);
        tabLayout.setTabTextColors(getResources().getColor(R.color.gray_light), getResources().getColor(R.color.colorPrimary));

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
        return view;
    }

    @Override
    public Context getContext() {
        return getActivity().getBaseContext();
    }
}
