package com.app.fitness24;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.app.fitness24.adapters.LogAdapter;
import com.app.fitness24.adapters.MealAdapter;
import com.app.fitness24.models.Goals;
import com.app.fitness24.models.Log;
import com.app.fitness24.models.Meal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LogActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton actionButton;
    LogAdapter adapter;
    List<Log> list;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String TAG = "theH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        recyclerView = findViewById(R.id.recycler_view);
        actionButton = findViewById(R.id.fab);

        list = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("logs").child(mAuth.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                android.util.Log.d(TAG, "onDataChange: "+dataSnapshot.getValue());
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();

                android.util.Log.d(TAG, "onDataChange: getChildrenCount "+dataSnapshot.getChildrenCount());
                list.clear();
                for (DataSnapshot data : snapshotIterator){
                    Log log = data.getValue(Log.class);
                    list.add(log);
                }


                adapter = new LogAdapter(LogActivity.this, list);
                recyclerView.setLayoutManager(new LinearLayoutManager(LogActivity.this, LinearLayoutManager.VERTICAL,false));
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                android.util.Log.w(TAG, "Failed to read value.", error.toException());
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
        final Dialog dialog = new Dialog(LogActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_log_dialog);

        final EditText etWeight = dialog.findViewById(R.id.et_weight);

        Button btnSave = dialog.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String weight = etWeight.getText().toString();
                myRef.child(UUID.randomUUID().toString()).setValue(new Log(getCurrentDate(), weight));
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }
}