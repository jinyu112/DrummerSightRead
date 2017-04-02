package com.tidisventures.drummersightread;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import com.tidisventures.inappbilling.util.IabHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;


public class MainActivity extends ActionBarActivity {

    public static final String MidiDataID = "MidiDataID";
    public static final String MidiTitleID = "MidiTitleID";
    public static final int settingsRequestCode = 1;

    private MidiPlayer player;   /* The play/stop/rewind toolbar */
    private ArrayList<MidiNote> notes;
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* THe layout */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and sound */
    private long midiCRC;      /* CRC of the midi bytes */
    private int lastStartJin;

    private static InternalDataForAdBoolean checkBasicVersion;
    private static final String ITEM_SKU = "com.tidisventures.drummerpremium"; //purchase ID in google play
    //private static final String ITEM_SKU = "android.test.purchased";
    IInAppBillingService mService;

    // connecting to in app purchase in google play
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        // if connected, check purchases. if purchases match the app, then set the internal basicVersion flag to false
        // meaning this user has purchased the premium version
        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            //Log.d("BJJ","on service connected");
            Bundle ownedItems = null;
            try {
                if (mService != null) {
                    ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                    ArrayList ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    if (ownedItems != null && ownedSkus != null) {
                        int response = ownedItems.getInt("RESPONSE_CODE"); //if returns 0, request was successful
                        int lenSkus = ownedSkus.size();
                        //Log.d("BJJ", "size of ownedskus: " + ownedSkus.size());
                        //Log.d("BJJ", "response: " + response);
                        if (response == 0 && lenSkus > 0) {
                            for (int i = 0; i < lenSkus; ++i) {
                                String sku = (String) ownedSkus.get(i);
                               // Log.d("BJJ", "PURACHSED ITEM " + i + " === " + sku);

                                // if purchases from google play are found and matches the purchase made, set internal flag to false
                                if (sku.equals(ITEM_SKU)) {
                                    checkBasicVersion.upgradeVer_InternalStorage(); //set internal flag to false (meaning app is premium version)
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                //Log.d("BJJ","failed to get purchases");
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //in app purchase initialization
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        //load internal data for basicVersion boolean
        checkBasicVersion = new InternalDataForAdBoolean(this);
        checkBasicVersion.initBasicVersionData(); //this should do nothing if it's not the first time the app is started

        Button button = (Button) findViewById(R.id.butt);
        //button.setEnabled(false);
        AppRater.app_launched(this);

        ImageView imgView = (ImageView) findViewById(R.id.imgview);
        imgView.setImageResource(R.drawable.icon);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    public void goToSightReader(View view) {
        Intent intent = new Intent(this,SightReader.class);
        startActivity(intent);
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(this,Settings.class);
        startActivity(intent);
    }

    public void goToAbout(View view) {
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
