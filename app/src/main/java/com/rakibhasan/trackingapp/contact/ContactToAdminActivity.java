package com.rakibhasan.trackingapp.contact;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rakibhasan.trackingapp.R;
import com.rakibhasan.trackingapp.adapters.MyListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactToAdminActivity extends AppCompatActivity {

    EditText userMgsET;
    Button sendBtn;
    FirebaseFirestore db;
    FirebaseUser user;
    ListView messagesLV;


    List<String> data;
    List<Boolean> isAdmin;

    MyListAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_to_admin);

        userMgsET = findViewById(R.id.user_message_et);
        sendBtn = findViewById(R.id.user_message_send_btn);
        messagesLV = findViewById(R.id.all_message_display_LV);


        //init fire store
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        data =  new ArrayList<>();
        isAdmin =  new ArrayList<>();
        getAllMessages();



        adapter=new MyListAdapter(this, data,isAdmin);
        messagesLV.setAdapter(adapter);



        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMgs = userMgsET.getText().toString().trim();
                if(userMgs.length() > 0){
                    userMgsUpdate(userMgs);
                    userMgsET.setText("");
                }

            }
        });

    }

    private void userMgsUpdate(String userMgs) {

        Map<String, Object> userContactChatUpdate = new HashMap<>();
        userContactChatUpdate.put("isSeen", false);
        userContactChatUpdate.put("msg", userMgs);
        userContactChatUpdate.put("receiverId", "admin");
        userContactChatUpdate.put("senderId", user.getUid());
        userContactChatUpdate.put("time", FieldValue.serverTimestamp());

        db.collection("messages")
                .add(userContactChatUpdate)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }

    private void getAllMessages() {


        db.collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        data.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            if(doc.getString("senderId").equals(user.getUid()) || doc.getString("receiverId").equals(user.getUid())){

                                data.add(doc.getString("msg"));

                                if(doc.getString("senderId").equals(user.getUid())){
                                    isAdmin.add(false);
                                }else{
                                    isAdmin.add(true);
                                }
                            }

                        }
                        adapter.notifyDataSetChanged();
                        messagesLV.post(new Runnable(){
                            public void run() {
                                messagesLV.setSelection(messagesLV.getCount() - 1);
                            }});

                    }
                });

    }


}