package gekaradchenko.gmail.com.mymoney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private EditText myGrnEditText;
    private EditText myRubleEditText;
    private EditText myDollarEditText;
    private EditText courseGrnEditText;
    private EditText courseRubleEditText;
    private TextView allGrnMoneyTextView;
    private TextView allRubleMoneyTextView;
    private TextView allDollarMoneyTextView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Double RUB = 0.d;
    private Double USD = 0.d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewFindViewById();
        setText();

    }

    public void setAllValAndSave(View view) {

        valSave();
        setText();
    }

    public void getValCourse(View view) {
        new GetCurrency().execute();
    }


    private void valSave() {
        isNullException();

        sharedPreferences =
                getSharedPreferences(getString(R.string.sharedPreference), Context.MODE_PRIVATE);

        int grn = Integer.parseInt(myGrnEditText.getText().toString());
        int ruble = Integer.parseInt(myRubleEditText.getText().toString());
        int dollar = Integer.parseInt(myDollarEditText.getText().toString());
        Float courseGrn = Float.parseFloat(courseGrnEditText.getText().toString());
        Float courseRuble = Float.parseFloat(courseRubleEditText.getText().toString());

        float allGrn = (grn + (dollar * courseGrn) + ((ruble / courseRuble) * courseGrn));
        float allRuble = (ruble + (dollar * courseRuble) + ((grn / courseGrn) * courseRuble));
        float allDollar = ((grn / courseGrn) + (ruble / courseRuble) + dollar);

        editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.grn_shared), grn);
        editor.putInt(getString(R.string.ruble_shared), ruble);
        editor.putInt(getString(R.string.dollar_shared), dollar);
        editor.putFloat(getString(R.string.course_grn_shared), courseGrn);
        editor.putFloat(getString(R.string.course_ruble_shared), courseRuble);
        editor.putFloat(getString(R.string.all_grn_shared), allGrn);
        editor.putFloat(getString(R.string.all_ruble_shared), allRuble);
        editor.putFloat(getString(R.string.all_dollar_shared), allDollar);
        editor.apply();
    }

    private void setText() {

        sharedPreferences =
                getSharedPreferences(getString(R.string.sharedPreference), Context.MODE_PRIVATE);

        int grn = sharedPreferences.getInt(getString(R.string.grn_shared), 0);
        int ruble = sharedPreferences.getInt(getString(R.string.ruble_shared), 0);
        int dollar = sharedPreferences.getInt(getString(R.string.dollar_shared), 0);
        float courseGrn = sharedPreferences.getFloat(getString(R.string.course_grn_shared), 1f);
        float courseRuble =
                sharedPreferences.getFloat(getString(R.string.course_ruble_shared), 1f);
        float allGrn = sharedPreferences.getFloat(getString(R.string.all_grn_shared), 0f);
        float allRuble = sharedPreferences.getFloat(getString(R.string.all_ruble_shared), 0f);
        float allDollar = sharedPreferences.getFloat(getString(R.string.all_dollar_shared), 0f);

        String resultAllGrn = String.format("%.2f", allGrn);
        String resultAllRuble = String.format("%.2f", allRuble);
        String resultAllDollar = String.format("%.2f", allDollar);


        myGrnEditText.setText(grn + "");
        myRubleEditText.setText(ruble + "");
        myDollarEditText.setText(dollar + "");
        courseGrnEditText.setText(courseGrn + "");
        courseRubleEditText.setText(courseRuble + "");
        allGrnMoneyTextView.setText(resultAllGrn);
        allRubleMoneyTextView.setText(resultAllRuble);
        allDollarMoneyTextView.setText(resultAllDollar);

    }

    private void isNullException() {
        if (myGrnEditText.getText().toString().trim().equals("")) {
            myGrnEditText.setText("0");
        }
        if (myRubleEditText.getText().toString().trim().equals("")) {
            myRubleEditText.setText("0");
        }
        if (myDollarEditText.getText().toString().trim().equals("")) {
            myDollarEditText.setText("0");
        }
        if (courseGrnEditText.getText().toString().trim().equals("")) {
            courseGrnEditText.setText("1");
        }
        if (courseRubleEditText.getText().toString().trim().equals("")) {
            courseRubleEditText.setText("1");
        }
    }

    private void viewFindViewById() {
        myGrnEditText = findViewById(R.id.myGrnEditText);
        myRubleEditText = findViewById(R.id.myRubleEditText);
        myDollarEditText = findViewById(R.id.myDollarEditText);
        courseGrnEditText = findViewById(R.id.courseGrnEditText);
        courseRubleEditText = findViewById(R.id.courseRubleEditText);
        allGrnMoneyTextView = findViewById(R.id.allGrnMoneyTextView);
        allRubleMoneyTextView = findViewById(R.id.allRubleMoneyTextView);
        allDollarMoneyTextView = findViewById(R.id.allDollarMoneyTextView);
    }


    private class GetCurrency extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... voids) {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            String nowDate = format.format(date);
            String result = getContent("https://api.privatbank.ua/p24api/exchange_rates?json&date=" + nowDate);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray array = jsonObject.getJSONArray("exchangeRate");
                for (int i = 1; i < array.length(); i++) {
                    JSONObject some = (JSONObject) array.get(i);
                    if (some.getString("currency").equals("RUB")) {
                        RUB = some.getDouble("saleRateNB");
                    }
                    if (some.getString("currency").equals("USD")) {
                        USD = some.getDouble("saleRateNB");
                    }
                }

                double rubl = (1 / RUB) * USD;
                BigDecimal decimal1 = new BigDecimal(USD);
                BigDecimal decimal2 = new BigDecimal(rubl);
                //   System.out.println(decimal1.setScale(3, RoundingMode.HALF_UP)+">>>>>>>>>>>>>>>>>>");
                if (decimal1.setScale(3, RoundingMode.HALF_UP).toString().equals(null)) {
                    setText();
                } else {
                    courseGrnEditText.setText(decimal1.setScale(3, RoundingMode.HALF_UP).toString());
                }
                if (decimal2.setScale(3, RoundingMode.HALF_UP).toString().equals(null)) {
                    setText();
                } else {
                    courseRubleEditText.setText(decimal2.setScale(3, RoundingMode.HALF_UP).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String getContent(String path) {
            try {
                URL url = new URL(path);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(20000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                String result = "";
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result += line + "\n";
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}