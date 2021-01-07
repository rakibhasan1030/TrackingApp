package com.rakibhasan.trackingapp.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rakibhasan.trackingapp.R;
import java.util.List;

public class ListViewCustomAdapter extends BaseAdapter {

    List<QueryDocumentSnapshot> mgsList;
    Context context;


    public ListViewCustomAdapter(@NonNull Context context, List<QueryDocumentSnapshot> mgsList) {
       this.context = context;
       this.mgsList = mgsList;
    }

    @Override
    public int getCount() {
        return mgsList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            TextView mgsTV, timeTV;
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.message_list_item_user, parent, false);
            mgsTV = convertView.findViewById(R.id.list_item_message_user);
            timeTV = convertView.findViewById(R.id.list_item_time_user);
            mgsTV.setText(mgsList.get(position).getString("msg"));
         //   timeTV.setText(mgsList.get(position).getString("time"));
        }
        return convertView;
    }
}
