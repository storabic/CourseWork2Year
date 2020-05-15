package com.example.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity representing new game settings
 */
public class LobbyActivity extends AppCompatActivity {

    /**
     * Default interval between get_info requests {@link GameActivity#update()}
     * 5 seconds by default, can be changed later
     */
    private int mInterval = 5000;

    /**
     * Handler for Running calls, needed for get_info periodic requests
     * {@link GameActivity#update()}
     */
    private Handler mHandler;

    /**
     * Player name
     */
    private String name;

    /**
     * Activity itself as context
     */
    private Context context;

    /**
     * Game id
     */
    private String gameKey;

    /**
     * Game round
     */
    private int round;

    /**
     * Day or night
     */
    private boolean is_night;

    /**
     * Initializes all elements and sets handlers
     * Starts {@link GameActivity#startAsking()} ()}
     * @param savedInstanceState saved elements values
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_actitivty);
        gameKey = getIntent().getStringExtra("GAME_ID");
        ((TextView) findViewById(R.id.label_gameid)).setText("ID: " + gameKey);
        name = getIntent().getStringExtra("NAME");
        mHandler = new Handler();
        context = this;
        round = 0;
        is_night = false;
        ((Button)findViewById(R.id.start_lobby_button)).
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObjectWithBody = new JSONObject();
                try {
                    jsonObjectWithBody.put("key", gameKey);
                    jsonObjectWithBody.put("n_mafia",
                            ((SeekBar)findViewById(R.id.seekBar)).getProgress());
                    jsonObjectWithBody.put("cop",
                            ((Switch)findViewById(R.id.cop_switch)).isChecked());
                    jsonObjectWithBody.put("doctor",
                            ((Switch)findViewById(R.id.doctor_switch)).isChecked());
                    jsonObjectWithBody.put("lover",
                            ((Switch)findViewById(R.id.lover_switch)).isChecked());
                } catch (JSONException e) {
                    Log.d("Can't make request:", e.toString());
                }
                final String requestBody = jsonObjectWithBody.toString();

                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                        getString(R.string.url) + "start_game",
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Intent intent_redirectToGame =
                                        new Intent(context, GameActivity.class).
                                                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).
                                                putExtra("GAME_ID", gameKey).
                                                putExtra("NAME", name);
                                stopRepeatingTask();
                                context.startActivity(intent_redirectToGame);
                            } else {
                                Toast toast = Toast.makeText(context,
                                        getString(R.string.role_error),
                                        Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
        startAsking();
    }

    /**
     * Calls when activity is getting geleted,
     * sends disconnect request and stops {@link GameActivity#update()}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    /**
     * Async runnable for {@link GameActivity#update()}
     */
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updatePlayerList(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    /**
     * Updates game info by get_info requests
     */
    void updatePlayerList() {
        final JSONObject jsonObjectWithBody = new JSONObject();
        try {
            jsonObjectWithBody.put("key", gameKey);
            jsonObjectWithBody.put("name", name);
            jsonObjectWithBody.put("round", round);
            jsonObjectWithBody.put("is_night", is_night);
        } catch (JSONException e) {
            Log.d("Can't make request:", e.toString());
            return;
        }
        final String requestBody = jsonObjectWithBody.toString();

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                getString(R.string.url) + "get_info",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        JSONArray arr = response.getJSONArray("players");
                        String res = "";
                        for (int i = 0; i < arr.length(); ++i)
                            res += arr.getString(i) + "\n";
                        ((TextView)findViewById(R.id.listPlayers)).setText(res);
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

    /**
     * Lets handler run its task
     */
    void startAsking() {
        mStatusChecker.run();
    }

    /**
     * Prevents handler from running his task
     */
    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
