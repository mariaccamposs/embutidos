package com.grupo5.parkingspaces;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.grupo5.parkingspaces.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


        GridLayout gridLayout = new GridLayout(this);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.height = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams.setMargins(10, 10, 10, 10);
        gridLayout.setLayoutParams(layoutParams);

        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(5);

        ViewGroup root =  (ViewGroup) findViewById(R.id.mainpage);

        int[] arr = {3, 4, 3, 2};
        int r = 0, c = 0;
        for (int i=0; i<4; i++) {
            for(int j=0; j<arr[i]; j++) {
                CardView cardView  =  createChild();
                if( c == 3 ) {
                    c = 0;
                    r++;
                }

                Button button  = new Button(this);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Snackbar.make(v , "Replace with your own action", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();                    }
                });
//                ImageView imageView = new ImageView(this);
//                imageView.setImageResource(R.mipmap.ic_launcher);
//                imageView.setLayoutParams(new ViewGroup.LayoutParams(150, 150));

                GridLayout.Spec rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
                GridLayout.Spec colSpan = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);

//                if(r == 0 && c == 0) {
//                    colSpan = GridLayout.spec(GridLayout.UNDEFINED, 1);
//                    rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 2);
//                }

                GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                        rowSpan, colSpan
                );
                cardView.addView(button);
                gridLayout.addView(cardView, gridParam);
                r++; c++;
            }

        }

        root.addView(gridLayout);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private CardView createChild() {
            CardView cardView = new CardView(this);
            cardView.setCardElevation(2);
            cardView.setUseCompatPadding(true);
            ViewGroup.LayoutParams cvLayoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardView.setLayoutParams(cvLayoutParams);
            return cardView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}