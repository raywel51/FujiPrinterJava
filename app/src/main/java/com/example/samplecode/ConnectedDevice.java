// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode;

import android.support.annotation.NonNull;

import com.fujitsu.fcl.ftp2166000r0.ftppos.ConnectionType;

import java.io.Serializable;
import java.util.UUID;


public class ConnectedDevice implements Serializable
{
    final private String mDeviceName;



    final private ConnectionType mConnectionType;

    /**
     * ID for BaseAdapter#getId()<br>
     */
    final private long mId;

    public ConnectedDevice(@NonNull final String deviceName,
                           @NonNull final ConnectionType connectionType)
    {
        mDeviceName = deviceName;
        mConnectionType = connectionType;
        mId = Math.abs(UUID.randomUUID().hashCode());
    }

    /**
     * Printer name
     * @return
     */
    public String getName()
    {
        return mDeviceName;
    }



    public ConnectionType getConnectionType()
    {
        return mConnectionType;
    }

    public long getId()
    {
        return mId;
    }
}
