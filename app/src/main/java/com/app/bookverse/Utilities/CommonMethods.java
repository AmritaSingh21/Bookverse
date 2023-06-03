package com.app.bookverse.Utilities;

import android.util.Log;

import com.app.bookverse.Entities.Book;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonMethods {

    public static Date fetchDateFromString(String strDate){
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            date = format.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("Utils", "Error occurred while parsing date.");
        }
        return date;
    }

    public static boolean showMenuOptionsForBook(String userId, Book book) {
        if(book.isSold() || !userId.equals(book.getOwnerId())){
            return false;
        }
        return true;
    }

    public static boolean showBuyOptionForBook(String userId, Book book) {
        if(book.isSold()){
            return false;
        }
        if (userId.equals(book.getOwnerId())) {
            return false;
        }
        return true;
    }

}
