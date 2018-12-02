package com.example.s1651374.david_coinz;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SignInOutTest {

    @Rule
    public ActivityTestRule<SignIn> mActivityTestRule = new ActivityTestRule<>(SignIn.class);

    @Test
    public void signInOutTest() {
        //==========================================================================================
        // Reset values for SendCoinsTest and ManageFriendsTest
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document("managef@test.com").collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if(documentSnapshots.getDocuments().get(0).exists()) {
                            HashMap<String, Integer> myUpdate = new HashMap<>();
                            myUpdate.put("Banked", 0);
                            firebaseFirestore.collection("Users").document("managef@test.com").collection("Limitations").document(documentSnapshots.getDocuments().get(0).getId()).delete();
                            firebaseFirestore.collection("Users").document("managef@test.com").collection("Limitations").document("12123123").set(myUpdate);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        firebaseFirestore.collection("Users").document("sendcoins@test.com").collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if(documentSnapshots.getDocuments().get(0).exists()) {
                            HashMap<String, Integer> myUpdate = new HashMap<>();
                            myUpdate.put("Banked", 0);
                            firebaseFirestore.collection("Users").document("sendcoins@test.com").collection("Limitations").document(documentSnapshots.getDocuments().get(0).getId()).delete();
                            firebaseFirestore.collection("Users").document("sendcoins@test.com").collection("Limitations").document("12123123").set(myUpdate);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


        //==========================================================================================
        // Initialise all required ViewInteractions for use throughout the test
        ViewInteraction emailText = onView(
                allOf(withId(R.id.SI_enterEmail),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction passwordText = onView(
                allOf(withId(R.id.SI_enterPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));

        ViewInteraction logIn = onView(
                allOf(withId(R.id.SI_loginButton), withText("LOG IN"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));

        ViewInteraction signUp = onView(
                allOf(withId(R.id.SI_signupButton), withText("SIGN UP"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));

        ViewInteraction logOut = onView(
                allOf(withId(R.id.MM_logout_button), withText("LOG OUT"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));

        // Enter the email address of a new account in the SignIn activity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String username = String.valueOf((int) Math.ceil(Math.random() * 100000)) + "@test.com";

        emailText.perform(replaceText(username), closeSoftKeyboard());

        //==========================================================================================
        // Enter the password for the new account in the SignIn activity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        passwordText.perform(replaceText("12345678"), closeSoftKeyboard(), pressImeActionButton());

        //==========================================================================================
        // Press the Sign Up button, to create the account on Firebase, and be taken to the
        // Main Menu activity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        signUp.perform(click());

        //==========================================================================================
        // From the MainMenu activity, press the Log Out button to be logged out of the account and
        // returned to the SignIn activity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logOut.perform(click());

        //==========================================================================================
        // Attempt to sign in with a fake account, and receive the 'Authentication failed' message
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        emailText.perform(replaceText("faker@test.com"), closeSoftKeyboard());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        passwordText.perform(replaceText("fake1234"), closeSoftKeyboard());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logIn.perform(click());

        //==========================================================================================
        // Sign in to the account created earlier, and receive access to the MainMenu activity as
        // expected
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        emailText.perform(replaceText(username), closeSoftKeyboard());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        passwordText.perform(replaceText("12345678"), closeSoftKeyboard());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logIn.perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
