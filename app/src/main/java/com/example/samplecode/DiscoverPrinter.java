// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode;


import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintService;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.fujitsu.fcl.ftp2166000r0.ftppos.ConnectionType;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrinterModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class DiscoverPrinter {

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";


    private static void outputLog(String message)
    {
        final String className = DiscoverPrinter.class.getSimpleName();
        Log.d(className, message);
    }



    /**
     * Discover USB printer.
     * @return USB printers
     */
    static public List<ConnectedUsbPrinter> DiscoverUsbPrinter(final Context context)
    {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0,
                                                                    new Intent(ACTION_USB_PERMISSION), 0);

        List<ConnectedUsbPrinter> printers = new ArrayList<>();

        try {
            final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            for (UsbDevice device : deviceList.values()) {
                outputLog(device.getProductName());
                outputLog(device.getDeviceName());

                //Request permission, if no permission.
                if (!usbManager.hasPermission(device)) {
                    usbManager.requestPermission(device, permissionIntent);
                    outputLog("No permission");
                } else {
                    outputLog("Have permission");
                }

                switch (device.getProductId()) {
                    case 0x0623://FTP-62GDSL000
                    case 0x6A4:// FTP-62GDSL120
                    case 1579://USB-COM FTP-62GDSL120
                        printers.add(
                                new ConnectedUsbPrinter(device.getDeviceName(), ConnectionType.Usb,
                                                        PrinterModel.Ftp_62gDsl000));
                        break;
                    case 0x0628:
                        printers.add(
                                new ConnectedUsbPrinter(device.getDeviceName(), ConnectionType.Usb,
                                                        PrinterModel.Ftp_62hDsl100));
                        break;
                    case 0x061D:
                    case 0x062F:
                    case 0x0609:
                        printers.add(
                                new ConnectedUsbPrinter(device.getDeviceName(), ConnectionType.Usb,
                                                        PrinterModel.Ftp_629Dsl350));

                        break;

                }

            }


            return printers;

        }catch (Exception exception) {
            outputLog("Not supported USB");
            return new ArrayList<ConnectedUsbPrinter>();
        }
    }






    /**
     * Discover Bluetooth device
     * @return Bluetooth device list
     */
    @NonNull
    static List<ConnectedDevice> DiscoverBluetoothPrinter(final Context applicationContext)
    {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            outputLog("Not supported Bluetooth");
            //Return empty list
            return new ArrayList<ConnectedDevice>();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(applicationContext, "Please Enable Bluetooth"
                    , Toast.LENGTH_SHORT).show();
            //Return empty list
            return new ArrayList<ConnectedDevice>();
        }

        final List<ConnectedDevice> list = new ArrayList<>();

        final Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (final BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                outputLog(device.getName() + "\n" + device.getAddress());
                list.add(new ConnectedDevice(device.getAddress(), ConnectionType.BluetoothClassic));

            }
            return list;
        }

        //Return empty list
        return new ArrayList<ConnectedDevice>();

    }



}