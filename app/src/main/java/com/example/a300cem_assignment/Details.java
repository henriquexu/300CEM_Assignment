package com.example.a300cem_assignment;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a300cem_assignment.Model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Details extends AppCompatActivity {
    TextView food_name, food_price, food_description;
    ImageView food_image;
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
        food_description = (TextView) findViewById(R.id.food_description);
        food_name = (TextView) findViewById(R.id.food_name);
        food_price = (TextView) findViewById(R.id.food_price);
        food_image = (ImageView) findViewById(R.id.img_category);
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
                Category category = dataSnapshot.getValue(Category.class);

                //Set Image
                Picasso.with(getBaseContext()).load(category.getImage()).into(food_image);

                collapsingToolbarLayout.setTitle(category.getName());
                food_price.setText(category.getName());
                food_name.setText(category.getName());
                food_description.setText(category.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
