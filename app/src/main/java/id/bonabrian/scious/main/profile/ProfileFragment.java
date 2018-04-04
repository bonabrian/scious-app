package id.bonabrian.scious.main.profile;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.login.LoginActivity;
import id.bonabrian.scious.main.MainActivity;
import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.BaseModel;
import id.bonabrian.scious.source.dao.User;
import id.bonabrian.scious.util.SessionManager;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ProfileFragment extends Fragment implements View.OnClickListener, ProfileContract.View {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private static int PADDING_LEFT = 24;
    private static int PADDING_DEFAULT = 16;

    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.btn_edit_name)
    LinearLayout btnEditName;
    @BindView(R.id.user_email)
    TextView userEmail;
    @BindView(R.id.btn_edit_email)
    LinearLayout btnEditEmail;
    @BindView(R.id.user_weight)
    TextView userWeight;
    @BindView(R.id.btn_edit_weight)
    LinearLayout btnEditWeight;
    @BindView(R.id.user_height)
    TextView userHeight;
    @BindView(R.id.btn_edit_height)
    LinearLayout btnEditHeight;
    @BindView(R.id.user_birthday)
    TextView userBirthday;
    @BindView(R.id.btn_edit_birthday)
    LinearLayout btnEditBirthday;
    @BindView(R.id.btn_logout)
    LinearLayout btnLogout;

    ProfileContract.Presenter presenter;
    private ProgressDialog progressDialog;
    private CompositeSubscription subscription;
    private int mYear = 0, mMonth = 0, mDay = 0;

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        initObject();
        onAttachView();
        initDate();
        setDataUser(SessionManager.getLoggedUser(getContext()));
        setOnClickButton();
        return view;
    }

    private void initObject() {
        subscription = new CompositeSubscription();
        presenter = new ProfilePresenter();
    }

    private void initDate() {
        final Calendar cal = Calendar.getInstance();
        if (mYear == 0) mYear = cal.get(Calendar.YEAR);
        if (mMonth == 0) mMonth = cal.get(Calendar.MONTH);
        if (mDay == 0) mDay = cal.get(Calendar.DAY_OF_YEAR);
    }

    private void setOnClickButton() {
        btnEditName.setOnClickListener(this);
        btnEditEmail.setOnClickListener(this);
        btnEditWeight.setOnClickListener(this);
        btnEditHeight.setOnClickListener(this);
        btnEditBirthday.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
    }

    private void setDataUser(User user) {
        userName.setText(user.getName());
        userEmail.setText(user.getEmail());
        userWeight.setText(user.getWeight());
        userHeight.setText(user.getHeight());
        String strCurrentDate = String.valueOf(user.getBirthday());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date newDate = format.parse(strCurrentDate);
            format = new SimpleDateFormat("MMM dd, yyyy");
            String date = format.format(newDate);
            userBirthday.setText(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        final EditText input;
        AlertDialog.Builder builder;
        LinearLayout.LayoutParams lp;
        int paddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_LEFT, getResources().getDisplayMetrics());
        int paddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_DEFAULT, getResources().getDisplayMetrics());
        int paddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_DEFAULT, getResources().getDisplayMetrics());
        int paddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_DEFAULT, getResources().getDisplayMetrics());
        switch (view.getId()) {
            case R.id.btn_edit_name:
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.edit_name_label));
                input = new EditText(getActivity().getApplicationContext());
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                input.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                builder.setView(input);
                input.setText(SessionManager.getLoggedUser(getActivity().getBaseContext()).getName());
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!SessionManager.getLoggedUser(getActivity().getBaseContext()).getName().toLowerCase().equalsIgnoreCase(input.getText().toString().toLowerCase())) {
                                    presenter.doEditName(SessionManager.getLoggedUser(getActivity().getBaseContext()).getUserId(), input.getText().toString());
                                } else {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
                break;
            case R.id.btn_edit_email:
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.edit_email_label));
                input = new EditText(getActivity().getApplicationContext());
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                input.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                builder.setView(input);
                input.setText(SessionManager.getLoggedUser(getActivity().getBaseContext()).getEmail());
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!SessionManager.getLoggedUser(getActivity().getBaseContext()).getEmail().toLowerCase().equalsIgnoreCase(input.getText().toString().toLowerCase())) {
                                    presenter.doEditEmail(SessionManager.getLoggedUser(getActivity().getBaseContext()).getUserId(), input.getText().toString());
                                } else {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
                break;
            case R.id.btn_edit_weight:
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.edit_weight_label));
                input = new EditText(getActivity().getApplicationContext());
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                input.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
                builder.setView(input);
                input.setText(SessionManager.getLoggedUser(getActivity().getBaseContext()).getWeight());
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!SessionManager.getLoggedUser(getActivity().getBaseContext()).getWeight().equals(input.getText().toString())) {
                                    presenter.doEditWeight(SessionManager.getLoggedUser(getActivity().getBaseContext()).getUserId(), Double.valueOf(input.getText().toString()));
                                } else {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
                break;
            case R.id.btn_edit_height:
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.edit_height_label));
                input = new EditText(getActivity().getApplicationContext());
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                input.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
                builder.setView(input);
                input.setText(SessionManager.getLoggedUser(getActivity().getBaseContext()).getHeight());
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!SessionManager.getLoggedUser(getActivity().getBaseContext()).getHeight().equals(input.getText().toString())) {
                                    presenter.doEditHeight(SessionManager.getLoggedUser(getActivity().getBaseContext()).getUserId(), Double.valueOf(input.getText().toString()));
                                } else {
                                    dialogInterface.cancel();
                                }
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
                break;
            case R.id.btn_edit_birthday:
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.DialogDatePickerTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        mYear = year;
                        mMonth = month + 1;
                        mDay = day;
                        updateBirthday(year, month + 1, day);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
                break;
            case R.id.btn_logout:
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.logout_hint));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                presenter.doLogout();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
                break;
        }
    }

    private void updateBirthday(int year, int month, int day) {
        StringBuilder newDate = new StringBuilder().append(year).append("-").append(month).append("-").append(day);
        if (!SessionManager.getLoggedUser(getActivity().getBaseContext()).getBirthday().equals(newDate)) {
            presenter.doEditBirthday(SessionManager.getLoggedUser(getActivity().getBaseContext()).getUserId(), String.valueOf(newDate));
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
    public void showLoginView() {
        subscription.clear();
        ((MainActivity) getActivity()).setLogout(true);
        SessionManager.logout(getActivity().getBaseContext());
        SessionManager.setFinishedWalkthrough(getActivity().getBaseContext(), true);
        Intent intent = new Intent(getActivity().getBaseContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
        getActivity().finishAffinity();
    }

    @Override
    public void showProgress() {
        progressDialog = ProgressDialog.show(getActivity(), "", "Updating...", true, false);
    }

    @Override
    public void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showSuccessMessage(String message, User user) {
        SessionManager.setLoggedUser(getActivity().getBaseContext(), user);
        setDataUser(user);

        Toast.makeText(getActivity().getBaseContext(), message, Toast.LENGTH_SHORT).show();
        userName.setText(user.getName());
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(getActivity().getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }
}
