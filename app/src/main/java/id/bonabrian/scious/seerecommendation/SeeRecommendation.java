package id.bonabrian.scious.seerecommendation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.recommendeddetail.RecommendedDetailsActivity;
import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.Recommended;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SeeRecommendation extends AppCompatActivity implements SeeRecommendationContract.View, SwipeRefreshLayout.OnRefreshListener, SeeRecommendationAdapter.RecommendedItemListener, View.OnClickListener {

    private static final String TAG = SeeRecommendation.class.getSimpleName();

    @BindView(R.id.recommended_swipe_refresh_list)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recommended_recycler_view_list)
    RecyclerView recyclerView;
    @BindView(R.id.layout_errors)
    RelativeLayout errorsLayout;
    @BindView(R.id.error_message)
    TextView errorMessage;
    @BindView(R.id.btn_retry)
    Button btnRetry;

    private SeeRecommendationContract.Presenter presenter;
    private SeeRecommendationAdapter adapter;
    private List<Recommended> recommendedList;
    private CompositeSubscription subscription;
    private String stressLevel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_recommendation);
        ButterKnife.bind(this);
        if (getIntent().getStringExtra("stress-result") != null) {
            stressLevel = getIntent().getStringExtra("stress-result");
        }
        recommendedList = new ArrayList<>();
        presenter = new SeeRecommendationPresenter();
        subscription = new CompositeSubscription();
        onAttachView();
        initObject();
    }

    private void initObject() {
        adapter = new SeeRecommendationAdapter(recommendedList, getContext(), this);
        setSwipeRefreshLayout();
        setRecyclerView();
        presenter.loadData(stressLevel, 0);
    }

    private void setSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setRecyclerView() {
        adapter.setLoadMoreListener(new SeeRecommendationAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (recommendedList.size() > 7) {
                            loadData(recommendedList.size());
                        }
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void loadData(final int page) {
        subscription.clear();
        if (page == 0) {
            recommendedList.clear();
            adapter.setMoreDataAvailable(true);
        }
        if (page > 0) {
            recommendedList.add(new Recommended("load"));
            adapter.notifyItemInserted(recommendedList.size() - 1);
        }
        Observable<Recommended.ListRecommended> call = ApiService.Factory.create().seeRecommendation(stressLevel, page);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Recommended.ListRecommended>() {
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
                    public void onNext(Recommended.ListRecommended listRecommended) {
                        if (listRecommended.getStatus() == 200) {
                            if (page == 0) {
                                recommendedList.clear();
                            }
                            if (page > 0) {
                                recommendedList.remove(recommendedList.size() - 1);
                                if (listRecommended.getRecommended().size() > 0) {
                                    recommendedList.addAll(listRecommended.getRecommended());
                                } else {
                                    adapter.setMoreDataAvailable(false);
                                }
                                adapter.notifyDataChanged();
                            } else {
                                if (listRecommended.getRecommended().size() > 0) {
                                    recommendedList.addAll(listRecommended.getRecommended());
                                    showRecommendedData(recommendedList);
                                } else {
                                    showError("Sorry, no recommended yet");
                                }
                            }
                        } else {
                            showError(listRecommended.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void onRefresh() {
        recyclerView.setVisibility(View.VISIBLE);
        errorsLayout.setVisibility(View.GONE);
        presenter.loadData(stressLevel, 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_retry:
                recyclerView.setVisibility(View.VISIBLE);
                errorsLayout.setVisibility(View.GONE);
                presenter.loadData(stressLevel, 0);
                break;
        }
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
    public void showRecommendedData(List<Recommended> recommendedList) {
        this.recommendedList.clear();
        this.recommendedList.addAll(recommendedList);
        adapter.setMoreDataAvailable(true);
        adapter.notifyDataChanged();
    }

    @Override
    public void showError(String message) {
        this.recommendedList.clear();
        adapter.notifyDataChanged();
        errorPage(message);
    }

    private void errorPage(String message) {
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        errorsLayout.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
        if (message.equals("Sorry, no recommended yet")) {
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
    public void showRecommendedDetailView(String extras) {
        Intent intent = new Intent(getContext(), RecommendedDetailsActivity.class);
        intent.putExtra("recommended-detail", extras);
        startActivity(intent);
    }

    @Override
    public void onRecommendedClick(String extra) {
        presenter.openRecommendedDetail(extra);
    }
}
