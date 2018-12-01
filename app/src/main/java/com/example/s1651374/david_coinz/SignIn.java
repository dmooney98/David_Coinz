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

public class SignIn extends AppCompatActivity {

    private final String tag = "SignIn";

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField; //= findViewById(R.id.enterEmail);
    private EditText mPasswordField; //= findViewById(R.id.enterPassword);

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Views
        //mStatusTextView = findViewById();
        //mDetailTextView = findViewById();
        mEmailField = findViewById(R.id.enterEmail);
        mPasswordField = findViewById(R.id.enterPassword);

        mEmailField.setText("");
        mPasswordField.setText("");

        // Buttons


        mAuth = FirebaseAuth.getInstance();

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

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

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

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

    public void updateUI(FirebaseUser theUser) {
        if (theUser == null) {
            // Do not grant access to the MainMenu
        }
        else {
            goToMainMenu();
        }
    }

    public void userLogin(View view) {
        if (TextUtils.isEmpty(mEmailField.getText().toString()) && TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mEmailField.setError("Required");
            mPasswordField.setError("Required");
            Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mPasswordField.getText().toString())){
            mPasswordField.setError("Required");
            Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
        else {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    public void userSignup(View view) {
        if (TextUtils.isEmpty(mEmailField.getText().toString()) && TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mEmailField.setError("Required");
            mPasswordField.setError("Required");
            Toast.makeText(this, "Please enter a valid email address and password", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(mPasswordField.getText().toString())){
            mPasswordField.setError("Required");
            Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_LONG).show();
        }
        else {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

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
                            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getEmail()).set(goldField);
                            FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getEmail()).collection("Limitations").document(today).set(banked);
                            goToMainMenu();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(tag, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                        }
                });
    }

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
                            Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void goToMainMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }


}
