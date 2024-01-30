// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public final class DiscoverPrinterActivity extends AppCompatActivity
{

    public static final String EXTRA_DATA_SELECTED_PRINTER = DiscoverPrinterActivity.class.getPackage().getName() + "SelectedPrinter";


    private boolean mDiscovering = false;

    private Button mDiscoverButton = null;

    @Nullable private PrinterListViewAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mListAdapter = new PrinterListViewAdapter(this.getApplicationContext(),
                                                  new ArrayList<ConnectedDevice>());

        setContentView(R.layout.activity_discover_printer);

        final ListView printerListView = findViewById(R.id.listView1);

        printerListView.setAdapter(mListAdapter);

        printerListView.setOnItemClickListener(generatePrinterListViewItemClickListener());

        mDiscoverButton = findViewById(R.id.buttonDiscover);

        mDiscoverButton.setOnClickListener(generateButtonDiscoverOnClickListener());
    }

    private static void outputLog(String message)
    {
        final String className = DiscoverPrinterActivity.class.getSimpleName();
        Log.d(className, message);
    }


    /**
     * Discover printer.<BR>
     * @return Discovered printers.
     */
    List<ConnectedDevice> discoverPrinter()
    {
        final List<ConnectedDevice> printers = new ArrayList<ConnectedDevice>();

        printers.addAll(DiscoverPrinter.DiscoverUsbPrinter(getApplicationContext()));

        return printers;
    }


    /**
     * Generate OnItemClickListener for click on printer list view
     * @return OnItemClickListener for click on printer list view
     */
    AdapterView.OnItemClickListener generatePrinterListViewItemClickListener()
    {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {


                //Return the previous Activity
                final Intent intent = new Intent();
                intent.putExtra(EXTRA_DATA_SELECTED_PRINTER, mListAdapter.mConnectedDevice.get(position));

                //Set OK and finish activity.
                setResult(Activity.RESULT_OK, intent);
                finish();


            }
        };
        return listener;
    }



    View.OnClickListener generateButtonDiscoverOnClickListener()
    {
        View.OnClickListener listener = new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                if (mDiscovering) {
                    //Discovering...
                } else {

                    mDiscoverButton.setText("Discovering...");
                    mDiscoverButton.setEnabled(false);
                    //Discover on background thread.
                    final DiscoverPrinterTask task = new DiscoverPrinterTask();
                    task.execute();
                }
            }
        };
        return listener;
    }



    class DiscoverPrinterTask extends AsyncTask<Void, Void, List<ConnectedDevice>>
    {
        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         *
         * <p>This method won't be invoked if the task was cancelled.</p>
         * @param connectedDevice The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         */
        @Override
        protected void onPostExecute(
                final List<ConnectedDevice> connectedDevice)
        {
            super.onPostExecute(connectedDevice);
            //Update printer list.
            mListAdapter.update(connectedDevice);
            mDiscoverButton.setText("Discover");
            mDiscoverButton.setEnabled(true);

        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         * @param aVoid The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<ConnectedDevice> doInBackground(final Void... aVoid)
        {
            return discoverPrinter();
        }
    }
}
