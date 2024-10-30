package com.fadhlillahb.covidtracker;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener{


    private static final String HEART_RATE_PATH = "/heart_rate"; // Use the same path for heart rate
    private static final String KEY_HEART_RATE = "key_heart_rate";
    TextView testtext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        testtext = findViewById(R.id.test);
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
                if (dataItem.getUri().getPath().equals(HEART_RATE_PATH)) { // Listen for HEART_RATE_PATH
                    DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                    int heartRate = dataMap.getInt(KEY_HEART_RATE); // Retrieve heart rate data
                    Log.d("MobileMainActivity", "Received heart rate: " + heartRate);

                    // Update the UI with the received heart rate
                    runOnUiThread(() -> testtext.setText("Heart Rate: " + heartRate));
                }
            }
        }
    }
}
