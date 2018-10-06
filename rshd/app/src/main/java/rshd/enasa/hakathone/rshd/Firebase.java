package rshd.enasa.hakathone.rshd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class Firebase {

    private static FirebaseDatabase database;
    private static User curr_user;


    public static User getCurr_user() {
        return curr_user;
    }


    private static Firebase singleton;

    private Firebase() {
        // Write a message to the database
        this.database = FirebaseDatabase.getInstance();
    }

    public static Firebase getInstance() {
        if (singleton == null) {
            singleton = new Firebase();
        }
        return singleton;
    }


    public void checkUser(final String email, final String pass, final Activity intent) {
        try {

            if(!isNetworkAvailable(intent)){
                Toast.makeText(intent, "Failed to connect internet.",
                        Toast.LENGTH_SHORT).show();
            }

            final String e = email.split("@")[0];

            DatabaseReference users = database.getReference("users").child(e);

            ValueEventListener usersListner = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("SNAPSHOT", "snapshot key = " + dataSnapshot);

                    if (dataSnapshot.hasChild("info")) {
                        String em = dataSnapshot.child("info").child("email").getValue().toString();
                        String pwd = dataSnapshot.child("info").child("password").getValue().toString();
//                        if (pwd.equals(pass) && em.equals(email)) {
                            String name = dataSnapshot.child("info").child("name").getValue().toString();
                            int limit = dataSnapshot.child("info").child("limit").getValue(Integer.class);
                            Log.d("PWD", pwd);

                            curr_user = new User(dataSnapshot.getKey(),name, em, "", limit);

                            intent.startActivity(new Intent(intent, MainActivity.class));
                            intent.finish();
//                        } else {
//                            Log.d("INVALID", "ERROR PASSWORD");
//                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w("onCancelled", "loadPost:onCancelled", databaseError.toException());
                    // [START_EXCLUDE]
                    Toast.makeText(intent, "Failed to listner users.",
                            Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }
            };


            users.addValueEventListener(usersListner);


        } catch (Exception e) {
            Log.d("EXCEPTION", e.getMessage());
        }

    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            int [] net = new int[]{ConnectivityManager.TYPE_BLUETOOTH,
                    ConnectivityManager.TYPE_DUMMY,
                    ConnectivityManager.TYPE_ETHERNET,
                    ConnectivityManager.TYPE_MOBILE,
                    ConnectivityManager.TYPE_MOBILE_DUN,
                    ConnectivityManager.TYPE_VPN,
                    ConnectivityManager.TYPE_WIFI,
                    ConnectivityManager.TYPE_WIMAX};
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : net) {
                NetworkInfo netInfo = cm.getNetworkInfo(networkType);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


}
class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private int limit;


    public User(String id ,String name, String email, String phone, int limit) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.limit = limit;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getLimit() {
        return limit;
    }

    public String getPhone() {
        return phone;
    }
}
