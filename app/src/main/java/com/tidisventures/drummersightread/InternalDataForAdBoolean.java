package com.tidisventures.drummersightread;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class InternalDataForAdBoolean {

    private Context context;
    String filenameInit = "premium";

    public InternalDataForAdBoolean(Context in) {
        this.context = in;
    }

    //use internal storage to save flags
    public void initBasicVersionData() {
        FileOutputStream outputStream;

        //if the file exists, do nothing
        if (fileExistence()) {
            //do nothing

        }
        else { //this should be the first time the class is defined and initialized so create the file and set the value to be true
            try {
                outputStream = context.openFileOutput(filenameInit, context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject("true"); // setting premium version to be true
                oos.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean fileExistence(){
        File file = context.getFileStreamPath(filenameInit);
        return file.exists();
    }


    //this function returns whether or not ads are present
    public boolean checkVersionState() {
        String basicVersionString = "true";
        boolean basicVersion = true;
        try {
            FileInputStream fin = context.openFileInput(filenameInit);
            ObjectInputStream ois = new ObjectInputStream(fin);
            basicVersionString = (String) ois.readObject();
            ois.close();
            fin.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (basicVersionString.equals("true")) {
            basicVersion = true;
        }
        else basicVersion = false;
        return basicVersion;
    }

    public boolean upgradeVer_InternalStorage() {
        String basicVersionString = "false";
        boolean success = false;
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filenameInit, context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(basicVersionString);
            oos.close();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

}