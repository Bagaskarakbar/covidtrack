package com.fadhlillahb.covidtracker.ui.History;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.fadhlillahb.covidtracker.FirebaseCryptoHelper;
import com.fadhlillahb.covidtracker.R;
import com.fadhlillahb.covidtracker.Session;
import com.fuzzylite.Engine;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    EditText edtHeartRate, edtSPO, edtTemp, edtSystole, edtDiastole, edtRespRate, edtTimestamp, edtFuzzyValue, edtFuzzy;
    private static final String TAG = "HISTORY_DETAIL";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference vitalSigns = db.collection("vitalSigns");
    Session session;

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
        edtFuzzyValue = findViewById(R.id.edtFuzzyValue);
        edtFuzzy = findViewById(R.id.edtFuzzy);
        session = new Session(getApplicationContext());

        Intent intent = getIntent();
        String id = intent.getStringExtra("ID");

        if (id != null) {
            DocumentReference documentReference = vitalSigns.document(id);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                FirebaseCryptoHelper crypto = new FirebaseCryptoHelper();

                                String heartRate = crypto.decrypt(document.getString("heartRate"));
                                String spo2 = crypto.decrypt(document.getString("spo2"));
                                String temperature = crypto.decrypt(document.getString("temperature"));
                                String systole = crypto.decrypt(document.getString("systole"));
                                String diastole = crypto.decrypt(document.getString("diastole"));
                                String respRate = crypto.decrypt(document.getString("respRate"));

                                edtHeartRate.setText(heartRate);
                                edtSPO.setText(spo2);
                                edtTemp.setText(temperature);
                                edtSystole.setText(systole);
                                edtDiastole.setText(diastole);
                                edtRespRate.setText(respRate);

                                float hr = Float.parseFloat(heartRate);
                                float sp = Float.parseFloat(spo2);
                                float temp = Float.parseFloat(temperature);
                                float sys = Float.parseFloat(systole);
                                float dias = Float.parseFloat(diastole);
                                float resp = Float.parseFloat(respRate);

                                checkfuzzy(hr, sp, temp, sys, dias, resp);

                                Date timestamp = document.getTimestamp("timestamp").toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                edtTimestamp.setText(sdf.format(timestamp));

                                Log.d(TAG, "Decrypted & Loaded ID: " + id + ", timestamp: " + timestamp);

                            } catch (Exception e) {
                                Log.e(TAG, "Decryption error", e);
                                Toast.makeText(getApplicationContext(), "Decryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Data not found", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Data not found");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error getting data", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting documents", task.getException());
                    }
                }
            });
        }
    }

    public void checkfuzzy(float heartrate, float spo2, float temperature, float systole, float diastole, float respRate) {
        Log.d("FUZZYINPUTSONHISTORY", String.valueOf("heart_rate: " + heartrate + ", spo2: " + spo2 + ", temperature: " + temperature + ", systole: " + systole + ", diastole: " + diastole + ", resp_rate: " + respRate));

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
                    edtFuzzy.setText(R.string.cov19_status_healthy);
                    edtFuzzyValue.setText(String.format("%.2f", predictionValue));
                    break;
                case "icu":
                    edtFuzzy.setText(R.string.cov19_status_EMERGENCY);
                    edtFuzzyValue.setText(String.format("%.2f", predictionValue));
                    break;
                case "emergency":
                    edtFuzzy.setText(R.string.cov19_status_warning);
                    edtFuzzyValue.setText(String.format("%.2f", predictionValue));
                    break;
                default:
                    break;
            }
        }
    }
}

