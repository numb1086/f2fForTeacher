package mitlab.edu.ntust.f2f;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by 勇霆 on 2016/3/31.
 */
public class SelectActivity extends AppCompatActivity {
    private Button btnHome,btnRefresh;
    private ListView listView;
    private ListViewAdapter2 adapter;
    private ArrayList<HashMap<Integer, String>> list;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_list);
        setBackground();
        btnRefresh = (Button) findViewById(R.id.btnRefresh2);
        btnHome = (Button) findViewById(R.id.btnHome);
        listView = (ListView)findViewById(R.id.selectResult);

        list = new ArrayList<>();
        adapter = new ListViewAdapter2(SelectActivity.this,android.R.layout.simple_list_item_1,list);
        getData();//Get database data

        btnRefresh.setOnClickListener(btnListener);
        btnHome.setOnClickListener(btnListener);
        listView.setOnItemClickListener(listViewListener);

    }
    private Button.OnClickListener btnListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnHome:
                    finish();
                    break;
                case R.id.btnRefresh2:
                    //Get data
                    list.clear();
                    adapter.clear();
                    getData();
                    break;
                default:
            }

        }
    };
    ListView.OnItemClickListener listViewListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            final EditText editName = new EditText(SelectActivity.this);
            editName.setGravity(Gravity.CENTER);
            new AlertDialog.Builder(SelectActivity.this).setTitle("Select date/time").setMessage("Input your name:")
                    .setIcon(R.drawable.favicon).setView(editName)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String date,time,name;
                            date = "date=" + list.get(position).get(R.string.Date).toString();
                            time = "&time=" + list.get(position).get(R.string.Time).toString();
                            name = "&name=" + editName.getText().toString();
                            final String postData = date + time + name;
                            if(editName.length()!=0) {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        postText(postData);
                                        //Get data
                                        list.clear();
                                        getData();
                                    }
                                });
                                thread.start();
                            }else{
                                Toast.makeText(SelectActivity.this,"Failed: Input name is empty.",Toast.LENGTH_LONG).show();
                            }
                        }
                    }).setNegativeButton("Cancel", null).show();
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
        lin.setBackgroundResource(bg[(int) (Math.random() * 8)]);
    }
    public void getData(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Loading data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress = new ProgressDialog(SelectActivity.this);
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
    private void post(){
        try {
            URL url = new URL("http://140.118.122.246/f2f/app/getAvailable.php");
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
            for (int i = 0; i < message.length; i++){
                HashMap<Integer, String> temp = new HashMap<>();
                detail = message[i].split(",");
                temp.put(R.string.Date, detail[0]);
                temp.put(R.string.Time, detail[1]);
                list.add(temp);
            }
            progress.dismiss();
            if(list.size()==0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(SelectActivity.this)
                                .setTitle("Message").setIcon(R.drawable.favicon)
                                .setMessage("There is no any date/time can be selected.")
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
    // This will post our text data
    private void postText(String postData){
        try {
            URL url = new URL("http://140.118.122.246/f2f/app/selectData.php");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setConnectTimeout(30000);
            //Send request
            OutputStream output = http.getOutputStream();
            output.write(postData.getBytes());
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
                        if (str.equals("1")) { //Add successfully
                            Toast.makeText(SelectActivity.this, "Succeed!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SelectActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            http.disconnect();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
