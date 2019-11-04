package mitlab.edu.ntust.f2f;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Button btnRefresh,btnAdd;
    private ListView listView;
    private ListViewAdapter adapter;
    private ArrayList<HashMap<Integer, String>> list;
    private ProgressDialog progress;
    private String removeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBackground();
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		btnAdd = (Button) findViewById(R.id.start);
		listView = (ListView)findViewById(R.id.listResult);
		btnRefresh.setOnClickListener(btnListener);
        btnAdd.setOnClickListener(btnListener);
        listView.setOnItemClickListener(listViewListener);
        list = new ArrayList<>();
        adapter = new ListViewAdapter(this, android.R.layout.simple_list_item_1, list);

        if(!isNetworkConnected(MainActivity.this))
            Toast.makeText(MainActivity.this,"Please connect to the network!",Toast.LENGTH_LONG).show();
        else
            getData();//Get database data
    }
    private Button.OnClickListener btnListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnRefresh:
                    if(!isNetworkConnected(MainActivity.this)){
                        Toast.makeText(MainActivity.this,"Please connect to the network!",Toast.LENGTH_LONG).show();
                    }else{
                        list.clear();
                        adapter.clear();
                        getData();
                    }
                    break;
                case R.id.start:
                    if(!isNetworkConnected(MainActivity.this)){
                        Toast.makeText(MainActivity.this,"Please connect to the network!",Toast.LENGTH_LONG).show();
                    }else{
                       Intent intent = new Intent(MainActivity.this, SelectActivity.class);
                       startActivity(intent);
                    }
                    break;
                default:
            }

        }
    };
    private ListView.OnItemClickListener listViewListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Remove this date/time").setMessage("Are you sure?")
                    .setIcon(R.drawable.favicon).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            removeData = "dataAll[]=" + list.get(position).get(R.string.Date).toString() + ","
                                    + list.get(position).get(R.string.Time).toString() + ",0";
                            //Remove data
                            removeText(removeData);
                            //Get data
                            list.clear();
                            getData();
                        }
                    });
                    thread.start();
                }
            }).setNegativeButton("No",null).show();
        }
    };
    private void setBackground(){
        LinearLayout lin = (LinearLayout) findViewById(R.id.linLayout);
        int[] bg = new int[8];
        bg[0] = R.drawable.bg1;
        bg[1] = R.drawable.bg2;
        bg[2] = R.drawable.bg3;
        bg[3] = R.drawable.bg4;
        bg[4] = R.drawable.bg5;
        bg[5] = R.drawable.bg6;
        bg[6] = R.drawable.bg7;
        bg[7] = R.drawable.bg8;
        lin.setBackgroundResource(bg[(int)(Math.random()*8)]);
    }
    public void getData(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Loading data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress = new ProgressDialog(MainActivity.this);
                        progress.setTitle("Loading data");
                        progress.setMessage("Please wait a moment...");
                        progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        progress.show();
                    }
                });
                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                //Get data
                post();
            }
        });
        thread.start();
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private void post(){
        try {
            URL url = new URL("http://xxx.xxx.xxx.xxx/f2f/app/list.php");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setConnectTimeout(30000);
            //Get Response
            InputStream input = http.getInputStream();
            byte[] data = new byte[2048];
            int size = input.read(data);
            String str = new String(data, 0, size);
            input.close();
            http.disconnect();
            String[] message = str.split(" ");
            String[] detail;
            for (int i = 0; i < message.length-1; i++){
                HashMap<Integer, String> temp = new HashMap<>();
                detail = message[i].split(",");
                temp.put(R.string.Date, detail[0]);
                temp.put(R.string.Time, detail[1]);
                temp.put(R.string.Name, detail[2]);
                list.add(temp);
            }
            progress.dismiss();
            if(list.size()==0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Message").setIcon(R.drawable.favicon)
                                .setMessage("Nobody selected any date and time.")
                                .setPositiveButton("Confirm", null).show();
                        listView.setAdapter(adapter);
                    }
                });
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void removeText(String removeData){
        try {
            URL url = new URL("http://140.118.122.246/f2f/app/deleteData.php");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");

            //Send request
            OutputStream output = http.getOutputStream();
            output.write(removeData.getBytes());
            output.flush();
            output.close();

            //Get Response
            int responseCode = http.getResponseCode();
            if(responseCode == 200) {
                InputStream input = http.getInputStream();
                byte[] data = new byte[2048];
                int size = input.read(data);
                final String str = new String(data, 0, size);
                input.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!str.equals("0")) { //Add successfully
                            Toast.makeText(MainActivity.this, "Succeed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            http.disconnect();
            progress.dismiss();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
