package com.app.fitness24;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.app.fitness24.models.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

    RadioGroup radioGroup;
    RadioButton rbMetric, rbImperial;
    EditText etHeight, etWeight;
    Button button;

    boolean isMetric = true;
    boolean isEditMode = false;
    double height = 0.0, weight = 0.0;
    String TAG = "theH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        radioGroup = findViewById(R.id.rg_system);
        rbMetric = findViewById(R.id.rb_metric);
        rbImperial = findViewById(R.id.rb_imperial);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        button = findViewById(R.id.btn);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("profiles").child(mAuth.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Profile profile = dataSnapshot.getValue(Profile.class);
                if (profile != null) {
                    isMetric = profile.isMetric();
                    height = profile.getHeight();
                    weight = profile.getWeight();
                    etHeight.setText(String.format("%.02f",height));
                    etWeight.setText(String.format("%.02f",weight));
                    isEditMode();
                    Log.d(TAG, "Value is: " + profile.toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: before isEditMode: "+isEditMode);

                if (isEditMode) {
                    if (!TextUtils.isEmpty(etHeight.getText()) && !TextUtils.isEmpty(etWeight.getText())) {
                        height = Double.parseDouble(etHeight.getText().toString());
                        weight = Double.parseDouble(etWeight.getText().toString());
                        myRef.setValue(new Profile(isMetric, height, weight));
                    }else {
                        Toast.makeText(getApplicationContext(), "Provide height and weight.", Toast.LENGTH_SHORT).show();
                    }

                }

                isEditMode = !isEditMode;
                isEditMode();
                Log.d(TAG, "onClick: after isEditMode: " + isEditMode);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!TextUtils.isEmpty(etHeight.getText()) && !TextUtils.isEmpty(etWeight.getText())) {
                    height = Double.parseDouble(etHeight.getText().toString());
                    weight = Double.parseDouble(etWeight.getText().toString());
                    if (checkedId == R.id.rb_metric) {
                        isMetric = true;
                        etHeight.setText(String.format("%.02f",convertKgToPounds(height)));
                        etWeight.setText(String.format("%.02f",convertMeterToInches(weight)));
                    } else {
                        isMetric = false;
                        etHeight.setText(String.format("%.02f",convertPoundsToKg(height)));
                        etWeight.setText(String.format("%.02f",convertInchesToMeter(weight)));
                    }
                }else {
                    if (checkedId == R.id.rb_metric) {
                        isMetric = true;
                        etHeight.setHint("Enter height in meters.");
                        etWeight.setHint("Enter weight in kilograms.");
                    } else {
                        isMetric = false;
                        etHeight.setHint("Enter height in inches.");
                        etWeight.setHint("Enter weight in pounds.");
                    }
                }
            }
        });


    }

    public void isEditMode() {
        rbMetric.setChecked(isMetric);
        rbImperial.setChecked(!isMetric);

        rbMetric.setClickable(isEditMode);
        rbImperial.setClickable(isEditMode);

        etHeight.setEnabled(isEditMode);
        etWeight.setEnabled(isEditMode);
        rbMetric.setEnabled(isEditMode);
        rbImperial.setEnabled(isEditMode);

        if (isEditMode) {
            button.setText("Save");
        } else {
            button.setText("Edit");
        }
    }

    public double convertKgToPounds(double v) {
        return v * 2.205;
    }

    public double convertPoundsToKg(double v) {
        return v / 2.205;
    }

    public double convertMeterToInches(double v) {
        return v * 39.37;
    }

    public double convertInchesToMeter(double v) {
        return v / 39.37;
    }
}