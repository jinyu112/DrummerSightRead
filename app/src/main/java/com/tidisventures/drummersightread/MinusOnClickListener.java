package com.tidisventures.drummersightread;

import android.view.View;
import android.widget.TextView;

public class MinusOnClickListener implements View.OnClickListener {

    private TextView tvatt;
    private TextView tvsuc;
    private boolean singleTvFlag = true;
    public MinusOnClickListener (TextView tv_temp1, TextView tv_temp2){
        this.tvsuc = tv_temp2;
        this.tvatt = tv_temp1;
        this.singleTvFlag = false;
    }

    public MinusOnClickListener (TextView tv_temp1){
        this.tvatt = tv_temp1;
        this.singleTvFlag = true;
    }

    @Override
    public void onClick(View v) {
        String tempStr;
        int tempPlusValue = 0;
        if (!singleTvFlag) {
            tempStr = tvsuc.getText().toString();
            try {
                tempPlusValue = Integer.parseInt(tempStr);
            } catch (NumberFormatException ex) {
                tempPlusValue = 1;
                System.err.println("Caught NumberFormatException in minusonclick listener class: "
                        + ex.getMessage());
            }
            tempPlusValue = tempPlusValue - 1;
            if (tempPlusValue<30) tempPlusValue = 30;
            tvsuc.setText(String.format("%d",tempPlusValue));

            tempStr = tvatt.getText().toString();
            try {
                tempPlusValue = Integer.parseInt(tempStr);
            } catch (NumberFormatException ex) {
                tempPlusValue = 1;
                System.err.println("Caught NumberFormatException in minusonclick listener class: "
                        + ex.getMessage());
            }
            tempPlusValue = tempPlusValue - 1;
            if (tempPlusValue<30) tempPlusValue = 30;

            tvatt.setText(String.format("%d",tempPlusValue));
        } else {
            tempStr = tvatt.getText().toString();
            try {
                tempPlusValue = Integer.parseInt(tempStr);
            } catch (NumberFormatException ex) {
                tempPlusValue = 1;
                System.err.println("Caught NumberFormatException in minusonclick listener class: "
                        + ex.getMessage());
            }
            tempPlusValue = tempPlusValue - 1;
            if (tempPlusValue<30) tempPlusValue = 30;
            tvatt.setText(String.format("%d",tempPlusValue));
        }
    }
}