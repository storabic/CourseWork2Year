package com.example.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Primary activity for GAME lobby
 */
public class GameActivity extends AppCompatActivity {

    /**
     * Player name
     */
    private String name;

    /**
     * Game id
     */
    private String gameKey;

    /**
     * Game round
     */
    private int round;

    /**
     * Is day or night
     */
    private boolean is_night;

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
     * Activity itself as context
     */
    Context context;

    /**
     * Player list
     */
    ListView playerList;

    /**
     * Game Info GUI
     */
    TextView gameInfo;

    /**
     * Message to chat
     */
    EditText message;

    String result;

    /**
     * Button to send message {@link GameActivity#message)}
     */
    Button button;

    /**
     * Game session chat
     */
    TextView chat;

    /**
     * Help Toast
     */
    Button faq;

    /**
     * Initializes all elements and sets handlers
     * Starts {@link GameActivity#startAsking()} ()}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameKey = getIntent().getStringExtra("GAME_ID");
        name = getIntent().getStringExtra("NAME");
        context = this;
        mHandler = new Handler();
        round = -100;
        is_night = false;
        result = "";
        playerList = findViewById(R.id.playerList);
        playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject jsonObjectWithBody = new JSONObject();
                try {
                    jsonObjectWithBody.put("name", name);
                    jsonObjectWithBody.put("choice", ((TextView)view).getText());
                } catch (JSONException e) {
                    Log.d("Can't make request:", e.toString());
                }
                final String requestBody = jsonObjectWithBody.toString();

                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                        getString(R.string.url) + "select_player",
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
        gameInfo = findViewById(R.id.game_info);
        findViewById(R.id.res).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result.length() > 0) {
                    Toast toast = Toast.makeText(context,
                            result,
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(context,
                            "Новостные сводки пока пусты",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        chat = findViewById(R.id.chat);
        chat.setMovementMethod(new ScrollingMovementMethod());
        button = findViewById(R.id.button);
        message = findViewById(R.id.editText);
        faq = findViewById(R.id.faq);
        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(context,
                        getString(R.string.game_faq),
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getText().length() > 0) {
                    JSONObject jsonObjectWithBody = new JSONObject();
                    try {
                        jsonObjectWithBody.put("name", name);
                        jsonObjectWithBody.put("key", gameKey);
                        jsonObjectWithBody.put("msg", message.getText());
                    } catch (JSONException e) {
                        Log.d("Can't make request:", e.toString());
                    }
                    final String requestBody = jsonObjectWithBody.toString();

                    RequestQueue queue = Volley.newRequestQueue(context);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                            getString(R.string.url) + "send_msg",
                            null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
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
                    message.setText("");
                }
            }
        });
        startAsking();
    }

    /**
     * Async runnable for {@link GameActivity#update()}
     */
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                update(); //this function can change value of mInterval.
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
    void update() {
        JSONObject jsonObjectWithBody = new JSONObject();
        try {
            jsonObjectWithBody.put("key", gameKey);
            jsonObjectWithBody.put("name", name);
            jsonObjectWithBody.put("round", round);
            jsonObjectWithBody.put("is_night", is_night);
        } catch (JSONException e) {
            Log.d("Can't make request:", e.toString());
        }
        final String requestBody = jsonObjectWithBody.toString();

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                getString(R.string.url) + "get_info",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    try {
                        chat.setText(response.getString("chat"));
                    } catch (Exception ignored) {
                    }
                    if (response.getString("message").equals("new")) {
                        chat.setText(response.getString("chat"));
                        round = response.getInt("day");
                        if (is_night != response.getBoolean("is_night")) {
                            Toast toast = Toast.makeText(context,
                                    "Наступает " + (response.getBoolean("is_night") ? getString(R.string.night) :
                                            getString(R.string.not_night)),
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            is_night = response.getBoolean("is_night");
                        }
                        JSONArray arr = response.getJSONArray("players");
                        String friends = "";
                        if (response.getInt("role") == 2) {
                            JSONArray fr = response.getJSONArray("friends");
                            for (int i = 0; i < fr.length(); ++i) {
                                friends += fr.getString(i) + (i == fr.length() - 1 ? "" : ", ");
                            }
                        }
                        result = response.getString("result");
                        String role;
                        switch (response.getInt("role")) {
                            case (2):
                                role = getString(R.string.mafia);
                                break;
                            case (3):
                                role = getString(R.string.has_cop);
                                break;
                            case (4):
                                role = getString(R.string.has_doctor);
                                break;
                            case (5):
                                role = getString(R.string.has_lover);
                                break;
                            default:
                                role = getString(R.string.civilian);
                                break;
                        }
                        gameInfo.setText(getString(R.string.your_name) + ": " + name + " / " +
                                role + "\n" +
                                (friends.length() != 0 ?
                                        getString(R.string.friends) + ": " + friends : "") + "\n" +
                                getString(R.string.day) + ": " + response.getInt("day") + "\n" +
                                (response.getBoolean("is_night") ? getString(R.string.night) :
                                        getString(R.string.not_night)));
                        if (arr.length() != playerList.getCount()) {
                            String[] res = new String[arr.length()];
                            for (int i = 0; i < arr.length(); ++i) {
                                res[i] = arr.getString(i);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                    R.layout.list_iter, res);
                            playerList.setAdapter(adapter);
                        }
                    } else {
                        try {
                            result = response.getString("result");
                            Toast toast = Toast.makeText(context,
                                    "Конец игры\n" + result,
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            stopRepeatingTask();
                        } catch (Exception ignored) {
                            Toast toast = Toast.makeText(context,
                                    "Конец игры\n" + "Вы были убиты",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            stopRepeatingTask();
                        }
                    }
                } catch (JSONException ignored) {
                    Toast toast = Toast.makeText(context,
                            "Конец игры\n",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    stopRepeatingTask();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Toast toast = Toast.makeText(context,
                            "Игра окончена\nВы выиграли",
                            Toast.LENGTH_SHORT);
                    stopRepeatingTask();
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

    /**
     * Calls when activity is getting geleted,
     * sends disconnect request and stops {@link GameActivity#update()}
     */
    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            stopRepeatingTask();
            super.onStop();
            JSONObject jsonObjectWithBody = new JSONObject();
            try {
                jsonObjectWithBody.put("name", name);
                jsonObjectWithBody.put("key", gameKey);
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
        } catch(Exception ignored) {
        }
    }
}
