package com.fadhlillahb.covidtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    private RecyclerView rvHistory;
    private ListHistoryAdapter historyAdapter;
    private List<VitalSignModel> vitalSignModelList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference vitalSigns = db.collection("vitalSigns");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("VIEWTRIGGERED", "HISTORY ACTIVITY");

        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new ListHistoryAdapter(vitalSignModelList);
        rvHistory.setAdapter(historyAdapter);

        loadData();
    }

    private void loadData() {
        Log.d("VIEWTRIGGERED", "LOADING DATA HISTORY RV");
        vitalSigns.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               vitalSignModelList.clear();
               for (QueryDocumentSnapshot document : task.getResult()) {
                   String id = document.getId();
                   Timestamp timestamp = document.getTimestamp("timestamp");
                   Date formattedtime  = null;
                   if (timestamp != null) {
                       formattedtime = timestamp.toDate();
                   }
                   vitalSignModelList.add(new VitalSignModel(id, formattedtime));
                   Log.d("LOADEDHISTORY", "id: " + id + ", timestamp: " + formattedtime);
               }
               historyAdapter.notifyDataSetChanged();
               rvHistory.addOnItemTouchListener(new ListHistoryAdapter.RecylerTouchListener(getApplicationContext(), rvHistory, new ListHistoryAdapter.ClickListener() {
                   @Override
                   public void onItemClick(int position, View v) {
                       String id = vitalSignModelList.get(position).getId();
                       Intent intent = new Intent(getApplicationContext(), HistoryDetail.class);
                       intent.putExtra("ID", id);
                       startActivity(intent);
                   }

                   @Override
                   public void onItemLongClick(int position, View v) {
                       String id = vitalSignModelList.get(position).getId();
                       Intent intent = new Intent(getApplicationContext(), HistoryDetail.class);
                       intent.putExtra("ID", id);
                       startActivity(intent);
                   }
               }));


           } else {
               Log.d(TAG, "Error getting documents: ", task.getException());
           }
        });
    }


}
