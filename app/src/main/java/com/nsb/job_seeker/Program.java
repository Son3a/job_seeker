package com.nsb.job_seeker;

import static java.lang.Math.abs;

import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class Program {
    public static String token = "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2NDRjMGUzMDU0ZTIxZTk3YjQzMzljYWYiLCJyb2xlIjoiYWRtaW4iLCJpYXQiOjE2ODI4NDYyMzUsImV4cCI6MTY4Mjg0OTgzNX0.d5eo4U8JXNqKKQeP6qf1ppbJA-ZzbQnVt0Z2hUF-eRU";
    public static String idUser = "643d7c3decdddca0bf7de48b";

    public static String formatSalary(String salary) {
        if(!salary.matches(".*\\d.*")) return salary;
        NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator('.');
        dfs.setMonetaryDecimalSeparator('.');
        df.setMaximumFractionDigits(0);
        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);
        return df.format(Integer.parseInt(salary)).substring(1);
    }

    public static long calculateTime(String timeCreate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date timeNow = new Date(System.currentTimeMillis());
        Date create = format.parse(timeCreate);
        long difference = abs(timeNow.getTime() - create.getTime());
        return difference;
    }

    public static String setTime(String timeCreate) throws ParseException {
        long time = calculateTime(timeCreate);
        int s, m, h, date, month, year;

        s = (int) (time / 1000);
        m = s / 60;
        h = m / 60;
        date = h / 60;
        month = date / 30;
        year = month / 365;

        if (year != 0) return year + " năm";
        if (month != 0) return month + " tháng";
        if (date != 0) return date + " ngày";
        if (h != 0) return h + " giờ";
        if (m != 0) return m + " phút";
        return null;
    }

    public static String formatTimeDDMMYYYY(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");

        String newTime = output.format(sdf.parse(time));
        return newTime;
    }

    public static boolean checkValidDeadline(int day, int month, int year) {
        LocalDate currentdate = LocalDate.now();
        int currentDay = currentdate.getDayOfMonth();
        //Getting the current month
        int currentMonth = currentdate.getMonthValue();
        //getting the current year
        int currentYear = currentdate.getYear();

        if (year < currentYear) return false;
        if (month < currentMonth) return false;
        if (day < currentDay) return false;

        return true;
    }

    public static void addNewLine(CharSequence text, int lengthBefore, int lengthAfter, EditText edtText) {
        if (lengthAfter > lengthBefore) {
            if (text.toString().length() == 1) {
                text = "• " + text;
                edtText.setText(text);
                edtText.setSelection(edtText.getText().length());
            }

            if (text.toString().endsWith("\n")) {
                text = text.toString().replace("\n", "\n• ");
                text = text.toString().replace("• •", "•");
                text = text.toString().replace("\n• \n• ", "\n• ");
                text = text.toString().replace("• \n• ", "• ");
                edtText.setText(text);
                edtText.setSelection(edtText.getText().length());
            }
        }
    }

    public static String formatStringFromBullet(String oldString) {
        String newString = "";
        String temp = "";
        String[] listString = oldString.substring(1).split("•");
        for (int i = 0; i < listString.length; i++) {
            temp = listString[i].trim().replace(".", "");
            newString = newString + temp + ".";
        }
        return newString;
    }
}
