/*

 * @startuml

 * testdot

 * @enduml

 */
package com.example.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    /**
     * Shared Preferences, used to save condition of needed fields
     */
    SharedPreferences sharedPreferences;

    /**
     * Context of the activity
     */
    Context context;

    /**
     * Current user login
     */
    private String username = "Player";

    /**
     * GameKey formed by KeyPicker Fragment, or got from lobby.
     */
    private String gameKey;

    /**
     * Field for change of player's name
     */
    EditText login;

    /**
     * Sets handlers for buttons and login TextView in menu
     */
    void setHandlers() {

        // Makes username TextField save its value in SharedPreferences:
        login = findViewById(R.id.login_field);
        login.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        username = s.toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
        // Calls Lobby activity
        findViewById(R.id.create_lobby_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.length() < 3 || username.length() > 12) {
                    login.setText("Login");
                    Toast toast = Toast.makeText(context,
                            getString(R.string.login_length),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                JSONObject jsonObjectWithBody = new JSONObject();
                try {
                    jsonObjectWithBody.put("name", username);
                } catch (JSONException e) {
                    Log.d("Can't make request:", e.toString());
                }
                final String requestBody = jsonObjectWithBody.toString();

                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        getString(R.string.url) + "create_lobby",
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                gameKey = response.getString("key");
                                Intent intent_redirectToLobby = new Intent(context, LobbyActivity.class).
                                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).
                                        putExtra("GAME_ID", gameKey).
                                        putExtra("NAME", username);
                                startActivity(intent_redirectToLobby);
                            } else {
                                try {
                                    Toast toast = Toast.makeText(context,
                                            getString(R.string.problem),
                                            Toast.LENGTH_SHORT);

                                    toast.show();
                                } catch (Exception ignored) {
                                }
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Toast toast = Toast.makeText(context,
                                    getString(R.string.problem),
                                    Toast.LENGTH_SHORT);

                            toast.show();
                        } catch (Exception ignored) {
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }

                    @Override
                    public byte[] getBody() {
                        try {
                            return requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            Log.d("Error", "UTF-8 encoding error");
                            return null;
                        }
                    }


                };
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        5000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonObjectRequest);
            }
        });

        // Calls KeyPicker activity
        findViewById(R.id.join_lobby_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.length() < 3 || username.length() > 12) {
                    login.setText("Login");
                    Toast toast = Toast.makeText(context,
                            getString(R.string.login_length),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                KeyPicker keyPickerDialog = new KeyPicker(username, context);
                keyPickerDialog.show(getSupportFragmentManager(), "key_picker");
            }
        });

        // Calls HELP fragment
        findViewById(R.id.help_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast toast = Toast.makeText(context,
                            getString(R.string.helpmsg),
                            Toast.LENGTH_LONG);

                    toast.show();
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * Gets values of elements stored in SharedPreferences:
     * 1. Username
     */
    void getSavedElements() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        username = sharedPreferences.getString("login", "");
        ((EditText) findViewById(R.id.login_field)).setText(username);
    }

    /**
     * Initializes everything when activity is created
     *
     * @param savedInstanceState saved elements values
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        setHandlers();
        getSavedElements();
    }
}
