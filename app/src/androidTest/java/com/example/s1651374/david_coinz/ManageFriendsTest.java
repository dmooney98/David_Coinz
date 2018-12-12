package com.example.s1651374.david_coinz;


import android.support.annotation.NonNull;
import android.support.test.espresso.DataInteraction;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ManageFriendsTest {

    @Rule
    public ActivityTestRule<SignIn> mActivityTestRule = new ActivityTestRule<>(SignIn.class);

    //==============================================================================================
    // -- IMPORTANT INFORMATION FOR MARKERS -- IMPORTANT INFORMATION FOR MARKERS --
    // --  DO NOT RUN TEST BEFORE READING   --  DO NOT RUN TEST BEFORE READING   --
    // -- IMPORTANT INFORMATION FOR MARKERS -- IMPORTANT INFORMATION FOR MARKERS --
    //
    // This test will test the app's ability to sign in to a pre-created account, and add 3 friends
    // which are other users pre-created on the database.  It will then remove these friends, then
    // attempt to add a fake account, then attempt to add the email address of the user who is
    // signed in.
    //
    // TO RUN THIS TEST:
    //
    // Run the app separately and ensure that it is currently logged out of any pre-signed in user.
    // Do not run the same test twice in a row.  To run this test a second time, ensure one of the
    // other tests have been ran after it, from the manually logged out state, and then ensure that
    // the app is once again in a logged out state.  This is due to the other tests resetting the
    // values needed in the database for this test to be ran twice, and the tests being unable to be
    // ended on the SignIn activity.
    //
    // 1. Launch app and ensure app is logged out of any user
    // 2. Launch this test
    // 3. Launch app and ensure app is logged out of any user
    // 4. Launch a different test
    // 5. Launch app and ensure app is logged out of any user
    // 6. This test can now be re-ran
    //
    // IF THESE INSTRUCTIONS ARE NOT FOLLOWED, THIS AND OTHER TESTS MAY BE RENDERED OBSOLETE, CRASH,
    // AND BE UNABLE TO BE RE-RAN.  THIS IS DUE TO THE TEST MAKING CHANGES TO THE DATABASE WHICH
    // CANNOT BE RESET OTHERWISE.
    @Test
    public void manageFriendsTest() {
        //==========================================================================================
        // Reset values for SendCoinsTest
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document("sendcoins@test.com")
                .collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if(documentSnapshots.getDocuments().get(0).exists()) {
                            HashMap<String, Integer> myUpdate = new HashMap<>();
                            myUpdate.put("Banked", 0);
                            firebaseFirestore.collection("Users")
                                    .document("sendcoins@test.com")
                                    .collection("Limitations")
                                    .document(documentSnapshots.getDocuments()
                                            .get(0).getId()).delete();
                            firebaseFirestore.collection("Users")
                                    .document("sendcoins@test.com")
                                    .collection("Limitations")
                                    .document("12123123").set(myUpdate);
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

        ViewInteraction manage_friends_button = onView(
                allOf(withId(R.id.MM_manage_friends_button), withText("MANAGE FRIENDS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));

        ViewInteraction friendAdderText = onView(
                allOf(withId(R.id.MF_friendAdderText),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));

        ViewInteraction addFriendButton = onView(
                allOf(withId(R.id.MF_addFriendButton), withText("ADD FRIEND"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));

        DataInteraction manage_friends_list = onData(anything())
                .inAdapterView(allOf(withId(R.id.MF_friendList),
                        childAtPosition(
                                withClassName(is("android.support.constraint." +
                                        "ConstraintLayout")),
                                2)))
                .atPosition(0);

        ViewInteraction removeFriendButton = onView(
                allOf(withId(R.id.MF_removeFriendButton), withText("REMOVE FRIEND"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));

        ViewInteraction confirm = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));

        ViewInteraction manage_friends_back = onView(
                allOf(withId(R.id.MF_back_button), withText("BACK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction dailyOkay = onView(
                allOf(withId(R.id.DU_okay_button), withText("OKAY"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        //==========================================================================================
        // Sign in as user
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        emailText.perform(replaceText("manageF@test.com"), closeSoftKeyboard());

        passwordText.perform(replaceText("12345678"), closeSoftKeyboard());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logIn.perform(click());

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dailyOkay.perform(click());

        //==========================================================================================
        // Go to the ManageFriends activity
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manage_friends_button.perform(click());

        //==========================================================================================
        // Add 3 friends to the friends list, who are other users who exist on Firestore
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        friendAdderText.perform(click());

        friendAdderText.perform(replaceText("friend1@test.com"), closeSoftKeyboard());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        addFriendButton.perform(click());

        friendAdderText.perform(replaceText("friend2@test.com"), closeSoftKeyboard());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        addFriendButton.perform(click());

        friendAdderText.perform(replaceText("friend3@test.com"), closeSoftKeyboard());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        addFriendButton.perform(click());

        //==========================================================================================
        // Now remove these 3 friends from the friends list

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manage_friends_list.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        removeFriendButton.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        confirm.perform(scrollTo(), click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manage_friends_list.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        removeFriendButton.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        confirm.perform(scrollTo(), click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manage_friends_list.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        removeFriendButton.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        confirm.perform(scrollTo(), click());

        //==========================================================================================
        // Attempt to add a non-existent user as a friend

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        friendAdderText.perform(click());

        friendAdderText.perform(replaceText("fake@test.com"), closeSoftKeyboard());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        addFriendButton.perform(click());

        //==========================================================================================
        // Attempt to add the current signed in user as a friend

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        friendAdderText.perform(click());

        friendAdderText.perform(replaceText("managef@test.com"));

        friendAdderText.perform(closeSoftKeyboard());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        addFriendButton.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manage_friends_back.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //==========================================================================================
        // Reset values correctly so that this test can be run multiple times
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        firebaseFirestore.collection("Users").document(currentUser)
                .collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        HashMap<String, Integer> myUpdate = new HashMap<>();
                        myUpdate.put("Banked", 0);
                        firebaseFirestore.collection("Users").document(currentUser)
                                .collection("Limitations")
                                .document(documentSnapshots.getDocuments().get(0).getId()).delete();
                        firebaseFirestore.collection("Users").document(currentUser)
                                .collection("Limitations")
                                .document("12123123").set(myUpdate);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

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
