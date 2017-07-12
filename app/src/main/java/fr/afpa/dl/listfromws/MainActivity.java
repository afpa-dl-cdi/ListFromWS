package fr.afpa.dl.listfromws;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Activité de lancement affichant une liste d'éléments
 */
public class MainActivity extends AppCompatActivity
{
    /**
     * URL to connect to web services
     */
    private String webServicesUrl = "http://your.url.com";

    /**
     * specific uri to get an array
     */
    private String uri = "/user";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String httpResponse =  connect();                                                            // get response and raw data from web services (the call starts here)

        int status = 99;                                                                             // 99 is not a true number send by the server

        JSONObject data = null;

        ArrayList<String> listdata = new ArrayList<String>();                                        // initialize an arraylist where the data will be put

        try {
            data = stringToJson(httpResponse);                                                       // get the json from the httpresponse
            Log.i("HTTP", "status : " + data.getString("status"));
            status = Integer.valueOf(data.getString("status"));                                      // get the int of the status

            // if status ok get the user array
            if (status == 0)                                                                         // from the response if we get a status 0 (internal convention) , the json should
            {                                                                                        // have our array
                JSONArray jArray = (JSONArray)data.getJSONArray("users");
                if (jArray != null) {
                    for (int i=0;i<jArray.length();i++)
                    {
                        // get an object
                        JSONObject row =  new JSONObject(jArray.getString(i).toString());             // get the object in the parent array
                        Log.e("json", jArray.getString(i) );
                        Log.e("json", row.getString("nameUser") );
                        listdata.add(row.getString("nameUser").toString());                          // move jsonobject in our java list
                    }
                }
            }

        } catch (Exception e)
        {
            //TODO
        }

        // récupération du list view implémenter dans la structure du xml
        ListView listView = (ListView) findViewById(R.id.myListView);

        // création d'un tableau d'éléments pour l'exemple
        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile" };


        // l'élément ListView va être chargée par l'adapter
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listdata );

        // l'adapter est chargé dans l'élément du layout
        listView.setAdapter(arrayAdapter);

        // Au click sur un élément, nous allons basculer sur la vue détail
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
               String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                startActivity(intent);
                            }
                        });
            }
        });
    }

    /**
     * Connect to web services et return only the string received
     * @return String response
     */
    public String connect ()
    {
        // New object type AsyncTask to capture the data form onPostExecute
        AsyncTask loginReturn = new GetContacts().execute();

        // To be sure to retrieve any type of data, new variable type Object
        Object resultTask = null;
        String task = null;

        // I get the data and put it into my Object.
        // Then parse my object into a string.
        try
        {
            resultTask = loginReturn.get();
            task = new String(resultTask.toString());
            Log.e("HTTP", "result :  " + task);

        }
        catch(ExecutionException ee){
            Log.e("HTTP", Log.getStackTraceString(ee));

        }
        catch(InterruptedException ie){
            Log.e("HTTP", Log.getStackTraceString(ie));
        }

        return task;
    }

    /**
     * convert a String (based on JSON parsed from server side) in JSONobject
     * @param data
     * @return the true json
     */
    private JSONObject stringToJson (String data)
    {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    /**
     * Async task to connect to web services
     */
    protected class GetContacts extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Please wait while we are checking your login", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... arg0)
        {
            Log.i("info", "***************do in background*********" );
            StringBuilder builder = new StringBuilder();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("172.19.254.1", 8080));   // only if you need to pass through a proxy

            try {
                URL object = new URL(webServicesUrl+uri);

                HttpURLConnection con = (HttpURLConnection) object.openConnection(proxy);
                con.setDoInput(true);
                con.setRequestMethod("GET");
                int HttpResult = con.getResponseCode();

                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;

                    while ((line = br.readLine()) != null)
                    {
                        builder.append(line + "\n");
                    }

                    br.close();

                    Log.i("info", "*******************SB STRING*****" + builder.toString());

                } else {
                    Log.i("info", "*************CON RESPONSE***********" + con.getResponseMessage());
                }
            } catch (MalformedURLException mue) {
                mue.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            //catch (JSONException jse) {
              //  jse.printStackTrace();
            //}
            return builder.toString();
        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            Log.i("info", "***************ONPOSTEXECUTE*********" + result);

        }
    }
}


