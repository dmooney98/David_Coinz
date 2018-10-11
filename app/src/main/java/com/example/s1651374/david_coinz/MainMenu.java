package com.example.s1651374.david_coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.content.Intent;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void goToMapScreen(View view){
        Intent intent = new Intent (this, MapScreen.class);
        startActivity(intent);
    }

}
