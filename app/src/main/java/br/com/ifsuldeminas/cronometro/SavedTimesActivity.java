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
        savedTimesList = new ArrayList<>(); // Inicializa a lista de tempos salvos
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedTimesList); // Cria um adaptador para a lista de tempos salvos
        listView.setAdapter(adapter); // Define o adaptador como o adaptador do ListView

        // Recuperar os tempos salvos do SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int count = sharedPreferences.getInt("count", 0); // Obtém a quantidade de tempos salvos
        for (int i = 0; i < count; i++) {
            String time = sharedPreferences.getString("time_" + i, ""); // Obtém o tempo salvo do SharedPreferences
            savedTimesList.add(time); // Adiciona o tempo à lista de tempos salvos
        }

        adapter.notifyDataSetChanged(); // Notifica o adaptador de que houve alteração nos dados da lista
    }
}
