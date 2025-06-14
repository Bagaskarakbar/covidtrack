package com.fadhlillahb.covidtracker.ui.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.Context;

import com.fadhlillahb.covidtracker.FirebaseCryptoHelper;
import com.fadhlillahb.covidtracker.ui.History.HistoryActivity;
import com.fadhlillahb.covidtracker.R;
import com.fadhlillahb.covidtracker.Session;
import com.fuzzylite.activation.General;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.TNorm;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Term;
import com.fuzzylite.term.Trapezoid;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
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
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fuzzylite.*;
import com.fadhlillahb.covidtracker.databinding.ActivityMainBinding;


public class MainActivity extends Fragment implements DataClient.OnDataChangedListener{

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final String DATA_PATH = "/data_path"; // Define a specific path for heart rate
    private static final String KEY_TEMP = "key_temp";
    private static final String KEY_SPO = "key_spo";
    private static final String KEY_HR = "key_hr";
    private static final String TAG = "MOBILE_MAIN";
    public EditText edtHeartRate, edtSPO, edtTemp, edtSystole, edtDiastole, edtRespRate;
    public TextView txStatus, txtSPO, txtTemp, txtHeartRate, txtSystole, txtDiastole, txtRespRate, txtTimestamp, txvalue;
    public Button btnInsert, btnEmergency;
    public RelativeLayout rlEmergency;
    public Context context;
    public Session session;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityMainBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = ActivityMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        edtHeartRate = binding.edtHeartRate;
        edtSPO = binding.edtSPO;
        edtTemp = binding.edtTemp;
        edtSystole = binding.edtSystole;
        edtDiastole = binding.edtDiastole;
        edtRespRate = binding.edtRespRate;
        txStatus = binding.txStatus;
        btnInsert = binding.btnInsert;
//        btnHistory = binding.btnHistory;
        btnEmergency = binding.btnEmergency;
        txtHeartRate = binding.txtLastHeartRate;
        txtSPO = binding.txtLastSPO;
        txtTemp = binding.txtLastTemp;
        txtSystole = binding.txtLastSystole;
        txtDiastole = binding.txtLastDiastole;
        txtRespRate = binding.txtLastRespRate;
        txtTimestamp = binding.txtTimestamp;
        txvalue = binding.txValue;
        rlEmergency = binding.warningLayout;

        context = getActivity().getBaseContext();

        session = new Session(context);

        String userid = session.getUserID();
        Log.d("USERIDCHECK", userid);

        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callEmergency();
            }
        });

//        btnHistory.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                moveTohistory();
//            }
//        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performInsert(v);
            }
        });

        checklastVital();

        return root;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Wearable.getDataClient(getActivity()).addListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Wearable.getDataClient(getActivity()).removeListener(this);
        binding = null;
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        String userid = session.getUserID();
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
                    getActivity().runOnUiThread(() -> edtHeartRate.setText(String.valueOf(heartrate)));
                    getActivity().runOnUiThread(() -> edtSPO.setText(String.valueOf(spo)));
                    getActivity().runOnUiThread(() -> edtTemp.setText(String.valueOf(temp)));
                }
            }
        }
    }

    public void performInsert(View view) {

        String hr = edtHeartRate.getText().toString();
        String spo = edtSPO.getText().toString();
        String temp = edtTemp.getText().toString();
        String systole = edtSystole.getText().toString();
        String diastole = edtDiastole.getText().toString();
        String respRate = edtRespRate.getText().toString();


        String userid = "1bVNU0sQ5zBAmD9o0XJV";
        if(hr.isEmpty() || edtSPO.getText().toString().isEmpty() || spo.isEmpty() || edtSystole.getText().toString().isEmpty() || temp.isEmpty() || systole.isEmpty() || diastole.isEmpty() || respRate.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
        } else if(Float.valueOf(hr) < 0 || Float.valueOf(hr) > 200 || Float.valueOf(spo) < 0 || Float.valueOf(spo) > 100 || Float.valueOf(temp) < 0 || Float.valueOf(temp) > 45 || Float.valueOf(systole) < 90 || Float.valueOf(systole) > 160 || Float.valueOf(diastole) < 0 || Float.valueOf(diastole) > 120 || Float.valueOf(respRate) < 0 || Float.valueOf(respRate) > 60){
            Toast.makeText(getActivity(), "Please fill the fields with correct values", Toast.LENGTH_SHORT).show();
        }else {
            Map<String, Object> vitalSigns = new HashMap<>();
            try {
                FirebaseCryptoHelper crypto = new FirebaseCryptoHelper();
                vitalSigns.put("heartRate", crypto.encrypt(edtHeartRate.getText().toString()));
                vitalSigns.put("spo2", crypto.encrypt(edtSPO.getText().toString()));
                vitalSigns.put("temperature", crypto.encrypt(edtTemp.getText().toString()));
                vitalSigns.put("systole", crypto.encrypt(edtSystole.getText().toString()));
                vitalSigns.put("diastole", crypto.encrypt(edtDiastole.getText().toString()));
                vitalSigns.put("respRate", crypto.encrypt(edtRespRate.getText().toString()));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Encryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

//            vitalSigns.put("heartRate", edtHeartRate.getText().toString());
//            vitalSigns.put("spo2", edtSPO.getText().toString());
//            vitalSigns.put("temperature", edtTemp.getText().toString());
//            vitalSigns.put("systole", edtSystole.getText().toString());
//            vitalSigns.put("diastole", edtDiastole.getText().toString());
//            vitalSigns.put("respRate", edtRespRate.getText().toString());
            vitalSigns.put("userId", userid);
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
                            Toast.makeText(getActivity(), "data added successfully", Toast.LENGTH_SHORT).show();


                            getActivity().getSupportFragmentManager().beginTransaction().detach(MainActivity.this).attach(MainActivity.this).commit();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(getActivity(), "Error adding vital signs, try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void moveTohistory() {
        final Intent intent = new Intent(getActivity(), HistoryActivity.class);
        Log.d("ACTIONCALLED", "history button clicked " + intent.getComponent());
        startActivity(intent);
    }

    public void callEmergency() {
        String emergencyNumber = "tel:119";

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not already granted
            ActivityCompat.requestPermissions(getActivity(),
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
                Toast.makeText(getActivity(), "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checklastVital() {
        CollectionReference lastVital = db.collection("vitalSigns");
        String userid = session.getUserID();

        lastVital.orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().getDocuments().isEmpty()) {
                DocumentSnapshot vitals = task.getResult().getDocuments().get(0);

                try {
                    FirebaseCryptoHelper crypto = new FirebaseCryptoHelper();

                    String decryptedHeartRate = crypto.decrypt(String.valueOf(vitals.get("heartRate")));
                    String decryptedSPO2 = crypto.decrypt(String.valueOf(vitals.get("spo2")));
                    String decryptedTemp = crypto.decrypt(String.valueOf(vitals.get("temperature")));
                    String decryptedSystole = crypto.decrypt(String.valueOf(vitals.get("systole")));
                    String decryptedDiastole = crypto.decrypt(String.valueOf(vitals.get("diastole")));
                    String decryptedRespRate = crypto.decrypt(String.valueOf(vitals.get("respRate")));

                    txtHeartRate.setText(decryptedHeartRate);
                    txtSPO.setText(decryptedSPO2);
                    txtTemp.setText(decryptedTemp);
                    txtSystole.setText(decryptedSystole);
                    txtDiastole.setText(decryptedDiastole);
                    txtRespRate.setText(decryptedRespRate);

                    checkfuzzy(
                            Float.parseFloat(decryptedHeartRate),
                            Float.parseFloat(decryptedSPO2),
                            Float.parseFloat(decryptedTemp),
                            Float.parseFloat(decryptedSystole),
                            Float.parseFloat(decryptedDiastole),
                            Float.parseFloat(decryptedRespRate)
                    );

                    Date timestamp = vitals.getTimestamp("timestamp").toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    txtTimestamp.setText(sdf.format(timestamp));

                    Log.d("LOADEDHISTORY", "id: " + vitals.getId() + ", timestamp: " + timestamp);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Decryption error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.d("LOADEDHISTORY", "Data not found");
                Toast.makeText(getActivity(), "Data not found, Please insert data first", Toast.LENGTH_SHORT).show();
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

    public void checkfuzzy(float heartrate, float spo2, float temperature, float systole, float diastole, float respRate) {
        Log.d("FUZZYINPUTS", String.valueOf("heart_rate: " + heartrate + ", spo2: " + spo2 + ", temperature: " + temperature + ", systole: " + systole + ", diastole: " + diastole + ", resp_rate: " + respRate));

        Engine prediction = new Engine();
        prediction.setName("covid_indentification");

        InputVariable SPO2 = new InputVariable();
        SPO2.setName("spo2");
        SPO2.setEnabled(true);
        SPO2.setRange(80, 100);
        SPO2.setLockValueInRange(false);
        SPO2.addTerm(new Trapezoid("low",80,80,90,92));
        SPO2.addTerm(new Trapezoid("medium",60,70,80,100));
        SPO2.addTerm(new Trapezoid("high",90,110,180,180));
        prediction.addInputVariable(SPO2);

        InputVariable heartRate = new InputVariable();
        heartRate.setName("heart_rate");
        heartRate.setEnabled(true);
        heartRate.setRange(40, 180);
        heartRate.setLockValueInRange(false);
        heartRate.addTerm(new Trapezoid("low",40,40,50,60));
        heartRate.addTerm(new Trapezoid("medium",60,70,80,100));
        heartRate.addTerm(new Trapezoid("high",90,110,180,180));
        prediction.addInputVariable(heartRate);

        InputVariable bodyTemp = new InputVariable();
        bodyTemp.setName("body_temp");
        bodyTemp.setEnabled(true);
        bodyTemp.setRange(34, 41);
        bodyTemp.setLockValueInRange(false);
        bodyTemp.addTerm(new Trapezoid("low",34,34,37.2,37.5));
        bodyTemp.addTerm(new Trapezoid("medium",37.3,37.5,38.5,39));
        bodyTemp.addTerm(new Trapezoid("high",38.8,39.2,41,41));
        prediction.addInputVariable(bodyTemp);

        InputVariable Systole = new InputVariable();
        Systole.setName("systole");
        Systole.setEnabled(true);
        Systole.setRange(60, 160);
        Systole.setLockValueInRange(false);
        Systole.addTerm(new Trapezoid("low",60,60,80,90));
        Systole.addTerm(new Trapezoid("medium",80,100,120,130));
        Systole.addTerm(new Trapezoid("high",120,140,180,180));
        prediction.addInputVariable(Systole);

        InputVariable Diastole = new InputVariable();
        Diastole.setName("diastole");
        Diastole.setEnabled(true);
        Diastole.setRange(40, 110);
        Diastole.setLockValueInRange(false);
        Diastole.addTerm(new Trapezoid("low",40,40,50,60));
        Diastole.addTerm(new Trapezoid("medium",55,65,80,85));
        Diastole.addTerm(new Trapezoid("high",80,90,110,110));
        prediction.addInputVariable(Diastole);

        InputVariable respirationRate = new InputVariable();
        respirationRate.setName("respiration_rate");
        respirationRate.setEnabled(true);
        respirationRate.setRange(10, 40);
        respirationRate.setLockValueInRange(false);
        respirationRate.addTerm(new Trapezoid("low",10,10,12,14));
        respirationRate.addTerm(new Trapezoid("medium",12,16,20,24));
        respirationRate.addTerm(new Trapezoid("high",20,25,40,40));
        prediction.addInputVariable(respirationRate);

        OutputVariable emergency_prediction = new OutputVariable();
        emergency_prediction.setName("emergency_prediction");
        emergency_prediction.setEnabled(true);
        emergency_prediction.setRange(0, 1);
        emergency_prediction.setLockValueInRange(false);
        emergency_prediction.setLockPreviousValue(false);
        emergency_prediction.setAggregation(new Maximum());
        emergency_prediction.setDefuzzifier(new Centroid(100));
        emergency_prediction.setDefaultValue(Double.NaN);
        emergency_prediction.addTerm(new Trapezoid("healthy",0,0,0.2,0.3));
        emergency_prediction.addTerm(new Trapezoid("emergency",0.2,0.3,0.6,0.7));
        emergency_prediction.addTerm(new Trapezoid("icu",0.6,0.7,1,1));
        prediction.addOutputVariable(emergency_prediction);

        RuleBlock mamdani = new RuleBlock();
        mamdani.setName("mamdani");
        mamdani.setEnabled(true);
        mamdani.setConjunction(new TNorm() {
            @Override
            public double compute(double v, double v1) {
                return Math.min(v,v1);
            }
        });
        mamdani.setImplication(new AlgebraicProduct());
        mamdani.setActivation(new General());
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is high and systole is low and diastole is low and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is medium and systole is high and diastole is high and respiration_rate is high then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is medium and systole is low and diastole is low and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is high and systole is medium and diastole is medium and respiration_rate is low then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is high and systole is high and diastole is high and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is medium and systole is high and diastole is high and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is low and systole is medium and diastole is medium and respiration_rate is high then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is high and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is medium and systole is medium and diastole is low and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is low and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is high and systole is high and diastole is high and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is medium and systole is medium and diastole is medium and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is high and body_temp is low and systole is high and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is low and systole is medium and diastole is high and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is medium and systole is high and diastole is high and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is low and systole is low and diastole is medium and respiration_rate is low then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is high and systole is high and diastole is high and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is high and body_temp is medium and systole is medium and diastole is high and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is high and body_temp is low and systole is high and diastole is high and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is high and systole is high and diastole is medium and respiration_rate is low then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is medium and systole is medium and diastole is medium and respiration_rate is high then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is high and systole is medium and diastole is medium and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is high and body_temp is high and systole is high and diastole is high and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is high and body_temp is high and systole is low and diastole is low and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is high and body_temp is high and systole is medium and diastole is medium and respiration_rate is high then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is high and body_temp is medium and systole is medium and diastole is medium and respiration_rate is high then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is medium and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is high and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is high and body_temp is low and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is high and body_temp is medium and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is high and body_temp is high and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is low and body_temp is low and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is high and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is low and body_temp is high and systole is low and diastole is low and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is low and body_temp is high and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is low and body_temp is high and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is low and body_temp is high and systole is high and diastole is high and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is low and body_temp is high and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is low and body_temp is high and systole is high and diastole is high and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is high and body_temp is medium and systole is high and diastole is high and respiration_rate is high then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is low and systole is high and diastole is high and respiration_rate is high then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is low and systole is high and diastole is medium and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is high and systole is high and diastole is medium and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is low and systole is high and diastole is high and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is high and body_temp is high and systole is medium and diastole is high and respiration_rate is high then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is high and systole is high and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is high and systole is high and diastole is high and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is high and systole is high and diastole is medium and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is high and systole is high and diastole is medium and respiration_rate is low then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is high and systole is high and diastole is medium and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is medium and body_temp is low and systole is high and diastole is high and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is low and systole is medium and diastole is low and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is low and systole is medium and diastole is low and respiration_rate is medium then emergency_prediction is emergency",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is low and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is low and systole is medium and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is medium and heart_rate is high and body_temp is high and systole is high and diastole is medium and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is high and body_temp is high and systole is high and diastole is medium and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is high and systole is medium and diastole is high and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is low and systole is high and diastole is high and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is high and heart_rate is medium and body_temp is low and systole is high and diastole is medium and respiration_rate is medium then emergency_prediction is healthy",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is high and systole is high and diastole is medium and respiration_rate is high then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is high and systole is medium and diastole is low and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is high and systole is medium and diastole is low and respiration_rate is medium then emergency_prediction is icu",prediction));
        mamdani.addRule(Rule.parse("if spo2 is low and heart_rate is medium and body_temp is high and systole is medium and diastole is low and respiration_rate is medium then emergency_prediction is icu",prediction));
        prediction.addRuleBlock(mamdani);

        StringBuilder status = new StringBuilder();
        if (! prediction.isReady(status))
            throw new RuntimeException("[engine error] engine is not ready:\n" + status);

        InputVariable oxygen = prediction.getInputVariable("spo2");
        InputVariable heart = prediction.getInputVariable("heart_rate");
        InputVariable body = prediction.getInputVariable("body_temp");
        InputVariable syst = prediction.getInputVariable("systole");
        InputVariable dias = prediction.getInputVariable("diastole");
        InputVariable respr = prediction.getInputVariable("respiration_rate");
        OutputVariable emergency = prediction.getOutputVariable("emergency_prediction");

        oxygen.setValue(spo2);
        heart.setValue(heartrate);
        body.setValue(temperature);
        syst.setValue(systole);
        dias.setValue(diastole);
        respr.setValue(respRate);

        prediction.process();

        double predictionValue = emergency.getValue();
        Log.d(TAG, "checkfuzzy: " + String.valueOf(predictionValue));

        String bestTermName = null;


        double maxDegree = 0.0;

        for (Term term : emergency.getTerms()) {
            double degree = term.membership(predictionValue);

            if (degree > maxDegree) {
                maxDegree = degree;
                bestTermName = term.getName();
            }
        }

        System.out.println("Best Term for : " + bestTermName);

        if (bestTermName != null) {
            switch (bestTermName){
                case "healthy":
                    txStatus.setText(R.string.cov19_status_healthy);
                    txvalue.setText("fuzzy value : " + String.format("%.2f", predictionValue));
                    rlEmergency.setBackgroundResource(R.drawable.rounded_ok);
                    break;
                case "icu":
                    txStatus.setText(R.string.cov19_status_EMERGENCY);
                    txvalue.setText("fuzzy value : " + String.format("%.2f", predictionValue));
                    rlEmergency.setBackgroundResource(R.drawable.rounded_emergency);
                    break;
                case "emergency":
                    txStatus.setText(R.string.cov19_status_warning);
                    txvalue.setText("fuzzy value : " + String.format("%.2f", predictionValue));
                    rlEmergency.setBackgroundResource(R.drawable.rounded_warning);
                    break;
                default:
                break;
            }
        }
    }
    }
