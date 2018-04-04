package id.bonabrian.scious.articlesdetail;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.source.dao.Articles;
import id.bonabrian.scious.util.AppConstant;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ArticlesDetailActivity extends AppCompatActivity implements ArticlesDetailContract.View {
    private static final String TAG = ArticlesDetailActivity.class.getSimpleName();
    @BindView(R.id.articles_detail_image)
    ImageView articleDetailsImage;
    @BindView(R.id.articles_detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.articles_detail_date)
    TextView articlesDetailsDate;
    @BindView(R.id.articles_detail_author)
    TextView articlesDetailsAuthor;
    @BindView(R.id.articles_detail_content)
    TextView articlesDetailContent;
    private String articleId;
    Articles articles;
    ArticlesDetailContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_detail);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        initObject();
        onAttachView();

        if (getIntent().getStringExtra("articles-detail") != null) {
            showArticlesData(new Gson().fromJson(getIntent().getStringExtra("articles-detail"), Articles.class));
        } else {
            setArticlesId("1");
        }
    }

    private void initObject() {
        presenter = new ArticlesDetailPresenter();
    }

    private void showArticlesData(Articles articles) {
        this.articles = articles;
        Glide.with(this)
                .load(AppConstant.Api.BASE_URL + articles.getImage())
                .fitCenter()
                .error(R.drawable.avatar)
                .into(articleDetailsImage);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(articles.getTitle());
        }
        String strCurrentDate = String.valueOf(articles.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date newDate = format.parse(strCurrentDate);
            format = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            String date = format.format(newDate);
            articlesDetailsDate.setText(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        articlesDetailsAuthor.setText(articles.getAuthor());
        articlesDetailContent.setText(Html.fromHtml(articles.getContent()).toString());
        Log.i(TAG, "ArticlesID: " + articles.getId());
        setArticlesId(articles.getId());
    }

    public void setArticlesId(String articleId) {
        this.articleId = articleId;
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
    public Context getContext() {
        return ArticlesDetailActivity.this;
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
