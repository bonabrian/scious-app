package id.bonabrian.scious.measuringresult;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.measuring.MeasuringActivity;
import id.bonabrian.scious.seerecommendation.SeeRecommendation;
import id.bonabrian.scious.util.SessionManager;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MeasuringResultActivity extends AppCompatActivity implements View.OnClickListener, MeasuringResultContract.View {
    @BindView(R.id.sdnn_result)
    TextView sdnnResult;
//    @BindView(R.id.rmssd_result)
//    TextView rmssdResult;
    @BindView(R.id.stress_status)
    TextView stressStatus;
    @BindView(R.id.mean_rr_result)
    TextView meanRRResult;
    @BindView(R.id.mean_hr_result)
    TextView meanHRResult;
    @BindView(R.id.btn_save)
    Button btnSaveMeasurement;
//    @BindView(R.id.mood_rating_value)
//    RatingBar moodRating;
    @BindView(R.id.stress_image)
    ImageView stressImage;
    @BindView(R.id.result_top)
    RelativeLayout resultTopLayout;
    @BindView(R.id.recyclerview_result)
    RecyclerView recyclerView;
    @BindView(R.id.see_details)
    TextView seeDetails;
    @BindView(R.id.see_recommendation)
    TextView seeRecommendation;
    @BindView(R.id.layout_details)
    LinearLayout layoutDetails;

    private String stressResult;

    //private double rmssd;
    private double sdnn;
    private double meanHR;
    private double meanRR;
    private String stressLevel;
    String time;
    private List<Integer> heartList = new ArrayList<>();
    private List<Double> rrList = new ArrayList<>();
    ResultAdapter adapter;

    MeasuringResultContract.Presenter presenter;
    private ProgressDialog progressDialog;
    int ratingMood = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measuring_result);
        ButterKnife.bind(this);
        heartList = (List<Integer>) getIntent().getSerializableExtra("data-heartrate");
        seeDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutDetails.getVisibility() == View.VISIBLE) {
                    layoutDetails.setVisibility(View.GONE);
                    seeDetails.setText(getString(R.string.see_details));
                } else if (layoutDetails.getVisibility() == View.GONE) {
                    layoutDetails.setVisibility(View.VISIBLE);
                    seeDetails.setText(getString(R.string.hide_details));
                }
            }
        });
        seeRecommendation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeasuringResultActivity.this, SeeRecommendation.class);
                intent.putExtra("stress-result", setStressLevel(sdnn));
                startActivity(intent);
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = Calendar.getInstance().getTime();
        time = dateFormat.format(now);

        initObject();
//        if (getIntent().getDoubleExtra("rmssd-result", 0) != 0) {
//            rmssd = getIntent().getDoubleExtra("rmssd-result", 0);
//        }
        if (getIntent().getSerializableExtra("data-heartrate") != null && getIntent().getSerializableExtra("data-rr") != null) {
            heartList = (List<Integer>) getIntent().getSerializableExtra("data-heartrate");
            rrList = (List<Double>) getIntent().getSerializableExtra("data-rr");
            adapter = new ResultAdapter(heartList, rrList, this);
        }
        if (getIntent().getDoubleExtra("sdnn-result", 0) != 0) {
            sdnn = getIntent().getDoubleExtra("sdnn-result", 0);
        }
        if (getIntent().getDoubleExtra("mean-rr-result", 0) != 0) {
            meanRR = getIntent().getDoubleExtra("mean-rr-result", 0);
        }
        if (getIntent().getDoubleExtra("mean-hr-result", 0) != 0) {
            meanHR = getIntent().getDoubleExtra("mean-hr-result", 0);
        }
        DecimalFormat df = new DecimalFormat("#.#");

        onAttachView();
        setRecyclerView();

        btnSaveMeasurement.setOnClickListener(this);
//        moodRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
//                setMoodRating((int) moodRating.getRating());
//            }
//        });
        sdnnResult.setText(String.valueOf(df.format(sdnn)));
        //rmssdResult.setText(String.valueOf(df.format(rmssd)));
        meanRRResult.setText(String.valueOf(df.format(meanRR)));
        meanHRResult.setText(String.valueOf(df.format(meanHR)));
        if (meanHR >= 60 && meanHR <= 100) {
            meanHRResult.setTextColor(getColor(R.color.green));
        } else if (meanHR > 100) {
            meanHRResult.setTextColor(getColor(R.color.red));
        }

        if (setStressLevel(sdnn).equals("High Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.ic_sad_24dp)
                    .into(stressImage);
            resultTopLayout.setBackgroundColor(getResources().getColor(R.color.scious_high_stress));
        } else if (setStressLevel(sdnn).equals("Medium Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.ic_sad_24dp)
                    .into(stressImage);
            resultTopLayout.setBackgroundColor(getResources().getColor(R.color.scious_medium_stress));
        } else if (setStressLevel(sdnn).equals("Low Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.ic_normal_24dp)
                    .into(stressImage);
            resultTopLayout.setBackgroundColor(getResources().getColor(R.color.scious_low_stress));
        } else if (setStressLevel(sdnn).equals("No Stress")) {
            Glide.with(this)
                    .load("")
                    .placeholder(R.drawable.ic_happy_24dp)
                    .into(stressImage);
            resultTopLayout.setBackgroundColor(getResources().getColor(R.color.scious_no_stress));
        }
        String stressLevel = setStressLevel(sdnn);
        stressStatus.setText(stressLevel);
    }

    private void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private String setStressLevel(double sdnn) {
        String stress_level = "";
        if (getAge() >= 10 && getAge() <= 50) {
            if (sdnn > 50) {
                stress_level = this.getResources().getString(R.string.no_stress);
            } else if (sdnn > 35 && sdnn <= 50) {
                stress_level = this.getResources().getString(R.string.low_stress);
            } else if (sdnn >= 20 && sdnn <= 35) {
                stress_level = this.getResources().getString(R.string.medium_stress);
            } else if (sdnn < 20) {
                stress_level = this.getResources().getString(R.string.high_stress);
            }
        } else if (getAge() > 50 && getAge() <= 60) {
            if (sdnn > 40) {
                stress_level = this.getResources().getString(R.string.no_stress);
            } else if (sdnn > 20 && sdnn <= 40) {
                stress_level = this.getResources().getString(R.string.low_stress);
            } else if (sdnn >= 15 && sdnn <= 20) {
                stress_level = this.getResources().getString(R.string.medium_stress);
            } else if (sdnn < 15) {
                stress_level = this.getResources().getString(R.string.high_stress);
            }
        }
        return stress_level;
    }

    private int calculateAge(Date birthdate) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthdate);
        Calendar today = Calendar.getInstance();

        int yearDiff = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH)) {
            yearDiff--;
        } else {
            if (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
                yearDiff--;
            }
        }
        return yearDiff;
    }

    private int getAge() {
        String birthdateStr = SessionManager.getLoggedUser(this).getBirthday();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
        int age = 0;
        try {
            Date birthdate = df.parse(birthdateStr);
            age = calculateAge(birthdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return age;
    }

    private void initObject() {
        presenter = new MeasuringResultPresenter();
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                LayerDrawable drawable = (LayerDrawable) moodRating.getProgressDrawable();
//                drawable.getDrawable(2).setColorFilter(this.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void setMoodRating(int ratingMood) {
        this.ratingMood = ratingMood;
    }

    @Override
    public Context getContext() {
        return MeasuringResultActivity.this;
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
    public void showProgress() {
        progressDialog = ProgressDialog.show(getContext(), "", getResources().getString(R.string.saving_result), true, false);
    }

    @Override
    public void hideProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void showErrorMessage(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getResources().getString(R.string.error));
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void showSuccessMessage(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getResources().getString(R.string.success));
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                presenter.saveMeasurement(SessionManager.getLoggedUser(getContext()).getUserId(), setStressLevel(sdnn), sdnn, meanHR, meanRR, time);
                break;
            case R.id.see_recommendation:
                Intent intent = new Intent(MeasuringResultActivity.this, SeeRecommendation.class);
                startActivity(intent);
                break;
        }
    }

    class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Integer> heartList;
        private List<Double> rrList;
        private Context context;

        public ResultAdapter(List<Integer> heartList, List<Double> rrList, Context context) {
            this.heartList = heartList;
            this.rrList = rrList;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new ItemViewHolder(inflater.inflate(R.layout.adapter_measuring, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ItemViewHolder) holder).bindData(heartList.get(position), rrList.get(position));
        }

        @Override
        public int getItemCount() {
            return heartList.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.item_measuring_number)
            TextView itemNumber;
            @BindView(R.id.item_measuring_hr)
            TextView itemHr;
            @BindView(R.id.item_measuring_rr)
            TextView itemRr;

            public ItemViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            void bindData(Integer hr, Double rr) {
                DecimalFormat df = new DecimalFormat("#.####");
                itemNumber.setText(String.valueOf(getAdapterPosition()));
                itemHr.setText(String.valueOf(hr));
                if (hr >= 60 && hr <= 100) {
                    itemHr.setTextColor(getColor(R.color.green));
                } else if (hr > 100) {
                    itemHr.setTextColor(getColor(R.color.red));
                }
                itemRr.setText(String.valueOf(df.format(rr)));
            }
        }
    }
}
