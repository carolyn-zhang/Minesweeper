package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 8;
    private int clock = 0;
    private int numFlagsLeft = 4; // 4 b/c we have 4 mines
    private boolean running = false;
    private boolean flag = false;
    private boolean pick = true;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private ArrayList<Integer> mineLocations;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();
        runTimer();
        mineLocations = generateMineLocations();

        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i<=9; i++) {
            for (int j=0; j<=7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 24 );//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }
        final TextView flagsLeft = (TextView) findViewById(R.id.flagsLeftValue);
        flagsLeft.setText(String.valueOf(numFlagsLeft));

        TextView pick = (TextView) findViewById(R.id.pick);
        pick.setOnClickListener(this::onClickPick);

        TextView flag = (TextView) findViewById(R.id.flag);
        flag.setOnClickListener(this::onClickFlag);
        flag.setVisibility(TextView.INVISIBLE);

    }

    private ArrayList<Integer> generateMineLocations() {
        ArrayList<Integer> mineLocations = new ArrayList<Integer>(4);
        int randomNum;
        while(mineLocations.size() < 4) {
            randomNum = (int) (Math.random() * 80);
            if(!mineLocations.contains(randomNum)){
                mineLocations.add(randomNum);
            }
        }
        for(int i = 0; i < mineLocations.size(); i++){
            System.out.println(mineLocations.get(i));
        }
        return mineLocations;
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    // user wants to toggle to setting flags now
    public void onClickPick(View view){
        if(numFlagsLeft > 0) {
            pick = false;
            flag = true;

            TextView pick = (TextView) findViewById(R.id.pick);
            pick.setVisibility(TextView.INVISIBLE);

            TextView flag = (TextView) findViewById(R.id.flag);
            flag.setVisibility(TextView.VISIBLE);
        }
    }

    // user wants to toggle to picking now
    public void onClickFlag(View view){
        flag = false;
        pick = true;

        TextView flag = (TextView) findViewById(R.id.flag);
        flag.setVisibility(TextView.INVISIBLE);

        TextView pick = (TextView) findViewById(R.id.pick);
        pick.setVisibility(TextView.VISIBLE);
    }

    // toggle to picking, call if no more flags left
    public void toggleToPicking() {
        flag = false;
        pick = true;

        TextView flag = (TextView) findViewById(R.id.flag);
        flag.setVisibility(TextView.INVISIBLE);

        TextView pick = (TextView) findViewById(R.id.pick);
        pick.setVisibility(TextView.VISIBLE);
    }

    public void hidePickAndFlag() {
        flag = false;
        pick = false;

        TextView flag = (TextView) findViewById(R.id.flag);
        flag.setVisibility(TextView.INVISIBLE);

        TextView pick = (TextView) findViewById(R.id.pick);
        pick.setVisibility(TextView.INVISIBLE);
    }

    public void onClickTV(View view){
        running = true;
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;
        if(pick && tv.getText() == "") {
            // only pick cell if not already flagged
            if(mineLocations.contains(n)){ // the cell picked has a mine
                hidePickAndFlag(); // game over so don't let user pick/flag
                revealMines();
            } else { // the cell picked doesn't have a mine
                if (tv.getCurrentTextColor() == Color.GREEN) {
                    tv.setBackgroundColor(Color.LTGRAY);
                    tv.setTextColor(Color.GRAY);
                    tv.setText(String.valueOf(i)+String.valueOf(j)); // cell value
                }
            }
        } else if (flag && tv.getText() == "") {
            // only flag cell if not already picked
            tv.setText(R.string.flag);
            final TextView flagsLeft = (TextView) findViewById(R.id.flagsLeftValue);
            numFlagsLeft -= 1;
            flagsLeft.setText(String.valueOf(numFlagsLeft));
            if(numFlagsLeft == 0) { // no more flags left, go to pick
                toggleToPicking();
            }
        }
    }

    private void revealMines() {
        for(int i = 0; i < cell_tvs.size(); i++) {
            if(mineLocations.contains(i)) { // cell_tvs[i] is a mine
                cell_tvs.get(i).setBackgroundColor(Color.LTGRAY);
                cell_tvs.get(i).setText(R.string.mine);
            }
        }
    }

    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.timer);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int seconds = clock%60;
                String time = String.format("%03d", seconds);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }
}