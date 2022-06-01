package com.example.olxclone.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.R;
import com.example.olxclone.adapter.EstadoAdapter;
import com.example.olxclone.helper.EstadosList;
import com.example.olxclone.helper.SPFiltro;
import com.example.olxclone.model.Estado;

import java.util.ArrayList;
import java.util.List;

public class EstadosActivity extends AppCompatActivity implements EstadoAdapter.OnClickListener {

    private RecyclerView rv_estados;
    private EstadoAdapter estadoAdapter;
    private Boolean acesso = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estados);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            acesso = bundle.getBoolean("filtros");
        }
        iniciaComponentes();
        configRV();
        configCliques();
    }

    private void iniciaComponentes() {
        TextView text_toolbar = findViewById(R.id.text_toolbar);
        text_toolbar.setText("Estados");
        rv_estados = findViewById(R.id.rv_estados);
    }

    private void configCliques() {
        findViewById(R.id.ib_toolbar_voltar).setOnClickListener(v -> finish());
    }

    private void configRV() {
        rv_estados.setLayoutManager(new LinearLayoutManager(this));
        rv_estados.setHasFixedSize(true);
        estadoAdapter = new EstadoAdapter(EstadosList.getList(), this);
        rv_estados.setAdapter(estadoAdapter);
    }

    @Override
    public void OnClick(Estado estado) {

        if (acesso) {
            Intent intent = new Intent();
            intent.putExtra("estado", estado.getNome());
            intent.putExtra("uf", estado.getUf());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            if (!estado.getNome().equals("Brasil")) {
                SPFiltro.setFiltro(this, "ufEstado", estado.getUf());
                SPFiltro.setFiltro(this, "nomeEstado", estado.getNome());
                Intent intent = new Intent(this, RegioesActivity.class);
                intent.putExtra("filtros", false);
                intent.putExtra("uf", SPFiltro.getFiltro(this).getEstado().getUf());
                startActivity(intent);
            } else {
                SPFiltro.setFiltro(this, "ufEstado", "");
                SPFiltro.setFiltro(this, "nomeEstado", "");
                SPFiltro.setFiltro(this, "regiao", "");
                finish();
            }
        }
    }
}