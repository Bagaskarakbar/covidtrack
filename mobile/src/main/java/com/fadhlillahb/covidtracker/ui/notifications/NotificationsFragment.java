package com.fadhlillahb.covidtracker.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fadhlillahb.covidtracker.FirebaseCryptoHelper;
import com.fadhlillahb.covidtracker.R;
import com.fadhlillahb.covidtracker.databinding.FragmentHelpBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class NotificationsFragment extends Fragment {

    private FragmentHelpBinding binding;
    private Button btnMigrate;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        btnMigrate = root.findViewById(R.id.btnMigrate);
//
//        btnMigrate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                performMigrate();
//            }
//
//            private void performMigrate() {
//                FirebaseFirestore db = FirebaseFirestore.getInstance();
//                FirebaseCryptoHelper crypto = null;
//                try {
//                    crypto = new FirebaseCryptoHelper();
//                    CollectionReference vitalSigns = db.collection("vitalSigns");
//                    FirebaseCryptoHelper finalCrypto = crypto;
//                    vitalSigns.get().addOnSuccessListener(querySnapshot -> {
//                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                            try {
//                                // Skip already-encrypted entries (basic heuristic: Base64 strings are long and look encoded)
//                                String heartRate = String.valueOf(doc.get("heartRate"));
//                                if (heartRate.length() > 20) continue;
//
//                                Map<String, Object> encrypted = new HashMap<>();
//                                encrypted.put("heartRate", finalCrypto.encrypt(heartRate));
//                                encrypted.put("spo2", finalCrypto.encrypt(String.valueOf(doc.get("spo2"))));
//                                encrypted.put("temperature", finalCrypto.encrypt(String.valueOf(doc.get("temperature"))));
//                                encrypted.put("systole", finalCrypto.encrypt(String.valueOf(doc.get("systole"))));
//                                encrypted.put("diastole", finalCrypto.encrypt(String.valueOf(doc.get("diastole"))));
//                                encrypted.put("respRate", finalCrypto.encrypt(String.valueOf(doc.get("respRate"))));
//
//                                doc.getReference().update(encrypted).addOnSuccessListener(v -> {
//                                    Log.d("MIGRATION", "Updated " + doc.getId());
//                                });
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}