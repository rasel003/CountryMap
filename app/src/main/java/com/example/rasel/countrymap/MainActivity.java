package com.example.rasel.countrymap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private WebView webViewResult;
    private Spinner spinnerCountry;
    ArrayList<String> countryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerCountry = findViewById(R.id.spinnerCountry);
        countryList = new ArrayList<String>(2);
        countryList.add("Select a Country");

        webViewResult = findViewById(R.id.webView);
        webViewResult.setWebViewClient(new WebViewClient());
        webViewResult.loadUrl("https://www.google.com/maps/place/Bangladesh/");

        WebSettings webSettings = webViewResult.getSettings();
        webSettings.setJavaScriptEnabled(true);

        new FetchCountryList().execute("http://customs.samsonict.com/api/core/countryList");

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String countryName = spinnerCountry.getSelectedItem().toString();
                if (i != 0) {
                    webViewResult.loadUrl("https://www.google.com/maps/place/" + countryName.trim());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webViewResult.canGoBack()) {
            webViewResult.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class FetchCountryList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            InputStream inputStream = null;
            String result = null;
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0]);

            try {

                HttpResponse response = client.execute(httpGet);
                inputStream = response.getEntity().getContent();

                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    Log.i("App", "Data received:" + result);

                } else
                    result = "Failed to fetch data";
                return result;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String dataFetched) {

            try {
                JSONArray jsonMainNode = new JSONArray(dataFetched);

                int jsonArrLength = jsonMainNode.length();

                for (int i = 0; i < jsonArrLength; i++) {
                    JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                    String postTitle1 = jsonChildNode.getString("country_name");
                    countryList.add(postTitle1);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, countryList);
                spinnerCountry.setAdapter(adapter);
                spinnerCountry.setSelection(0);
            } catch (Exception e) {
                Log.i("App", "Error parsing data" + e.getMessage());
            }
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;
        }
    }
}
