package com.fadhlillahb.covidtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fadhlillahb.covidtracker.ui.home.MainActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    Button btnRegister, btnLogin;
    EditText edtEmail, edtPassword;

    Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Session session;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        session = new Session(context);
        session.clear();

        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> performLogin());
        btnRegister.setOnClickListener(v -> goRegister());
    }

    private void goRegister() {
        Intent intent = new Intent(context, RegisterActivity.class);
        startActivity(intent);
    }

    public void performLogin(){
        CollectionReference lastVital = db.collection("Users");
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        lastVital.whereEqualTo("email", email).whereEqualTo("password", encodeToMD5(password)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().getDocuments().isEmpty()){
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                String userID = document.getId();
                Log.d("USERRIGHTHERE", userID);
                session.setUserID(userID);

                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, primaryMenu.class);
                startActivity(intent);
            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String encodeToMD5(String input) {
        try {
            // Create MD5 Hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();

            // Convert byte array to Hexadecimal String
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Pad with leading zero
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
