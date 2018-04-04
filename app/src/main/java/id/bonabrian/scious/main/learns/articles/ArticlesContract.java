package id.bonabrian.scious.main.learns.articles;

import java.util.List;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;
import id.bonabrian.scious.source.dao.Articles;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ArticlesContract {
    public interface View extends BaseView {
        void showArticlesData(List<Articles> articlesList);
        void showError(String message);
        void showProgress();
        void hideProgress();
        void showArticlesDetailView(String extras);
    }

    public interface Presenter extends BasePresenter<View> {
        void loadData(int page);
        void openArticlesDetail(String extras);
    }
}
