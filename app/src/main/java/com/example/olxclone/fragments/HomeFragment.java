package com.example.olxclone.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.R;
import com.example.olxclone.activities.CategoriasActivity;
import com.example.olxclone.activities.DetalhesAnuncioActivity;
import com.example.olxclone.activities.EstadosActivity;
import com.example.olxclone.activities.FiltrosActivity;
import com.example.olxclone.activities.FormAnuncioActivity;
import com.example.olxclone.adapter.AnuncioAdapter;
import com.example.olxclone.autenticacao.LoginActivity;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.helper.SPFiltro;
import com.example.olxclone.model.Anuncio;
import com.example.olxclone.model.Filtro;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements AnuncioAdapter.OnClickListener {

    private Button btn_novo_anuncio;
    private AnuncioAdapter anuncioAdapter;
    private List<Anuncio> anuncioList = new ArrayList<>();

    private RecyclerView rv_anuncios;
    private ProgressBar progressBar;
    private TextView text_info;

    private Button btn_regioes;
    private Button btn_categorias;
    private Button btn_filtros;

    private SearchView search_view;
    private EditText edit_search_view;

    private Filtro filtro = new Filtro();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        iniciaComponentes(view);
        configRV();
        configCliques();
        configSearchView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        configFiltros();
    }

    private void configSearchView() {
        edit_search_view = search_view.findViewById(androidx.appcompat.R.id.search_src_text);

        search_view.findViewById(androidx.appcompat.R.id.search_close_btn).setOnClickListener(v -> {
            limparPesquisa();
        });

        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SPFiltro.setFiltro(requireActivity(), "pesquisa", newText);
                configFiltros();
                ocultaTeclado();
                return true;
            }
        });

    }

    private void limparPesquisa() {
        ocultaTeclado();
        edit_search_view.setText("");
        search_view.clearFocus();

        SPFiltro.setFiltro(requireActivity(), "pesquisa", "");
        configFiltros();

    }

    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(search_view.getWindowToken(),
                inputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void recuperaAnuncios() {

        DatabaseReference anunciosRef = FirebaseHelper.getDatabaseReference()
                .child("anuncios_publicos");
        anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    anuncioList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Anuncio anuncio = ds.getValue(Anuncio.class);
                        anuncioList.add(anuncio);
                    }


                    if (!filtro.getCategoria().isEmpty()) {
                        if (!filtro.getCategoria().equals("Todas as categorias")) {
                            for (Anuncio anuncio : new ArrayList<>(anuncioList)) {
                                if (!anuncio.getCategoria().equals(filtro.getCategoria())) {
                                    anuncioList.remove(anuncio);
                                }
                            }
                        }
                    }

                    if (!filtro.getEstado().getUf().isEmpty()) {
                        for (Anuncio anuncio : new ArrayList<>(anuncioList)) {
                            if (!anuncio.getLocal().getUf().equals(filtro.getEstado().getUf())) {
                                anuncioList.remove(anuncio);
                            }
                        }
                    }

                    if (!filtro.getEstado().getDdd().isEmpty()) {
                        for (Anuncio anuncio : new ArrayList<>(anuncioList)) {
                            if (!anuncio.getLocal().getDdd().equals(filtro.getEstado().getDdd())) {
                                anuncioList.remove(anuncio);
                            }
                        }
                    }

                    if (!filtro.getPesquisa().isEmpty()) {
                        for (Anuncio anuncio : new ArrayList<>(anuncioList)) {
                            if (!anuncio.getTitulo().toLowerCase().contains(filtro.getPesquisa().toLowerCase())) {
                                anuncioList.remove(anuncio);
                            }
                        }
                    }

                    if (filtro.getValorMin() > 0) {
                        for (Anuncio anuncio : new ArrayList<>(anuncioList)) {
                            if (anuncio.getValor() < filtro.getValorMin()) {
                                anuncioList.remove(anuncio);
                            }
                        }
                    }

                    if (filtro.getValorMax() > 0) {
                        for (Anuncio anuncio : new ArrayList<>(anuncioList)) {
                            if (anuncio.getValor() > filtro.getValorMax()) {
                                anuncioList.remove(anuncio);
                            }
                        }
                    }


                    text_info.setText("");
                    Collections.reverse(anuncioList);
                    anuncioAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                } else {
                    text_info.setText("Nenhum anúncio cadastrado.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configRV() {
        rv_anuncios.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_anuncios.setHasFixedSize(true);
        anuncioAdapter = new AnuncioAdapter(anuncioList, this);
        rv_anuncios.setAdapter(anuncioAdapter);


    }

    private void configFiltros() {
        filtro = SPFiltro.getFiltro(requireActivity());
        String regiao = filtro.getEstado().getRegiao();
        String categoria = filtro.getCategoria();
        if (!categoria.isEmpty()) {
            btn_categorias.setText(categoria);
        } else {
            btn_categorias.setText("Categorias");
        }
        if (!regiao.isEmpty()) {
            btn_regioes.setText(regiao);
        } else {
            btn_regioes.setText("Regiões");
        }

        recuperaAnuncios();
    }


    private void configCliques() {
        btn_novo_anuncio.setOnClickListener(v -> {
            if (FirebaseHelper.getAutenticado()) {
                startActivity(new Intent(getActivity(), FormAnuncioActivity.class));
            } else {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        btn_categorias.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CategoriasActivity.class);
            intent.putExtra("todas", true);
            intent.putExtra("fromhome", true);
            startActivity(intent);
        });

        btn_regioes.setOnClickListener(v -> startActivity(new Intent(requireActivity(), EstadosActivity.class)));

        btn_filtros.setOnClickListener(v -> startActivity(new Intent(requireActivity(), FiltrosActivity.class)));


    }

    private void iniciaComponentes(View view) {
        btn_novo_anuncio = view.findViewById(R.id.btn_novo_anuncio);
        rv_anuncios = view.findViewById(R.id.rv_anuncios);
        text_info = view.findViewById(R.id.text_info);
        progressBar = view.findViewById(R.id.progressBar);
        btn_regioes = view.findViewById(R.id.btn_regioes);
        btn_categorias = view.findViewById(R.id.btn_categorias);
        btn_filtros = view.findViewById(R.id.btn_filtros);

        search_view = view.findViewById(R.id.search_view);

    }

    @Override
    public void OnClick(Anuncio anuncio) {
        Intent intent = new Intent(requireActivity(), DetalhesAnuncioActivity.class);
        intent.putExtra("anuncioSelecionado", anuncio);
        startActivity(intent);

    }
}
