package com.bartovapps.employeescanner.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bartovapps.employeescanner.R;
import com.bartovapps.employeescanner.model.Employee;
import com.bartovapps.employeescanner.model.EmployeesDataSource;

import java.util.ArrayList;

/**
 * Created by BartovMoti on 08/31/16.
 */
public class EmployeesRecyclerAdapter extends RecyclerView.Adapter<EmployeesRecyclerAdapter.EmployeesViewHolder> implements SwipeListener{

    private final static String TAG = EmployeesRecyclerAdapter.class.getSimpleName();
    LayoutInflater mInflater;
    ArrayList<Employee> mItems;
    Context mContext;

    public EmployeesRecyclerAdapter(Context context, ArrayList<Employee> data) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        updateList(data);
    }

    private ArrayList<String> initValues() {
        ArrayList<String> dummyItems = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            dummyItems.add("Item " + i);
        }
        return dummyItems;
    }

    public void updateList(ArrayList<Employee> results) {
        Log.i(TAG, "updateList, dataSize: " + results.size());
        this.mItems = results;
        notifyDataSetChanged();
    }

    @Override
    public EmployeesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        Log.i(TAG, "onCreateViewHolder was called");
        view = mInflater.inflate(R.layout.employee_list_item, parent, false);
        return new EmployeesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EmployeesViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder");
        Employee employee = mItems.get(position);
        holder.setTagId(employee.getTag_id());

    }

    @Override
    public int getItemCount() {

        Log.i(TAG, "getItemCount: " + mItems.size());
        return mItems.size();

    }

    @Override
    public void onSwipe(int position) {
        if (position < mItems.size()) {
            EmployeesDataSource dataSource = new EmployeesDataSource(mContext);
            dataSource.open();
            dataSource.delete(mItems.get(position));
            updateList(dataSource.findAll());
            dataSource.close();
            notifyItemRemoved(position);
        }
    }


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
}

