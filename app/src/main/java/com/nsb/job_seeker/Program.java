package com.nsb.job_seeker;

import static java.lang.Math.abs;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class Program {
    public static String token = "";
    public static String url_dev = "https://job-seeker-smy5.onrender.com";
    public static String idUser;
    public static String idCompany;
    public static String role = "user";
    public static String url_product = "https://job-seeker-smy5.onrender.com";
    public static String sharedPreferencesName = "JobSharedPreference";
    public static List<String> idListJobSaved;

    public static String formatSalary(String salary) {
        if (!salary.matches(".*\\d.*")) return salary;
        NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator('.');
        dfs.setMonetaryDecimalSeparator('.');
        df.setMaximumFractionDigits(0);
        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);
        return df.format(Integer.parseInt(salary));
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
        LocalDate currentdate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentdate = LocalDate.now();
        }
        int currentDay = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDay = currentdate.getDayOfMonth();
        }
        //Getting the current month
        int currentMonth = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentMonth = currentdate.getMonthValue();
        }
        //getting the current year
        int currentYear = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentYear = currentdate.getYear();
        }

        if(year > currentYear) return true;
        if (year == currentYear && month > currentMonth) return true;
        if (year == currentYear && month == currentMonth && day > currentDay) return true;

        return false;
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

    public static String formatStringToBullet(String string){
        String newString = "";
        String[] listString = string.split("\\.");
        for (int i = 0; i < listString.length; i++) {
            newString = newString + "• " + listString[i] + "\n";
        }

        return newString.substring(0,newString.length()-1);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
