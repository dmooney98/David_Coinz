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

//==================================================================================================
// This activity allows the user to enter another user's email address to add them as a friend, as
// well as remove them from their friends list.  Once another user has been added to a user's
// friends list, this allows the user to send them coins from their spare change.  This activity
// also allows the user to view the friends list and delete friends from it, via a confirmation
// pop-up.  From this activity, the user can return to MainMenu
public class ManageFriends extends AppCompatActivity {

    //==============================================================================================
    // Create all required variables for connecting to Firebase in order to add or remove friends,
    // as well as creating the variables required for displaying the friends in a ListView

    // Variables required for connecting to Firebase and retrieving the required information about
    // users and the user's friends, in order to determine whether an email address can be added as
    // a friend
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private int check = 0;
    private ArrayList<String> friends = new ArrayList<>();
    private ArrayList<String> users = new ArrayList<>();

    // Variables required to display and select friends in a ListView
    private ListView listViewF;
    private ArrayAdapter arrayAdapterF;
    private String selectedFriend = " ";


    //==============================================================================================
    // Sets the current user variable appropriately using FirebaseAuth, as well as using a method to
    // prevent the keyboard from appearing as soon as the activity is launched and crushing all of
    // objects on the screen up to the top
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        if (mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getEmail();
        }

    }

    //==============================================================================================
    // Set up appropriate variables for the class using SharedPreferences, and connect to Firebase
    // to fill the correct ArrayLists with the current user's friends, as well as all users on the
    // database
    public void onStart() {
        super.onStart();

        // Acquires the SharedPreferences file and retrieves the background variable, and sets up
        // the ImageView for setting the background
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.MF_background);

        // Creates Listeners for the 'REMOVE FRIEND' button and the 'ADD FRIEND' button
        Button removeFriendButton = findViewById(R.id.MF_removeFriendButton);
        removeFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = doubleCheck();
                if (dialog!=null){
                    dialog.show();
                }
            }
        });

        Button addFriendButton = findViewById(R.id.MF_addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();
            }
        });

        // Set the background using the value obtained from SharedPreferences
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

        // Perform this check to prevent issues occurring with Firebase's retrieval of information
        // from the database
        if(check == 0) {
            // Make sure the friends ArrayList is cleared, and set up the ListView correctly
            friends.clear();
            listViewF = (ListView) findViewById(R.id.MF_friendList);

            // Access Firebase to populate the friends ArrayList with the user's friends
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Friends").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            // Iterate over the friends in the user's friends list
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                // These variables are created to store information on the user's
                                // friends, so that the ListView can be updated to display the
                                // friends and date they were added
                                String friend = documentSnapshots.getDocuments().get(i).getId()
                                        + " \nFriend since " + documentSnapshots.getDocuments()
                                        .get(i).get("Date added").toString();
                                friends.add(friend);
                            }

                            // Display text message to fill the empty space left when the user has
                            // users in their friends list
                            TextView friendHelp = (TextView) findViewById(R.id.MF_friendHelp);
                            if(friends.size() == 0) {
                                friendHelp.setText("You don't have any friends added yet." +
                                        "  Add some!");
                            }
                            else {
                                friendHelp.setText("");
                            }

                            // Remove any duplicate values from the ArrayList friends, as this can
                            // occur under certain circumstances and have duplicate coins appear in
                            // the ListView
                            HashSet friendSet = new HashSet();
                            friendSet.addAll(friends);
                            friends.clear();
                            friends.addAll(friendSet);

                            // Set up the ArrayAdapter for the ListView, to display the ArrayList,
                            // friends
                            arrayAdapterF = new ArrayAdapter(ManageFriends.this,
                                    R.layout.my_layout, R.id.row_layout, friends);
                            listViewF.setAdapter(arrayAdapterF);
                            listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(ManageFriends.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Perform this check to prevent issues occurring with Firebase's retrieval of information
        // from the database
        if(check == 0) {
            // Access Firebase to populate the users ArrayList with all of the users on the database
            firebaseFirestore.collection("Users").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            // Iterate over the friends in the user's friends list
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                // These variables are created to store information on the user's
                                // friends, so that these can be referenced when deciding if the
                                // email address that the user provides is a valid user that can b
                                // added to their friends list
                                String user = documentSnapshots.getDocuments().get(i).getId();
                                users.add(user);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(ManageFriends.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Update the selectedFriend variable correct user that the current user has selected from
        // the ListView
        listViewF.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] temps = ((TextView) view).getText().toString().split(" ");
                selectedFriend = temps[0];
            }
        });

        // Update the variable required for the check
        check = 1;

    }

    //==============================================================================================
    // Returns a pop-up dialogue which will request confirmation from the user when attempting to
    // remove a friend
    public Dialog doubleCheck() {
        // If statement to ensure the user has selected a friend before pressing the 'REMOVE FRIEND'
        // button
        if (!selectedFriend.equals(" ")) {
            // The user has selected a friend to be removed and pressed the button

            // Create the appropriate strings for the question and options to present to the user in
            // the dialogue
            String message = "Are you sure you want to remove this friend?";
            String confirm = "Yes";
            String cancel = "No";

            // Create the Dialog using these valyes
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message)
                    .setPositiveButton(confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // When the user chooses "Yes", remove the selected friend
                            removeFriend();
                        }
                    }).setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // The user has chosen "No", so the Dialog will close
                        }
                    });
            // Return this created Dialog
            return builder.create();
        }
        else {
            // The user pressed the button without first selecting a friend to remove, so send them
            // a message informing them that they must first select a friend from the list
            Toast.makeText(this, "Please select friend to remove.",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //==============================================================================================
    // Update the database by deleting the selected friend, then give the user a message confirming
    // that the friend was removed.  Then update the ListView to no longer display this removed
    // friend
    public void removeFriend() {
        firebaseFirestore.collection("Users").document(currentUser)
                .collection("Friends").document(selectedFriend).delete();
        Toast.makeText(this, selectedFriend + " removed successfully.",
                Toast.LENGTH_SHORT).show();
        updateList();
    }

    //==============================================================================================
    // Perform checks on the email that the user provided, then add the user to the current user's
    // friends list if the attempt is valid
    public void addFriend() {
        // Get the text from the EditText where the user can enter the email address to add
        EditText friendAdder = (EditText) findViewById(R.id.MF_friendAdderText);
        String request = friendAdder.getText().toString();

        // Perform checks on the input
        if(request.isEmpty()) {
            // There is no input, inform the user they must enter an email address to add them
            Toast.makeText(this, "Please enter a friend's email address to add them.",
                    Toast.LENGTH_SHORT).show();
        }
        else if(request.equals(currentUser)){
            // The input is the same email address as the current user, inform them that they cannot
            // add themselves
            Toast.makeText(this, "You cannot add yourself as a friend!",
                    Toast.LENGTH_SHORT).show();
        }
        else if(!users.contains(request)) {
            // The list of all users found in onStart() does not contain the email address that the
            // user is attempting to add, inform them that this user does not exist
            Toast.makeText(this, "This user does not exist.",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            // The add request was valid

            // Create the required HashMap for adding a new entry to the database
            HashMap<String, Object> toPut = new HashMap<>();

            // Create a string which records the current date, so that the user will be able to see
            // which date the friend was added on, and put this into the HashMap
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            toPut.put("Date added", today);

            // Update the user's database to have this user in their friends list, and display a
            // message to the user, informing them that the action was successful
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Friends").document(request).set(toPut);
            Toast.makeText(this, "Friend added successfully!",
                    Toast.LENGTH_SHORT).show();

            // Reset the text in the input field
            friendAdder.setText("");

            // Update the ListView in the activity to now display this newly added friend
            updateList();
        }
    }

    //==============================================================================================
    // Update the ListView after a user has been added or removed from the current user's friend
    // list
    public void updateList() {
        // Make sure the friends ArrayList is cleared, and set up the ListView correctly
        friends.clear();
        listViewF = (ListView) findViewById(R.id.MF_friendList);

        // Access Firebase to populate the friends ArrayList with the user's friends
        firebaseFirestore.collection("Users").document(currentUser)
                .collection("Friends").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        // Iterate over the friends in the user's friends list
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            // These variables are created to store information on the user's
                            // friends, so that the ListView can be updated to display the
                            // friends and date they were added
                            String friend = documentSnapshots.getDocuments().get(i).getId()
                                    + " \nFriend since " + documentSnapshots.getDocuments()
                                    .get(i).get("Date added").toString();
                            friends.add(friend);
                        }

                        // Display text message to fill the empty space left when the user has
                        // users in their friends list
                        TextView friendHelp = (TextView) findViewById(R.id.MF_friendHelp);
                        if(friends.size() == 0) {
                            friendHelp.setText("You don't have any friends added yet.  Add some!");
                        }
                        else {
                            friendHelp.setText("");
                        }

                        // Remove any duplicate values from the ArrayList friends, as this can
                        // occur under certain circumstances and have duplicate coins appear in
                        // the ListView
                        HashSet friendSet = new HashSet();
                        friendSet.addAll(friends);
                        friends.clear();
                        friends.addAll(friendSet);

                        // Set up the ArrayAdapter for the ListView, to display the ArrayList,
                        // friends
                        arrayAdapterF = new ArrayAdapter(ManageFriends.this,
                                R.layout.my_layout, R.id.row_layout, friends);
                        listViewF.setAdapter(arrayAdapterF);
                        listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Inform the user of error with connection
                        Toast.makeText(ManageFriends.this,
                                "Failed to establish connection to DataBase, " +
                                        "please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //==============================================================================================
    // Take the user to the MainMenu activity, nothing needs to be passed
    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

}
