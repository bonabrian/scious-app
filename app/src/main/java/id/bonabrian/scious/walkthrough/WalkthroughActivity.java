package id.bonabrian.scious.walkthrough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.login.LoginActivity;
import id.bonabrian.scious.main.MainActivity;
import id.bonabrian.scious.util.CirclePageIndicator;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class WalkthroughActivity extends AppCompatActivity implements WalkthroughContract.View {

    @BindView(R.id.walkthrough_pager)
    ViewPager pager;

    @BindView(R.id.btn_getstarted)
    Button btnGetStarted;

    @BindView(R.id.walkthrough_indicator)
    CirclePageIndicator indicator;

    WalkthroughContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);
        ButterKnife.bind(this);
        presenter = new WalkthroughPresenter();
        onAttachView();

        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new WelcomeFragment();
                    case 1:
                        return new SciousDescriptionFragment();
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.finishWalkthrough();
            }
        });

        pager.setAdapter(adapter);
        indicator.setViewPager(pager);

        indicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(final int position) {
                if (position == 1)
                    btnGetStarted.setVisibility(View.VISIBLE);
                else
                    btnGetStarted.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onAttachView() {
        presenter.onAttach(this);
    }

    @Override
    public void onDetachView() {
        presenter.onDetach();
    }

    @Override
    public void showMainView() {
        Intent intent = new Intent(WalkthroughActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showLoginView() {
        Intent intent = new Intent(WalkthroughActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
