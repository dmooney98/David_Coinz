package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

//==================================================================================================
// This activity allows the user to create accounts, as well as sign in to a pre-existing account
// that they have made.  From this activity the user can go to the MainMenu, once they either enter
// valid details for creating a new account, or sign in to a pre-existing one
public class SignIn extends AppCompatActivity {

    //==============================================================================================
    // Create all required variables for the user to be able to enter their credentials and sign in
    // to the app, or create a new account
    private final String tag = "SignIn";
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;

    //==============================================================================================
    // Initialise the text fields to be able to interpret the user's input, and set these to empty
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialise the text fields for interpreting the user's input
        mEmailField = findViewById(R.id.SI_enterEmail);
        mPasswordField = findViewById(R.id.SI_enterPassword);

        // Set the text fields to empty
        mEmailField.setText("");
        mPasswordField.setText("");

        mAuth = FirebaseAuth.getInstance();

    }

    //==============================================================================================
    // Check if user is already signed in, acquire SharedPreferences and set the background
    // accordingly
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // Acquire SharedPreferences file, obtain information for background settings, and
        // initialise the ImageView for setting the background
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.SI_background);

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

    }

    //==============================================================================================
    // Determine whether or not the activity should transfer to the MainMenu, depending on if user
    // is null or not
    public void updateUI(FirebaseUser theUser) {
        if (theUser == null) {
            // Do not grant access to the MainMenu
        }
        else {
            // The user is valid, so access the MainMenu
            goToMainMenu();
        }
    }

    //==============================================================================================
    // Allow user to log in if their credentials are correct.  If there is information from one or
    // both of the text fields missing, inform the user accordingly
    public void userLogin(View view) {
        if (TextUtils.isEmpty(mEmailField.getText().toString()) &&
                TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mEmailField.setError("Required");
            mPasswordField.setError("Required");
            Toast.makeText(SignIn.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            Toast.makeText(SignIn.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mPasswordField.getText().toString())){
            mPasswordField.setError("Required");
            Toast.makeText(SignIn.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    //==============================================================================================
    // Allow user to sign up for the app if their credentials are valid.  If there is information
    // from one or both of the text fields missing, inform the user accordingly
    public void userSignup(View view) {
        if (TextUtils.isEmpty(mEmailField.getText().toString()) &&
                TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mEmailField.setError("Required");
            mPasswordField.setError("Required");
            Toast.makeText(this, "Please enter a valid email address and password",
                    Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            Toast.makeText(this, "Please enter a valid email address",
                    Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(mPasswordField.getText().toString())){
            mPasswordField.setError("Required");
            Toast.makeText(this, "Please enter a valid password",
                    Toast.LENGTH_LONG).show();
        }
        else {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    //==============================================================================================
    // Function which creates a new user account, using the code given by Android Studio about
    // utilising Firebase.  When the user creates a new account, they are given a Gold field which
    // set to 0, as well as having their limitations set up to record the current day as the most
    // recent day on which they played, and have the Banked variable in this branch of their
    // database set to 0, indicating correctly that they have not yet banked any coins today
    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(tag, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            HashMap<String, Object> banked = new HashMap<>();
                            HashMap<String, Double> goldField = new HashMap<>();
                            banked.put("Banked", 0);
                            goldField.put("Gold", 0.0);
                            String today = LocalDate.now().
                                    format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(mAuth.getCurrentUser().getEmail()).set(goldField);
                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(mAuth.getCurrentUser().getEmail())
                                    .collection("Limitations")
                                    .document(today).set(banked);
                            goToMainMenu();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(tag, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    //==============================================================================================
    // Function which allows user to sign in to the app, using an already existent account.  This is
    // done using the code given by Android Studio about utilising Firebase
    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(tag, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            goToMainMenu();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(tag, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }

    //==============================================================================================
    // Take the user to the MainMenu activity, nothing needs to be passed
    public void goToMainMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }


}
