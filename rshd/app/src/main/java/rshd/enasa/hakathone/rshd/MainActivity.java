package rshd.enasa.hakathone.rshd;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    double totalCost = 0;
    double totalUsage = 0;

    User currUser;
    FirebaseDatabase db;
    TextView limi_tv;

    ProgressDialog progressDialog;
    HashMap<String, DeviceType> devicesTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();

        db = FirebaseDatabase.getInstance();

        // read devices types
        devicesTypes = new HashMap<>();
        readDeviceTeypes();


        currUser = Firebase.getCurr_user();
        int userLimit = currUser.getLimit();

        ((TextView) findViewById(R.id.imageView)).setText(String.format(getResources().getString(R.string.hi), currUser.getName()));
//        ((TextView) findViewById(R.id.user_name)).setText( currUser.getName());

        int current_day = new Date().getDay();
        if (current_day <= 10){
            ((TextView) findViewById(R.id.limit_tv)).setText(String.format(getResources().getString(R.string.limit_day),current_day ));
        } else{
            ((TextView) findViewById(R.id.limit_tv)).setText(String.format(getResources().getString(R.string.limit_days), new Date().getDay()));
        }
        ((TextView) findViewById(R.id.user_limit)).setText(String.format(getResources().getString(R.string.user_limit), userLimit));

        // get user limit

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.show();


        // set user name


        // get table
        final TableLayout devicesTable = findViewById(R.id.device_list_layout);

        getDevicesAndCalculateUsage(devicesTable);

    }

    private void readDeviceTeypes() {
        db.getReference("devicesTypes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot type : dataSnapshot.getChildren()) {
                    String id = type.getKey();
                    String name = type.child("name").getValue().toString();
                    double watt = Double.parseDouble(type.child("watt").getValue().toString());
                    devicesTypes.put(id, new DeviceType(id, name, watt));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getDevicesAndCalculateUsage(final TableLayout devicesTable) {
        DatabaseReference devices = db.getReference("users").child(currUser.getId()).child("devices");
        devices.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("SNAPSHOT", "key : " + dataSnapshot.getKey() + " | value : " + dataSnapshot.getValue().toString());

                String key = dataSnapshot.getKey();

                for (DataSnapshot deviceSnapshot : dataSnapshot.getChildren()) {
                    Log.d("SNAPSHOT", "key : " + deviceSnapshot.getKey() + " | value : " + deviceSnapshot.getValue().toString());


                    convertDeviceToRow(devicesTable, deviceSnapshot);
                }

                if (progressDialog.isShowing()) {
                    progressDialog.hide();
                }

                // after collecting from database
                int usagePercent = (int) (Math.ceil((totalCost / currUser.getLimit()) * 100));
                ((TextView) findViewById(R.id.total_usage)).setText(usagePercent + "%");

                // set progress bar
                ProgressBar progressBar = findViewById(R.id.progressBar1);
                progressBar.setMax(100);
                progressBar.setProgress(usagePercent);

                ((TextView) findViewById(R.id.totalCost)).setText(getResources().getString(R.string.total_text) + " : \t " + String.format("%.2f", totalCost) + "  " + getResources().getString(R.string.riyal));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void convertDeviceToRow(TableLayout devicesTable, DataSnapshot deviceSnapshot) {


        // create row
        TableRow deviceRow = new TableRow(MainActivity.this);
        deviceRow.setBackgroundColor(getResources().getColor(R.color.opacity));
        TableLayout.LayoutParams deviceLayoutParam = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        deviceLayoutParam.setMargins(0, 0, 0, 10);
        deviceRow.setLayoutParams(deviceLayoutParam);
        deviceRow.setBackground(getDrawable(R.drawable.rounded_shape));
        deviceRow.setBackgroundColor(getColor(R.color.opacity));


        // create 2 text view
        TextView deviceName = new TextView(MainActivity.this);
        deviceName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 3f));
        deviceName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        deviceName.setTextAppearance(R.style.TextAppearance_AppCompat_Headline);
        deviceName.setText(deviceSnapshot.child("name").getValue().toString());


        TextView usage = new TextView(MainActivity.this);
        usage.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        // calculate usage
        usage.setText(calculateUsage(deviceSnapshot) + "");
        usage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        usage.setTextAppearance(R.style.TextAppearance_AppCompat_Headline);


        // add view to row
        deviceRow.addView(deviceName);
        deviceRow.addView(usage);

        // add row to table
        devicesTable.addView(deviceRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

    }

    private void setup() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

    }

    private String calculateUsage(DataSnapshot dataSnapshot) {
        Date currentDate = Calendar.getInstance().getTime();

        long diff;

        long sumDiff = 0;


        int diffInDays = 0;


        String lastState = "";
        Date lastDate = null;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");

        Date snapshotDate = null;
        for (DataSnapshot historyRecord : dataSnapshot.child("history").getChildren()) {
            String KeyValue = "Key : " + historyRecord.getKey() + " | Vaalue : " + historyRecord.getValue();


            String date = historyRecord.child("date").getValue().toString();
            String time = historyRecord.child("time").getValue().toString();
            String state = historyRecord.child("state").getValue().toString();

            try {
                snapshotDate = format.parse(date + " " + time);

                if (snapshotDate.getMonth() != currentDate.getMonth())
                    continue;


                // the device is off
                if (state.equals("0") && lastDate != null) {

                    // diff between off state and on state
                    if (lastState.equals("1")) {
                        diff = snapshotDate.getTime() - lastDate.getTime();
                        diffInDays += ((int) ((snapshotDate.getTime() - lastDate.getTime()) / (1000 * 60 * 60 * 24)));
                        sumDiff += diff;
                    }
                }
                // the device is on
                else if (state.equals("1")) {


                }

            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("ParseException", e.getMessage());
            }


            Log.d("", "");
            lastState = state;
            lastDate = snapshotDate;

        }

        if (lastState.equals("1")) {
            diff = lastDate.getTime() - currentDate.getTime();
            diffInDays += ((int) ((snapshotDate.getTime() - lastDate.getTime()) / (1000 * 60 * 60 * 24)));
            sumDiff += diff;
        }


        long diffhrs = (sumDiff / (60 * 60 * 1000) % 24) + (diffInDays * 24);

        String deviceType = dataSnapshot.child("type").getValue().toString();
        double watt = (devicesTypes.get(deviceType) == null) ? 3 : devicesTypes.get(deviceType).getWatt();

        // kwatt * cost per hr * hrs = total cost
        double deviceCost = watt * 0.18 * diffhrs;

        totalCost += deviceCost;
        totalUsage += diffhrs;


        return String.format("%.2f", deviceCost) + " " + getResources().getString(R.string.riyal);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
