package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_page);
        Bundle extras = getIntent().getExtras();

        // Display Results
        if (extras != null) {
            // time
            Integer time = extras.getInt("clock");
            final TextView timeMessage = (TextView) findViewById(R.id.timeMessage);
            timeMessage.setTextSize(32);
            timeMessage.setText("Used " + time + " seconds.");

            // won or lost
            Boolean won = extras.getBoolean("won");
            final TextView wonMessage = (TextView) findViewById(R.id.wonMessage);
            final TextView extraMessage = (TextView) findViewById(R.id.extraMessage);
            wonMessage.setTextSize(32);
            extraMessage.setTextSize(32);
            if(won) {
                wonMessage.setText("You won.");
                extraMessage.setText("Good job!");
            } else {
                wonMessage.setText("You lost.");
                extraMessage.setText("Nice try!");
            }
        }

        // Play Again button
        Button button = (Button) findViewById(R.id.playAgainButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}