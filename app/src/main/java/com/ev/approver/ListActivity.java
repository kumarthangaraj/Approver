package com.ev.approver;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    public static ArrayList<JSONObject> requestList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        requestList = new ArrayList<JSONObject>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(getMenuBar());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ListView listView = (ListView) findViewById(R.id.requestListView);
        ListDataAdapter listAdapter = new ListDataAdapter(this,getApplicationContext(),requestList);
        listAdapter.formRequestList();
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showRequestPage(parent,view,position,id);
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
       int id = item.getItemId();
        if(id == R.id.logout){
            LoginHandler loginHandler = new LoginHandler(this);
            loginHandler.logout();
            return true;
        }
        return false;
    }

    private void showRequestPage(AdapterView<?> parent,View view,int position,long id){
        JSONObject clickedRequest = requestList.get(position);
        try {
            String requestId = clickedRequest.getString("id");
            if(requestId != null){
                ApprovalHandler approvalHandler = new ApprovalHandler(((ListDataAdapter)parent.getAdapter()).getActivity());
                approvalHandler.getPendingRequests(requestId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Drawable getMenuBar(){
        Drawable icMenu = ContextCompat.getDrawable(this,R.drawable.ic_menu).mutate();
        icMenu.setTint(getColor(R.color.white));
        return icMenu;
    }

}
