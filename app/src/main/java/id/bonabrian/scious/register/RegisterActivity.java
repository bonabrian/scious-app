package id.bonabrian.scious.register;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.app.SciousApplication;
import id.bonabrian.scious.main.MainActivity;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, RegisterContract.View {

    @BindView(R.id.scrollview_layout_register)
    ScrollView scrollViewLayout;

    @BindView(R.id.input_name)
    EditText inputName;

    @BindView(R.id.input_email)
    EditText inputEmail;

    @BindView(R.id.input_password)
    EditText inputPassword;

    @BindView(R.id.input_confirm_password)
    EditText inputConfirmPassword;

    @BindView(R.id.input_weight)
    EditText inputWeight;

    @BindView(R.id.input_height)
    EditText inputHeight;

    @BindView(R.id.input_birthday)
    EditText inputBirthday;

    @BindView(R.id.btn_register)
    Button btnRegister;

    @BindView(R.id.action_login)
    TextView linkLogin;

    private RegisterContract.Presenter presenter;
    private ProgressDialog progressDialog;
    private DatePicker datePicker;
    private Calendar calendar;
    private int mYear = 0, mMonth = 0, mDay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        presenter = new RegisterPresenter();
        onAttachView();
        prepareDataFromIntent();
        initDate();

        inputBirthday.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        linkLogin.setOnClickListener(this);
    }

    private void prepareDataFromIntent() {
        if (getIntent().getExtras() != null) {
            if (!getIntent().getExtras().getString("register-email").toString().isEmpty()) {
                inputEmail.setText(getIntent().getExtras().getString("register-email").toString());
            }
            if (!getIntent().getExtras().getString("register-name").toString().isEmpty()) {
                inputName.setText(getIntent().getExtras().getString("register-name").toString());
            }
        }
    }

    private void initDate() {
        final Calendar cal = Calendar.getInstance();
        if (mYear == 0) mYear = cal.get(Calendar.YEAR);
        if (mMonth == 0) mMonth = cal.get(Calendar.MONTH);
        if (mDay == 0) mDay = cal.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.input_birthday:
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DialogDatePickerTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        mYear = year;
                        mMonth = month + 1;
                        mDay = day;
                        showDate(year, month + 1, day);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
                break;
            case R.id.btn_register:
                presenter.registerUser(
                        inputName.getText().toString(),
                        inputEmail.getText().toString(),
                        inputPassword.getText().toString(),
                        inputConfirmPassword.getText().toString(),
                        inputWeight.getText().toString(),
                        inputHeight.getText().toString(),
                        inputBirthday.getText().toString()
                );
                break;
            case R.id.action_login:
                showLoginView();
                break;
        }
    }

    private void showDate(int year, int month, int day) {
        inputBirthday.setText(new StringBuilder().append(year).append("-").append(month).append("-").append(day));
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
    public void showErrorMessage(String message) {
        Snackbar.make(scrollViewLayout, message, 10000)
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        return;
                    }
                })
                .show();
    }

    @Override
    public void showLoginView() {
        finish();
    }

    @Override
    public void showSuccessMessage(String message) {
        AlertDialog.Builder builder;
        if (SciousApplication.isRunningLollipopOrLater()) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle("Success")
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void showProgress() {
        progressDialog = ProgressDialog.show(RegisterActivity.this, "", "Processing", true, false);
    }

    @Override
    public void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showMainView() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showRegisterForm(String email, String name) {
        inputEmail.setText(email);
        inputName.setText(name);
    }
}
