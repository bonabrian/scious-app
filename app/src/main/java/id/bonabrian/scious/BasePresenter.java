package id.bonabrian.scious;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface BasePresenter<T extends BaseView> {
    void onAttach(T view);

    void  onDetach();
}
