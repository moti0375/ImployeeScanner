package com.bartovapps.employeescanner;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bartovapps.employeescanner.adapters.Divider;
import com.bartovapps.employeescanner.adapters.EmployeesRecyclerAdapter;
import com.bartovapps.employeescanner.adapters.SimpleTouchCallback;
import com.bartovapps.employeescanner.adapters.SwipeListener;
import com.bartovapps.employeescanner.fragments.ScannerDialogFragment;
import com.bartovapps.employeescanner.model.Employee;
import com.bartovapps.employeescanner.model.EmployeesDbOpenHelper;
import com.bartovapps.employeescanner.model.EmployeesProvider;
import com.bartovapps.employeescanner.utils.EmployeeScannerUtils;
import com.crittercism.app.Crittercism;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmChangeListener;

public class MainActivity extends ActionBarActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, SwipeListener {
    public static final String DIALOG_TAG = "DIALOG";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQ = 300;
    private static final int CAMERA_REQ_CODE = 100;
    private static final int EXTERNAL_STORAGE_REQ_CODE = 200;

    FloatingActionButton mFloatingButton;
    RecyclerView mRecyclerView;
    TextView tvListHeader;
    EmployeesRecyclerAdapter mAdapter;
    Cursor mCursor;
    ActionMode mActionMode;
    Toolbar toolbar;
    boolean continuanceScan = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), "93d5c608ee654190a35ea163cf5cb82b00555300");
        setContentView(R.layout.activity_main_coordinator);
        toolbar = (Toolbar) findViewById(R.id.app_bar);

        try {
            setSupportActionBar(toolbar);
        } catch (Throwable t) {
            // WTF SAMSUNG!
        }
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        checkPermissions();
        readPreferences();
//        loadData();
        setViews();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void readPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        continuanceScan = sharedPreferences.getBoolean(getString(R.string.key_cont_scan), false);
    }

    private void checkPermissions() {
        String [] permissions=new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
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
                    Toast.makeText(MainActivity.this, getString(R.string.pd_list_sharing), Toast.LENGTH_SHORT).show();
                    CsvShareTask shareTask = new CsvShareTask();
                    shareTask.execute(EmployeeScannerUtils.getEmployeesFromCursor(mCursor));

                }else{
                    Toast.makeText(MainActivity.this, R.string.no_items_to_share, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.ic_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(settingsIntent, 100);
                break;
        }



        return true;
    }



    private void loadData() {
//        Employee[] employees = (Employee[]) mRealmResults.toArray();
//        EmployeesDataSource dataSource = new EmployeesDataSource(MainActivity.this);
//        dataSource.open();
//        mEmployeesList = dataSource.findAll();
//        dataSource.close();
//
//        if(mEmployeesList != null){
//            mAdapter.updateList(mEmployeesList);
//        }

        mCursor = getContentResolver().query(EmployeesProvider.CONTENT_URI, EmployeesDbOpenHelper.allColumns, null, null, null);
        if(mCursor != null){
            Log.i(TAG, "loadData: cursor columns # = " + mCursor.getColumnCount());
        }

    }

    private void setViews() {

        mFloatingButton = (FloatingActionButton) findViewById(R.id.fbAddEmployee);
        if (mFloatingButton == null) {
            Log.i(TAG, "fab is null");
        }
        mFloatingButton.setOnClickListener(this);
        tvListHeader = (TextView) findViewById(R.id.tvListHeader);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvEmployeesRecyclerView);
//        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(MainActivity.this, mRecyclerView, actionModeCallback, new ClickListener() {
//            @Override
//            public void onClick(View v, int position) {
//                //   Toast.makeText(GpsRecTripsList.this, "RecyclerView item " + position + " clicked..", Toast.LENGTH_SHORT).show();
//                if (mActionMode != null) {
//
//                } else {
//
//                    Log.i(TAG, "List item " + position + " was clicked...");
//
//                }
//            }
//
//            @Override
//            public void onLongClick(View v, int position) {
//                //Longclick is handled by the onLongPress of the RecyclerTouchListener down in this activity..
//            }
//        }));
        mRecyclerView.addItemDecoration(new Divider(this, LinearLayoutManager.VERTICAL));
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new EmployeesRecyclerAdapter(MainActivity.this, null);
        mRecyclerView.setAdapter(mAdapter);
        SimpleTouchCallback touchCallback = new SimpleTouchCallback(this);
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
                    Toast.makeText(MainActivity.this, R.string.permission_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            case EXTERNAL_STORAGE_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, R.string.permission_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            case PERMISSION_REQ:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, R.string.permission_required, Toast.LENGTH_LONG).show();
//                    finish();
                }
        }
    }

    private void showDialog() {
        ScannerDialogFragment dialogFragment = new ScannerDialogFragment();
        Bundle b = new Bundle();
        b.putBoolean(getString(R.string.key_cont_scan), continuanceScan);
        dialogFragment.setArguments(b);
        dialogFragment.setScanEventListener(mEventListener);
        dialogFragment.show(getSupportFragmentManager(), DIALOG_TAG);
    }


    ScannerDialogFragment.ScannerEventListener mEventListener = new ScannerDialogFragment.ScannerEventListener() {
        @Override
        public void onScanComplete(String scanStr) {
//            Toast.makeText(MainActivity.this, "Added " + scanStr, Toast.LENGTH_SHORT).show();
            getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
//            loadData();
//            mAdapter.updateList();
        }

        @Override
        public void onScanFailed(String message) {
            Toast.makeText(MainActivity.this, getString(R.string.scan_failed), Toast.LENGTH_SHORT).show();
        }
    };

    private RealmChangeListener realmChangeListener = new RealmChangeListener() {
        public final String TAG = RealmChangeListener.class.getSimpleName();

        @Override
        public void onChange(Object element) {
            Log.i(TAG, "onChange: ");
//            mAdapter.updateList();
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
            progressDialog.setMessage(getString(R.string.pd_list_sharing));
            progressDialog.show();

        }

        @Override
        protected File doInBackground(ArrayList<Employee>... arrayLists) {

            ArrayList<Employee> list = arrayLists[0];
            File outputCsv = null;
            try {
                for (int i = 0; i < list.size(); i++){
                    list.get(i).setItemNo(i+1);
                }
                outputCsv = EmployeeScannerUtils.dataListToCsv(MainActivity.this, list);
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
                Toast.makeText(MainActivity.this, R.string.share_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void shareFile(File outputCsv) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        if(outputCsv.exists()) {
            intentShareFile.setType("application/vnd.ms-excel");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+outputCsv));

            String subject = getString(R.string.app_name) + " - " + getString(R.string.file_sharing_title) + ": ";

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    subject + outputCsv.getName());
//            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

            startActivity(Intent.createChooser(intentShareFile, subject));
        }
    }

    @Override
    public void onSwipe(int position) {


        if (mCursor != null && mCursor.moveToPosition(position)) {
            int columnIndx = mCursor.getColumnIndex(EmployeesDbOpenHelper.COLUMN_TAG_ID);
            Log.i(TAG, "bindView: column index = " + columnIndx);
            String tagId = mCursor.getString(columnIndx);
            String where = EmployeesDbOpenHelper.COLUMN_TAG_ID + "=\"" + mCursor.getString(columnIndx) + "\"";
            Log.i(TAG, "onSwipe: about to remove item " + where);
            getContentResolver().delete(EmployeesProvider.CONTENT_URI, where, null);
            Log.i(TAG, "item deleted... ");
            getSupportLoaderManager().restartLoader(0, null, this);
//            notifyItemRemoved(position);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader was called");

        return new CursorLoader(this, EmployeesProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished was called");
        mCursor = data;
        mAdapter.updateCursor(data);
        tvListHeader.setText(mCursor.getCount() + getString(R.string.scans));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG, "onLoaderReset was called");
        mAdapter.updateCursor(null);
    }


    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private final String LOG_TAG = RecyclerTouchListener.class.getSimpleName();
        GestureDetector gestureDetector;
        ClickListener clickListener;

        public RecyclerTouchListener(final Activity context, final RecyclerView recyclerView, final android.view.ActionMode.Callback actionModeCallback, final ClickListener clickListener) {
            Log.i(LOG_TAG, "constructor was invoked");
            this.clickListener = clickListener;

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
//                    Log.i(LOG_TAG, "onSingleTapUp was invoked..: " + e);
//                    return super.onSingleTapUp(e);
                    return true;
                }


                @Override
                public void onLongPress(MotionEvent e) {
                    Log.i(LOG_TAG, "onLongPress was invoked..: " + e);
                    View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (mActionMode != null) {
                        return;
                    } else {
//                        mActionMode = MainActivity.this.startActionMode(actionModeCallback);
//                        Log.i(LOG_TAG, "ActionMode = " + mActionMode);
////                        int idx = recyclerView.getChildPosition(childView);   //this method was deprecated and caused app to crash! replaced with the one below..
//                        int idx = recyclerView.getChildLayoutPosition(childView);
//                        Log.i(LOG_TAG, "indx = " + idx);
                    }

                    super.onLongPress(e);
                }
            });
        }


        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//            Log.i(LOG_TAG, "onInterceptTouchEvent was called: " + gestureDetector.onTouchEvent(e));
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e) == true) {
                // clickListener.onClick(child, rv.getChildPosition(child));
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            Log.i(LOG_TAG, "onTouchEvent was called: " + e);

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            Log.i(TAG, "onCreateActionMode");
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.action_mode_menu, menu);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                toolbar.setLayoutParams(params);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            Log.i(TAG, "onDestroyActionMode");
            mActionMode = null;
            //tripListView.removeAllViews();
            //   tripListView.setAdapter(checkedListAdapter);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                params.setMargins(0, getResources().getDimensionPixelOffset(R.dimen.appBarTopMargin), 0, 0);
                toolbar.setLayoutParams(params);
            }

        }
    };

    public interface ClickListener {
         void onClick(View v, int position);

        void onLongClick(View v, int position);
    }

}
