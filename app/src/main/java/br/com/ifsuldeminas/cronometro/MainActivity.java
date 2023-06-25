package br.com.ifsuldeminas.cronometro;

import static android.text.format.DateUtils.formatElapsedTime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvElapsedTime; // TextView para exibir o tempo decorrido
    private Button btnStartPause; // Botão para iniciar/pausar o cronômetro
    private Button btnReset; // Botão para reiniciar o cronômetro
    private Button btnSave; // Botão para salvar o tempo decorrido
    private boolean isRunning; // Variável para controlar se o cronômetro está em execução
    private long startTime; // Tempo de início do cronômetro
    private long elapsedTime; // Tempo decorrido desde o início do cronômetro
    private long pausedTime; // Tempo decorrido quando o cronômetro foi pausado
    private Handler handler; // Manipulador para atualizar o tempo decorrido na interface
    private Runnable runnable; // Tarefa a ser executada periodicamente para atualizar o tempo
    private SharedPreferences sharedPreferences; // Objeto para acessar preferências compartilhadas

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        tvElapsedTime = findViewById(R.id.tvElapsedTime); // Associa TextView do layout à variável
        btnStartPause = findViewById(R.id.btnStartPause); // Associa botão do layout à variável
        btnReset = findViewById(R.id.btnReset); // Associa botão do layout à variável
        btnSave = findViewById(R.id.btnSave); // Associa botão do layout à variável
        View btnHora2 = findViewById(R.id.button2); // Associa botão do layout à variável

        handler = new Handler(); // Inicializa o manipulador
        runnable = new Runnable() { // Define a tarefa que será executada periodicamente
            @Override
            public void run() {
                updateElapsedTime(); // Atualiza o tempo decorrido
                handler.postDelayed(this, 100); // Executa a tarefa novamente após 100 milissegundos
            }
        };

        boolean showMilliseconds = sharedPreferences.getBoolean("showMilliseconds", true); // Obtém a preferência de exibir milissegundos

        if (!showMilliseconds) {
            tvElapsedTime.setText("00:00"); // Define o texto do tempo decorrido como "00:00" se a exibição de milissegundos estiver desativada
        } else {
            updateElapsedTime();
        }

        btnStartPause.setOnClickListener(new View.OnClickListener() { // Define o listener para o botão de iniciar/pausar
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    pauseTimer(); // Pausa o cronômetro se estiver em execução
                } else {
                    startTimer(); // Inicia o cronômetro se estiver pausado
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer(); // Reinicia o cronômetro
            }
        });

        btnHora2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetTimeAsyncTask().execute(); // Executa a tarefa assíncrona para obter a hora atual de um servidor externo
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveElapsedTime(); // Salva o tempo decorrido
            }
        });

        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings(); // Abre a atividade de configurações
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Infla o menu da atividade
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Abri a atividade de configurações
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_saved_times) {
            // Abri a atividade de tempos salvos
            Intent intent = new Intent(this, SavedTimesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_app_details) {
            // Abri a atividade de detalhes do aplicativo
            Intent intent = new Intent(this, AppDetailsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis(); // Obtém o tempo de início atual
            isRunning = true; // Define que o cronômetro está em execução
            btnStartPause.setText("Pausar"); // Atualiza o texto do botão para "Pausar"
            btnReset.setVisibility(View.INVISIBLE); // Torna o botão de reiniciar invisível
            btnSave.setVisibility(View.INVISIBLE); // Torna o botão de salvar invisível
            handler.postDelayed(runnable, 0); // Agenda a execução da tarefa para atualizar o tempo decorrido
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            isRunning = false; // Define que o cronômetro está pausado
            btnStartPause.setText("Continuar"); // Atualiza o texto do botão para "Continuar"
            btnReset.setVisibility(View.VISIBLE); // Torna o botão de reiniciar visível
            btnSave.setVisibility(View.VISIBLE); // Torna o botão de salvar visível
            pausedTime = elapsedTime; // Armazena o tempo decorrido atual
            handler.removeCallbacks(runnable); // Remove a execução da tarefa de atualização do tempo
        }
    }

    private void resetTimer() {
        isRunning = false; // Define que o cronômetro está pausado
        elapsedTime = 0; // Reseta o tempo decorrido
        btnStartPause.setText("Iniciar"); // Atualiza o texto do botão para "Iniciar"
        btnReset.setVisibility(View.INVISIBLE); // Torna o botão de reiniciar invisível
        btnSave.setVisibility(View.INVISIBLE); // Torna o botão de salvar invisível
        handler.removeCallbacks(runnable); // Remove a execução da tarefa de atualização do tempo
        tvElapsedTime.setText("00:00"); // Define o texto do tempo decorrido como "00:00"
    }

    private void updateElapsedTime() {
        if (isRunning) {
            elapsedTime = System.currentTimeMillis() - startTime + pausedTime; // Calcula o tempo decorrido somando o tempo desde o início, considerando o tempo pausado
            String formattedTime = formatElapsedTime(elapsedTime / 1000); // Formata o tempo decorrido em um formato legível
            tvElapsedTime.setText(formattedTime); // Atualiza o texto do tempo decorrido na interface
        }
    }

    private void saveElapsedTime() {
        String time = formatElapsedTime(elapsedTime / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()); // Define o formato da data/hora
        String timestamp = sdf.format(new Date()); // Obtém o timestamp atual formatado
        String savedTime = time + " - " + timestamp; // Concatena o tempo decorrido com o timestamp
        SharedPreferences.Editor editor = sharedPreferences.edit(); // Obtém o editor de sharedPreferences
        editor.putString("savedTime", savedTime); // Salva o tempo decorrido com o timestamp nas sharedPreferences
        editor.apply(); // Aplica as alterações
        Toast.makeText(this, "Tempo salvo com sucesso!", Toast.LENGTH_SHORT).show();
    }

    private class GetTimeAsyncTask extends AsyncTask<Void, Void, String> {
        private static final String API_URL = "http://worldclockapi.com/api/json/utc/now"; // URL da API para obter a hora atual

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_URL)
                    .build();
            try {
                Response response = client.newCall(request).execute(); // Executa a requisição HTTP para obter a resposta
                if (response.isSuccessful()) {
                    String responseBody = response.body().string(); // Obtém o corpo da resposta como uma string
                    JSONObject jsonObject = new JSONObject(responseBody); // Converte a string em um objeto JSON
                    String time = jsonObject.getString("currentDateTime"); // Obtém a propriedade "currentDateTime" do objeto JSON
                    return time; // Retorna o valor do tempo atual obtido da API
                }
            } catch (IOException | JSONException e) {
                Log.e("GetTimeAsyncTask", "Error: " + e.getMessage()); // Registra um erro caso ocorra uma exceção
            }
            return null; // Retorna null se não foi possível obter a hora atual
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(MainActivity.this, "Hora atual: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Falha ao obter a hora atual", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
