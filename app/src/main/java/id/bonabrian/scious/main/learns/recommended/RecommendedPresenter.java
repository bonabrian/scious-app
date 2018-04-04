package id.bonabrian.scious.main.learns.recommended;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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

public class RecommendedPresenter implements RecommendedContract.Presenter {
    private static final String TAG = RecommendedPresenter.class.getSimpleName();

    private RecommendedContract.View recommendedView;
    private CompositeSubscription subscription;
    public List<Recommended> recommendedList = new ArrayList<>();
    public RecommendedAdapter adapter;

    @Override
    public void onAttach(RecommendedContract.View view) {
        this.recommendedView = view;
        subscription = new CompositeSubscription();
        adapter = new RecommendedAdapter(recommendedList, recommendedView.getContext());
    }

    @Override
    public void onDetach() {
        this.recommendedView = null;
        subscription.clear();
    }

    @Override
    public void loadData(String user_id, final int page) {
        subscription.clear();
        if (page == 0) {
            recommendedList.clear();
            recommendedView.showProgress();
            adapter.setMoreDataAvailable(true);
        }
        if (page > 0) {
            recommendedList.add(new Recommended("load"));
            adapter.notifyItemInserted(recommendedList.size() - 1);
        }
        Observable<Recommended.ListRecommended> call = ApiService.Factory.create().getListRecommended(user_id, page);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Recommended.ListRecommended>() {
                    @Override
                    public void onCompleted() {
                        recommendedView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        recommendedView.hideProgress();
                        if (e.getMessage() != null) {
                            if (e.getMessage().equals("Unable to resolve host \"scious.000webhostapp.com\": No address associated with hostname")) {
                                recommendedView.showError("Oops! Please check your internet connection");
                            } else {
                                recommendedView.showError("Something wrong :(");
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
                                    recommendedView.showRecommendedData(recommendedList);
                                } else {
                                    recommendedView.showError("Yay...you're doing great, keep it :)");
                                }
                            }
                        } else {
                            recommendedView.showError(listRecommended.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void openRecommendedDetail(String extras) {
        recommendedView.showRecommendedDetailView(extras);
    }
}
