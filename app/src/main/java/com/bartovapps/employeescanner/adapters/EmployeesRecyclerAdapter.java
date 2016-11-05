package com.bartovapps.employeescanner.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bartovapps.employeescanner.R;
import com.bartovapps.employeescanner.model.Employee;
import com.bartovapps.employeescanner.model.EmployeesDbOpenHelper;

import java.util.ArrayList;

/**
 * Created by BartovMoti on 08/31/16.
 */
public class EmployeesRecyclerAdapter extends RecyclerView.Adapter<EmployeesRecyclerAdapter.EmployeesViewHolder>  {

    private final static String TAG = EmployeesRecyclerAdapter.class.getSimpleName();
    LayoutInflater mInflater;
    ArrayList<Employee> mItems;
    Context mContext;
    Cursor mCursor;
    boolean mDataValid;
    private int mRowIdColumn;
    private SparseBooleanArray selectedItems;


    private DataSetObserver mDataSetObserver;


    CursorAdapter mCursorAdapter;

//    public EmployeesRecyclerAdapter(Context context, ArrayList<Employee> data) {
//        mInflater = LayoutInflater.from(context);
//        mContext = context;
//        updateList();
//    }

    public EmployeesRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;

        mDataSetObserver = new NotifyingDataSetObserver();
        selectedItems = new SparseBooleanArray();

        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

        mCursorAdapter = new CursorAdapter(mContext, mCursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                final LayoutInflater inflater = LayoutInflater.from(context);
                View view = inflater.inflate(R.layout.employee_list_item, viewGroup, false);

                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                Log.i(TAG, "bindView called");

                String tagId = null;
                TextView tvTagId = (TextView) view.findViewById(R.id.item_tag_id);


                if (cursor != null && cursor.getCount() > 0) {
                    int columnIndx = cursor.getColumnIndex(EmployeesDbOpenHelper.COLUMN_TAG_ID);
                    Log.i(TAG, "bindView: column index = " + columnIndx);
                    tagId = cursor.getString(columnIndx);
                }

                if (tvTagId != null) {
                    tvTagId.setText(tagId);
                }

            }
//            @Override
//            public Cursor swapCursor(Cursor newCursor) {
//                Log.i(TAG, "swapCursor was called");
//                if (newCursor == mCursor) {
//                    return null;
//                }
//                final Cursor oldCursor = mCursor;
//                if (oldCursor != null && mDataSetObserver != null) {
//                    oldCursor.unregisterDataSetObserver(mDataSetObserver);
//                }
//                mCursor = newCursor;
//                if (mCursor != null) {
//                    if (mDataSetObserver != null) {
//                        mCursor.registerDataSetObserver(mDataSetObserver);
//                    }
//                    mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
//                    mDataValid = true;
//                    notifyDataSetChanged();
//                } else {
//                    mRowIdColumn = -1;
//                    mDataValid = false;
//                    notifyDataSetChanged();
//                    //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
//                }
//                return oldCursor;
//            }
        };


    }

    private ArrayList<String> initValues() {
        ArrayList<String> dummyItems = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            dummyItems.add("Item " + i);
        }
        return dummyItems;
    }

//    public void updateList() {
//        Log.i(TAG, "updateList" );
//        Cursor newCursor = mContext.getContentResolver().query(EmployeesProvider.CONTENT_URI, EmployeesDbOpenHelper.allColumns, null, null, null);
//        Cursor oldCursor = mCursorAdapter.swapCursor(newCursor);
//        if(oldCursor != null){
//            oldCursor.close();
//        }
//    }

    public void updateCursor(Cursor data) {
        Log.i(TAG, "updateCursor called");
        mCursor = data;
        if(data != null){
            mDataValid = true;
            Log.i(TAG, "New cursor size: " + data.getCount());
        }

        Cursor oldCursor = mCursorAdapter.swapCursor(data);
        if (oldCursor != null) {
            oldCursor.close();
            Log.i(TAG, "old cursor closed");
        }
        notifyDataSetChanged();
    }

    @Override
    public EmployeesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        Log.i(TAG, "onCreateViewHolder was called");
        view = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new EmployeesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EmployeesViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder");

        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        if (!mCursorAdapter.getCursor().moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {

        if (mDataValid) {
//            Log.i(TAG, "getItemCount: " + mCursorAdapter.getCount());
            return mCursorAdapter.getCount();
        }
        return 0;

    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor.moveToPosition(position)) {
            return mCursorAdapter.getCursor().getLong(mRowIdColumn);
        }
        return 0;
    }

//    @Override
//    public void onSwipe(int position) {
//
//        if (mCursorAdapter.getCursor() != null && position < mCursorAdapter.getCursor().getCount() && mCursorAdapter.getCursor().moveToPosition(position)) {
//            int columnIndx = mCursorAdapter.getCursor().getColumnIndex(EmployeesDbOpenHelper.COLUMN_TAG_ID);
//            Log.i(TAG, "bindView: column index = " + columnIndx);
//            String tagId = mCursor.getString(columnIndx);
//            String where = EmployeesDbOpenHelper.COLUMN_TAG_ID + "=\"" + mCursor.getString(columnIndx) + "\"";
//            Log.i(TAG, "onSwipe: about to remove item " + where);
//            mContext.getContentResolver().delete(EmployeesProvider.CONTENT_URI, where, null);
//            Log.i(TAG, "item deleted... ");
//
////            notifyItemRemoved(position);
//        }
//    }


    public class EmployeesViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagId;


        public EmployeesViewHolder(View itemView) {
            super(itemView);
            tvTagId = (TextView) itemView.findViewById(R.id.item_tag_id);
        }


        public void setTagId(String tagId) {
            tvTagId.setText(tagId);
        }
    }


    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            Log.i(TAG, "onChanged was called");
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }

    public void toggleSelection(int position) {
        Log.i(TAG, "toggleSelection was called");
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }
}

