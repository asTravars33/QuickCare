package com.wangjessica.wecare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FormActivity extends AppCompatActivity {

    private EditText[] fields = new EditText[9];
    private Button submit;
    private String contactKey;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Intent parent = getIntent();
        contactKey = parent.getStringExtra("key");

        // Firebase
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userId = auth.getCurrentUser().getUid();

        // Layout components
        submit = findViewById(R.id.submit_button);
        int editInd = 0;
        for(int i=1; i<=7; i++){
            LinearLayout layout = findViewById(getResources().getIdentifier("row"+i, "id", getPackageName()));
            for(int k=0; k<layout.getChildCount(); k++){
                fields[editInd++] = (EditText) layout.getChildAt(k);
            }
        }

        initializeFieldVals();

        // Handle form submit
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInfo();
            }
        });
    }

    private void initializeFieldVals() {
        DatabaseReference contactRef = rootRef.child("Users").child(userId);
        for(EditText field: fields){
            contactRef.child(field.getTag().toString()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        field.setText(snapshot.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateInfo() {
        DatabaseReference contactRef = rootRef.child("Users").child(userId);
        for(int i=0; i<fields.length; i++){
            EditText field = fields[i];
            System.out.println(field.getTag().toString());
            System.out.println(field.getText().toString());
            System.out.println(field.getTag().toString());
            contactRef.child(field.getTag().toString()).setValue(field.getText().toString());
        }
        // Return to main activity
        Intent mainIntent = new Intent(FormActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}