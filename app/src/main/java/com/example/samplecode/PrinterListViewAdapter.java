// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;


class PrinterListViewHolder
{
    TextView mTextViewPrinterName;
    TextView mTextViewModel;
    TextView mConnection;

}


/**
 * Adapter class for printer list
 */
class PrinterListViewAdapter extends BaseAdapter
{

    @NonNull final List<ConnectedDevice> mConnectedDevice = new ArrayList<>();
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    PrinterListViewAdapter(Context context, List<ConnectedDevice> connectedDeviceList)
    {
        super();
        mContext = context;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mConnectedDevice.addAll(connectedDeviceList);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     * @return Count of items.
     */
    @Override
    public int getCount()
    {
        return mConnectedDevice.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position)
    {
        return mConnectedDevice.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position)
    {
        return mConnectedDevice.get(position).getId();
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     * <br>
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        PrinterListViewHolder holder = null;

        if (convertView != null) {
            holder = (PrinterListViewHolder)convertView.getTag();
            return convertView;
        }

        convertView = mLayoutInflater.inflate(R.layout.layout_connected_printer_row, parent, false);

        holder = new PrinterListViewHolder();
        holder.mTextViewPrinterName = convertView.findViewById(R.id.textViewDeviceName);
        holder.mTextViewModel = convertView.findViewById(R.id.textViewModel);
        holder.mConnection = convertView.findViewById(R.id.textViewConnection);
        convertView.setTag(holder);



        //Render current row
        final ConnectedDevice device = mConnectedDevice.get(position);
        if (device instanceof ConnectedUsbPrinter) {
            outputLog("USB");
            ConnectedUsbPrinter usbPrinter = (ConnectedUsbPrinter)device;
            switch (usbPrinter.model) {

                case Ftp_62gDsl000:
                    holder.mTextViewModel.setText("FTP-62GDSL000");
                    break;

                case Ftp_62hDsl100:
                    holder.mTextViewModel.setText("FTP-62HDSL100");
                    break;

                case Ftp_629Dsl350:
                    holder.mTextViewModel.setText("FTP-629DSL350");
                    break;
            }

        } else {
            outputLog("Bluetooth");
            //Bluetooth
            holder.mTextViewModel.setText("");
        }

        holder.mTextViewPrinterName.setText(device.getName());
        switch (device.getConnectionType()) {

            case Usb:
                holder.mConnection.setText("USB");
                break;
            case BluetoothClassic:
                holder.mConnection.setText("Bluetooth Classic");
                break;
        }


        return convertView;
    }

    /**
     * Update printer list.
     * @param newList New list
     */
    public void update(List<ConnectedDevice> newList)
    {
        mConnectedDevice.clear();;
        mConnectedDevice.addAll(newList);
        notifyDataSetChanged();
    }

    private static void outputLog(String text)
    {
        Log.d("", text);
    }
}