package id.bonabrian.scious.historydetails;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.source.dao.Measurements;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class HistoryDetailActivity extends AppCompatActivity {
    private static final String TAG = HistoryDetailActivity.class.getSimpleName();

    @BindView(R.id.history_detail_image)
    ImageView historyDetailImage;
    @BindView(R.id.history_detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.history_detail_result_top)
    RelativeLayout historyDetailResultTop;
    @BindView(R.id.history_detail_stress_status)
    TextView historyDetailStressStatus;
//    @BindView(R.id.rmssd_result)
//    TextView historyDetailRmssdResult;
    @BindView(R.id.sdnn_result)
    TextView historyDetailSdnnResult;
    @BindView(R.id.mean_hr_result)
    TextView historyDetailMeanHRResult;
    @BindView(R.id.mean_rr_result)
    TextView historyDetailMeanRRResult;
//    @BindView(R.id.history_detail_rating_value)
//    RatingBar historyDetailMoodRating;
    private String measurementId;
    Measurements measurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (getIntent().getStringExtra("history-detail") != null) {
            showHistoryData(new Gson().fromJson(getIntent().getStringExtra("history-detail"), Measurements.class));
        } else {
            setMeasurementId("1");
        }
    }

    private void showHistoryData(Measurements measurements) {
        this.measurements = measurements;
        if (measurements.getStressLevel().equals("High Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.red)
                    .into(historyDetailImage);
            historyDetailResultTop.setBackgroundColor(getResources().getColor(R.color.scious_high_stress));
        } else if (measurements.getStressLevel().equals("Medium Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.orange)
                    .into(historyDetailImage);
            historyDetailResultTop.setBackgroundColor(getResources().getColor(R.color.scious_medium_stress));
        } else if (measurements.getStressLevel().equals("Low Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.blue)
                    .into(historyDetailImage);
            historyDetailResultTop.setBackgroundColor(getResources().getColor(R.color.scious_low_stress));
        } else if (measurements.getStressLevel().equals("No Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.green)
                    .into(historyDetailImage);
            historyDetailResultTop.setBackgroundColor(getResources().getColor(R.color.scious_no_stress));
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String strCurrentDate = String.valueOf(measurements.getTime());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date newDate = format.parse(strCurrentDate);
                format = new SimpleDateFormat("MMM dd, yyyy HH:mm aa");
                String date = format.format(newDate);
                getSupportActionBar().setTitle(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        historyDetailStressStatus.setText(measurements.getStressLevel());
        //historyDetailRmssdResult.setText(String.valueOf(measurements.getRmssd()));
        historyDetailSdnnResult.setText(String.valueOf(measurements.getSdnn()));
        historyDetailMeanHRResult.setText(String.valueOf(measurements.getMeanHR()));
        historyDetailMeanRRResult.setText(String.valueOf(measurements.getMeanRR()));
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                LayerDrawable drawable = (LayerDrawable) historyDetailMoodRating.getProgressDrawable();
//                drawable.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        historyDetailMoodRating.setRating(measurements.getMoodRating());
    }

    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId;
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
