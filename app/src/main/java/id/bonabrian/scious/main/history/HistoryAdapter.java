package id.bonabrian.scious.main.history;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.source.dao.Measurements;
import id.bonabrian.scious.util.AppConstant;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Measurements> measurementsList;
    Context context;

    OnLoadMoreListener loadMoreListener;
    boolean isLoading = false, isMoreDataAvailable = true;
    private HistoryItemListener historyItemListener;

    public HistoryAdapter(List<Measurements> measurementsList, Context context, HistoryItemListener historyItemListener) {
        this.measurementsList = measurementsList;
        this.context = context;
        this.historyItemListener = historyItemListener;
    }

    public HistoryAdapter(List<Measurements> measurementsList, Context context) {
        this.measurementsList = measurementsList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == AppConstant.Tag.TAG_ADAPTERTYPE_LIST) {
            return new HistoryHolder(inflater.inflate(R.layout.adapter_history, parent, false));
        } else {
            return new LoadHolder(inflater.inflate(R.layout.adapter_load, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true;
            loadMoreListener.onLoadMore();
        }
        if (getItemViewType(position) == AppConstant.Tag.TAG_ADAPTERTYPE_LIST) {
            ((HistoryHolder) holder).bindData(measurementsList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return measurementsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (measurementsList.get(position).getTime().equals("load")) {
            return AppConstant.Tag.TAG_ADAPTERTYPE_LOAD;
        } else {
            return AppConstant.Tag.TAG_ADAPTERTYPE_LIST;
        }
    }

    static class LoadHolder extends RecyclerView.ViewHolder {
        public LoadHolder(View view) {
            super(view);
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    public void notifyDataChanged() {
        notifyDataSetChanged();
        isLoading = false;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.stress_image_history)
        ImageView stressImageLevel;
        @BindView(R.id.stress_level)
        TextView stressLevel;
        @BindView(R.id.history_time)
        TextView historyTime;
//        @BindView(R.id.feeling_rating)
//        RatingBar feelingRating;
        @BindView(R.id.stress_level_layout)
        LinearLayout stressLevelLayout;
        @BindView(R.id.stress_image_label)
        ImageView stressImageLabel;

        public HistoryHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void bindData(Measurements measurements) {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                try {
//                    LayerDrawable drawable = (LayerDrawable) feelingRating.getProgressDrawable();
//                    drawable.getDrawable(2).setColorFilter(context.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            if (measurements.getStressLevel().equals("High Stress")) {
                Glide.with(context)
                        .load("")
                        .placeholder(R.drawable.highstress)
                        .into(stressImageLevel);
                stressLevelLayout.setBackgroundColor(context.getResources().getColor(R.color.scious_high_stress));
            } else if (measurements.getStressLevel().equals("Medium Stress")) {
                Glide.with(context)
                        .load("")
                        .placeholder(R.drawable.mediumstress)
                        .into(stressImageLevel);
                stressLevelLayout.setBackgroundColor(context.getResources().getColor(R.color.scious_medium_stress));
            } else if (measurements.getStressLevel().equals("Low Stress")) {
                Glide.with(context)
                        .load("")
                        .placeholder(R.drawable.lowstress)
                        .into(stressImageLevel);
                stressLevelLayout.setBackgroundColor(context.getResources().getColor(R.color.scious_low_stress));
            } else if (measurements.getStressLevel().equals("No Stress")) {
                Glide.with(context)
                        .load("")
                        .placeholder(R.drawable.nostress)
                        .into(stressImageLevel);
                stressLevelLayout.setBackgroundColor(context.getResources().getColor(R.color.scious_no_stress));
            }
            stressLevel.setText(setStressLevel(measurements.getStressLevel()));
            String strCurrentDate = String.valueOf(measurements.getTime());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date newDate = format.parse(strCurrentDate);
                format = new SimpleDateFormat("MMM dd, yyyy HH:mm aa");
                String date = format.format(newDate);
                historyTime.setText(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //feelingRating.setRating(measurements.getMoodRating());
        }

        @Override
        public void onClick(View view) {
            historyItemListener.onHistoryClick(new Gson().toJson(measurementsList.get(getAdapterPosition())));
        }
    }

    public interface HistoryItemListener {
        void onHistoryClick(String extra);
    }

    private String setStressLevel(String stressLevel) {
        String level = "No Stress";
        if (stressLevel.equals("High Stress")) {
            level = context.getResources().getString(R.string.high_stress);
        } else if (stressLevel.equals("Medium Stress")) {
            level = context.getResources().getString(R.string.medium_stress);
        } else if (stressLevel.equals("Low Stress")) {
            level = context.getResources().getString(R.string.low_stress);
        } else if (stressLevel.equals("No Stress")) {
            level = context.getResources().getString(R.string.no_stress);
        }
        return level;
    }
}
