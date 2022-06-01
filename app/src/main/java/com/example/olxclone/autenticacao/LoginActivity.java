package com.example.olxclone.autenticacao;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.activities.MainActivity;
import com.example.olxclone.R;
import com.example.olxclone.helper.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CRIAR_CONTA = 100;
    private EditText edit_email;
    private EditText edit_senha;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        iniciaComponentes();
        configCliques();
    }

    private void iniciaComponentes() {
        TextView tb_titulo = findViewById(R.id.text_toolbar);
        tb_titulo.setText("Login");

        edit_email = findViewById(R.id.edt_email);
        edit_senha = findViewById(R.id.edt_senha);
        progressBar = findViewById(R.id.progressBar);
    }

    private void configCliques() {
        findViewById(R.id.ib_toolbar_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.text_cadastro).setOnClickListener(v -> {
            Intent intent = new Intent(this, CriarContaActivity.class);

            startActivityForResult(intent, REQUEST_CRIAR_CONTA);
        });
        findViewById(R.id.recuperar_senha).setOnClickListener(v -> {
            startActivity(new Intent(this, RecuperarSenhaActivity.class));
        });
    }

    public void validaDados(View view) {
        String email = edit_email.getText().toString().trim();
        String senha = edit_senha.getText().toString();

        if (!email.isEmpty()) {
            if (!senha.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);

                logar(email, senha);
            } else {
                edit_senha.requestFocus();
                edit_senha.setError("Preencha a senha.");
            }
        } else {
            edit_email.requestFocus();
            edit_email.setError("Preencha o email.");
        }
    }

    private void logar(String email, String senha) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(
                email, senha
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                finish();
            } else {
                String erro = FirebaseHelper.validaErros(task.getException().getMessage());
                Toast.makeText(this, erro, Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CRIAR_CONTA && FirebaseHelper.getAutenticado()) {
                finish();
            }
        }
    }
}
