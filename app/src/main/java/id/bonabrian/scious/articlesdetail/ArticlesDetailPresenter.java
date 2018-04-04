package id.bonabrian.scious.articlesdetail;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ArticlesDetailPresenter implements ArticlesDetailContract.Presenter {
    ArticlesDetailContract.View view;
    @Override
    public void onAttach(ArticlesDetailContract.View view) {
        this.view = view;
    }

    @Override
    public void onDetach() {
        this.view = null;
    }
}
