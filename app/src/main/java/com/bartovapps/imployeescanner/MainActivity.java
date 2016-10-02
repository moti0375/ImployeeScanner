package com.bartovapps.imployeescanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.bartovapps.imployeescanner.fragments.ScannerDialogFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String DIALOG_TAG = "DIALOG";

    Button btScanBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();


    }

    private void setViews() {
        btScanBarcode = (Button) findViewById(R.id.btScanBarcode);
        btScanBarcode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        showDialog();
    }

    private void showDialog() {
        ScannerDialogFragment dialogFragment = new ScannerDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), DIALOG_TAG);
    }
}
