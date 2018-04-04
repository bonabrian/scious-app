package id.bonabrian.scious.main.history;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.Measurements;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class HistoryPresenter implements HistoryContract.Presenter {
    private static final String TAG = HistoryPresenter.class.getSimpleName();

    private HistoryContract.View measurementView;
    private CompositeSubscription subscription;
    public List<Measurements> measurementsList = new ArrayList<>();
    public HistoryAdapter adapter;

    @Override
    public void onAttach(HistoryContract.View view) {
        this.measurementView = view;
        subscription = new CompositeSubscription();
        adapter = new HistoryAdapter(measurementsList, measurementView.getContext());
    }

    @Override
    public void onDetach() {
        subscription.clear();
        this.measurementView = null;
    }

    @Override
    public void loadData(String user_id, final int page) {
        subscription.clear();
        if (page == 0) {
            measurementsList.clear();
            measurementView.showProgress();
            adapter.setMoreDataAvailable(true);
        }
        if (page > 0) {
            measurementsList.add(new Measurements("load"));
            adapter.notifyItemInserted(measurementsList.size() - 1);
        }
        Observable<Measurements.ListMeasurements> call = ApiService.Factory.create().getListMeasurements(user_id, page);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Measurements.ListMeasurements>() {
                    @Override
                    public void onCompleted() {
                        measurementView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        measurementView.hideProgress();
                        if (e.getMessage() != null) {
                            if (e.getMessage().equals("Unable to resolve host \"scious.000webhostapp.com\": No address associated with hostname")) {
                                measurementView.showError("Oops! Please check your internet connection");
                            } else {
                                measurementView.showError("Something wrong :(");
                            }
                        }
                        Log.e(TAG, "Error: " + e.getMessage());
                        Log.e(TAG, "Error: " + e.getStackTrace());
                    }

                    @Override
                    public void onNext(Measurements.ListMeasurements listMeasurements) {
                        if (listMeasurements.getStatus() == 200) {
                            if (page == 0) {
                                //measurementsList.clear();
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
                                    measurementView.showHistoryData(measurementsList);
                                } else {
                                    measurementView.showError("No history data");
                                }
                            }
                        } else {
                            measurementView.showError(listMeasurements.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void openHistoryDetail(String extras) {
        measurementView.showHistoryDetailView(extras);
    }
}
