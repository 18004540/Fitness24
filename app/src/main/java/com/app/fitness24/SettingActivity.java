package com.app.fitness24;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.app.fitness24.models.Setting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

    RadioGroup radioGroup;
    RadioButton rbMetric, rbImperial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        radioGroup = findViewById(R.id.rg_system);
        rbMetric = findViewById(R.id.rb_metric);
        rbImperial = findViewById(R.id.rb_imperial);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("setting").child(mAuth.getUid());

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_metric) {
                    myRef.setValue(new Setting(true));
                } else {
                    myRef.setValue(new Setting(false));
                }
            }
        });


    }
}