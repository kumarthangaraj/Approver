package com.ev.approver;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class ApprovalActivity extends AppCompatActivity {

    private String requestId;
    ApprovalHandler approvalHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(getMenuBar());
        requestId = getIntent().getStringExtra("requestId");
        approvalHandler = new ApprovalHandler(getApplicationContext());
        populateFields(getIntent());
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private void populateFields(Intent intent){
        String amountWithCurrency = intent.getStringExtra("currency") + " "+intent.getStringExtra("amount");
        TextView fromAcct = (TextView)findViewById(R.id.fromAcctDtls);
        TextView toAcct = (TextView)findViewById(R.id.toAcctDtls);
        TextView amount = (TextView)findViewById(R.id.amount);
        TextView remarks = (TextView)findViewById(R.id.remarks);
        fromAcct.setText(intent.getCharSequenceExtra("from_acct"));
        toAcct.setText(intent.getCharSequenceExtra("to_acct"));
        remarks.setText(intent.getCharSequenceExtra("tran_particulars"));
        amount.setText(amountWithCurrency);
    }

    protected void approveRequest(View view){
        JSONObject input = new JSONObject();
        try {
            input.put("requestId", requestId);
            input.put("action", "A");
            EditText approverComments = (EditText)findViewById(R.id.approverComments);
            input.put("approver_comments",approverComments.getText().toString());
            approvalHandler.updateApprovalRequest(input);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void rejectRequest(View view){
        JSONObject input = new JSONObject();
        try {
            input.put("requestId", requestId);
            input.put("action", "R");
            approvalHandler.updateApprovalRequest(input);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private Drawable getMenuBar(){
        Drawable icMenu = ContextCompat.getDrawable(this,R.drawable.ic_menu).mutate();
        icMenu.setTint(getColor(R.color.white));
        return icMenu;
    }
}
