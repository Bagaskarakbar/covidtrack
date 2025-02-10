package com.fadhlillahb.covidtracker;

import android.os.Bundle;
import android.content.Context;
import android.os.PersistableBundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.datepicker.MaterialDatePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtUsername, edtDoB;
    Button btnRegister;
    RadioGroup rgGender;
    Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtDoB = findViewById(R.id.edtDoB);
        btnRegister = findViewById(R.id.btnRegister);
        rgGender = findViewById(R.id.rgGender);
        btnRegister.setOnClickListener(v -> performRegister());
        edtDoB.setOnClickListener(v -> showMaterialDatePicker());
    }

    public void performRegister(){
        final String email = edtEmail.getText().toString();
        final String password = edtPassword.getText().toString();
        final String username = edtUsername.getText().toString();
        final String dob = edtDoB.getText().toString();
    }

    private void showMaterialDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            edtDoB.setText(datePicker.getHeaderText());
        });

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }
}
