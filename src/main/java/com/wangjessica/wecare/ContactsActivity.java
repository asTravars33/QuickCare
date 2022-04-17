package com.wangjessica.wecare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ContactsActivity extends AppCompatActivity {

    DatabaseReference rootRef;
    DatabaseReference contactsRef;
    FirebaseAuth auth;
    String userId;

    private ListView listView;
    private ArrayList<String> contactsList = new ArrayList<String>();
    private ArrayAdapter<String> contactsAdapter;

    Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Firebase
        rootRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        contactsRef = rootRef.child("Users").child(userId);

        displayContacts();

        // Contacts list
        listView = findViewById(R.id.contacts_list);
        contactsAdapter = new ArrayAdapter<String>(ContactsActivity.this, android.R.layout.simple_list_item_1, contactsList);
        listView.setAdapter(contactsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        // Add button
        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();
            }
        });
    }

    private void displayContacts() {
        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<String>();
                Iterator iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey().toString());
                }
                for(String s: set){
                    System.out.println(s);
                    contactsRef.child(s).child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            contactsList.add(snapshot.getValue().toString());
                            contactsAdapter.notifyDataSetChanged();
                            System.out.println(contactsList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addContact() {
        // Add the contact to Firebase
        String contactKey = contactsRef.push().getKey();
        DatabaseReference contactRef = contactsRef.child(contactKey);
        HashMap<String, Object> contactInfo = new HashMap<String, Object>();
        contactInfo.put("name", "");
        contactInfo.put("age", "");
        contactInfo.put("height", "");
        contactRef.updateChildren(contactInfo);
        // Send the user to more specific details
        Intent formIntent = new Intent(ContactsActivity.this, FormActivity.class);
        formIntent.putExtra("key", contactKey);
        startActivity(formIntent);
    }

}
