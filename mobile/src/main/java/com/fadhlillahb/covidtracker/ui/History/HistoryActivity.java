package com.fadhlillahb.covidtracker.ui.History;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fadhlillahb.covidtracker.R;
import com.fadhlillahb.covidtracker.Session;
import com.fadhlillahb.covidtracker.VitalSignModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.fadhlillahb.covidtracker.databinding.ActivityHistoryBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryActivity extends Fragment {

    private ActivityHistoryBinding binding;

    private static final String TAG = "HistoryActivity";
    private Session session;

    private RecyclerView rvHistory;
    private ListHistoryAdapter historyAdapter;
    private List<VitalSignModel> vitalSignModelList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference vitalSigns = db.collection("vitalSigns");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = ActivityHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        rvHistory = binding.rvHistory;
        rvHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        historyAdapter = new ListHistoryAdapter(vitalSignModelList);
        rvHistory.setAdapter(historyAdapter);
        session = new Session(getActivity());

        loadData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadData() {
        String userID = session.getUserID();
        Log.d("VIEWTRIGGERED", "LOADING DATA HISTORY RV");
//        vitalSigns.orderBy("timestamp", Query.Direction.DESCENDING).whereEqualTo("userid",userID).get().addOnCompleteListener(task -> {
        vitalSigns.orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
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
               rvHistory.addOnItemTouchListener(new ListHistoryAdapter.RecylerTouchListener(getActivity().getApplicationContext(), rvHistory, new ListHistoryAdapter.ClickListener() {
                   @Override
                   public void onItemClick(int position, View v) {
                       String id = vitalSignModelList.get(position).getId();
                       Intent intent = new Intent(getActivity(), HistoryDetail.class);
                       intent.putExtra("ID", id);
                       startActivity(intent);
                   }

                   @Override
                   public void onItemLongClick(int position, View v) {
                       String id = vitalSignModelList.get(position).getId();
                       Intent intent = new Intent(getActivity(), HistoryDetail.class);
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
