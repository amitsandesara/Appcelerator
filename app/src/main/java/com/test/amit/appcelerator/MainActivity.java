package com.test.amit.appcelerator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button saveMessage, editMessage;
    EditText setMessage;
    TextView viewMessage;
    LinearLayout edit;
    String message = "";
    SharedPreferences sharedPref;
    String URL = "";
    JSONArray arr;
    static JSONObject obj = null;
    String TodayWord = "";
    Boolean vibrate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Appcelerator");

        saveMessage = (Button) findViewById(R.id.saveMessage);
        editMessage = (Button) findViewById(R.id.editMessage);
        setMessage = (EditText) findViewById(R.id.message);
        viewMessage = (TextView) findViewById(R.id.viewMessage);
        edit = (LinearLayout) findViewById(R.id.LayoutViewMessage);
        sharedPref = getApplicationContext().getSharedPreferences("UserMessage", Context.MODE_PRIVATE);

        updateUI(sharedPref);

        callAPI();

        TodayWord = sharedPref.getString("Today_Word", "").toLowerCase();

        if (viewMessage.getText().toString().toLowerCase().contains(TodayWord) && vibrate){
//            final Toast toast = Toast.makeText(getApplicationContext(), "Word found:- "+ TodayWord, Toast.LENGTH_SHORT);
//            toast.show();
            Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            // pass the number of millseconds fro which you want to vibrate the phone here we
            // have passed 2000 so phone will vibrate for 2 seconds.
            v.vibrate(2000);
        }
    }

    private void callAPI() {
        URL += "http://api.wordnik.com:80/v4/words.json/wordOfTheDay?date=";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String strDate = sdf.format(c.getTime());
        URL += strDate;
        URL+= "&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";

        new HttpAsyncTask().execute(URL);
        GET(URL);

    }


    private void updateUI(final SharedPreferences sharedPref) {

        message = sharedPref.getString("Message", "");
        Log.d("Message:- ", message+"");
        if (message.length() != 0){
            edit.setVisibility(View.VISIBLE);
            viewMessage.setText(message);
        }
        else{
            setMessage.setVisibility(View.VISIBLE);
            saveMessage.setVisibility(View.VISIBLE);
            vibrate = false;
        }
        saveMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                message = setMessage.getText().toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Message", message);
                edit.setVisibility(View.VISIBLE);
                setMessage.setVisibility(View.GONE);
                saveMessage.setVisibility(View.GONE);
                viewMessage.setText(message);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        editMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.setVisibility(View.GONE);
                setMessage.setVisibility(View.VISIBLE);
                saveMessage.setVisibility(View.VISIBLE);
                setMessage.setText(message);
            }
        });
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateUI(sharedPref);
    }

    public String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputStream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage()+"");
        }

        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException, JSONException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.

        @Override
        protected void onPostExecute(String result) {

            Log.d("Result", result);
            try {
                obj = new JSONObject(result);
                sharedPref = getApplicationContext().getSharedPreferences("UserMessage", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Today_Word", obj.getString("word"));
                editor.apply();
                obj.getString("word");
                Log.d("WOrd:- ", obj.getString("word"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
