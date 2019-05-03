package com.example.a300cem_assignment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Resource;
import com.example.a300cem_assignment.Common.Common;
import com.example.a300cem_assignment.Interface.ItemClickListener;
import com.example.a300cem_assignment.Model.Event;
import com.example.a300cem_assignment.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Place auto-complete fragment
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
    AutocompleteSupportFragment places_fragment;
    Place address;

    //Current User
    TextView txtUserName;
    String userId = "";

    //Firebase
    FirebaseDatabase database;
    DatabaseReference events;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Event, MenuViewHolder> adapter;

    //View
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    //Add new menu layout
    EditText edtEventName;
    Button btnSelect, btnUpload, btnDate;
    ImageView img_event;
    TextView txtEventDate;

    Event newEvent;

    Uri saveUri;
    private final int PICK_IMAGE = 10;
    private final int PICK_CAMERA = 20;

    DrawerLayout drawer;

    DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Init Places API method
        initPlaces();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.home));
        setSupportActionBar(toolbar);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        events = database.getReference("Events");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateDialog();
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Set user name
        View headerView = navigationView.getHeaderView(0);
        txtUserName = headerView.findViewById(R.id.txtUserName);
        txtUserName.setText(Common.currentUser.getName());

        //Init View
        recycler_menu =  findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);

        //Get intent
        if (getIntent() != null) {
            userId = getIntent().getStringExtra("UserId");
        }
        if (!userId.isEmpty()) {
            loadMenu(userId);
        }
    }

    //Init Places API
    private void initPlaces() {
        Places.initialize(this, "AIzaSyAvM-YZRAPxd0oWuCgMq2vTp0B-6tgSR0k");
        placesClient = Places.createClient(this);
    }

    private void showCreateDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle(getString(R.string.addEvent));
        alertDialog.setMessage(getString(R.string.fillInfo));
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu, null);

        //Place auto-complete fragment
        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        places_fragment.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);
        ((EditText) places_fragment.getView().findViewById(R.id.places_autocomplete_search_input)).setHint(getString(R.string.eventAddressHint));
        ((EditText) places_fragment.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(30.0f);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                address = place;
                Toast.makeText(Home.this, "" + place.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Home.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        edtEventName = add_menu_layout.findViewById(R.id.edtEventName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        img_event = add_menu_layout.findViewById(R.id.img_event);
        txtEventDate = add_menu_layout.findViewById(R.id.txtEvent_date);
        btnDate = add_menu_layout.findViewById(R.id.btnDate);

        //Event for button
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Select event date
                selectDate();
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Choose image from gallery or camera
                showImageImportDialog();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_home_black_24dp);

        //Set button
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Create new category
                if (newEvent != null) {
                    events.push().setValue(newEvent);
                    Snackbar.make(drawer, getString(R.string.created)+newEvent.getName(), Snackbar.LENGTH_SHORT).show();
                }
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment)).commit();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment)).commit();
            }
        });
        alertDialog.show();

    }

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(
                Home.this, android.R.style.Theme_Holo_Dialog_MinWidth, mDateSetListener, year, month, day);
        dialog.show();
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = dayOfMonth + "/" + month + "/" + year;
                txtEventDate.setText(date);
            }
        };

    }

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog loadDialog = new ProgressDialog(this);
            loadDialog.setMessage(getString(R.string.uploading));
            loadDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            loadDialog.dismiss();
                            Toast.makeText(Home.this, getString(R.string.uploaded), Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    btnUpload.setText(getString(R.string.uploaded));
                                    newEvent = new Event(edtEventName.getText().toString(), uri.toString(), address.getAddress(), address.getLatLng().latitude, address.getLatLng().longitude, userId, txtEventDate.getText().toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadDialog.dismiss();
                            Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            loadDialog.setMessage(getString(R.string.uploading) + " " + progress + "%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            img_event.setImageURI(saveUri);
            btnSelect.setText(getString(R.string.selected));
        }
        if (requestCode == PICK_CAMERA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            img_event.setImageURI(saveUri);
            btnSelect.setText(getString(R.string.selected));
        }
    }

    private void pickGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select)), PICK_IMAGE);
    }

    private void pickCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, saveUri);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select)), PICK_CAMERA);
    }

    private void showImageImportDialog() {
        String[] items = {getString(R.string.camera), getString(R.string.gallery)};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.select));
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pickCamera();
                }
                if (which == 1) {
                    pickGallery();
                }
            }

        });
        dialog.create().show();
    }

    private void loadMenu(String userId) {
        Query getByUserId = events.orderByChild("userId").equalTo(userId);
        FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>()
                .setQuery(getByUserId,Event.class).build();
        adapter = new FirebaseRecyclerAdapter<Event, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Event model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(Home.this).load(model.getImage()).into(viewHolder.imageView);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start Activity
                        Intent detail = new Intent(Home.this, Details.class);
                        detail.putExtra("EventId", adapter.getRef(position).getKey());
                        startActivity(detail);
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.menu_item,viewGroup,false);
                return new MenuViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadMenu(userId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_logout) {
            //Logout
            Intent main = new Intent(Home.this, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteEvent(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }


    private void showUpdateDialog(final String key, final Event item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle(getString(R.string.updateEvent));
        alertDialog.setMessage(getString(R.string.fillInfo));
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu, null);


        //Place auto-complete fragment
        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        places_fragment.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);
        ((EditText) places_fragment.getView().findViewById(R.id.places_autocomplete_search_input)).setHint(getString(R.string.eventAddressHint));
        ((EditText) places_fragment.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(30.0f);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                address = place;
                Toast.makeText(Home.this, "" + place.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Home.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        edtEventName = add_menu_layout.findViewById(R.id.edtEventName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        img_event = add_menu_layout.findViewById(R.id.img_event);
        txtEventDate = add_menu_layout.findViewById(R.id.txtEvent_date);
        btnDate = add_menu_layout.findViewById(R.id.btnDate);

        //Event for button
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Select date for event
                selectDate();
            }
        });

        //Default Values
        edtEventName.setText(item.getName());
        txtEventDate.setText(item.getDate());
        places_fragment.setText(item.getAddress());


        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Choose image from gallery or camera
                showImageImportDialog();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_home_black_24dp);

        //Set button
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Update Values
                item.setName(edtEventName.getText().toString());
                item.setAddress(address.getAddress());
                item.setLat(address.getLatLng().latitude);
                item.setLng(address.getLatLng().longitude);
                item.setDate(txtEventDate.getText().toString());
                events.child(key).setValue(item);

                Snackbar.make(drawer, item.getName(), Snackbar.LENGTH_SHORT).show();


                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment)).commit();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment)).commit();
            }
        });
        alertDialog.show();
    }

    private void changeImage(final Event item) {
        if (saveUri != null) {
            final ProgressDialog loadDialog = new ProgressDialog(this);
            loadDialog.setMessage(getString(R.string.uploading));
            loadDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            loadDialog.dismiss();
                            Toast.makeText(Home.this, getString(R.string.uploaded), Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    btnUpload.setText(getString(R.string.uploaded));
                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadDialog.dismiss();
                            Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            loadDialog.setMessage(getString(R.string.uploading) + " " + progress + "%");
                        }
                    });
        }
    }

    private void deleteEvent(String key) {
        events.child(key).removeValue();
    }
}
