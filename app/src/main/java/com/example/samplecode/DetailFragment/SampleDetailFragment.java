// Copyright (c) FUJITSU COMPONENT LIMITED. All rights reserved.
// Licensed under the MIT License.

package com.example.samplecode.DetailFragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.samplecode.ConnectedDevice;
import com.example.samplecode.ConnectedUsbPrinter;
import com.example.samplecode.DiscoverPrinterActivity;
import com.example.samplecode.R;
import com.example.samplecode.Utility;
import com.fujitsu.fcl.ftp2166000r0.ftppos.ConnectionType;
import com.fujitsu.fcl.ftp2166000r0.ftppos.FtpConst;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PosException;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PosPrinter;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PosPrinterConst;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrinterConfiguration;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrinterModel;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrintingQuality;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrintingResolution;
import com.fujitsu.fcl.ftp2166000r0.ftppos.PrintingWidth;
import com.fujitsu.fcl.ftp2166000r0.ftppos.events.StatusUpdateEvent;
import com.fujitsu.fcl.ftp2166000r0.ftppos.events.StatusUpdateListener;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class SampleDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    public static final int REQUEST_CODE_TO_DISCOVER_PRINTER_ACTIVITY = 1000;

    private static final int REQUEST_CODE_TO_CAMERA = 1001;

    public static final int REQUEST_CODE_CAMERA_PERMISSION = 1002;

    private TextView mTextViewDeviceName = null;


    private Uri mPhotoUri;

    private PrinterModel mModel = null;

    @NonNull
    private PosPrinter mPosPrinter = new PosPrinter();

    private final Button[] mPrintingButtons = new Button[PrintingButtonEnum.values().length];

    private enum PrintingButtonEnum {
        Receipt,
        Text,
        QrShiftJis,
        QrUtf8,
        QrGbk,
        Bitmap,
        Close,
        Camera,
        MarkFeed,
        CutPaper,
        SendBinary;

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SampleDetailFragment() {
        mPosPrinter.addStatusUpdateListener(generateStatusUpdateListener());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.


        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.sample_detail, container, false);

        attachViewItemToField(rootView);

        final Button connectButton = rootView.findViewById(R.id.buttonSampleConnect);
        connectButton.setOnClickListener(generateButtonConnectOnClickListener());

        final Button discoverPrinterButton = rootView.findViewById(
                R.id.buttonSampleDiscoverPrinter);
        discoverPrinterButton.setOnClickListener(generateButtonSelectPrinterOnClickListener());

        final Button descriptionButton = rootView.findViewById(R.id.buttonDescription);
        descriptionButton.setOnClickListener(generateButtonDescriptionOnClickListener());

        changeButtonPrintEnable(false);

        return rootView;
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TO_DISCOVER_PRINTER_ACTIVITY) {
            if (data == null) {
                return;
            }
            final ConnectedDevice connectedDevice = (ConnectedDevice) data.getSerializableExtra(
                    DiscoverPrinterActivity.EXTRA_DATA_SELECTED_PRINTER);
            if (connectedDevice == null) {
                return;
            }

            mTextViewDeviceName.setText(connectedDevice.getName());

            setRadioButtonConnectionState(connectedDevice.getConnectionType());

            if (connectedDevice instanceof ConnectedUsbPrinter) {
                ConnectedUsbPrinter usbPrinter = (ConnectedUsbPrinter) connectedDevice;

                setRadioButtonModelState(usbPrinter.model);
            }

        } else if (requestCode == REQUEST_CODE_TO_CAMERA) {
            if (data != null && data.getExtras() != null) {
                //Data include only thumbnail.
                final Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            }

            try {
                final Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(
                        getActivity().getContentResolver(), mPhotoUri);
                if (bitmap1 != null) {
                    printBitmap(bitmap1);
                }
            } catch (IOException exception) {
                Utility.displayOkDialog(getActivity(), exception.toString());
            }
        }
    }


    /**
     * Attach view to field
     *
     * @param view View of this fragment
     */
    private void attachViewItemToField(View view) {

        mTextViewDeviceName = view.findViewById(R.id.editTextDeviceName);

        mPrintingButtons[PrintingButtonEnum.Receipt.ordinal()] = view.findViewById(R.id.buttonPrintReceipt);

        mPrintingButtons[PrintingButtonEnum.QrShiftJis.ordinal()] = view.findViewById(R.id.buttonQrShiftJis);
        mPrintingButtons[PrintingButtonEnum.QrUtf8.ordinal()] = view.findViewById(R.id.buttonQrUtf8);
        mPrintingButtons[PrintingButtonEnum.QrGbk.ordinal()] = view.findViewById(R.id.buttonQrGbk);

        mPrintingButtons[PrintingButtonEnum.Text.ordinal()] = view.findViewById(R.id.buttonPrintText);

        mPrintingButtons[PrintingButtonEnum.Bitmap.ordinal()] = view.findViewById(R.id.buttonPrintBitmap);

        mPrintingButtons[PrintingButtonEnum.Camera.ordinal()] = view.findViewById(R.id.buttonPrintBitmapCamera);

        mPrintingButtons[PrintingButtonEnum.MarkFeed.ordinal()] = view.findViewById(R.id.buttonMarkFeed);

        mPrintingButtons[PrintingButtonEnum.CutPaper.ordinal()] = view.findViewById(R.id.buttonCutPaper);

        mPrintingButtons[PrintingButtonEnum.Close.ordinal()] = view.findViewById(R.id.buttonClose);

        mPrintingButtons[PrintingButtonEnum.Receipt.ordinal()].setOnClickListener(
                generateButtonPrintReceiptOnClickListener());

        mPrintingButtons[PrintingButtonEnum.SendBinary.ordinal()] = view.findViewById(R.id.buttonSendBinary);

        mPrintingButtons[PrintingButtonEnum.Text.ordinal()].setOnClickListener(
                generateButtonPrintTextOnClickListener());

        mPrintingButtons[PrintingButtonEnum.QrShiftJis.ordinal()].setOnClickListener(
                generateButtonPrintQrShiftJis());
        mPrintingButtons[PrintingButtonEnum.QrUtf8.ordinal()].setOnClickListener(
                generateButtonPrintQrUtf8());
        mPrintingButtons[PrintingButtonEnum.QrGbk.ordinal()].setOnClickListener(
                generateButtonPrintQrGbk());

        mPrintingButtons[PrintingButtonEnum.Bitmap.ordinal()].setOnClickListener(
                generateButtonPrintBitmapOnClickListener());

        mPrintingButtons[PrintingButtonEnum.Camera.ordinal()].setOnClickListener(
                generateButtonPrintBitmapFromCameraOnClickListener());

        mPrintingButtons[PrintingButtonEnum.Close.ordinal()].setOnClickListener(
                generateButtonCloseOnClickListener());

        mPrintingButtons[PrintingButtonEnum.MarkFeed.ordinal()].setOnClickListener(
                generateButtonMarkFeedOnClickListener());

        mPrintingButtons[PrintingButtonEnum.CutPaper.ordinal()].setOnClickListener(
                generateButtonCutPaperOnClickListener());

        mPrintingButtons[PrintingButtonEnum.SendBinary.ordinal()].setOnClickListener(
                generateButtonSendBinaryOnClickListener());

    }


    private void changeButtonPrintEnable(final boolean enable) {
        for (Button item : mPrintingButtons) {
            item.setEnabled(enable);
        }

    }


    private void changeButtonConnectEnable(final boolean enable) {
        final Button connect = getView().findViewById(R.id.buttonSampleConnect);
        connect.setEnabled(enable);
    }

    private View.OnClickListener generateButtonSelectPrinterOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final Context context = getContext();
                final Intent intent = new Intent(context, DiscoverPrinterActivity.class);

                startActivityForResult(intent, REQUEST_CODE_TO_DISCOVER_PRINTER_ACTIVITY);

            }
        };
    }

    private View.OnClickListener generateButtonDescriptionOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.displayOkDialog(getActivity(), mPosPrinter.getDescription() + "\n" +
                        "Version " + mPosPrinter.getVersion());
            }
        };
    }

    private View.OnClickListener generateButtonConnectOnClickListener() {

        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String deviceName = ((EditText) getView().findViewById(
                        R.id.editTextDeviceName)).getText().toString();

                final ConnectionType connection = getRadioButtonConnectionState();

                mModel = getRadioButtonModelState();

                final RadioGroup radioGroupModel = getView().findViewById(R.id.radioGroupSampleModel);
                if (radioGroupModel.getCheckedRadioButtonId() == -1) {
                    Utility.displayOkDialog(getActivity(), "Please select model.");
                    return;
                }


                final PrintingWidth width = getRadioButtonStateWidth();
                if (width == null) {
                    Utility.displayOkDialog(getActivity(), "Please select width");
                    return;
                }

                try {
                    final PrintingQuality quality = PrintingQuality.createWithEnergy(8,
                            PrintingQuality.DEFAULT_ENERGY);
                    PrinterConfiguration deviceConfiguration = new PrinterConfiguration(mModel,
                            connection,
                            deviceName,
                            width,
                            PrintingResolution.Resolution203,
                            quality);


                    mPosPrinter.open(deviceConfiguration, getActivity().getApplicationContext());
                    mPosPrinter.claim(2000);

                    mPosPrinter.setDeviceEnabled(true);

                    //set Japanese character set
                    mPosPrinter.setCharacterSet(126);

                    changeButtonPrintEnable(true);

                    changeButtonConnectEnable(false);

                } catch (PosException exception) {
                    try {
                        mPosPrinter.close();
                    } catch (PosException closeException) {

                    }
                    Utility.displayOkDialogForFtpException(getActivity(), exception);
                }
            }
        };


    }


    View.OnClickListener generateButtonPrintReceiptOnClickListener() {

        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {

                    mPosPrinter.transactionPrint(PosPrinterConst.PTR_S_RECEIPT,
                            PosPrinterConst.PTR_TP_TRANSACTION);

                    mPosPrinter.printNormal(PosPrinterConst.PTR_S_RECEIPT,
                            "FUJITSU COMPONENT LIMITED\n\n\n\n");

                    mPosPrinter.printNormal(PosPrinterConst.PTR_S_RECEIPT,
                            "นโยบายคุกกี้ฉบับนี้ได้อธิบายความหมายและวิธีการใช้คุกกี้ของเว็บไซต์ www.8interactive.co.th ซึ่งให้บริการโดยบริษัท ออคตากอน อินเทอร์แอคทีฟ จำกัด ต่อไปนี้จะเรียกว่า “บริษัท” ทั้งนี้ โปรดศึกษานโยบายคุกกี้ฉบับนี้เพื่อให้ท่านสามารถเข้าใจแนวปฏิบัติของบริษัทเกี่ยวกับการเก็บรวบรวม ใช้ หรือการเปิดเผยคุกกี้ รวมถึงทางเลือกในการใช้คุกกี้ของบริษัท การเข้าสู่เว็บไซต์นี้ถือว่าท่านได้อนุญาตให้บริษัทใช้คุกกี้ตามนโยบายคุกกี้ที่มีรายละเอียด ดังต่อไปนี้");

                    mPosPrinter.printNormal(PosPrinterConst.PTR_S_RECEIPT, "QR\n");
                    //QR
                    mPosPrinter.printBarCode(PosPrinterConst.PTR_S_RECEIPT, "FCL printer",
                            PosPrinterConst.PTR_BCS_QRCODE,
                            200, 200, PosPrinterConst.PTR_BC_CENTER,
                            PosPrinterConst.PTR_BC_TEXT_NONE);


                    mPosPrinter.printNormal(PosPrinterConst.PTR_S_RECEIPT, "ITF\n");
                    mPosPrinter.printBarCode(PosPrinterConst.PTR_S_RECEIPT, "602030",
                            PosPrinterConst.PTR_BCS_ITF,
                            100, 200, PosPrinterConst.PTR_BC_CENTER,
                            PosPrinterConst.PTR_BC_TEXT_BELOW);

                    mPosPrinter.printNormal(PosPrinterConst.PTR_S_RECEIPT, "EAN8\n");
                    mPosPrinter.printBarCode(PosPrinterConst.PTR_S_RECEIPT, "12345678",
                            PosPrinterConst.PTR_BCS_EAN8, 100, 200, PosPrinterConst.PTR_BC_CENTER,
                            PosPrinterConst.PTR_BC_TEXT_ABOVE);

                    final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fclprinter);
                    int width = PosPrinterConst.PTR_BM_ASIS;
                    if (bitmap.getWidth() > mPosPrinter.getRecLineWidth()) {
                        width = mPosPrinter.getRecLineWidth();
                    }

                    mPosPrinter.printBitmap(PosPrinterConst.PTR_S_RECEIPT, bitmap,
                            width, PosPrinterConst.PTR_BM_LEFT);


                    final int lineToPaperCut = mPosPrinter.getRecLinesToPaperCut();
                    for (int i = 0; i < lineToPaperCut; i++) {
                        mPosPrinter.printNormal(PosPrinterConst.PTR_S_RECEIPT, "\n");
                    }
                    if (mModel != PrinterModel.Ftp_62hDsl100) {
                        mPosPrinter.cutPaper(100);
                    }

                    mPosPrinter.transactionPrint(PosPrinterConst.PTR_S_RECEIPT,
                            PosPrinterConst.PTR_TP_NORMAL);

                } catch (PosException exception) {
                    Utility.displayOkDialogForFtpException(getActivity(), exception);
                }
            }
        };
    }


    private View.OnClickListener generateButtonPrintTextOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final EditText editText = getView().findViewById(R.id.editTextPrintText);
                final String text = editText.getText().toString() + "\n";

                try {
                    mPosPrinter.printNormal(PosPrinterConst.PTR_S_RECEIPT, text);
                } catch (PosException exception) {
                    Utility.displayOkDialog(getActivity(), "Text printing is failed.");
                }
            }
        };
    }

    private View.OnClickListener generateButtonPrintQr(final int encoding) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = getView().findViewById(R.id.editTextPrintQr);
                final String text = editText.getText().toString();

                try {
                    mPosPrinter.directIO(FtpConst.PTR_DIO_SET_QR_ENCODING, new int[]{encoding}, null);


                    mPosPrinter.printBarCode(PosPrinterConst.PTR_S_RECEIPT, text,
                            PosPrinterConst.PTR_BCS_QRCODE, 200, 200,
                            PosPrinterConst.PTR_BC_LEFT,
                            PosPrinterConst.PTR_BC_TEXT_NONE);


                    mPosPrinter.directIO(FtpConst.PTR_DIO_SET_QR_ENCODING, new int[]{FtpConst.PTR_DIO_SHIFT_JIS}, null);
                } catch (PosException e) {
                    Utility.displayOkDialogForFtpException(getActivity(), e);
                }

            }
        };
    }

    private View.OnClickListener generateButtonPrintQrShiftJis() {
        return generateButtonPrintQr(FtpConst.PTR_DIO_SHIFT_JIS);
    }

    private View.OnClickListener generateButtonPrintQrUtf8() {
        return generateButtonPrintQr(FtpConst.PTR_DIO_UTF8);

    }

    private View.OnClickListener generateButtonPrintQrGbk() {
        return generateButtonPrintQr(FtpConst.PTR_DIO_GBK);

    }

    private View.OnClickListener generateButtonPrintBitmapOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.fclprinter);
                printBitmap(bitmap);
            }
        };
    }


    private View.OnClickListener generateButtonPrintBitmapFromCameraOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (haveCameraPermission()) {

                    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                        Utility.displayOkDialog(getActivity(), "Can not start camera.");
                        return;
                    }
                    final File file;

                    try {
                        file = Utility.createImageFile(getActivity());
                    } catch (IOException ioException) {
                        Utility.displayOkDialog(getActivity(), "File access error");
                        return;
                    }

                    final String authority = Objects.requireNonNull(getContext()).getPackageName() + ".fileprovider";

                    mPhotoUri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                            authority, file);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);

                    startActivityForResult(intent, REQUEST_CODE_TO_CAMERA);

                } else {

                    requestCameraPermissionAndPrintBitmapForCamera();
                }
            }
        };
    }


    @Override
    public void onRequestPermissionsResult(
            final int requestCode, @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length != 1 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Utility.displayMessage(getActivity().getApplicationContext(),
                        "Camera permission is granted.");
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private boolean haveCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted
            return true;
        }

        return false;
    }

    private void requestCameraPermissionAndPrintBitmapForCamera() {
        // Here, thisActivity is the current activity


        // Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            // Display a dialog to request permission.
            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                    getActivity().getApplicationContext());
            alertBuilder.setTitle("Description");
            alertBuilder.setMessage("You need permission to take photos with this app");
            alertBuilder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    getActivity(),
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CODE_CAMERA_PERMISSION);
                        }
                    });
            alertBuilder.create();
            alertBuilder.show();

        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA_PERMISSION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }


    }


    private View.OnClickListener generateButtonCloseOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    mPosPrinter.close();
                    changeButtonPrintEnable(false);
                    changeButtonConnectEnable(true);
                } catch (PosException exception) {
                    Utility.displayOkDialogForFtpException(getActivity(), exception);
                }
            }
        };
    }

    private void setRadioButtonModelState(PrinterModel model) {
        final RadioGroup radioGroup = this.getView().findViewById(R.id.radioGroupSampleModel);
        switch (model) {

            case Ftp_62gDsl000:
                radioGroup.check(R.id.radioButtonSampleFtp_62Gdsl000);
                break;

            case Ftp_62hDsl100:
                radioGroup.check(R.id.radioButtonSampleFtp_62hDsl100);
                break;
            case Ftp_629Dsl350:
                radioGroup.check(R.id.radioButtonSampleFtp_629Dsl350);
                break;
        }
    }

    private PrinterModel getRadioButtonModelState() {
        final RadioGroup radioGroup = getView().findViewById(R.id.radioGroupSampleModel);

        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButtonSampleFtp_62Gdsl000:
            default:
                return PrinterModel.Ftp_62gDsl000;

            case R.id.radioButtonSampleFtp_62hDsl100:
                return PrinterModel.Ftp_62hDsl100;

            case R.id.radioButtonSampleFtp_629Dsl350:
                return PrinterModel.Ftp_629Dsl350;
        }

    }

    private void setRadioButtonConnectionState(ConnectionType connectionType) {
        final RadioGroup radioGroup = getView().findViewById(R.id.radioGroupConnection);
        switch (connectionType) {

            case Usb:
            default:
                radioGroup.check(R.id.radioButtonConnectionUsb);
                break;
            case BluetoothClassic:
                radioGroup.check(R.id.radioButtonBluetoothClassic);
                break;
        }
    }


    private ConnectionType getRadioButtonConnectionState() {
        final RadioGroup radioGroup = getView().findViewById(R.id.radioGroupConnection);
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButtonConnectionUsb:
            default:
                return ConnectionType.Usb;

            case R.id.radioButtonBluetoothClassic:
                return ConnectionType.BluetoothClassic;
        }
    }


    @Nullable
    private PrintingWidth getRadioButtonStateWidth() {
        final RadioGroup radioGroup = getView().findViewById(R.id.radioGroupSampleWidth);

        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButtonSample384:
                return PrintingWidth.Width384;
            case R.id.radioButtonSample432:
                return PrintingWidth.Width432;
            case R.id.radioButtonSample576:
                return PrintingWidth.Width576;
            case R.id.radioButtonSample832:
                return PrintingWidth.Width832;

            default:
                return null;

        }
    }


    private void printBitmap(final Bitmap bitmap) {
        try {
            int width = PosPrinterConst.PTR_BM_ASIS;
            if (bitmap.getWidth() > mPosPrinter.getRecLineWidth()) {
                width = mPosPrinter.getRecLineWidth();
            }

            mPosPrinter.printBitmap(PosPrinterConst.PTR_S_RECEIPT, bitmap,
                    width, PosPrinterConst.PTR_BM_LEFT);
        } catch (PosException exception) {
            Utility.displayOkDialogForFtpException(getActivity(), exception);
        }
    }

    private StatusUpdateListener generateStatusUpdateListener() {
        return new StatusUpdateListener() {
            @Override
            public void statusUpdateOccurred(final StatusUpdateEvent e) {
                Utility.displayToastForStatusUpdateEvent(getActivity(), e);


            }
        };
    }

    private View.OnClickListener generateButtonMarkFeedOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    mPosPrinter.markFeed(PosPrinterConst.PTR_MF_TO_NEXT_TOF);
                } catch (PosException exception) {
                    Utility.displayOkDialogForFtpException(getActivity(), exception);
                }
            }
        };
    }

    private View.OnClickListener generateButtonCutPaperOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    mPosPrinter.cutPaper(100);
                } catch (PosException exception) {
                    Utility.displayOkDialogForFtpException(getActivity(), exception);
                }
            }
        };
    }

    private View.OnClickListener generateButtonSendBinaryOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    byte[] binary = new byte[4];
                    binary[0] = 0x30;
                    binary[1] = 0x31;
                    binary[2] = 0x32;
                    binary[3] = 0x0A;


                    mPosPrinter.directIO(FtpConst.PTR_DIO_SEND_BINARY_DATA, null, binary);
                } catch (PosException exception) {
                    Utility.displayOkDialogForFtpException(getActivity(), exception);
                }
            }
        };
    }


}

