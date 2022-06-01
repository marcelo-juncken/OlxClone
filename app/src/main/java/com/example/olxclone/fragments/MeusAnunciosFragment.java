package com.example.olxclone.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.olxclone.R;
import com.example.olxclone.activities.FormAnuncioActivity;
import com.example.olxclone.adapter.AnuncioAdapter;
import com.example.olxclone.autenticacao.LoginActivity;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.model.Anuncio;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeusAnunciosFragment extends Fragment implements AnuncioAdapter.OnClickListener {

    private AnuncioAdapter anuncioAdapter;
    private List<Anuncio> anuncioList = new ArrayList<>();

    private SwipeableRecyclerView rv_anuncios;
    private ProgressBar progressBar;
    private TextView text_info;
    private Button btn_logar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meus_anuncios, container, false);

        iniciaComponentes(view);
        configRV();
        configCliques();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperaAnuncios();
    }

    private void recuperaAnuncios() {
        if (FirebaseHelper.getAutenticado()) {
            btn_logar.setVisibility(View.GONE);
            DatabaseReference anunciosRef = FirebaseHelper.getDatabaseReference()
                    .child("meus_anuncios")
                    .child(FirebaseHelper.getIdFirebase());
            anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        anuncioList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Anuncio anuncio = ds.getValue(Anuncio.class);
                            anuncioList.add(anuncio);
                        }
                        text_info.setText("");
                        Collections.reverse(anuncioList);
                        anuncioAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        text_info.setText("Nenhum anúncio cadastrado.");
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

    private void configRV() {
        rv_anuncios.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_anuncios.setHasFixedSize(true);
        anuncioAdapter = new AnuncioAdapter(anuncioList, this);
        rv_anuncios.setAdapter(anuncioAdapter);

        rv_anuncios.setListener(new SwipeLeftRightCallback.Listener() {
            @Override
            public void onSwipedLeft(int position) {
                showDialogRemove(anuncioList.get(position));
            }

            @Override
            public void onSwipedRight(int position) {
                showDialogEdit(anuncioList.get(position));
            }
        });

    }

    private void showDialogEdit(Anuncio anuncio){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar o anúncio");
        builder.setCancelable(false);
        builder.setMessage("Deseja editar o anúncio?");
        builder.setNegativeButton("Não", (dialog, which) -> {
            dialog.dismiss();
            anuncioAdapter.notifyDataSetChanged();
        });
        builder.setPositiveButton("Sim",(dialog, which) -> {
            Intent intent = new Intent(requireActivity(), FormAnuncioActivity.class);
            intent.putExtra("anuncioSelecionado", anuncio);
            startActivity(intent);
            anuncioAdapter.notifyDataSetChanged();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogRemove(Anuncio anuncio){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete anúncio");
        builder.setCancelable(false);
        builder.setMessage("Deseja remover o anúncio?");
        builder.setNegativeButton("Não", (dialog, which) -> {
            dialog.dismiss();
            anuncioAdapter.notifyDataSetChanged();
        });
        builder.setPositiveButton("Sim",(dialog, which) -> {
            anuncioList.remove(anuncio);
            anuncio.deletar();
            anuncioAdapter.notifyDataSetChanged();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void configCliques(){
        btn_logar.setOnClickListener(v -> startActivity(new Intent(requireActivity(),LoginActivity.class)));
    }

    private void iniciaComponentes(View view) {
        rv_anuncios = view.findViewById(R.id.rv_anuncios);
        text_info = view.findViewById(R.id.text_info);
        btn_logar = view.findViewById(R.id.btn_logar);
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void OnClick(Anuncio anuncio) {
        Intent intent = new Intent(requireActivity(), FormAnuncioActivity.class);
        intent.putExtra("anuncioSelecionado", anuncio);
        startActivity(intent);
        anuncioAdapter.notifyDataSetChanged();
    }
}