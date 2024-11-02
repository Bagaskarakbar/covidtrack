package com.fadhlillahb.covidtracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryDetail extends AppCompatActivity {

    EditText edtHeartRate, edtSPO, edtTemp, edtSystole, edtDiastole, edtRespRate, edtTimestamp;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference vitalSigns = db.collection("vitalSigns");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history_detail);

        edtHeartRate = findViewById(R.id.edtHeartRate);
        edtSPO = findViewById(R.id.edtSPO);
        edtTemp = findViewById(R.id.edtTemp);
        edtSystole = findViewById(R.id.edtSystole);
        edtDiastole = findViewById(R.id.edtDiastole);
        edtRespRate = findViewById(R.id.edtRespRate);
        edtTimestamp = findViewById(R.id.edtTimestamp);

        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");

        if (id != null){
            DocumentReference documentReference = vitalSigns.document(id);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            edtHeartRate.setText(document.getString("heartRate"));
                            edtSPO.setText(document.getString("spo2"));
                            edtTemp.setText(document.getString("temperature"));
                            edtSystole.setText(document.getString("systole"));
                            edtDiastole.setText(document.getString("diastole"));
                            edtRespRate.setText(document.getString("respRate"));
                            Date timestamp = document.getTimestamp("timestamp").toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            edtTimestamp.setText(sdf.format(timestamp));

                            Log.d("LOADEDHISTORY", "id: " + id + ", timestamp: " + timestamp);
                        } else {
                            Toast.makeText(getApplicationContext(), "Data not found", Toast.LENGTH_SHORT).show();
                            Log.d("LOADEDHISTORY", "Data not found");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error getting data", Toast.LENGTH_SHORT).show();
                        Log.d("LOADEDHISTORY", "Error getting documents: ", task.getException());
                    }
                }
            });
        }

    }


}
