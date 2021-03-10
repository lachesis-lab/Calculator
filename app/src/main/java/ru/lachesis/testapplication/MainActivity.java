package ru.lachesis.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextInputEditText inputText = findViewById(R.id.inputString);

        MaterialButton button = findViewById(R.id.callCalculatorButton);
        button.setOnClickListener(view->{
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra("inputString",inputText.getText().toString());
            intent.addCategory("android.intent.category.APP_CALCULATOR");
            List<ResolveInfo> queryIntentActivities = pm.queryIntentActivities(intent,0);
            if (queryIntentActivities.size()>0)
                startActivity(intent);
            else inputText.setText("Приложение не найдено");

        });
    }
}