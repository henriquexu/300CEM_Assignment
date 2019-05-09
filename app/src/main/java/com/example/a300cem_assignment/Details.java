package com.example.a300cem_assignment;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a300cem_assignment.Model.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Details extends AppCompatActivity {
    TextView event_date, event_description;
    ImageView event_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    SupportMapFragment map;

    String eventId = "";

    FirebaseDatabase database;
    DatabaseReference events;

    Double lat = 0.0;
    Double lng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Map Fragment
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //Firebase init
        database = FirebaseDatabase.getInstance();
        events = database.getReference("Events");

        //Init view
        event_description = findViewById(R.id.event_description);
        event_date = findViewById(R.id.event_date);
        event_image = findViewById(R.id.event_image);
        collapsingToolbarLayout = findViewById(R.id.collapsing);

        //Get intent
        if (getIntent() != null) {
            eventId = getIntent().getStringExtra("EventId");
        }
        if (!eventId.isEmpty()) {
            getEventDetail(eventId);

        }
    }

    //Get Event Detail
    private void getEventDetail(final String eventId) {
        events.child(eventId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                Event event = dataSnapshot.getValue(Event.class);

                //Set Image
                Picasso.with(getBaseContext()).load(event.getImage()).into(event_image);
                //Set Event Details
                collapsingToolbarLayout.setTitle(event.getName());
                event_date.setText(event.getDate());
                event_description.setText(event.getAddress());
                //Set Google Map
                lat = event.getLat();
                lng = event.getLng();
                map.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng location = new LatLng(lat, lng);
                        googleMap.addMarker(new MarkerOptions().position(location).title(getString(R.string.location)));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
