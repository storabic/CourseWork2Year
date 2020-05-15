package com.example.android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
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

/**
 * Presents dialog for choosing lobby
 */
public class KeyPicker extends DialogFragment {
    /**
     * Wheel for choosing one symbol of game key
     */
    NumberPicker charPicker0;
    /**
     * Wheel for choosing one symbol of game key
     */
    NumberPicker charPicker1;
    /**
     * Wheel for choosing one symbol of game key
     */
    NumberPicker charPicker2;
    /**
     * Wheel for choosing one symbol of game key
     */
    NumberPicker charPicker3;

    /**
     * Player name
     */
    private String name;

    /**
     * MainActivity context
     */
    private Context context;

    /**
     * Constructor, sets name and context
     * @param name name
     * @param context context
     */
    KeyPicker(String name, Context context) {
        this.name = name;
        this.context = context;
    }

    /**
     * Initalizes everything in dalog when created
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
                "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        View root = getActivity().getLayoutInflater().
                inflate(R.layout.fragment_key_picker, null);
        charPicker0 = root.findViewById(R.id.char0);
        charPicker1 = root.findViewById(R.id.char1);
        charPicker2 = root.findViewById(R.id.char2);
        charPicker3 = root.findViewById(R.id.char3);
        charPicker0.setMinValue(0);
        charPicker0.setMaxValue(alphabet.length - 1);
        charPicker0.setDisplayedValues(alphabet);
        charPicker1.setMinValue(0);
        charPicker1.setMaxValue(alphabet.length - 1);
        charPicker1.setDisplayedValues(alphabet);
        charPicker2.setMinValue(0);
        charPicker2.setMaxValue(alphabet.length - 1);
        charPicker2.setDisplayedValues(alphabet);
        charPicker3.setMinValue(0);
        charPicker3.setMaxValue(alphabet.length - 1);
        charPicker3.setDisplayedValues(alphabet);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogBW);
        builder.setTitle("Выберите ключ игры или присоединитесь к случайной");
        builder.setView(root);
        builder.setPositiveButton(R.string.join, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result = alphabet[charPicker0.getValue()] + alphabet[charPicker1.getValue()]
                        + alphabet[charPicker2.getValue()] + alphabet[charPicker3.getValue()];
                JSONObject jsonObjectWithBody = new JSONObject();
                try {
                    jsonObjectWithBody.put("name", name);
                    jsonObjectWithBody.put("key", result);
                } catch (JSONException e) {
                    Log.d("Can't make request:", e.toString());
                }

                final String requestBody = jsonObjectWithBody.toString();

                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                        getString(R.string.url) + "join_lobby",
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String gameKey = response.getString("key");
                            Intent intent_redirectToGame =
                                    new Intent(context, GameActivity.class).
                                            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).
                                            putExtra("GAME_ID", gameKey).
                                            putExtra("NAME", name);
                            context.startActivity(intent_redirectToGame);
                        } catch (JSONException e) {
                            Log.d("Error", e.toString());
                            Toast toast = Toast.makeText(context,
                                    context.getString(R.string.name),
                                    Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Toast toast = Toast.makeText(getContext(),
                                    getString(R.string.problem),
                                    Toast.LENGTH_LONG);
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
                queue.add(jsonObjectRequest);

            }
        });
        builder.setNeutralButton(R.string.join_random, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JSONObject jsonObjectWithBody = new JSONObject();
                try {
                    jsonObjectWithBody.put("name", name);
                } catch (JSONException e) {
                    Log.d("Can't make request:", e.toString());
                }

                final String requestBody = jsonObjectWithBody.toString();

                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                        getString(R.string.url) + "join_lobby",
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String gameKey = response.getString("key");
                            Intent intent_redirectToGame =
                                    new Intent(context, GameActivity.class).
                                            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).
                                            putExtra("GAME_ID", gameKey).
                                            putExtra("NAME", name);
                            context.startActivity(intent_redirectToGame);
                        } catch (JSONException e) {
                            Log.d("Error", e.toString());
                            Toast toast = Toast.makeText(context,
                                    context.getString(R.string.name),
                                    Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Toast toast = Toast.makeText(getContext(),
                                    getString(R.string.problem),
                                    Toast.LENGTH_LONG);
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
                queue.add(jsonObjectRequest);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        return builder.create();
    }
}