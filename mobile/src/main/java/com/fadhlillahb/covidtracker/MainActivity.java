package com.fadhlillahb.covidtracker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener{

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final String DATA_PATH = "/data_path"; // Define a specific path for heart rate
    private static final String KEY_TEMP = "key_temp";
    private static final String KEY_SPO = "key_spo";
    private static final String KEY_HR = "key_hr";
    private static final String TAG = "MOBILE_MAIN";
    public EditText edtHeartRate, edtSPO, edtTemp, edtSystole, edtDiastole, edtRespRate;
    public TextView txStatus, txtSPO, txtTemp, txtHeartRate, txtSystole, txtDiastole, txtRespRate, txtTimestamp;
    public Button btnInsert, btnHistory, btnEmergency;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        edtHeartRate = findViewById(R.id.edtHeartRate);
        edtSPO = findViewById(R.id.edtSPO);
        edtTemp = findViewById(R.id.edtTemp);
        edtSystole = findViewById(R.id.edtSystole);
        edtDiastole = findViewById(R.id.edtDiastole);
        edtRespRate = findViewById(R.id.edtRespRate);
        txStatus = findViewById(R.id.txEmergencyStatus);
        btnInsert = findViewById(R.id.btnInsert);
        btnHistory = findViewById(R.id.btnHistory);
        btnEmergency = findViewById(R.id.btnEmergency);
        txtHeartRate = findViewById(R.id.txtLastHeartRate);
        txtSPO = findViewById(R.id.txtLastSPO);
        txtTemp = findViewById(R.id.txtLastTemp);
        txtSystole = findViewById(R.id.txtLastSystole);
        txtDiastole = findViewById(R.id.txtLastDiastole);
        txtRespRate = findViewById(R.id.txtLastRespRate);
        txtTimestamp = findViewById(R.id.txtTimestamp);


        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callEmergency();
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTohistory();
            }
        });

        checklastVital();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                if (Objects.equals(dataItem.getUri().getPath(), DATA_PATH)) { // Listen for HEART_RATE_PATH
                    DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();

                    float temp = dataMap.getFloat(KEY_TEMP); // Retrieve temperature data
                    int spo = dataMap.getInt(KEY_SPO); // Retrieve SPO2 data
                    int heartrate = dataMap.getInt(KEY_HR); // Retrieve heart rate data

                    Log.d("MOBILEDATACLIENT", "Received heart rate: " + heartrate + "bpm" + " Temperature: " + temp + "°C" + " SPO2: " + spo);

                    // Update the UI with the received heart rate
                    runOnUiThread(() -> edtHeartRate.setText(String.valueOf(heartrate)));
                    runOnUiThread(() -> edtSPO.setText(String.valueOf(spo)));
                    runOnUiThread(() -> edtTemp.setText(String.valueOf(temp)));
                }
            }
        }
    }

    public void performInsert(View view) {
        Map<String, Object> vitalSigns = new HashMap<>();
        vitalSigns.put("heartRate", edtHeartRate.getText().toString());
        vitalSigns.put("spo2", edtSPO.getText().toString());
        vitalSigns.put("temperature", edtTemp.getText().toString());
        vitalSigns.put("systole", edtSystole.getText().toString());
        vitalSigns.put("diastole", edtDiastole.getText().toString());
        vitalSigns.put("respRate", edtRespRate.getText().toString());
        vitalSigns.put("timestamp", FieldValue.serverTimestamp());

        db.collection("vitalSigns")
                .add(vitalSigns)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        edtHeartRate.setText("");
                        edtSPO.setText("");
                        edtTemp.setText("");
                        edtSystole.setText("");
                        edtDiastole.setText("");
                        edtRespRate.setText("");
                        Toast.makeText(MainActivity.this, "data added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(MainActivity.this, "Error adding vital signs, try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void moveTohistory() {
        final Intent intent = new Intent(this, HistoryActivity.class);
        Log.d("ACTIONCALLED", "history button clicked " + intent.getComponent());
        startActivity(intent);
    }

    public void callEmergency() {
        String emergencyNumber = "tel:119";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not already granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            // Start the call if permission is granted
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(emergencyNumber));
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callEmergency();
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checklastVital(){
        CollectionReference lastVital = db.collection("vitalSigns");

        lastVital.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().getDocuments().isEmpty()){
                DocumentSnapshot vitals = task.getResult().getDocuments().get(0);

                txtHeartRate.setText(String.valueOf(vitals.get("heartRate")));
                txtSPO.setText(String.valueOf(vitals.get("spo2")));
                txtTemp.setText(String.valueOf(vitals.get("temperature")));
                txtSystole.setText(String.valueOf(vitals.get("systole")));
                txtDiastole.setText(String.valueOf(vitals.get("diastole")));
                txtRespRate.setText(String.valueOf(vitals.get("respRate")));
                Date timestamp = vitals.getTimestamp("timestamp").toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                txtTimestamp.setText(sdf.format(timestamp));

                Log.d("LOADEDHISTORY", "id: " + vitals.getId() + ", timestamp: " + timestamp);
            } else {
                Log.d("LOADEDHISTORY", "Data not found");
                Toast.makeText(getApplicationContext(), "Data not found, Please insert data first", Toast.LENGTH_SHORT).show();
                txtHeartRate.setText("-");
                txtSPO.setText("-");
                txtTemp.setText("-");
                txtSystole.setText("-");
                txtDiastole.setText("-");
                txtRespRate.setText("-");
                txtTimestamp.setText("-");
            }
        });
    }

}
