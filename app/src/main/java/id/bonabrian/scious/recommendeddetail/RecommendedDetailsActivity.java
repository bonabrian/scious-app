package id.bonabrian.scious.recommendeddetail;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.historydetails.HistoryDetailActivity;
import id.bonabrian.scious.source.dao.Recommended;
import id.bonabrian.scious.util.AppConstant;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class RecommendedDetailsActivity extends AppCompatActivity {
    private static final String TAG = RecommendedDetailsActivity.class.getSimpleName();

    @BindView(R.id.recommended_detail_image)
    ImageView recommendedDetailImage;
    @BindView(R.id.recommended_detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recommended_detail_date)
    TextView recommendedDetailDate;
    @BindView(R.id.recommended_detail_content)
    TextView recommendedDetailContent;
    private String recommendedId;
    Recommended recommended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_detail);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (getIntent().getStringExtra("recommended-detail") != null) {
            showRecommendedData(new Gson().fromJson(getIntent().getStringExtra("recommended-detail"), Recommended.class));
        } else {
            setRecommendedId("1");
        }
    }

    private void showRecommendedData(Recommended recommended) {
        this.recommended = recommended;
        Glide.with(this)
                .load(AppConstant.Api.BASE_URL + recommended.getImage())
                .fitCenter()
                .error(R.drawable.avatar)
                .into(recommendedDetailImage);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(recommended.getTitle());
        }
        String strCurrentDate = String.valueOf(recommended.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date newDate = format.parse(strCurrentDate);
            format = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            String date = format.format(newDate);
            recommendedDetailDate.setText(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        recommendedDetailContent.setText(Html.fromHtml(recommended.getContent()).toString());
        Log.i(TAG, "RecommededID: " + recommended.getId());
        setRecommendedId(recommended.getId());
    }

    public void setRecommendedId(String recommendedId) {
        this.recommendedId = recommendedId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            collapsingToolbarLayout.setExpandedTitleTextColor(ColorStateList.valueOf(Color.TRANSPARENT));
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
