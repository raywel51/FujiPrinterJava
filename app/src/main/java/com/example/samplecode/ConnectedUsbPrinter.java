// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode;

import android.support.annotation.NonNull;

import com.fujitsu.fcl.ftp2166000r0.ftppos.PrinterModel;
import com.fujitsu.fcl.ftp2166000r0.ftppos.ConnectionType;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrinterModel;

/**
 * Connected USB printer
 */
public class ConnectedUsbPrinter extends ConnectedDevice
{

    /**
     * Printer model
     */
    public final PrinterModel model;


    public ConnectedUsbPrinter(@NonNull final String printerName,
                               @NonNull final ConnectionType connectionType,
                               @NonNull final PrinterModel supportedModel)
    {
        super(printerName, connectionType);
        model = supportedModel;
    }


}
