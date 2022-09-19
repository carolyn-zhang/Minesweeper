package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.content.Intent;
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
    private int numNonMinesRevealed = 0;
    private boolean running = false;
    private boolean flag = false;
    private boolean pick = true;
    private boolean won = false;
    private boolean gameOver = false;
    private boolean bombsFinishedRevealing = false;

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

        // dynamically add cells
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
            System.out.println(mineLocations.get(i) + " row: " + mineLocations.get(i)/COLUMN_COUNT +
                    " col: " + mineLocations.get(i)%COLUMN_COUNT);
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

    private int getNumNeighboringMines(int n) {
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;
        int numMines = 0;
        if(j - 1 >= 0) { // left
            if(mineLocations.contains(n - 1))
                numMines += 1;
        }
        if(i - 1 >= 0 && j - 1 >= 0) { // topLeft
            if(mineLocations.contains(n - 8 - 1))
                numMines += 1;
        }
        if(i - 1 >= 0) { // top
            if(mineLocations.contains(n - 8))
                numMines += 1;
        }
        if(i - 1 >= 0 && j + 1 <= 7) { // topRight
            if(mineLocations.contains(n - 8 + 1))
                numMines += 1;
        }
        if(j + 1 <= 7) { // right
            if(mineLocations.contains(n + 1))
                numMines += 1;
        }
        if(i + 1 <= 9 && j + 1 <= 7) { // bottomRight
            if(mineLocations.contains(n + 8 + 1))
                numMines += 1;
        }
        if(i + 1 <= 9) { // bottom
            if(mineLocations.contains(n + 8))
                numMines += 1;
        }
        if(i + 1 <= 9 && j - 1 >= 0) { // leftBottom
            if(mineLocations.contains(n + 8 - 1))
                numMines += 1;
        }
        return numMines;
    }

    // user wants to toggle to setting flags now
    public void onClickPick(View view){
        pick = false;
        flag = true;

        TextView pick = (TextView) findViewById(R.id.pick);
        pick.setVisibility(TextView.INVISIBLE);

        TextView flag = (TextView) findViewById(R.id.flag);
        flag.setVisibility(TextView.VISIBLE);
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

    public void hidePickAndFlag() {
        flag = false;
        pick = false;

        TextView flag = (TextView) findViewById(R.id.flag);
        flag.setVisibility(TextView.INVISIBLE);

        TextView pick = (TextView) findViewById(R.id.pick);
        pick.setVisibility(TextView.INVISIBLE);
    }

    public void onClickTV(View view){
        if(!running && !gameOver)
            running = true; // start timer
        if(bombsFinishedRevealing){
            showResultsPage();
        }

        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        if(pick && tv.getText() == "") {
            // only pick cell if not already flagged
            if(mineLocations.contains(n)){
                // the cell picked has a mine, game has been lost
                hidePickAndFlag(); // game over so don't let user pick/flag
                running = false; // stop timer
                gameOver = true;
                // reveal mine that was picked, followed by the other mines
                revealMines(n);
            } else { // the cell picked doesn't have a mine
                revealCell(n);
                if(getNumNeighboringMines(n) == 0) {
                    revealAdjacentCells(n);
                }
                if(numNonMinesRevealed == 76) { // 80 cells total - 4 mines
                    // game has been won
                    hidePickAndFlag(); // game over so don't let user pick/flag
                    won = true;
                    running = false; // stop timer
                    gameOver = true;
                    revealMines(n);
                }
            }
        } else if (flag && tv.getText() == "") {
            // only flag cell if not already picked
            tv.setText(R.string.flag);
            final TextView flagsLeft = (TextView) findViewById(R.id.flagsLeftValue);
            numFlagsLeft -= 1;
            flagsLeft.setText(String.valueOf(numFlagsLeft));
        } else if(flag && tv.getText().toString().equals(getString(R.string.flag))) {
            // un-flag cell if already flagged
            tv.setText("");
            final TextView flagsLeft = (TextView) findViewById(R.id.flagsLeftValue);
            numFlagsLeft += 1;
            flagsLeft.setText(String.valueOf(numFlagsLeft));
        }
    }

    private void revealMines(int n) {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!mineLocations.isEmpty()) {
                    if(mineLocations.contains(Integer.valueOf(n))){
                        if(!won) {
                            // leave cell background green if won game
                            cell_tvs.get(n).setBackgroundColor(Color.LTGRAY);
                        }
                        cell_tvs.get(n).setText(R.string.mine);
                        mineLocations.remove(Integer.valueOf(n));
                    } else {
                        if(!won) {
                            // leave cell background green if won game
                            cell_tvs.get(mineLocations.get(0)).setBackgroundColor(Color.LTGRAY);
                        }
                        cell_tvs.get(mineLocations.get(0)).setText(R.string.mine);
                        mineLocations.remove(0);
                    }
                } else {
                    handler.removeCallbacksAndMessages(null);
                    xIncorrectFlags();
                    bombsFinishedRevealing = true;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    // after game has been lost, reveal any incorrectly flagged cells with an "X"
    private void xIncorrectFlags() {
        for(int i = 0; i < cell_tvs.size(); i++) {
            if( cell_tvs.get(i).getText().toString().equals(getString(R.string.flag))) {
                // user incorrectly flagged a non-mine cell
                cell_tvs.get(i).setTextColor(Color.RED);
                cell_tvs.get(i).setText("X");
            }
        }
    }

    // only handles non-mine cells
    private void revealCell(int n) {
        int numNeighboringMines = getNumNeighboringMines(n);
        cell_tvs.get(n).setBackgroundColor(Color.LTGRAY);
        cell_tvs.get(n).setTextColor(Color.GRAY);
        cell_tvs.get(n).setText(String.valueOf(numNeighboringMines));

        if(numNeighboringMines == 0) {
            cell_tvs.get(n).setTextColor(Color.LTGRAY);
            revealAdjacentCells(n);
        }
        numNonMinesRevealed += 1;
    }

    private void revealAdjacentCells(int n) {
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;

        if(j - 1 >= 0 && cell_tvs.get(n - 1).getText() == "") { // left
            revealCell(n - 1);
        }
        if(i - 1 >= 0 && j - 1 >= 0 && cell_tvs.get(n - 8 - 1).getText() == "") { // topLeft
            revealCell(n - 8 - 1);
        }
        if(i - 1 >= 0 && cell_tvs.get(n - 8).getText() == "") { // top
            revealCell(n - 8);
        }
        if(i - 1 >= 0 && j + 1 <= 7 && cell_tvs.get(n - 8 + 1).getText() == "") { // topRight
            revealCell(n - 8 + 1);
        }
        if(j + 1 <= 7 && cell_tvs.get(n + 1).getText() == "") { // right
            revealCell(n + 1);
        }
        if(i + 1 <= 9 && j + 1 <= 7 && cell_tvs.get(n + 8 + 1).getText() == "") { // bottomRight
            revealCell(n + 8 + 1);
        }
        if(i + 1 <= 9 && cell_tvs.get(n + 8).getText() == "") { // bottom
            revealCell(n + 8);
        }
        if(i + 1 <= 9 && j - 1 >= 0 && cell_tvs.get(n + 8 - 1).getText() == "") { // leftBottom
            revealCell(n + 8 - 1);
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
                handler.postDelayed(this, 1500);
            }
        });
    }

    private void showResultsPage() {
        Intent i = new Intent(getApplicationContext(), ResultsPage.class);
        i.putExtra("clock", clock);
        i.putExtra("won", won);
        startActivity(i);
    }
}