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

    private TextView tvElapsedTime;
    private Button btnStartPause;
    private Button btnReset;
    private Button btnSave;
    private boolean isRunning;
    private long startTime;
    private long elapsedTime;
    private long pausedTime;
    private Handler handler;
    private Runnable runnable;
    private SharedPreferences sharedPreferences;

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        tvElapsedTime = findViewById(R.id.tvElapsedTime);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        btnSave = findViewById(R.id.btnSave);
        View btnHora2 = findViewById(R.id.button2);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateElapsedTime();
                handler.postDelayed(this, 100);
            }
        };

        boolean showMilliseconds = sharedPreferences.getBoolean("showMilliseconds", true);

        if (!showMilliseconds) {
            tvElapsedTime.setText("00:00");
        } else {
            updateElapsedTime();
        }

        btnStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        btnHora2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetTimeAsyncTask().execute();
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveElapsedTime();
            }
        });

        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Abrir a atividade de configurações
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_saved_times) {
            // Abrir a atividade de tempos salvos
            Intent intent = new Intent(this, SavedTimesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_app_details) {
            // Abrir a atividade de detalhes do aplicativo
            Intent intent = new Intent(this, AppDetailsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis();
            isRunning = true;
            btnStartPause.setText("Pausar");
            btnReset.setVisibility(View.INVISIBLE);
            btnSave.setVisibility(View.INVISIBLE);
            handler.postDelayed(runnable, 0);
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            isRunning = false;
            btnStartPause.setText("Continuar");
            btnReset.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            pausedTime = elapsedTime; // Armazena o tempo decorrido atual
            handler.removeCallbacks(runnable);
        }
    }

    private void resetTimer() {
        isRunning = false;
        elapsedTime = 0;
        pausedTime = 0;
        updateElapsedTime();
        btnReset.setVisibility(View.INVISIBLE);
        btnStartPause.setText("Iniciar");
    }

    private void saveElapsedTime() {
        if (!isRunning) {
            // Salvar o tempo decorrido em SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int count = sharedPreferences.getInt("count", 0);
            editor.putString("time_" + count, String.valueOf(tvElapsedTime.getText()));
            editor.putInt("count", count + 1);
            editor.apply();

            // Exibir notificação
            Toast.makeText(this, "Tempo salvo com sucesso!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateElapsedTime() {
        if (isRunning) {
            elapsedTime = System.currentTimeMillis() - startTime + pausedTime;
        }

        int minutes = (int) (elapsedTime / 1000) / 60;
        int seconds = (int) (elapsedTime / 1000) % 60;

        // Verificar se os milissegundos devem ser exibidos com base no estado do checkbox
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String k = sharedPreferences.getString("mostrarMilliseconds", "deu ruim");


        String formattedTime;
        if (k.equals("true")) {
            int milliseconds = (int) (elapsedTime % 1000);
            formattedTime = String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);
        } else {
            formattedTime = String.format("%02d:%02d", minutes, seconds);
        }

        tvElapsedTime.setText(formattedTime);
    }

    public void onSavedTimesClicked(View view) {
        Intent intent = new Intent(this, SavedTimesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private class GetTimeAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            // Criar a solicitação GET
            Request request = new Request.Builder()
                    .url("https://worldtimeapi.org/api/ip")
                    .build();

            try {
                // Executar a solicitação e obter a resposta
                Response response = client.newCall(request).execute();

                // Verificar se a resposta é bem-sucedida
                if (response.isSuccessful()) {
                    // Extrair a hora atual da resposta
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    String currentTime = jsonObject.getString("datetime");

                    return currentTime;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String currentTime) {
            super.onPostExecute(currentTime);

            if (currentTime != null) {
                // Mostrar a hora atual em um Toast
                Toast.makeText(MainActivity.this, "Hora atual: " + formatarHora(currentTime), Toast.LENGTH_LONG).show();
            } else {
                // Mostrar uma mensagem de erro caso não seja possível obter a hora atual
                Toast.makeText(MainActivity.this, "Falha ao obter a hora atual", Toast.LENGTH_LONG).show();
            }
        }

        private String formatarHora(String hora) {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault());
            SimpleDateFormat formatoSaida = new SimpleDateFormat("HH 'horas' mm 'minutos' ss 'segundos'", Locale.getDefault());

            try {
                Date data = formatoEntrada.parse(hora);
                return formatoSaida.format(data);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return hora;
        }

    }

}
