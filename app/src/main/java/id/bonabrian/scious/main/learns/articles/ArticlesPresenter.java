package id.bonabrian.scious.main.learns.articles;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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

public class ArticlesPresenter implements ArticlesContract.Presenter {
    private static final String TAG = ArticlesPresenter.class.getSimpleName();

    private ArticlesContract.View articlesView;
    private CompositeSubscription subscription;
    public List<Articles> articlesList = new ArrayList<>();
    public ArticlesAdapter adapter;

    @Override
    public void onAttach(ArticlesContract.View view) {
        this.articlesView = view;
        subscription = new CompositeSubscription();
        adapter = new ArticlesAdapter(articlesList, articlesView.getContext());
    }

    @Override
    public void onDetach() {
        subscription.clear();
        this.articlesView = null;
    }

    @Override
    public void loadData(final int page) {
        subscription.clear();
        if (page == 0) {
            articlesList.clear();
            articlesView.showProgress();
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
                        articlesView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        articlesView.hideProgress();
                        if (e.getMessage() != null) {
                            if (e.getMessage().equals("Unable to resolve host \"scious.000webhostapp.com\": No address associated with hostname")) {
                                articlesView.showError("Oops! Please check your internet connection");
                            } else {
                                articlesView.showError("Something wrong :(");
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
                                    articlesList.clear();
                                    articlesList.addAll(listArticles.getArticles());
                                    articlesView.showArticlesData(articlesList);
                                } else {
                                    articlesView.showError("Sorry, articles not available yet :(");
                                }
                            }
                        } else {
                            articlesView.showError(listArticles.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void openArticlesDetail(String extras) {
        articlesView.showArticlesDetailView(extras);
    }
}
