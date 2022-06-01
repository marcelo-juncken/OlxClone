package com.example.olxclone.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.R;
import com.example.olxclone.api.CEPService;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.model.Endereco;
import com.example.olxclone.model.Local;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.santalu.maskara.widget.MaskEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EnderecoActivity extends AppCompatActivity {

    private static final int REQUEST_ESTADO = 150;
    private MaskEditText edit_cep;
    private Button btn_uf;
    private EditText edit_cidade;
    private EditText edit_bairro;

    private Endereco endereco;

    private Retrofit retrofit;
    private Local local;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endereco);

        iniciaComponentes();
        configCliques();
        carregaEndereco();
        iniciaRetrofit();
        recuperaEndereco();
    }



    private void recuperaEndereco() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference enderecoRef = FirebaseHelper.getDatabaseReference()
                .child("enderecos")
                .child(FirebaseHelper.getIdFirebase());
        enderecoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    endereco = snapshot.getValue(Endereco.class);
                    if (endereco != null) edit_cep.setText(endereco.getCep());
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void carregaEndereco() {
        edit_cep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String cep = s.toString().replaceAll("_", "").replace("-", "");
                if (cep.length() == 8) {
                    buscarEndereco(cep);
                } else {
                    local = null;
                    configEndereco();
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void buscarEndereco(String cep) {

        progressBar.setVisibility(View.VISIBLE);

        CEPService cepService = retrofit.create(CEPService.class);
        Call<Local> call = cepService.recuperaCEP(cep);

        call.enqueue(new Callback<Local>() {
            @Override
            public void onResponse(Call<Local> call, Response<Local> response) {
                if (response.isSuccessful()) {
                    local = response.body();

                    if (local.getLocalidade() == null) {
                        Toast.makeText(EnderecoActivity.this, "CEP inválido", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EnderecoActivity.this, "Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                configEndereco();
            }

            @Override
            public void onFailure(Call<Local> call, Throwable t) {
                Toast.makeText(EnderecoActivity.this, "Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void configEndereco() {
        if (local != null) {
            if (local.getLocalidade() != null) {
                btn_uf.setText(local.getUf());
                edit_cidade.setText(local.getLocalidade());
                edit_bairro.setText(local.getBairro());

                progressBar.setVisibility(View.GONE);
            } else {
                btn_uf.setText("");
                edit_cidade.setText("");
                edit_bairro.setText("");
            }
        } else {
            btn_uf.setText("");
            edit_cidade.setText("");
            edit_bairro.setText("");
        }
        progressBar.setVisibility(View.GONE);
    }


    private void iniciaRetrofit() {
        retrofit = new Retrofit
                .Builder()
                .baseUrl("https://viacep.com.br/ws/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void validaDados(View view) {
        String cep = edit_cep.getText().toString();
        String uf = btn_uf.getText().toString();
        String cidade = edit_cidade.getText().toString();
        String bairro = edit_bairro.getText().toString();
        if (!cep.isEmpty()) {
            if (cep.replaceAll("_", "").replace("-", "").length() == 8) {
                if (uf.length() == 2) {
                    btn_uf.setError(null);
                    if (!cidade.isEmpty()) {
                        if (!bairro.isEmpty()) {
                            progressBar.setVisibility(View.VISIBLE);

                            if (endereco == null) endereco = new Endereco();
                            Endereco endereco = new Endereco();
                            endereco.setCep(cep);
                            endereco.setUf(uf);
                            endereco.setCidade(cidade);
                            endereco.setBairro(bairro);

                            endereco.salvar(FirebaseHelper.getIdFirebase(), getBaseContext(), progressBar);


                        } else {
                            edit_bairro.requestFocus();
                            edit_bairro.setError("Digite o seu bairro");
                        }
                    } else {
                        edit_cidade.requestFocus();
                        edit_cidade.setError("Digite a sua cidade");
                    }
                } else {
                    btn_uf.requestFocus();
                    btn_uf.setError("Escolha o seu Estado");
                }
            } else {
                edit_cep.requestFocus();
                edit_cep.setError("Cep inválido");
            }
        } else {
            edit_cep.requestFocus();
            edit_cep.setError("Digite o seu cep");
        }
    }


    private void configCliques() {
        findViewById(R.id.ib_toolbar_voltar).setOnClickListener(v -> finish());

    }

    private void iniciaComponentes() {
        TextView edit_titulo = findViewById(R.id.text_toolbar);
        edit_titulo.setText("Endereço");

        edit_cep = findViewById(R.id.edit_cep);
        btn_uf = findViewById(R.id.btn_uf);
        edit_cidade = findViewById(R.id.edit_cidade);
        edit_bairro = findViewById(R.id.edit_bairro);

        progressBar = findViewById(R.id.progressBar);
    }

    public void selecionarUF(View view) {
        Intent intent = new Intent(this, EstadosActivity.class);
        intent.putExtra("filtros", true);
        startActivityForResult(intent, REQUEST_ESTADO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ESTADO) {
                String Estado = (String) data.getExtras().getSerializable("uf");
                btn_uf.setText(Estado);
            }
        }
    }
}