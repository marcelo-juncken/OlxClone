package com.example.olxclone.autenticacao;

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
import com.example.olxclone.model.Usuario;
import com.santalu.maskara.widget.MaskEditText;

public class CriarContaActivity extends AppCompatActivity {

    private EditText edit_nome;
    private EditText edit_email;
    private MaskEditText edit_telefone;
    private EditText edit_senha;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_conta);

        iniciaComponentes();
        configCliques();
    }

    private void iniciaComponentes() {
        TextView tb_titulo = findViewById(R.id.text_toolbar);
        tb_titulo.setText("Cadastro");

        edit_nome = findViewById(R.id.edt_nome);
        edit_email = findViewById(R.id.edt_email);
        edit_telefone = findViewById(R.id.edt_telefone);
        edit_senha = findViewById(R.id.edt_senha);

        progressBar = findViewById(R.id.progressBar);
    }

    private void configCliques() {
        findViewById(R.id.ib_toolbar_voltar).setOnClickListener(v -> finish());
    }

    public void validaDados(View view) {
        String nome = edit_nome.getText().toString().trim();
        String email = edit_email.getText().toString();
        String telefone = edit_telefone.getMasked();
        String senha = edit_senha.getText().toString();

        if (!nome.isEmpty()) {
            if (!email.isEmpty()) {
                if (!telefone.isEmpty()) {
                    if (telefone.length() == 15) {
                        if (!senha.isEmpty()) {

                            progressBar.setVisibility(View.VISIBLE);
                            Usuario usuario = new Usuario();
                            usuario.setNome(nome);
                            usuario.setEmail(email);
                            usuario.setTelefone(telefone);
                            usuario.setSenha(senha);

                            cadastraUsuario(usuario);
                        } else {
                            edit_senha.requestFocus();
                            edit_senha.setError("Preencha sua senha.");
                        }

                    } else {
                        edit_telefone.requestFocus();
                        edit_telefone.setError("Telefone inválido.");
                    }
                } else {
                    edit_telefone.requestFocus();
                    edit_telefone.setError("Preencha seu telefone.");
                }
            } else {
                edit_email.requestFocus();
                edit_email.setError("Preencha seu e-mail.");
            }
        } else {
            edit_nome.requestFocus();
            edit_nome.setError("Preencha seu nome.");
        }
    }

    private void cadastraUsuario(Usuario usuario) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String id = task.getResult().getUser().getUid();

                usuario.setId(id);
                usuario.salvar(progressBar, getBaseContext());
                Intent intent = new Intent();
                intent.putExtra("conta_criada",true);
                setResult(RESULT_OK, intent);
                finish();

            } else {
                String erro = FirebaseHelper.validaErros(task.getException().getMessage());
                Toast.makeText(this, erro, Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        });

    }
}