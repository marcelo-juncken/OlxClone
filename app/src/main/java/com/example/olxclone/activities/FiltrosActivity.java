package com.example.olxclone.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.olxclone.R;
import com.example.olxclone.helper.GetMask;
import com.example.olxclone.helper.SPFiltro;
import com.example.olxclone.model.Categoria;
import com.example.olxclone.model.Filtro;

import java.util.Locale;

public class FiltrosActivity extends AppCompatActivity {

    private static final int REQUEST_CATEGORIA = 100;
    private static final int REQUEST_ESTADO = 101;
    private static final int REQUEST_REGIAO = 102;
    private Button btn_estado;
    private Button btn_regiao;

    private Button btn_categoria;
    private CurrencyEditText edit_min;
    private CurrencyEditText edit_max;

    private String estadoSelecionado;
    private String ufSelecionado;
    private String regiao;
    private String ddd;

    private String categoriaSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtros);

        iniciaComponentes();

        configCliques();

        carregaFiltros();
    }


    private void carregaFiltros() {
        Filtro filtro = SPFiltro.getFiltro(this);
        categoriaSelecionada = filtro.getCategoria();
        estadoSelecionado = filtro.getEstado().getNome();
        ufSelecionado = filtro.getEstado().getUf();
        regiao = filtro.getEstado().getRegiao();
        ddd = filtro.getEstado().getDdd();

        if (!categoriaSelecionada.isEmpty()) {
            btn_categoria.setText(categoriaSelecionada);
        } else {
            btn_categoria.setText("Todas as categorias");
        }


        if (!estadoSelecionado.isEmpty()) {
            btn_estado.setText(estadoSelecionado);
            btn_regiao.setVisibility(View.VISIBLE);

        } else {
            btn_estado.setText("Todos os Estados");
            btn_regiao.setVisibility(View.GONE);
        }

        if (!regiao.isEmpty()) {
            btn_regiao.setText(regiao);
        } else {
            btn_regiao.setText("Todas as regiões");
        }


        recuperaValores();
    }

    private void recuperaValores() {
        edit_min.setText(GetMask.getValor(SPFiltro.getFiltro(this).getValorMin()));
        edit_max.setText(GetMask.getValor(SPFiltro.getFiltro(this).getValorMax()));
    }


    private void filtraValores() {
        String valorMin = String.valueOf(edit_min.getRawValue() / 100);
        String valorMax = String.valueOf(edit_max.getRawValue() / 100);

        SPFiltro.setFiltro(this, "valorMin", valorMin);
        SPFiltro.setFiltro(this, "valorMax", valorMax);
        SPFiltro.setFiltro(this, "categoria", categoriaSelecionada);
        SPFiltro.setFiltro(this, "nomeEstado", estadoSelecionado);
        SPFiltro.setFiltro(this, "ufEstado", ufSelecionado);
        SPFiltro.setFiltro(this, "regiao", regiao);
        SPFiltro.setFiltro(this, "ddd", ddd);
    }

    private void limpaFiltros() {
        edit_min.setValue(0);
        edit_max.setValue(0);
        btn_categoria.setText("Todas as categorias");
        btn_estado.setText("Todos os Estados");
        btn_regiao.setText("Todas as regiões");
        btn_regiao.setVisibility(View.GONE);
        categoriaSelecionada = "";
        estadoSelecionado = "";
        ufSelecionado = "";
        regiao = "";
        ddd = "";

    }

    private void configCliques() {
        findViewById(R.id.ib_toolbar_filtro_voltar).setOnClickListener(v -> finish());

        btn_categoria.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoriasActivity.class);
            intent.putExtra("todas", true);
            intent.putExtra("fromhome", false);
            startActivityForResult(intent, REQUEST_CATEGORIA);
        });

        btn_estado.setOnClickListener(v -> {

            Intent intent = new Intent(this, EstadosActivity.class);
            intent.putExtra("filtros", true);
            startActivityForResult(intent, REQUEST_ESTADO);
        });

        btn_regiao.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegioesActivity.class);
            intent.putExtra("filtros", true);
            intent.putExtra("uf", ufSelecionado);
            startActivityForResult(intent, REQUEST_REGIAO);
        });

        findViewById(R.id.btn_limpar).setOnClickListener(v -> {
            limpaFiltros();
        });

        findViewById(R.id.btn_filtrar).setOnClickListener(v -> {
            filtraValores();
            finish();
        });


    }


    private void iniciaComponentes() {
        btn_estado = findViewById(R.id.btn_estado);
        btn_regiao = findViewById(R.id.btn_regiao);
        btn_categoria = findViewById(R.id.btn_categoria);
        edit_min = findViewById(R.id.edit_min);
        edit_max = findViewById(R.id.edit_max);
        edit_min.setLocale(new Locale("PT", "br"));
        edit_max.setLocale(new Locale("PT", "br"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CATEGORIA) {
                Categoria categoria = (Categoria) data.getExtras().getSerializable("categoriaSelecionada");
                categoriaSelecionada = categoria.getNome();
                btn_categoria.setText(categoriaSelecionada);

            } else if (requestCode == REQUEST_ESTADO) {
                estadoSelecionado = data.getExtras().getString("estado");
                ufSelecionado = data.getExtras().getString("uf");
                regiao = "";
                ddd = "";
                btn_regiao.setText("Todas as regiões");
                if (!estadoSelecionado.equals("Brasil")) {
                    btn_estado.setText(estadoSelecionado);
                    btn_regiao.setVisibility(View.VISIBLE);
                } else {
                    btn_estado.setText("Todos os Estados");
                    btn_regiao.setVisibility(View.GONE);
                }

            } else if (requestCode == REQUEST_REGIAO) {
                regiao = data.getExtras().getString("regiao");
                ddd = data.getExtras().getString("ddd");
                btn_regiao.setText(regiao);
            }
        }
    }
}