package com.example.a300cem_assignment;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a300cem_assignment.Model.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Details extends AppCompatActivity {
    TextView event_name, food_price, event_description;
    ImageView event_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;

    String categoryId = "";

    FirebaseDatabase database;
    DatabaseReference categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Firebase init
        database = FirebaseDatabase.getInstance();
        categories = database.getReference("Categories");

        //Init view
        event_description = (TextView) findViewById(R.id.event_description);
        event_name = (TextView) findViewById(R.id.event_name);
        food_price = (TextView) findViewById(R.id.food_price);
        event_image = (ImageView) findViewById(R.id.event_image);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        
        //Get intent
        if (getIntent()!=null){
            categoryId = getIntent().getStringExtra("CategoryId");
        }
        if (!categoryId.isEmpty()){
            getDetailFood(categoryId);
        }
    }

    private void getDetailFood(final String categoryId) {
        categories.child(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                Event event = dataSnapshot.getValue(Event.class);

                //Set Image
                Picasso.with(getBaseContext()).load(event.getImage()).into(event_image);

                collapsingToolbarLayout.setTitle(event.getName());
                food_price.setText(event.getLat_Lng());
                event_name.setText(event.getName());
                event_description.setText(event.getAddress());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
