package mitlab.edu.ntust.f2f;

/**
 * Created by 勇霆 on 2016/3/19.
 */
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListViewAdapter extends ArrayAdapter<HashMap<Integer, String>> {

    private ArrayList<HashMap<Integer, String>> list;
    private Activity activity;

    class ViewHolder {
        TextView txtFirst;
        TextView txtSecond;
        TextView txtThird;
    }

    public ListViewAdapter(Activity activity,int textViewResourceId,ArrayList<HashMap<Integer, String>> list){
        super(activity, textViewResourceId, list);
        this.activity = activity;
        this.list = list;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public HashMap<Integer, String> getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();
        if(convertView == null){
            convertView=inflater.inflate(R.layout.column_row, null);
            holder = new ViewHolder();
            holder.txtFirst =(TextView) convertView.findViewById(R.id.date);
            holder.txtSecond  =(TextView) convertView.findViewById(R.id.time);
            holder.txtThird = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        HashMap<Integer, String> map = getItem(position);
        holder.txtFirst.setText(map.get(R.string.Date));
        holder.txtSecond.setText(map.get(R.string.Time));
        holder.txtThird.setText(map.get(R.string.Name));

        return convertView;
    }

}
