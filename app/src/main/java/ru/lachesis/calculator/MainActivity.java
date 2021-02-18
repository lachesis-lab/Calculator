package ru.lachesis.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalendarView calendarView = findViewById(R.id.calendarView1);

        calendarView.requestFocus();
    }

    public void runActivitySecond(View view){
        Intent intent = new Intent(this, ActivitySecond.class);
        startActivity(intent);

    }
}