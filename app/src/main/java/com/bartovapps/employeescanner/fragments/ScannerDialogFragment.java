package com.bartovapps.employeescanner.fragments;

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bartovapps.employeescanner.CameraPreview;
import com.bartovapps.employeescanner.R;
import com.bartovapps.employeescanner.model.Employee;
import com.bartovapps.employeescanner.model.EmployeesDbOpenHelper;
import com.bartovapps.employeescanner.model.EmployeesProvider;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * Created by BartovMoti on 10/01/16.
 */
public class ScannerDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = ScannerDialogFragment.class.getSimpleName();
    ImageButton btOk;
    ImageButton btTryAgain;
    TextView tvScanText;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private Image barcode;
    FrameLayout mPreviewLayout;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;
    ScannerEventListener mEventListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        autoFocusHandler = new Handler();


        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scan_dialog_layout, container, false);
        tvScanText = (TextView) view.findViewById(R.id.tvScanText);
        btOk = (ImageButton) view.findViewById(R.id.btOk);
        btOk.setOnClickListener(this);
        btTryAgain = (ImageButton) view.findViewById(R.id.btCancel);
        btTryAgain.setOnClickListener(this);
        mPreviewLayout = (FrameLayout) view.findViewById(R.id.flPreview);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCamera = getCameraInstance();
        if (mCamera == null) {
            Log.i(TAG, "onCreate: Camera = null!!");
            Toast.makeText(getActivity(), "Unable to open rear camera!", Toast.LENGTH_LONG).show();
            dismiss();
        }

        mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera, previewCb, autoFocusCB);
        mPreview.setBackgroundResource(R.drawable.scan_dialog_background);
        mPreviewLayout.addView(mPreview);
    }

    public void setScanEventListener(ScannerEventListener eventListener) {
        mEventListener = eventListener;
    }


    @Override
    public void onClick(View view) {

        if (view == btTryAgain) {
            btTryAgain.setVisibility(View.INVISIBLE);
            btOk.setVisibility(View.INVISIBLE);
            tvScanText.setText(null);

            if (barcodeScanned) {
                barcodeScanned = false;
                mCamera.setPreviewCallback(previewCb);
                mCamera.startPreview();
                previewing = true;
                mCamera.autoFocus(autoFocusCB);
            }
        }
        if (view == btOk) {
            addEmployeeToDb();
            dismiss();
        }
    }


    private void addEmployeeToDb() {
        Log.i(TAG, "About to add employee to db via Content Provider...");
        final String tagId = tvScanText.getText().toString();

        Employee employee = new Employee();
        employee.setTag_id(tagId);
        employee.setArrived(true);

//        EmployeesDataSource dataSource = new EmployeesDataSource(getActivity());
//        dataSource.open();
//        employee = dataSource.insert(employee);
//        dataSource.close();

        ContentValues values = new ContentValues();
        values.put(EmployeesDbOpenHelper.COLUMN_TAG_ID, tagId);
        values.put(EmployeesDbOpenHelper.COLUMN_ARRIVED, employee.isArrived() ? 1 : 0);

        Uri uri = getActivity().getContentResolver().insert(EmployeesProvider.CONTENT_URI, values);

        if(uri != null){
            Log.i(TAG, "inserted via content provider");
            if(mEventListener != null){
                mEventListener.onScanComplete(employee.getTag_id());
            }
        }else{
            Log.i(TAG, "Failed to insert to db via content provider ");
            if(mEventListener != null){
                mEventListener.onScanFailed("Failed to add to database");
            }
        }
    }


    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);


        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);

            if (scanner == null) {
                Log.i(TAG, "onAutoFocus: Scanner is null");
            }

            if (barcode == null) {
                Log.i(TAG, "onAutoFocus: barcode is null");
            }

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                btOk.setVisibility(View.VISIBLE);
                btTryAgain.setVisibility(View.VISIBLE);
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                playBeep();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    tvScanText.setText(sym.getData());
                    barcodeScanned = true;
                }
            }
        }
    };

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
        dismiss();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private void playBeep() {
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 100);
    }

    public interface ScannerEventListener {
        void onScanComplete(String scanStr);
        void onScanFailed(String message);
    }


}
