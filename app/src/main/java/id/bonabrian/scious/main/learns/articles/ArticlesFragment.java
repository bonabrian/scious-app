package id.bonabrian.scious.main.learns.articles;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.articlesdetail.ArticlesDetailActivity;
import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.Articles;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ArticlesFragment extends Fragment implements ArticlesContract.View, SwipeRefreshLayout.OnRefreshListener, ArticlesAdapter.ArticlesItemListener, View.OnClickListener {
    private static final String TAG = ArticlesFragment.class.getSimpleName();

    @BindView(R.id.articles_swipe_refresh_list)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.articles_recycler_view_list)
    RecyclerView recyclerView;
    @BindView(R.id.layout_errors)
    RelativeLayout errorsLayout;
    @BindView(R.id.error_message)
    TextView errorMessage;
    @BindView(R.id.btn_retry)
    Button btnRetry;

    private ArticlesContract.Presenter presenter;
    private ArticlesAdapter adapter;
    private List<Articles> articlesList = new ArrayList<>();
    private CompositeSubscription subscription;

    public ArticlesFragment() {
        // empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles, container, false);
        ButterKnife.bind(this, view);
        initObject();
        onAttachView();
        setSwipeRefreshLayout();
        setRecyclerView();
        presenter.loadData(0);
        return view;
    }

    private void initObject() {
        presenter = new ArticlesPresenter();
        subscription = new CompositeSubscription();
        adapter = new ArticlesAdapter(articlesList, getContext(), this);
    }

    private void setSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setRecyclerView() {
        adapter.setLoadMoreListener(new ArticlesAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (articlesList.size() > 7) {
                            loadData(articlesList.size());
                        }
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void loadData(final int page) {
        subscription.clear();
        if (page == 0) {
            articlesList.clear();
            adapter.setMoreDataAvailable(true);
        }
        if (page > 0) {
            articlesList.add(new Articles("load"));
            adapter.notifyItemInserted(articlesList.size() - 1);
        }
        Observable<Articles.ListArticles> call = ApiService.Factory.create().getListArticles(page);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Articles.ListArticles>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        if (e.getMessage() != null) {
                            if (e.getMessage().equals("Unable to resolve host \"scious.000webhostapp.com\": No address associated with hostname")) {
                                showError("Oops! Please check your internet connection");
                            } else {
                                showError("Something wrong :(");
                            }
                        }
                        Log.e(TAG, "Error: " + e.getMessage());
                        Log.e(TAG, "Error: " + e.getStackTrace());
                    }

                    @Override
                    public void onNext(Articles.ListArticles listArticles) {
                        if (listArticles.getStatus() == 200) {
                            if (page == 0) {
                                articlesList.clear();
                            }
                            if (page > 0) {
                                articlesList.remove(articlesList.size() - 1);
                                if (listArticles.getArticles().size() > 0) {
                                    articlesList.addAll(listArticles.getArticles());
                                } else {
                                    adapter.setMoreDataAvailable(false);
                                }
                                adapter.notifyDataChanged();
                            } else {
                                if (listArticles.getArticles().size() > 0) {
                                    articlesList.addAll(listArticles.getArticles());
                                    showArticlesData(articlesList);
                                } else {
                                    showError("Articles not available :(");
                                }
                            }
                        } else {
                            showError(listArticles.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void onRefresh() {
        recyclerView.setVisibility(View.VISIBLE);
        errorsLayout.setVisibility(View.GONE);
        presenter.loadData(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_retry:
                recyclerView.setVisibility(View.VISIBLE);
                errorsLayout.setVisibility(View.GONE);
                presenter.loadData(0);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
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
    public void showArticlesData(List<Articles> articlesList) {
        this.articlesList.clear();
        this.articlesList.addAll(articlesList);
        adapter.setMoreDataAvailable(true);
        adapter.notifyDataChanged();
    }

    @Override
    public void showError(String message) {
        this.articlesList.clear();
        adapter.notifyDataChanged();
        errorPage(message);
    }

    private void errorPage(String message) {
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        errorsLayout.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
        if (message.equals("Articles not available :(")) {
            btnRetry.setVisibility(View.GONE);
        }
        btnRetry.setOnClickListener(this);
    }

    @Override
    public void showProgress() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void hideProgress() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void showArticlesDetailView(String extras) {
        Intent intent = new Intent(getContext(), ArticlesDetailActivity.class);
        intent.putExtra("articles-detail", extras);
        startActivity(intent);
    }

    @Override
    public void onArticlesClick(String extra) {
        presenter.openArticlesDetail(extra);
    }
}
