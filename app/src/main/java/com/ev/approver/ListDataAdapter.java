package com.ev.approver;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kumar_Thangaraj on 8/6/2018.
 */

public class ListDataAdapter extends BaseAdapter{
    private ArrayList<JSONObject> list;
    Context context;
    Activity activity;

    public Context getActivity(){
        return activity;
    }

    public ListDataAdapter(Activity activity,Context context, ArrayList list){
        super();
        this.activity = activity;
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();

        convertView = inflater.inflate(R.layout.request_list_element,null);
        TextView fromAcct = convertView.findViewById(R.id.fromAcct);
        TextView currency = convertView.findViewById(R.id.currency);
        TextView amount = convertView.findViewById(R.id.amount);
        TextView exception = convertView.findViewById(R.id.approveRemarks);

        JSONObject requestObject = list.get(position);
        try {
            fromAcct.setText(requestObject.getString("from_acct"));
            currency.setText(requestObject.getString("currency"));
            amount.setText(requestObject.getString("amount"));
            exception.setText(requestObject.getString("tran_particulars"));
        }catch(Exception e){
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public void formRequestList(){
        ApprovalHandler approvalHandler = new ApprovalHandler(this.activity.getApplicationContext());
        approvalHandler.getPendingRequestList();
    }
}


