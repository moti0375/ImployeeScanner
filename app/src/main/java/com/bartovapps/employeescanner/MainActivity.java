package com.bartovapps.employeescanner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bartovapps.employeescanner.adapters.Divider;
import com.bartovapps.employeescanner.adapters.EmployeesRecyclerAdapter;
import com.bartovapps.employeescanner.adapters.SimpleTouchCallback;
import com.bartovapps.employeescanner.fragments.ScannerDialogFragment;
import com.bartovapps.employeescanner.model.Employee;
import com.bartovapps.employeescanner.model.EmployeesDataSource;
import com.bartovapps.employeescanner.utils.EmployeeScannerUtils;
import com.crittercism.app.Crittercism;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String DIALOG_TAG = "DIALOG";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQ = 300;
    private static final int CAMERA_REQ_CODE = 100;
    private static final int EXTERNAL_STORAGE_REQ_CODE = 200;

    FloatingActionButton mFloatingButton;
    RecyclerView mRecyclerView;
    EmployeesRecyclerAdapter mAdapter;
    Realm mRealm;
    ArrayList<Employee> mEmployeesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), "93d5c608ee654190a35ea163cf5cb82b00555300");
        setContentView(R.layout.activity_main);
        checkPermissions();
        mRealm = Realm.getDefaultInstance();
        setViews();
        loadData();
    }

    private void checkPermissions() {
        String [] permissions=new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.GET_TASKS
        };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission:permissions) {
            if (ContextCompat.checkSelfPermission(this,permission )!= PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQ);
        }

//        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
//                Toast.makeText(MainActivity.this, "This is a reminder regard camera permission...", Toast.LENGTH_SHORT).show();
//            }
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQ_CODE);
//        }
//
//        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                Toast.makeText(MainActivity.this, "This is a reminder regard camera permission...", Toast.LENGTH_SHORT).show();
//            }
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQ_CODE);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int itemId = item.getItemId();
        switch (itemId){
            case R.id.ic_action_share:
                if(mAdapter.getItemCount() > 0){
                    Toast.makeText(MainActivity.this, "About to share list", Toast.LENGTH_SHORT).show();
                    CsvShareTask shareTask = new CsvShareTask();
                    shareTask.execute(mEmployeesList);

                }else{
                    Toast.makeText(MainActivity.this, "No items to share..", Toast.LENGTH_SHORT).show();
                }
                break;
        }



        return true;
    }



    private void loadData() {
//        Employee[] employees = (Employee[]) mRealmResults.toArray();
        EmployeesDataSource dataSource = new EmployeesDataSource(MainActivity.this);
        dataSource.open();
        mEmployeesList = dataSource.findAll();
        dataSource.close();

        if(mEmployeesList != null){
            mAdapter.updateList(mEmployeesList);
        }



    }

    private void setViews() {

        mFloatingButton = (FloatingActionButton) findViewById(R.id.fbAddEmployee);
        if (mFloatingButton == null) {
            Log.i(TAG, "fab is null");
        }
        mFloatingButton.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvEmployeesRecyclerView);
        mRecyclerView.addItemDecoration(new Divider(this, LinearLayoutManager.VERTICAL));
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new EmployeesRecyclerAdapter(MainActivity.this, mEmployeesList);

        mRecyclerView.setAdapter(mAdapter);
        SimpleTouchCallback touchCallback = new SimpleTouchCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(mRecyclerView);

    }

    @Override
    public void onClick(View view) {

        showDialog();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode) {
            case CAMERA_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "This app must have Camera permission to work, goodbye", Toast.LENGTH_LONG).show();
                    finish();
                }
            case EXTERNAL_STORAGE_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "This app must have external storage write permission, goodbye", Toast.LENGTH_LONG).show();
                    finish();
                }
            case PERMISSION_REQ:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "This app must have this permission, goodbye", Toast.LENGTH_LONG).show();
//                    finish();
                }
        }
    }

    private void showDialog() {
        ScannerDialogFragment dialogFragment = new ScannerDialogFragment();
        dialogFragment.setScanEventListener(mEventListener);
        dialogFragment.show(getSupportFragmentManager(), DIALOG_TAG);
    }


    ScannerDialogFragment.ScannerEventListener mEventListener = new ScannerDialogFragment.ScannerEventListener() {
        @Override
        public void onScanComplete(String scanStr) {
            Toast.makeText(MainActivity.this, "Added " + scanStr, Toast.LENGTH_SHORT).show();
            loadData();
        }

        @Override
        public void onScanFailed(String message) {
            Toast.makeText(MainActivity.this, "Failed, " + message, Toast.LENGTH_SHORT).show();
        }
    };

    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        public final String TAG = RealmChangeListener.class.getSimpleName();

        @Override
        public void onChange(Object element) {
            Log.i(TAG, "onChange: ");
            mAdapter.updateList(mEmployeesList);
        }
    };

    class CsvShareTask extends AsyncTask<ArrayList<Employee>, Void, File> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);

            progressDialog.setCancelable(false);
            progressDialog.setIcon(ContextCompat.getDrawable(MainActivity.this, R.mipmap.ic_launcher));
            progressDialog.setTitle(getString(R.string.app_name));
            progressDialog.setMessage("Preparing list for sharing, please wait...");
            progressDialog.show();

        }

        @Override
        protected File doInBackground(ArrayList<Employee>... arrayLists) {
            ArrayList<Employee> list = arrayLists[0];
            File outputCsv = null;
            try {
                outputCsv = EmployeeScannerUtils.realmDataListToCsv(MainActivity.this, list);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return outputCsv;
        }

        @Override
        protected void onPostExecute(File file) {

            if(file != null){
                shareFile(file);
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }else{
                Toast.makeText(MainActivity.this, "Error occurred..", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void shareFile(File outputCsv) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        if(outputCsv.exists()) {
            intentShareFile.setType("application/vnd.ms-excel");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+outputCsv));

            String subject = getString(R.string.app_name) + " - File Sharing: ";

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    subject + outputCsv.getName());
//            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

            startActivity(Intent.createChooser(intentShareFile, subject));
        }
    }
}
