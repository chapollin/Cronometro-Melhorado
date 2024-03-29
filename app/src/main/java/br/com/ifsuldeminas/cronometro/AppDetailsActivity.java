package br.com.ifsuldeminas.cronometro;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AppDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        TextView tvAppDetails = findViewById(R.id.tvAppDetails);
        String appDetails = "Cronômetro App\n\n" +
                "Versão: 1.2\n\n" +
                "Descrição:\n" +
                "O aplicativo de cronômetro permite que você meça o tempo com precisão de Millisegundos. " +
                "Você pode iniciar, pausar, reiniciar o cronômetro e salvar os tempos medidos. " +
                "Além disso, você pode configurar se deseja ou não exibir os milissegundos no cronômetro.\n\n" +
                "Instruções de uso:\n" +
                "- Toque no botão 'Iniciar' para iniciar o cronômetro.\n" +
                "- Toque no botão 'Pausar' para pausar o cronômetro. Toque em continuar para dar continuidade a contagem.\n" +
                "- Toque no botão 'Reiniciar' para zerar o cronômetro.\n" +
                "- Toque no botão 'Salvar' para salvar o tempo medido. Os tempos salvos podem ser acessados na tela 'Tempos Salvos'.\n" +
                "- Toque no botão 'Tempos Salvos' para visualizar a lista de tempos salvos.\n" +
                "- Toque no botão '⁝' para visualizar a lista de telas.\n" +
                "- Toque no botão 'Detalhes do Aplicativo' para visualizar as informações do APP.\n" +
                "- Toque no botão 'Configurações' para acessar a configuração do aplicativo.\n\n" +
                "Desenvolvido por: Marcos Daniel de Souza Cardoso";

        tvAppDetails.setText(appDetails);
    }
}
