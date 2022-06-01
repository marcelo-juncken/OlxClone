package com.example.olxclone.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.R;
import com.example.olxclone.activities.DetalhesAnuncioActivity;
import com.example.olxclone.adapter.AnuncioAdapter;
import com.example.olxclone.autenticacao.LoginActivity;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.model.Anuncio;
import com.example.olxclone.model.Favorito;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritosFragment extends Fragment implements AnuncioAdapter.OnClickListener {

    private AnuncioAdapter anuncioAdapter;
    private List<Anuncio> anuncioList = new ArrayList<>();

    private List<String> favoritosList = new ArrayList<>();


    private RecyclerView rv_anuncios;
    private ProgressBar progressBar;
    private TextView text_info;
    private Button btn_logar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favoritos, container, false);
        iniciaComponentes(view);
        configCliques();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        configRV();
        recuperaFavoritos();
    }

    private void configRV() {
        rv_anuncios.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_anuncios.setHasFixedSize(true);
        anuncioAdapter = new AnuncioAdapter(anuncioList, this);
        rv_anuncios.setAdapter(anuncioAdapter);
    }

    private void recuperaFavoritos() {
        favoritosList.clear();
        anuncioList.clear();
        if (FirebaseHelper.getAutenticado()) {
            btn_logar.setVisibility(View.GONE);
            DatabaseReference favoritosRef = FirebaseHelper.getDatabaseReference()
                    .child("favoritos")
                    .child(FirebaseHelper.getIdFirebase());
            favoritosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            favoritosList.add(ds.getValue(String.class));
                        }
                        recuperaAnuncios();
                    } else {
                        text_info.setText("Nenhum anúncio salvo.");
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            btn_logar.setVisibility(View.VISIBLE);
            text_info.setText("");
            progressBar.setVisibility(View.GONE);
        }
    }

    private void recuperaAnuncios() {
        DatabaseReference anunciosRef = FirebaseHelper.getDatabaseReference()
                .child("anuncios_publicos");
        anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Anuncio anuncio = ds.getValue(Anuncio.class);
                        if (favoritosList.contains(anuncio.getId())) {
                            anuncioList.add(anuncio);
                        }
                    }
                    text_info.setText("");
                    Collections.reverse(anuncioList);
                    anuncioAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void configCliques(){
        btn_logar.setOnClickListener(v -> startActivity(new Intent(requireActivity(), LoginActivity.class)));
    }

    private void iniciaComponentes(View view) {
        rv_anuncios = view.findViewById(R.id.rv_anuncios);
        progressBar = view.findViewById(R.id.progressBar);
        text_info = view.findViewById(R.id.text_info);
        btn_logar = view.findViewById(R.id.btn_logar);
    }

    @Override
    public void OnClick(Anuncio anuncio) {
        Intent intent = new Intent(requireActivity(), DetalhesAnuncioActivity.class);
        intent.putExtra("anuncioSelecionado", anuncio);
        startActivity(intent);
    }
}