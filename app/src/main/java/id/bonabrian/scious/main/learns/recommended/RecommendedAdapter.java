package id.bonabrian.scious.main.learns.recommended;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.source.dao.Recommended;
import id.bonabrian.scious.util.AppConstant;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class RecommendedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Recommended> recommendedList;
    private Context context;
    private int contentLength = 50;

    OnLoadMoreListener loadMoreListener;
    boolean isLoading = false, isMoreDataAvailable = true;
    private RecommendedItemListener recommendedItemListener;

    public RecommendedAdapter(List<Recommended> recommendedList, Context context, RecommendedItemListener recommendedItemListener) {
        this.recommendedList = recommendedList;
        this.context = context;
        this.recommendedItemListener = recommendedItemListener;
    }

    public RecommendedAdapter(List<Recommended> recommendedList, Context context) {
        this.recommendedList = recommendedList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == AppConstant.Tag.TAG_ADAPTERTYPE_LIST) {
            return new RecommendedHolder(inflater.inflate(R.layout.adapter_recommended, parent, false));
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
            ((RecommendedHolder) holder).bindData(recommendedList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return recommendedList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (recommendedList.get(position).getTitle().equals("load")) {
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

    class RecommendedHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.recommended_item_image)
        ImageView recommendedImage;
        @BindView(R.id.recommended_item_title)
        TextView recommendedTitle;
        @BindView(R.id.recommended_item_content_truncate)
        TextView recommendedContent;

        public RecommendedHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void bindData(Recommended recommended) {
            Glide.with(context)
                    .load(AppConstant.Api.BASE_URL + recommended.getImage())
                    .fitCenter()
                    .error(R.drawable.avatar)
                    .into(recommendedImage);
            recommendedTitle.setText(recommended.getTitle());
            String content = recommended.getContent();
            String mContent = Html.fromHtml(content).toString();
            String truncate;
            if (content.length() > contentLength) {
                truncate = mContent.substring(0, contentLength);
                recommendedContent.setText(truncate + "...");
            } else {
                recommendedContent.setText(mContent);
            }
        }

        @Override
        public void onClick(View view) {
            recommendedItemListener.onRecommendedClick(new Gson().toJson(recommendedList.get(getAdapterPosition())));
        }
    }

    public interface RecommendedItemListener {
        void onRecommendedClick(String extra);
    }
}
