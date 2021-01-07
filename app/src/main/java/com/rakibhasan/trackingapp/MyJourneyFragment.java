package com.rakibhasan.trackingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MyJourneyFragment extends Fragment {

    ListView listView;

    ArrayAdapter<String> stringArrayAdapter;
    String [] data = {"Patgram", "Lalmonirhat", "Ranngpur", "Dhaka"};
    public MyJourneyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_journey, container, false);
        listView = view.findViewById(R.id.my_journey_list_view);
        stringArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, data);
        listView.setAdapter(stringArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), "Position"+String.valueOf(position), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), JourneyRoutesActivity.class));
            }
        });
        return view;
    }
}