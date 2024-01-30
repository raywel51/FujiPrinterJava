// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.fujitsu.fcl.ftp2166000r0.ftppos.ConnectionType;
import com.fujitsu.fcl.ftp2166000r0.ftppos.FtpConst;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PosException;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PosConst;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PosPrinterConst;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrinterModel;
import com.fujitsu.fcl.ftp2166000r0.ftppos.events.StatusUpdateEvent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility
{



    public static void displayOkDialog(final Activity activity, final String string)
    {
        //Display on main thread.

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(string).setPositiveButton("OK", null);
                builder.show();

            }
        });

    }

    public static String convertConnectionTypeToString(ConnectionType connectionType)
    {
        switch (connectionType) {

            case Usb:
                return "USB";
            case BluetoothClassic:
                return "Bluetooth classic";
            default:
                return "";
        }
    }

    public static ConnectionType convertStringToConnectionType(String connection)
    {
        if (connection.equals("USB")) {
            return ConnectionType.Usb;
        } else if (connection.equals("Bluetooth classic")) {
            return ConnectionType.BluetoothClassic;
        }
        return ConnectionType.Usb;
    }

    public static void displayOkDialogForFtpException(@NonNull final Activity activity,
                                   @NonNull final PosException posException)
    {
        String error;
        switch (posException.getErrorCode()){
            case PosConst.E_CLOSED:
                error = "PosPrinter is closed";
                break;

            case PosConst.E_NOTCLAIMED:
                error = "PosPrinter is not claimed";
                break;

            case PosConst.E_DISABLED:
                error = "PosPrinter is disabled";
                break;

            case PosConst.E_ILLEGAL:
                error = "Illegal";
                break;

            case PosConst.E_NOHARDWARE:
                error = "Printer maybe offline";
                break;

            case PosConst.E_FAILURE:
                error = "Printer cannot perform the requested procedure,";
                break;

            case PosConst.E_TIMEOUT:
                error = "The response from printer is timeout";
                break;

            case PosConst.E_BUSY:
                error = "Printer is busy";
                break;

            case PosConst.E_EXTENDED:
                error = "ErrorCodeExtended\n" +
                        getTextFromErrorCodeExtended(posException.getErrorCodeExtended());
                break;
            default:
                error = "Unknown error code";
        }
        displayOkDialog(activity, error);
    }

    private static String getTextFromErrorCodeExtended(final int extended)
    {
        switch (extended) {
            case PosPrinterConst.EPTR_COVER_OPEN:
                return "Cover is opened";

            case PosPrinterConst.EPTR_REC_EMPTY:
                return "Paper is empty";

            case PosPrinterConst.EPTR_TOOBIG:
                return "Bitmap is too big";

            case PosPrinterConst.EPTR_BADFORMAT:
                return "File format is not supported";

            case FtpConst.EPTR_POWERSUPPLY:
                return "Power supply error";

            case FtpConst.EPTR_CUTTER:
                return "Cutter error";

            case FtpConst.EPTR_HARDWARE:
                return "Hardware error";

            case FtpConst.EPTR_HEADHOT:
                return "Printer head is too hot";

            case FtpConst.EPTR_MARK:
                return "Mark detection error";

            case FtpConst.EPTR_PRESENTER:
                return "Presenter is error";

            default:
                return "Unknown extended code";
        }
    }


    public static void displayMessage(
            @NonNull final Context applicationContext, final String text)
    {
        //Display on main thread.

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show();
            }
        });




    }



    public static void displayToastForStatusUpdateEvent(
            @NonNull final Context applicationContext,
            @NonNull final StatusUpdateEvent statusUpdateEvent)
    {


        String text;
        switch (statusUpdateEvent.getStatus()) {

            case PosPrinterConst.PTR_SUE_COVER_OPEN:
                text = "Cover is opened";
                break;
            case PosPrinterConst.PTR_SUE_COVER_OK:
                text = "Cover is closed.";
                break;
            case PosPrinterConst.PTR_SUE_REC_EMPTY:
                text = "Paper is empty";
                break;
            case PosPrinterConst.PTR_SUE_REC_NEAREMPTY:
                text = "Paper near end";
                break;
            case PosPrinterConst.PTR_SUE_REC_PAPEROK:
                text = "Paper is OK";
                break;
            case PosPrinterConst.PTR_SUE_IDLE:
                text = "Changed to idle state.";
                break;
            case PosConst.SUE_POWER_ONLINE:
                text = "Changed to online";
                break;
            case PosConst.SUE_POWER_OFF_OFFLINE:
                text = "Changed to offline";
                break;
            default:
                text = "Unknown code";
        }
        displayMessage(applicationContext, text);
    }



    public static File createImageFile(@NonNull final Activity activity) throws IOException
    {
        @SuppressLint("SimpleDateFormat") String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir =activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return file;
    }

    public static void addPictureToGallery(@NonNull final String photoPath, @NonNull final Activity activity)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }


}
