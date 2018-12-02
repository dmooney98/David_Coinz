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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SendCoinsTest {

    @Rule
    public ActivityTestRule<SignIn> mActivityTestRule = new ActivityTestRule<>(SignIn.class);

    @Test
    public void sendCoinsTest() {
        //==========================================================================================
        // Reset values for ManageFriendsTest
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

        ViewInteraction dailyOkay = onView(
                allOf(withId(R.id.DU_okay_button), withText("OKAY"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction deposit_coins_button = onView(
                allOf(withId(R.id.MM_deposit_coins_button), withText("DEPOSIT COINS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));

        ViewInteraction desposit_coins_spare_change = onView(
                allOf(withId(R.id.DC_spare_change_button), withText("MY SPARE CHANGE"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                14),
                        isDisplayed()));

        DataInteraction spare_change_wallet = onData(anything())
                .inAdapterView(allOf(withId(R.id.SC_wallet),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                2)))
                .atPosition(0);

        ViewInteraction transferButton = onView(
                allOf(withId(R.id.SC_transferButton), withText("TRANSFER"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));

        ViewInteraction sc_back = onView(
                allOf(withId(R.id.SC_back_button), withText("BACK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction dc_friend_button = onView(
                allOf(withId(R.id.DC_friend_button), withText("SEND COINS TO A FRIEND"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                13),
                        isDisplayed()));

        DataInteraction send_coins_coins = onData(anything())
                .inAdapterView(allOf(withId(R.id.SendC_coinList),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                2)))
                .atPosition(0);

        DataInteraction send_coins_friends = onData(anything())
                .inAdapterView(allOf(withId(R.id.SendC_friendList),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                3)))
                .atPosition(0);

        ViewInteraction send_coins_send_button = onView(
                allOf(withId(R.id.SendC_sendCoinsButton), withText("SEND COINS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));

        ViewInteraction send_coins_back_button = onView(
                allOf(withId(R.id.SendC_back_button), withText("BACK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction dc_bank_button = onView(
                allOf(withId(R.id.DC_bank_coins), withText("BANK COINS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                12),
                        isDisplayed()));

        ViewInteraction transfer_inform = onView(
                allOf(withId(R.id.TI_okay), withText("OKAY"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction goldInformF_okay = onView(
                allOf(withId(R.id.GIF_okay), withText("OKAY"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));

        DataInteraction bc_coins = onData(anything())
                .inAdapterView(allOf(withId(R.id.BC_coinList),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                2)))
                .atPosition(0);

        ViewInteraction bc_bank_button = onView(
                allOf(withId(R.id.BC_bank_button), withText("BANK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));

        ViewInteraction bc_small_gamble = onView(
                allOf(withId(R.id.BC_small_gamble), withText("SMALL GAMBLE"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));

        ViewInteraction bc_big_gamble = onView(
                allOf(withId(R.id.BC_big_gamble), withText("BIG GAMBLE"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));

        ViewInteraction goldInform_okay = onView(
                allOf(withId(R.id.GIF_okay), withText("OKAY"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));

        ViewInteraction bc_back = onView(
                allOf(withId(R.id.BC_back_button), withText("BACK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction dc_main_menu = onView(
                allOf(withId(R.id.DC_main_menu_button), withText("MAIN MENU"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));

        //==========================================================================================
        // Sign in as user, navigate past DailyUpdate
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        emailText.perform(replaceText("sendCoins@test.com"), closeSoftKeyboard());

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

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //==========================================================================================
        // Due to my feature of daily reset of wallet coins, for the test the wallet has some coins
        // manually inserted.  Do this, then go to the DepositCoins activity
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        for (int i = 0; i < 4; i++) {
            HashMap<String, Object> walletCoin = new HashMap<>();
            double spin = Math.ceil(Math.random() * 10);
            if (spin < 2.5) {
                walletCoin.put("currency", "QUID");
                walletCoin.put("value", spin);
            }
            else if (spin < 5.0) {
                walletCoin.put("currency", "SHIL");
                walletCoin.put("value", spin);
            }
            else if (spin < 7.5) {
                walletCoin.put("currency", "PENY");
                walletCoin.put("value", spin);
            }
            else {
                walletCoin.put("currency", "DOLR");
                walletCoin.put("value", spin);
            }
            firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(String.valueOf(i)).set(walletCoin);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        deposit_coins_button.perform(click());

        //==========================================================================================
        // Now go to the SpareChange activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        desposit_coins_spare_change.perform(click());

        //==========================================================================================
        // Now transfer one coin to the user's spare change
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        spare_change_wallet.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        transferButton.perform(click());

        //==========================================================================================
        // Return to the DepositCoins activity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        transfer_inform.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sc_back.perform(click());

        //==========================================================================================
        // Go to SendCoins activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dc_friend_button.perform(click());

        //==========================================================================================
        // Transfer one coin to a friend
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        send_coins_coins.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        send_coins_friends.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        send_coins_send_button.perform(click());

        //==========================================================================================
        // Return to the DepositCoins activity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        goldInformF_okay.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        send_coins_back_button.perform(click());

        //==========================================================================================
        // Go to BankCoins activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dc_bank_button.perform(click());

        //==========================================================================================
        // Bank one coin with standard banking
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bc_coins.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bc_bank_button.perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        goldInform_okay.perform(click());

        //==========================================================================================
        // Bank one coin with small gamble
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bc_coins.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bc_small_gamble.perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        goldInform_okay.perform(click());

        //==========================================================================================
        // Bank one coin with big gamble

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bc_coins.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bc_big_gamble.perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        goldInform_okay.perform(click());

        //==========================================================================================
        // Return to DepositCoins activity, then to MainMenu activity
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bc_back.perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dc_main_menu.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //==========================================================================================
        // Reset values correctly so that this test can be run multiple times
        firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        HashMap<String, Integer> myUpdate = new HashMap<>();
                        myUpdate.put("Banked", 0);
                        firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(documentSnapshots.getDocuments().get(0).getId()).delete();
                        firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document("12123123").set(myUpdate);
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