package com.example.clouddrive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.clouddrive.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    UserPref userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userPref = new UserPref(getApplicationContext());


        if (getIntent().getExtras() != null) {
            try {
                JSONObject jsonObject = new JSONObject(getIntent().getExtras().getString("json"));
                Log.d("MainActivity", jsonObject.toString());
                ((TextView) findViewById(R.id.user_name)).setText(jsonObject.getString("name"));
                ((TextView) binding.navView.findViewById(R.id.textView)).setText(jsonObject.getString("email"));

                userPref.setPreferences("email", jsonObject.getString("email"));
                userPref.setPreferences("name", jsonObject.getString("name"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ((TextView) findViewById(R.id.user_name)).setText(userPref.getPreferences("name", ""));
            ((TextView) findViewById(R.id.textView)).setText(userPref.getPreferences("email", ""));
        }

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;

        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_change, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Log.d("MainActivity", "action_logout");
            userPref.clearAll();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}