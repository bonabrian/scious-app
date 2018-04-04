package id.bonabrian.scious.main.history;

import android.content.Context;
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
import id.bonabrian.scious.historydetails.HistoryDetailActivity;
import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.Measurements;
import id.bonabrian.scious.util.SessionManager;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class HistoryFragment extends Fragment implements HistoryContract.View, SwipeRefreshLayout.OnRefreshListener, HistoryAdapter.HistoryItemListener, View.OnClickListener {

    private static final String TAG = HistoryFragment.class.getSimpleName();

    @BindView(R.id.history_swipe_refresh_list)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.history_recycler_view_list)
    RecyclerView recyclerView;
    @BindView(R.id.layout_errors)
    RelativeLayout errorsLayout;
    @BindView(R.id.error_message)
    TextView errorMessage;
    @BindView(R.id.btn_retry)
    Button btnRetry;

    private HistoryContract.Presenter presenter;
    private HistoryAdapter adapter;
    private List<Measurements> measurementsList;
    private CompositeSubscription subscription;

    public HistoryFragment() {
        // empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        initObject();
        onAttachView();

        setSwipeRefreshLayout();
        setRecyclerView();

        presenter.loadData(SessionManager.getLoggedUser(getActivity()).getUserId(), 0);
        return view;
    }

    private void initObject() {
        presenter = new HistoryPresenter();
        measurementsList = new ArrayList<>();
        subscription = new CompositeSubscription();
        adapter = new HistoryAdapter(measurementsList, getContext(), this);
    }

    private void setSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setRecyclerView() {
        adapter.setLoadMoreListener(new HistoryAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (measurementsList.size() > 7) {
                            loadData(measurementsList.size());
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

    private void loadData(final int page) {
        subscription.clear();
        if (page == 0) {
            measurementsList.clear();
            adapter.setMoreDataAvailable(true);
        }
        if (page > 0) {
            measurementsList.add(new Measurements("load"));
            adapter.notifyItemInserted(measurementsList.size() - 1);
        }
        Observable<Measurements.ListMeasurements> call = ApiService.Factory.create().getListMeasurements(SessionManager.getLoggedUser(getActivity()).getUserId(), page);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Measurements.ListMeasurements>() {
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
                    public void onNext(Measurements.ListMeasurements listMeasurements) {
                        if (listMeasurements.getStatus() == 200) {
                            if (page == 0) {
                                measurementsList.clear();
                            }
                            if (page > 0) {
                                measurementsList.remove(measurementsList.size() - 1);
                                if (listMeasurements.getMeasurements().size() > 0) {
                                    measurementsList.addAll(listMeasurements.getMeasurements());
                                } else {
                                    adapter.setMoreDataAvailable(false);
                                }
                                adapter.notifyDataChanged();
                            } else {
                                if (listMeasurements.getMeasurements().size() > 0) {
                                    measurementsList.addAll(listMeasurements.getMeasurements());
                                    showHistoryData(measurementsList);
                                } else {
                                    showError("No history data");
                                }
                            }
                        } else {
                            showError(listMeasurements.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void onRefresh() {
        recyclerView.setVisibility(View.VISIBLE);
        errorsLayout.setVisibility(View.GONE);
        presenter.loadData(SessionManager.getLoggedUser(getActivity()).getUserId(), 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_retry:
                recyclerView.setVisibility(View.VISIBLE);
                errorsLayout.setVisibility(View.GONE);
                presenter.loadData(SessionManager.getLoggedUser(getActivity()).getUserId(), 0);
                break;
        }
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
    public void showHistoryData(List<Measurements> measurementsList) {
        this.measurementsList.clear();
        this.measurementsList.addAll(measurementsList);
        adapter.setMoreDataAvailable(true);
        adapter.notifyDataChanged();
    }

    @Override
    public void showError(String message) {
        this.measurementsList.clear();
        adapter.notifyDataChanged();
        errorPage(message);
    }

    private void errorPage(String message) {
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        errorsLayout.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
        if (message.equals("No history data")) {
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
    public void showHistoryDetailView(String extras) {
        Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
        intent.putExtra("history-detail", extras);
        startActivity(intent);
    }

    @Override
    public Context getContext() {
        return getActivity().getBaseContext();
    }

    @Override
    public void onHistoryClick(String extra) {
        presenter.openHistoryDetail(extra);
    }
}
