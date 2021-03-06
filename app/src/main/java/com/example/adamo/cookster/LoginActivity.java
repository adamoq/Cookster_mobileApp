package com.example.adamo.cookster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity {

    String login, password = "";

    ProgressDialog progress;
    Context context;
    private LoaderManager.LoaderCallbacks<JSONObject> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<JSONObject>() {
        @Override
        public android.support.v4.content.Loader<JSONObject> onCreateLoader(int id, Bundle args) {
            progress = ProgressDialog.show(context, "Proszę czekać", "Pobieram dane z serwera...");
            return new LoginLoader(getApplicationContext(), "mobileapi/?login=" + login + "&password=" + password);
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<JSONObject> loader, JSONObject data) {
            try {
                readStream(data);
            } catch (JSONException e) {
                e.printStackTrace();
                messageBox(getBaseContext(), "Błąd", e.getMessage());
            }
            progress.dismiss();
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<JSONObject> loader) {
            progress.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences settings = getSharedPreferences("login", 0);
        if (settings.getString("name", "null") != "null") {
            hideLoginPanel(settings);
        } else {
            showLoginPanel();
        }
        context = this;
    }

    private void readStream(JSONObject obj) throws JSONException {
        if (obj.has("error")) messageBox(this, "Błąd", obj.getString("error"));
        if (obj == null) showError();
        else {
            SharedPreferences.Editor editor = getSharedPreferences("login", 0).edit();
            editor.putString("name", obj.getString("name") + " " + obj.getString("surname"));
            editor.putString("login", obj.getString("login"));
            editor.putString("id", obj.getString("id"));
            editor.putString("password", obj.getString("password"));
            if (obj.has("phonenumber"))
                editor.putString("phonenumber", obj.getString("phonenumber"));
            switch (obj.getString("position")) {
                case "0":
                    editor.putString("position", "Kelner");
                    break;
                case "1":
                    editor.putString("position", "Kucharz");
                    break;
                case "2":
                    editor.putString("position", "Dostawca");
                    break;
            }
            editor.commit();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }

    }

    private void showLoginPanel() {
        ((LinearLayout) findViewById(R.id.login_linear_logged_container)).removeAllViews();
        findViewById(R.id.login_linear).setVisibility(View.VISIBLE);
        findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                login = ((TextView) findViewById(R.id.login)).getText().toString();
                password = ((TextView) findViewById(R.id.password)).getText().toString();
                makeConnection();
            }
        });
    }

    private void hideLoginPanel(final SharedPreferences settings) {
        TextView tv = new TextView(getApplicationContext());
        tv.setText(settings.getString("name", "null"));
        tv.setTextColor(Color.LTGRAY);
        tv.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        tv.setTextSize(20);
        tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                login = settings.getString("login", "null");
                password = settings.getString("password", "null");
                makeConnection();
            }
        });
        ((LinearLayout) findViewById(R.id.login_linear_logged)).addView(tv);
        findViewById(R.id.change_account).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showLoginPanel();
            }
        });

    }

    private void makeConnection() {
        LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager.getLoader(0) != null)
            loaderManager.restartLoader(0, null, mLoaderCallbacks).forceLoad();
        else loaderManager.initLoader(0, null, mLoaderCallbacks).forceLoad();
    }

    private void showError() {
        TextView tv = findViewById(R.id.loginerror);
        if (tv.getVisibility() == View.INVISIBLE) tv.setVisibility(TextView.VISIBLE);
    }

    private void messageBox(Context context, String method, String message) {
        Log.d("EXCEPTION: " + method, message);
        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(method);
        messageBox.setMessage(message);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }
}
