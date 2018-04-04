package id.bonabrian.scious.main.learns;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import id.bonabrian.scious.R;
import id.bonabrian.scious.main.learns.articles.ArticlesFragment;
import id.bonabrian.scious.main.learns.recommended.RecommendedFragment;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class LearnsTabPagerAdapter extends FragmentPagerAdapter {
    private String[] title;

    public LearnsTabPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        title = new String[]{context.getString(R.string.title_articles), context.getString(R.string.title_recommended)};
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ArticlesFragment();
            case 1:
                return new RecommendedFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public String getPageTitle(int position) {
        return title[position];
    }
}
