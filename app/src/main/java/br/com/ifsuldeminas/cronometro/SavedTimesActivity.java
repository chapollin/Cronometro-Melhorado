package br.com.ifsuldeminas.cronometro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class SavedTimesActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> savedTimesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_times);

        listView = findViewById(R.id.listView);
        savedTimesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedTimesList);
        listView.setAdapter(adapter);

        // Recuperar os tempos salvos do SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int count = sharedPreferences.getInt("count", 0);
        for (int i = 0; i < count; i++) {
            String time = sharedPreferences.getString("time_" + i, "");
            savedTimesList.add(time);
        }

        adapter.notifyDataSetChanged();
    }
}
