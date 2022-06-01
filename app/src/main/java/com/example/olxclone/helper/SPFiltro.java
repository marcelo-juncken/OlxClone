package com.example.olxclone.helper;

import android.app.Activity;
import android.content.SharedPreferences;

import com.example.olxclone.model.Estado;
import com.example.olxclone.model.Filtro;

public class SPFiltro {

    private static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";

    public static void setFiltro(Activity activity, String chave, String valor){
        SharedPreferences preferences = activity.getSharedPreferences(ARQUIVO_PREFERENCIA,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(chave,valor);
        editor.commit();
    }

    public static Filtro getFiltro(Activity activity){
        SharedPreferences preferences = activity.getSharedPreferences(ARQUIVO_PREFERENCIA,0);

        String pesquisa = preferences.getString("pesquisa","");
        String ufEstado = preferences.getString("ufEstado","");
        String categoria = preferences.getString("categoria","");
        String nomeEstado = preferences.getString("nomeEstado","");
        String regiao = preferences.getString("regiao","");
        String ddd = preferences.getString("ddd","");

        String valorMin = preferences.getString("valorMin","");
        String valorMax = preferences.getString("valorMax","");

        Estado estado = new Estado();
        estado.setUf(ufEstado);
        estado.setNome(nomeEstado);
        estado.setDdd(ddd);
        estado.setRegiao(regiao);

        Filtro filtro = new Filtro();
        filtro.setEstado(estado);
        filtro.setPesquisa(pesquisa);
        filtro.setCategoria(categoria);

        if(!valorMin.isEmpty()) filtro.setValorMin(Double.parseDouble(valorMin));
        if(!valorMax.isEmpty()) filtro.setValorMax(Double.parseDouble(valorMax));

        return filtro;
    }

    public static void LimparFiltros(Activity activity){
        setFiltro(activity, "pesquisa", "");
        setFiltro(activity, "ufEstado", "");
        setFiltro(activity, "categoria", "");
        setFiltro(activity, "nomeEstado", "");
        setFiltro(activity, "regiao", "");
        setFiltro(activity, "ddd", "");
    }
}
