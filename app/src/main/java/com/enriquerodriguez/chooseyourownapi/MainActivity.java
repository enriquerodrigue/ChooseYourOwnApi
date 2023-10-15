package com.enriquerodriguez.chooseyourownapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.enriquerodriguez.chooseyourownapi.databinding.ActivityMainBinding;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AutoCompleteTextView searchEditText;
    private Button searchButton;
    private ImageView pokemonImageView;
    private TextView pokemonNameTextView;
    private TextView idTextView;
    private Button nextButton;
    private String id;
    private List<String> pokemonNames = new ArrayList<>();
    private ConstraintLayout appConstraintLayout;
    private final Map<String, String> typeColors = new HashMap<String, String>(){{
       put("fire","#AB402A");
       put("water","#45BAF1");
       put("grass","#6BDD75");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getViewsFromBinding();
        gatherPokemonNames();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pokemonNames);
        searchEditText.setAdapter(adapter);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networkRequest(searchEditText.getText().toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                nextButton.setEnabled(true);
                nextButton.setVisibility(View.VISIBLE);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nextPokemon = Integer.parseInt(id) + 1;
                networkRequest(String.valueOf(nextPokemon));
            }
        });
    }

    private void getViewsFromBinding(){
        searchEditText = binding.searchEditText;
        searchButton = binding.searchButton;
        pokemonImageView = binding.pokemonImageView;
        pokemonNameTextView = binding.pokemonNameTextView;
        appConstraintLayout = binding.appConstraintLayout;
        idTextView = binding.idTextView;
        nextButton = binding.nextButton;

        nextButton.setVisibility(View.INVISIBLE);
    }

    private void networkRequest(String param){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String url = "https://pokeapi.co/api/v2/pokemon/" + param.toLowerCase();
        params.put("limit", "6");
        params.put("page", "0");
        client.get(url, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON response) {
                        String name;
                        String imageUrl;
                        String type;
                        try {
                            name = response.jsonObject.getString("name");
                            imageUrl = response.jsonObject.getJSONObject("sprites")
                                    .getJSONObject("other").getJSONObject("official-artwork")
                                    .getString("front_default");
                            type = response.jsonObject.getJSONArray("types").
                                    getJSONObject(0).getJSONObject("type").getString("name");
                            id = String.valueOf(response.jsonObject.getInt("id"));
                            populateViews(name, imageUrl, type, id);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String errorResponse, Throwable t) {
                    }
                }
        );
    }

    @SuppressLint("SetTextI18n")
    private void populateViews(String name, String imageUrl, String type, String id) {
        String capitalizedName = name.substring(0,1).toUpperCase() + name.substring(1);
        pokemonNameTextView.setText(capitalizedName);
        Picasso.get().load(imageUrl).into(pokemonImageView);
        idTextView.setText("#"+id);
        if(typeColors.containsKey(type)) {
            appConstraintLayout.setBackgroundColor(Color.parseColor(typeColors.get(type)));
        } else {
            appConstraintLayout.setBackgroundColor(Color.WHITE);
        }
    }

    private void gatherPokemonNames(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String url = "https://pokeapi.co/api/v2/pokemon/";
        params.put("limit", "1292");
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON response) {
                try {
                    JSONArray jsonArray = response.jsonObject.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject pokemon = jsonArray.getJSONObject(i);
                        String name = pokemon.getString("name");
                        pokemonNames.add(name);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
    }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        });
    }
}