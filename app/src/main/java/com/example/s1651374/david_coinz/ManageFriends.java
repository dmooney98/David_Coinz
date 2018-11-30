package com.example.s1651374.david_coinz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nullable;

public class ManageFriends extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private ListView listViewF;
    private int check = 0;
    private ArrayList<String> friends = new ArrayList<String>();
    private ArrayList<String> users = new ArrayList<>();
    private ArrayAdapter arrayAdapterF;
    private String selectedFriend = " ";
    private Button removeFriendButton;
    private Button addFriendButton;
    private String today = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        currentUser = mAuth.getCurrentUser().getEmail();
    }

    public void onStart() {
        super.onStart();
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

        removeFriendButton = findViewById(R.id.removeFriendButton);
        removeFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = doubleCheck();
                if (dialog!=null){
                    dialog.show();
                }
            }
        });

        addFriendButton = findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();
            }
        });


        if (backgroundPick.equals("1")) {
            image.setImageResource(R.drawable.background1);
        } else if (backgroundPick.equals("2")) {
            image.setImageResource(R.drawable.background2);
        } else if (backgroundPick.equals("3")) {
            image.setImageResource(R.drawable.background3);
        } else if (backgroundPick.equals("4")) {
            image.setImageResource(R.drawable.background4);
        } else if (backgroundPick.equals("5")) {
            image.setImageResource(R.drawable.background5);
        } else if (backgroundPick.equals("6")) {
            image.setImageResource(R.drawable.background6);
        }


        if(check == 0) {
            friends.clear();
            listViewF = (ListView) findViewById(R.id.MF_friendList);
            firebaseFirestore.collection("Users").document(currentUser).collection("Friends").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                String friend = documentSnapshots.getDocuments().get(i).getId() + " \nFriend since " + documentSnapshots.getDocuments().get(i).get("Date added").toString();
                                friends.add(friend);
                            }

                            TextView friendHelp = (TextView) findViewById(R.id.MF_friendHelp);
                            if(friends.size() == 0) {
                                friendHelp.setText("You don't have any friends added yet.  Add some!");
                            }
                            else {
                                friendHelp.setText("");
                            }

                            HashSet friendSet = new HashSet();
                            friendSet.addAll(friends);
                            friends.clear();
                            friends.addAll(friendSet);

                            arrayAdapterF = new ArrayAdapter(ManageFriends.this, R.layout.my_layout, R.id.row_layout, friends);
                            listViewF.setAdapter(arrayAdapterF);

                            listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ManageFriends.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
            /*
            firebaseFirestore.collection("Users").document(currentUser).collection("Friends").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        String friend = queryDocumentSnapshots.getDocuments().get(i).getId() + " \nFriend since " + queryDocumentSnapshots.getDocuments().get(i).get("Date added").toString();
                        friends.add(friend);
                    }

                    TextView friendHelp = (TextView) findViewById(R.id.MF_friendHelp);
                    if(friends.size() == 0) {
                        friendHelp.setText("You don't have any friends added yet.  Add some!");
                    }
                    else {
                        friendHelp.setText("");
                    }

                    HashSet friendSet = new HashSet();
                    friendSet.addAll(friends);
                    friends.clear();
                    friends.addAll(friendSet);

                    arrayAdapterF = new ArrayAdapter(ManageFriends.this, R.layout.my_layout, R.id.row_layout, friends);
                    listViewF.setAdapter(arrayAdapterF);

                    listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                }
            });*/
        }

        if(check == 0) {
            firebaseFirestore.collection("Users").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                String user = documentSnapshots.getDocuments().get(i).getId();
                                users.add(user);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ManageFriends.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
            /*
            firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        String user = queryDocumentSnapshots.getDocuments().get(i).getId();
                        users.add(user);
                    }
                }
            });*/
        }

        listViewF.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] temps = ((TextView) view).getText().toString().split(" ");
                selectedFriend = temps[0];
            }
        });

        check = 1;

    }

    public Dialog doubleCheck() {
        if (!selectedFriend.equals(" ")) {
            String message = "Are you sure you want to remove this friend?";
            String confirm = "Yes";
            String cancel = "No";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message)
                    .setPositiveButton(confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeFriend();
                        }
                    }).setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            return builder.create();
        }
        else {
            Toast.makeText(this, "Please select friend to remove.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void removeFriend() {
        firebaseFirestore.collection("Users").document(currentUser).collection("Friends").document(selectedFriend).delete();
        Toast.makeText(this, selectedFriend + " removed successfully.", Toast.LENGTH_SHORT).show();
        updateList();
    }

    public void addFriend() {
        EditText friendAdder = (EditText) findViewById(R.id.friendAdder);
        String request = friendAdder.getText().toString();
        if(request.isEmpty()) {
            Toast.makeText(this, "Please enter a friend's email address to add them.", Toast.LENGTH_SHORT).show();
        }
        else if(request.equals(currentUser)){
            Toast.makeText(this, "You cannot add yourself as a friend!", Toast.LENGTH_SHORT).show();
        }
        else if(!users.contains(request)) {
            Toast.makeText(this, "This user does not exist.", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> toPut = new HashMap<>();
            today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            toPut.put("Date added", today);
            firebaseFirestore.collection("Users").document(currentUser).collection("Friends").document(request).set(toPut);
            Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
            friendAdder.setText("");
            updateList();
        }
    }

    public void updateList() {
        friends.clear();
        listViewF = (ListView) findViewById(R.id.MF_friendList);
        firebaseFirestore.collection("Users").document(currentUser).collection("Friends").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            String friend = documentSnapshots.getDocuments().get(i).getId() + " \nFriend since " + documentSnapshots.getDocuments().get(i).get("Date added").toString();
                            friends.add(friend);
                        }

                        TextView friendHelp = (TextView) findViewById(R.id.MF_friendHelp);
                        if(friends.size() == 0) {
                            friendHelp.setText("You don't have any friends added yet.  Add some!");
                        }
                        else {
                            friendHelp.setText("");
                        }

                        HashSet friendSet = new HashSet();
                        friendSet.addAll(friends);
                        friends.clear();
                        friends.addAll(friendSet);

                        arrayAdapterF = new ArrayAdapter(ManageFriends.this, R.layout.my_layout, R.id.row_layout, friends);
                        listViewF.setAdapter(arrayAdapterF);

                        listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ManageFriends.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
        /*
        firebaseFirestore.collection("Users").document(currentUser).collection("Friends").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    String friend = queryDocumentSnapshots.getDocuments().get(i).getId() + " \nFriend since " + queryDocumentSnapshots.getDocuments().get(i).get("Date added").toString();
                    friends.add(friend);
                }

                TextView friendHelp = (TextView) findViewById(R.id.MF_friendHelp);
                if(friends.size() == 0) {
                    friendHelp.setText("You don't have any friends added yet.  Add some!");
                }
                else {
                    friendHelp.setText("");
                }

                HashSet friendSet = new HashSet();
                friendSet.addAll(friends);
                friends.clear();
                friends.addAll(friendSet);

                arrayAdapterF = new ArrayAdapter(ManageFriends.this, R.layout.my_layout, R.id.row_layout, friends);
                listViewF.setAdapter(arrayAdapterF);

                listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            }
        });*/
    }

    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

}
