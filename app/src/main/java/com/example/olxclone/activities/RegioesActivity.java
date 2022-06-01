package com.example.olxclone.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.R;
import com.example.olxclone.adapter.RegiaoAdapter;
import com.example.olxclone.helper.RegioesList;
import com.example.olxclone.helper.SPFiltro;
import com.example.olxclone.model.Estado;

import java.util.ArrayList;
import java.util.List;

public class RegioesActivity extends AppCompatActivity implements RegiaoAdapter.OnClickListener {

    private RegiaoAdapter regiaoAdapter;
    private RecyclerView rv_regioes;
    private Boolean acesso = false;
    private String ufSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regioes);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            acesso = bundle.getBoolean("filtros");
            ufSelecionado = bundle.getString("uf");
        }

        iniciaComponentes();
        configCliques();

        configRV();
    }

    private void configCliques() {
        findViewById(R.id.ib_toolbar_voltar).setOnClickListener(v -> finish());
    }

    private void iniciaComponentes() {
        TextView text_toolbar = findViewById(R.id.text_toolbar);
        text_toolbar.setText("Selecione a região");
        rv_regioes = findViewById(R.id.rv_regioes);
    }

    private void configRV() {
        rv_regioes.setLayoutManager(new LinearLayoutManager(this));
        rv_regioes.setHasFixedSize(true);

        regiaoAdapter = new RegiaoAdapter(RegioesList.getList(ufSelecionado), this); // --------- no lugar de anuncio List, aqui passa o endereco da lista. pode ser EstadosList.getList(), por exemplo


        rv_regioes.setAdapter(regiaoAdapter);
    }

    @Override
    public void OnClick(String regiao) {
        String ddd = "";
        if (!regiao.equals("Todas as regiões")){
            ddd =  regiao.substring(4,6);
        }
        if (acesso) {
            Intent intent = new Intent();
            intent.putExtra("regiao", regiao);
            intent.putExtra("ddd", ddd);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            SPFiltro.setFiltro(this, "regiao", regiao);
            SPFiltro.setFiltro(this, "ddd", ddd);
            startActivity(new Intent(this, MainActivity.class));
        }

    }
}