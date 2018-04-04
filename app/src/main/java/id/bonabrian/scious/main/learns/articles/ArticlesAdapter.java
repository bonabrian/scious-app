package id.bonabrian.scious.main.learns.articles;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.source.dao.Articles;
import id.bonabrian.scious.util.AppConstant;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ArticlesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Articles> articlesList;
    private Context context;

    OnLoadMoreListener loadMoreListener;
    boolean isLoading = false, isMoreDataAvailable = true;
    private ArticlesItemListener articlesItemListener;

    public ArticlesAdapter(List<Articles> articlesList, Context context, ArticlesItemListener articlesItemListener) {
        this.articlesList = articlesList;
        this.context = context;
        this.articlesItemListener = articlesItemListener;
    }

    public ArticlesAdapter(List<Articles> articlesList, Context context) {
        this.articlesList = articlesList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == AppConstant.Tag.TAG_ADAPTERTYPE_LIST) {
            return new ArticlesHolder(inflater.inflate(R.layout.adapter_learns_articles, parent, false));
        } else {
            return new LoadHolder(inflater.inflate(R.layout.adapter_load, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null){
            isLoading = true;
            loadMoreListener.onLoadMore();
        }
        if (getItemViewType(position) == AppConstant.Tag.TAG_ADAPTERTYPE_LIST) {
            ((ArticlesHolder) holder).bindData(articlesList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (articlesList.get(position).getTitle().equals("load")) {
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

    public void notifyDataChanged(){
        notifyDataSetChanged();
        isLoading = false;
    }

    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    class ArticlesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.articles_item_image)
        ImageView articleImage;
        @BindView(R.id.articles_item_title)
        TextView articleTitle;
        @BindView(R.id.articles_item_time)
        TextView articleTime;

        public ArticlesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void bindData(Articles articles) {
            Glide.with(context)
                    .load(AppConstant.Api.BASE_URL + articles.getImage())
                    .fitCenter()
                    .error(R.drawable.avatar)
                    .into(articleImage);
            articleTitle.setText(articles.getTitle());
            String strCurrentDate = String.valueOf(articles.getTime());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date newDate = format.parse(strCurrentDate);
                format = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                String date = format.format(newDate);
                articleTime.setText(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View view) {
            articlesItemListener.onArticlesClick(new Gson().toJson(articlesList.get(getAdapterPosition())));
        }
    }

    public interface ArticlesItemListener {
        void onArticlesClick(String extra);
    }

}
