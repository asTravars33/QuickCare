package com.wangjessica.wecare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference myChatRef;
    private String userId;

    private ImageButton sendButton;
    private ImageButton sendImg;
    private LinearLayout msgDisplay;
    private ScrollView scrollView;

    private EditText messageField;
    private StorageReference imageRef;
    private StorageTask uploadTask;

    private int GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Firebase stuff
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userId = auth.getCurrentUser().getUid();
        myChatRef = rootRef.child("Chats").child(userId);
        imageRef = FirebaseStorage.getInstance().getReference();

        // Layout components
        sendButton = findViewById(R.id.send_button);
        sendImg = findViewById(R.id.send_media);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMessages(messageField.getText().toString());
            }
        });
        sendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImage();
            }
        });
        messageField = findViewById(R.id.input_message);
        msgDisplay = findViewById(R.id.messages_layout);
        scrollView = findViewById(R.id.scroll_view);

        // Send out the user's information
        DatabaseReference contactRef = rootRef.child("Users").child(userId);
        String[] keys = {"thing", "age", "height", "weight", "allergies", "medical_history", "current_conditions", "emergency_contacts", "local_hospitals"};
        String[] endings = {"", " years", " in", " lbs", "", "", "", "", "", ""};
        for(int i=0; i<keys.length; i++){
            String key = keys[i];
            final int j=i;
            contactRef.child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String message = key+": "+snapshot.getValue().toString()+endings[j];
                        if(key.equals("thing")){
                            message = "Name: "+snapshot.getValue().toString()+endings[j];
                        }
                        updateMessages(message);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void sendImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY);
    }

    public Bitmap useImage(Uri uri)
    {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("ON ACTIVITY RESULT");
        if(requestCode==GALLERY && resultCode==RESULT_OK && data!=null){
            Uri uri = data.getData();
            // New image message
            String messageKey = myChatRef.push().getKey();
            System.out.println(imageRef);
            // Store the image in Firebase
            /*StorageReference filePath = imageRef.child(messageKey + ".jpg");
            uploadTask = filePath.putFile(uri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        // Update the chat messages info
                        HashMap<String, Object> messageInfo = new HashMap<String, Object>();
                            messageInfo.put("content", downloadUri.toString());
                            messageInfo.put("type", "image");
                        myChatRef.child(messageKey).updateChildren(messageInfo);
                    }
                }
            });*/
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView imgView = new ImageView(this);
            imgView.setForegroundGravity(Gravity.CENTER);
            imgView.setLayoutParams(params);
            imgView.setImageBitmap(useImage(uri));
            msgDisplay.addView(imgView);
            updateMessages(analyzeWound());
        }
    }
    public String analyzeWound(){
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("script");
        PyObject obj = null;
        obj = pyobj.callAttr("main");
        return obj.toString();
    }
    public Bitmap getBitmap(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Bitmap d = BitmapFactory.decodeStream(is);
            is.close();
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*myChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayMessage(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    private void displayMessage(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String content = ((DataSnapshot) (iterator.next())).getValue().toString();
            String type = ((DataSnapshot) (iterator.next())).getValue().toString();

            // Create the message text and display
            if(type.equals("text")) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView messageView = new TextView(this);
                messageView.setLayoutParams(params);
                messageView.setText(content);
                msgDisplay.addView(messageView);
            }
            else{
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(params);
                imageView.setImageBitmap(getBitmap(content));
            }
            // Automatically scroll to the new message
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void updateMessages(String message) {
        String messageKey = myChatRef.push().getKey();
        HashMap<String, Object> messageInfo = new HashMap<String, Object>();
            messageInfo.put("content", message);
            messageInfo.put("type", "text");
        myChatRef.child(messageKey).updateChildren(messageInfo);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 10, 10, 10);
        TextView messageView = new TextView(this);
        messageView.setTextSize(22);
        messageView.setPadding(5, 5, 5, 5);
        messageView.setBackgroundResource(R.drawable.blue);
        messageView.setGravity(Gravity.CENTER);
        //messageView.setBackgroundColor(R.color.black);
        messageView.setLayoutParams(params);
        messageView.setText(message);
        msgDisplay.addView(messageView);
    }
}