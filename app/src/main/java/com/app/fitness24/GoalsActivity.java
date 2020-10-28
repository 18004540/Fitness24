package com.app.fitness24;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.fitness24.adapters.MealAdapter;
import com.app.fitness24.models.Goals;
import com.app.fitness24.models.Meal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GoalsActivity extends AppCompatActivity {

    FloatingActionButton actionButton;
    TextView tvDate, tvWeight, tvCalorie;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String TAG = "theH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        tvDate = findViewById(R.id.tv_date);
        tvWeight = findViewById(R.id.tv_weight);
        tvCalorie = findViewById(R.id.tv_calorie);
        actionButton = findViewById(R.id.fab);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("goals").child(mAuth.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot.getChildrenCount());
                Log.d(TAG, "onDataChange: "+dataSnapshot.getValue());
                if (dataSnapshot.exists()){
                    Goals goals = dataSnapshot.getValue(Goals.class);
                    if (goals != null) {
                        tvDate.setText(goals.getDate());
                        tvWeight.setText(goals.getWeight());
                        tvCalorie.setText(goals.getCalorie());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog();
            }
        });
    }

    public void dialog() {
        final Dialog dialog = new Dialog(GoalsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_goals_dialog);

        final EditText etWeight = dialog.findViewById(R.id.et_weight);
        final EditText etCalorie = dialog.findViewById(R.id.et_calorie);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weight = etWeight.getText().toString();
                String calorie = etCalorie.getText().toString();
                myRef.setValue(new Goals(getCurrentDate(), weight, calorie));
                dialog.dismiss();
            }
        });

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }
}