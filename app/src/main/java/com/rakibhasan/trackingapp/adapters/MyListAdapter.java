package com.rakibhasan.trackingapp.adapters;

import android.app.Activity;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.rakibhasan.trackingapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final List<String> msg;
    private final List<Boolean> isAdmin;

    public MyListAdapter(Activity context, List<String> msg, List<Boolean> isAdmin) {
        super(context, R.layout.mylist, msg);

        this.context = context;
        this.msg = msg;
        this.isAdmin = isAdmin;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.mylist, null, false);

        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        CircleImageView imageView = (CircleImageView) rowView.findViewById(R.id.icon);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.subtitle);
        CardView cardView = (CardView) rowView.findViewById(R.id.my_list_card_view);

        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();



        if (isAdmin.get(position)) {
            titleText.setText("Admin");
            cardView.setCardBackgroundColor(Color.parseColor("#676767"));
            Glide.with(context).load("https://www.clipartkey.com/mpngs/m/237-2374286_administrator-network-icons-system-avatar-computer-admin-icon.png").into(((CircleImageView)imageView));
            subtitleText.setText(msg.get(position).trim());

            cardViewMarginParams.setMargins(0, 0, 85, 30);
            cardView.requestLayout();

        } else {
            cardView.setCardBackgroundColor(Color.parseColor("#aaaaaa"));
            cardView.setPadding(8,0,0,0);
            titleText.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            Glide.with(context).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(((CircleImageView)imageView));
            subtitleText.setText(msg.get(position).trim());

            cardViewMarginParams.setMargins(85, 0, 0, 30);
            cardView.requestLayout();
        }


        return rowView;

    }

    ;
}