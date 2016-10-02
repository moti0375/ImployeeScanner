package com.bartovapps.imployeescanner.fragments;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
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

import com.bartovapps.imployeescanner.CameraPreview;
import com.bartovapps.imployeescanner.R;

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

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();
        if(mCamera != null){
            Log.i(TAG, "got camera instance" );
        }else{
            Log.i(TAG, "onCreate: Camera = null!!");
        }

        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

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

        mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera, previewCb, autoFocusCB);
        mPreview.setBackgroundResource(R.drawable.scan_dialog_background);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.flPreview);
        preview.addView(mPreview);
        return view;
    }


    @Override
    public void onClick(View view) {

        if(view == btTryAgain){
            btTryAgain.setVisibility(View.INVISIBLE);
            btOk.setVisibility(View.INVISIBLE);
            if (barcodeScanned) {
                barcodeScanned = false;
                mCamera.setPreviewCallback(previewCb);
                mCamera.startPreview();
                previewing = true;
                mCamera.autoFocus(autoFocusCB);
            }
        }
        if (view == btOk) {
            dismiss();
        }
    }


    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing) {
                tvScanText.setText("Scanning...");
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

            if(scanner == null){
                Log.i(TAG, "onAutoFocus: Scanner is null");
            }

            if (barcode == null){
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
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private void playBeep(){
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 100);
    }
}
