package br.com.ifsuldeminas.cronometro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox checkboxMilliseconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        checkboxMilliseconds = findViewById(R.id.checkboxMilliseconds);
        // Carregar o estado atual do checkbox a partir das preferências compartilhadas
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String k = sharedPreferences.getString("mostrarMilliseconds", "deu ruim");
        checkboxMilliseconds.setChecked(!Boolean.valueOf(k));

        //Altera a exibição do cronometro
        checkboxMilliseconds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putString("mostrarMilliseconds",String.valueOf(!checkboxMilliseconds.isChecked()));
            editor.commit();
        });
    }
}
