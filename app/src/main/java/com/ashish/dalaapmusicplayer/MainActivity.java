package com.ashish.dalaapmusicplayer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.Person;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ashish.dalaapmusicplayer.UpdateCheck.VersionChecker;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission_group.STORAGE;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

//5132234413676201121 play store adccount id
//Created on date 26/10/2019 by Ashish..
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getName();

    private boolean doubleBackToExitPressed = true;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.ashish.alaapmusicplayer.PlayNewAudio";
    public MediaPlayerService player;
    boolean serviceBound = false;
    public ArrayList<Audio> audioList;
    ImageView dock_image,dock_play;

    boolean isApprestarted=false;
    boolean isPaused=false;

    Button pause,next,previous,loop,dock_goTo_play;
    SlidingUpPanelLayout slidingUpPanelLayout;
    ImageView imageView;
    TextView songNameText,MaxSongLength,elapsedTime,dock_song_name;
    private int audioIndex = -1;
    int lastplayed=-1;
    public Audio activeAudio;
    Thread t2;
    int totalDuration,currentPosition;
    boolean isPlaying;
    SeekBar sb;
    Bitmap albumArt;
    private FirebaseAnalytics mFirebaseAnalytics;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    //g sign in
    SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;
    String name, email;
    String idToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    String app_version="";
    Button logOut;
    TextView UsernameTv,emailIdTv,version_text;
    ImageView profile_image;
    boolean isUserSignedIn=false;

    // playlist
    String fileName = "myPlaylist";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //For night mode theme
        if(isMyServiceRunning(MediaPlayerService.class))
        {
            Toasty.warning(this,"Service already running in background");
        }else
        {
            setContentView(R.layout.new_ui);
        }




        app_version = BuildConfig.VERSION_NAME;
        Log.d(TAG,"appV"+app_version);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Log.d("onOUT", "onBackPressed"+doubleBackToExitPressed);
        preferences =getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        editor = preferences.edit();


        nv = (NavigationView) findViewById(R.id.nav_view);
        signInButton=(SignInButton)nv.getHeaderView(0).findViewById(R.id.sign_in_btn);
        UsernameTv=(TextView)nv.getHeaderView(0).findViewById(R.id.name);
        emailIdTv=(TextView)nv.getHeaderView(0).findViewById(R.id.email);
        profile_image=(ImageView)nv.getHeaderView(0).findViewById(R.id.img_profile);
        logOut=(Button)nv.getHeaderView(0).findViewById(R.id.logOut);

        dock_play=findViewById(R.id.XbtnPlay);
        dock_goTo_play=findViewById(R.id.Xbutton1);
        dock_image=findViewById(R.id.dock_albumArt);
        dock_song_name=findViewById(R.id.XsongtextView);

        dock_image.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 20F);
            }
        });
        dock_image.setClipToOutline(true);

        isPlaying=false;
//        songNameText = (TextView) findViewById(R.id.txtSongLabel);
        sb= findViewById(R.id.seekBar);

//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        Bundle params = new Bundle();
//        params.putString("open_time", String.valueOf(System.currentTimeMillis()));
//        mFirebaseAnalytics.logEvent("app_open_time", params);


        //fcm
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            //To do//
//                            return;
//                        }
//
//                        // Get the Instance ID token//
//                        String token = task.getResult().getToken();
//                        String msg = getString(R.string.fcm_token, token);
//                        Log.d(TAG, msg);
//
//                    }
//                });

        loadAudioList();

        //check if user has logged in before on not



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, dl, toolbar, R.string.Open, R.string.Close);
        dl.setDrawerListener(toggle);
        toggle.syncState();
        dl.setScrimColor(Color.TRANSPARENT);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                switch(id)
                {
                    case R.id.nav_app_version:
                        Toasty.info(MainActivity.this, "v"+app_version, Toast.LENGTH_SHORT, true).show();
                        break;

                    case R.id.rate:
                        String packageName="com.ashish.alaapmusicplayer";

                        try{
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+packageName)));
                        }
                        catch (ActivityNotFoundException e){
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+packageName)));
                        }

                        break;
                    case R.id.update:
                        versionCheck();
                        break;
                    case R.id.invite:
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.ashish.alaapmusicplayer");
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                        break;

                    default:
                        return true;
                }

                dl.closeDrawer(GravityCompat.START);
                return true;

            }
        });



        dock_goTo_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{

                    if (player.mediaPlayer.isPlaying())
                    {
                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);


                    }
                }catch (NullPointerException e){}


            }
        });

        sb.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        sb.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);


        sb.setOnSeekBarChangeListener(new
                                              SeekBar.OnSeekBarChangeListener() {
                                                  @Override
                                                  public void onProgressChanged(SeekBar seekBar, int i,
                                                                                boolean b) {
                                                        currentPosition=player.mediaPlayer.getCurrentPosition();
                                                      String time = String.format("%02d:%02d",
                                                              TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                                              TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                                                                      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));
                                                     // elapsedTime.setText(time);
                                                  }
                                                  @Override
                                                  public void onStartTrackingTouch(SeekBar seekBar) {


                                                  }
                                                  @Override
                                                  public void onStopTrackingTouch(SeekBar seekBar) {
                                                      player.mediaPlayer.seekTo(seekBar.getProgress());


                                                  }
                                              });











        isApprestarted=preferences.getBoolean("appDestroyed",false);
        Log.d(TAG,"onCreate_appStartStatus"+isApprestarted);
        if (isApprestarted)
        {
            try {
                //Load data from SharedPreferences
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioIndex = storage.loadAudioIndex();
                Log.d(TAG,"onRes_audioIndex"+audioIndex);
                if (audioIndex != -1) {
                    dock_play.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);

                    //index is in a valid range

                    activeAudio = audioList.get(audioIndex);
                    String a = activeAudio.getTitle();
                    dock_song_name.setText(a);
                    dock_song_name.setSelected(true);
                } else {
                }
            } catch (NullPointerException e) {
            }

        }

        dock_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isApprestarted)
                {

                    playAudio(audioIndex);
                  //  Toast.makeText(MainActivity.this,"restarted_playBtn",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "lastPlayed" + audioIndex);
                    // dock_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);
                    isApprestarted=false;
                    Log.d(TAG,"c"+isApprestarted);
                }else
                {

                    playBtn();
                }


            }
        });


        //g sign in
        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))//you can also use R.string.default_web_client_id
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


        firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get signedIn user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //if user is signed in, we call a helper method to save the user details to Firebase
                if (user != null) {
                    // User is signed in
                    // you could place other firebase code
                    //logic to save the user details to Firebase
                    isUserSignedIn=true;

                    UsernameTv.setText("");
                    emailIdTv.setText("");
                    String UserName= preferences.getString("User_name",null);

                    String EmailID= preferences.getString("Email_id",null);

                    try{
                        String Photo=preferences.getString("Image_url",null);
                        Log.d(TAG,"name"+UserName+"ImageUrl"+Photo);
                        Glide.with(MainActivity.this).load(Photo).centerCrop().into(profile_image);
                    }catch (NullPointerException e){
                      //  Toast.makeText(getApplicationContext(),"image not found",Toast.LENGTH_LONG).show();
                        Toasty.error(MainActivity.this, "image not found", Toast.LENGTH_SHORT, true).show();

                    }
                    if (UserName!=null && EmailID!=null)
                    {
                        Log.d(TAG,"mail"+EmailID);

                        UsernameTv.setText(UserName);
                        emailIdTv.setText(EmailID);

                    }
                    logOut.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.GONE);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                }
            }
        };



        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable())
                {
                    Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(intent,RC_SIGN_IN);
                }else {
                    Toasty.error(MainActivity.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();

                }

            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvailable())
                {
                    FirebaseAuth.getInstance().signOut();
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                      //  Toast.makeText(getApplicationContext(), "Successfully Signed Out", Toast.LENGTH_LONG).show();
                                        Toasty.success(MainActivity.this, "Signed Out Successfully!", Toast.LENGTH_SHORT, true).show();

                                        dl.closeDrawer(GravityCompat.START);
                                        profile_image.setImageResource(R.drawable.ic_person_black_24dp);
                                        UsernameTv.setText("");
                                        emailIdTv.setText("");
                                        logOut.setVisibility(View.GONE);
                                        signInButton.setVisibility(View.VISIBLE);
                                        editor.remove("User_name");
                                        editor.remove("Email_id");
                                        editor.remove("Image_url");
                                        editor.clear();
                                        editor.commit();
                                        isUserSignedIn=false;

                                    } else {
                                        // Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_LONG).show();
                                        Toasty.error(MainActivity.this, "Session not close", Toast.LENGTH_SHORT, true).show();

                                    }
                                }
                            });

                }else {

                    Toasty.error(MainActivity.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();

                }

            }

        });


    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            idToken = account.getIdToken();
            name = account.getDisplayName();
            email = account.getEmail();
            Uri personPhoto = account.getPhotoUrl();

            UsernameTv.setText(name);
            emailIdTv.setText(email);


            try{
                Glide.with(this).load(account.getPhotoUrl()).centerCrop().into(profile_image);
            }catch (NullPointerException e){
                Toasty.error(MainActivity.this, "image not found", Toast.LENGTH_SHORT, true).show();
            }


          //  profile_image.setImageURI(account.getPhotoUrl());
            signInButton.setVisibility(View.GONE);
            logOut.setVisibility(View.VISIBLE);
            Log.d(TAG, "ID: "+idToken+"name: "+name+"email: "+email+"imageUrl"+personPhoto);
            // you can store user data to SharedPreference
           // editor.putString("imagePreferance", encodeTobase64());
            editor.putString("User_name",name);
            editor.putString("Email_id",email);
            editor.putString("Image_url", String.valueOf(personPhoto));
            editor.apply();

            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuthWithGoogle(credential);
        }else{
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Login Unsuccessful. "+result);
           // Toast.makeText(this, "Sign In Unsuccessful", Toast.LENGTH_SHORT).show();
            Toasty.error(MainActivity.this, "Sign In Unsuccessful", Toast.LENGTH_SHORT, true).show();

            if(!isNetworkAvailable()){
               // Toast.makeText(this, "Please, connect to Internet", Toast.LENGTH_SHORT).show();
                Toasty.error(MainActivity.this, "Please, connect to Internet", Toast.LENGTH_SHORT, true).show();

            }
        }
    }
    private void firebaseAuthWithGoogle(AuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                           // Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Toasty.success(MainActivity.this, "Sign In successful!", Toast.LENGTH_SHORT, true).show();

                        } else {
                            Log.w(TAG, "signInWithCredential" + task.getException().getMessage());
                            task.getException().printStackTrace();
//                            Toast.makeText(MainActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                            Toasty.error(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT, true).show();

                        }

                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authStateListener != null){
           // FirebaseAuth.getInstance().signOut();
        }
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
       // slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
      //  this.doubleBackToExitPressed = false;


        version_text=(TextView) MenuItemCompat.getActionView(nv.getMenu().
                findItem(R.id.nav_app_version));
        version_text.setGravity(Gravity.CENTER_VERTICAL);
        version_text.setTypeface(null, Typeface.BOLD);
        version_text.setTextColor(getResources().getColor(R.color.colorAccent));
        version_text.setText("v"+app_version);



        boolean ifPaused=preferences.getBoolean("paused",false);
        if (ifPaused)
        {
//           int seek= preferences.getInt("pos",0);
//            player.mediaPlayer.seekTo(seek);

        }



        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            public void run() {

                int index = -1;
                try{
                    index=player.audioIndex;
                    Log.d(TAG,"audio_index"+index);

                }catch (NullPointerException e){}

                if(index>=0)
                {
                  //  Log.d(TAG,"IndexG");
                    totalDuration=player.mediaPlayer.getDuration();
                    sb.setMax(player.mediaPlayer.getDuration());
                    String time = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(totalDuration),
                            TimeUnit.MILLISECONDS.toSeconds(totalDuration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration)));
                    // MaxSongLength.setText(time);
                    sb.setProgress(player.mediaPlayer.getCurrentPosition());
                    ShowAlbum();
                    if (player.CheckStatus()){

                        dock_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);

                    }else {
                        dock_play.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);

                    }
                }



                handler.postDelayed(this, 1000);  //for interval...
            }
        };
        handler.postDelayed(runnable, 1000);



    }


    @Override
    protected void onRestart() {
        super.onRestart();

    }

    private void loadAudioList() {
        loadAudio();
        initRecyclerView();

    }

    public void playBtn()
    {

        try {
            if (player.CheckStatus()){
              //  Toast.makeText(MainActivity.this,"playing",Toast.LENGTH_SHORT).show();

                player.pauseMedia();
                dock_play.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);
                player.StopNotify();

            }else {
                player.resumeMedia();
               // Toast.makeText(MainActivity.this,"paused",Toast.LENGTH_SHORT).show();
                dock_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);
                player.StartNotify();
            }
        }catch (NullPointerException e)
        {

        }

    }
    public void nextBtn(){
        dock_play.setBackgroundResource(R.drawable.pause);
        player.skipToNext();
        player.StartNotify();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sb.setMin(0);
        }
        ShowAlbum();
    }
    public void preBtn()
    {
        dock_play.setBackgroundResource(R.drawable.pause);
        player.skipToPrevious();
        player.StartNotify();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sb.setMin(0);
        }
        ShowAlbum();

    }
    public void loopBtn()
    {
        if (player.CheckLooping())
        {
            player.DisableLoop();
            // loop.setBackgroundColor(Color.WHITE);
          //  loop.setBackgroundResource(R.drawable.ic_loop_black_24dp);


        }else
        {
            player.EnableLoop();
            //  loop.setBackgroundColor(Color.RED);

          //  loop.setBackgroundResource(R.drawable.ic_autorenew_black_24dp);

        }
    }






    private void initRecyclerView() {
        if (audioList != null && audioList.size() > 0) {
            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            RecyclerView_Adapter adapter = new RecyclerView_Adapter(audioList, getApplication());
            recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {


                    playAudio(index);
                    dock_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);







                }
            }));


        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.rating) {
            String packageName="com.ashish.alaapmusicplayer";

            try{
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+packageName)));
            }
            catch (ActivityNotFoundException e){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+packageName)));
            }
            return true;
        }

        if (id == R.id.update) {

            VersionChecker versionChecker = new VersionChecker();
            try {
                String latestVersion = versionChecker.execute().get();
                String versionName = BuildConfig.VERSION_NAME.replace("-DEBUG","");
                Log.d(TAG,"app_version:"+ + Float.valueOf(versionName));
                if (latestVersion != null && !latestVersion.isEmpty()) {
                    if (!latestVersion.matches(versionName)) {
                        showDialogToSendToPlayStore();
                    }else{
                      //  Toast.makeText(MainActivity.this,"Alredy on latest version",Toast.LENGTH_LONG).show();
                        Toasty.info(MainActivity.this, "Alredy on latest version!", Toast.LENGTH_SHORT, true).show();

                    }
                }
                Log.d("update", "Current version " + Float.valueOf(versionName) + ", Playstore version " + Float.valueOf(latestVersion));

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void versionCheck(){
        VersionChecker versionChecker = new VersionChecker();
        try {
            String latestVersion = versionChecker.execute().get();
            String versionName = BuildConfig.VERSION_NAME.replace("-DEBUG","");
            if (latestVersion != null && !latestVersion.isEmpty()) {
                if (!latestVersion.matches(versionName)) {
                    showDialogToSendToPlayStore();
                }else{
                  //  Toast.makeText(MainActivity.this,"Alredy on latest version",Toast.LENGTH_LONG).show();
                    Toasty.info(MainActivity.this, "Alredy on latest version!", Toast.LENGTH_SHORT, true).show();

                }
            }
            Log.d("update", "Current version " + Float.valueOf(versionName) + ", Playstore version " + Float.valueOf(latestVersion));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void showDialogToSendToPlayStore(){


        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setIcon(R.drawable.splash_cent);
        dialog.setTitle("New version available");
        dialog.setMessage("\n\nTo enjoy the new features update to latest version." );
        dialog.setPositiveButton("UPDATE NOW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Action for "update".
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +"com.ashish.alaapmusicplayer")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +"com.ashish.alaapmusicplayer")));
                }
            }
        })
                .setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Action for "Cancel".
                       // Toast.makeText(MainActivity.this,"Pease update to latest version",Toast.LENGTH_LONG).show();
                        Toasty.info(MainActivity.this, "Please update to latest version", Toast.LENGTH_SHORT, true).show();

                        dialog.dismiss();
                    }
                });

        final AlertDialog alert = dialog.create();
        alert.show();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("serviceStatus", serviceBound);

        if (serviceBound && player.CheckStatus()){

            savedInstanceState.putInt("Position", player.mediaPlayer.getCurrentPosition());
            savedInstanceState.putBoolean("isplaying", player.mediaPlayer.isPlaying());
            savedInstanceState.putString("songName",activeAudio.getTitle());

        }


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");



    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(playerIntent);
            }else {
                startService(playerIntent);
            }
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            if (player.mediaPlayer.isPlaying()){
                player.mediaPlayer.stop();
                player.mediaPlayer.reset();
                Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
                Log.e("player_status","playing"+player.mediaPlayer);
            }else {

                Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
            }



        }



    }


    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("main_onDestroy", "main_onDestroy Called");
        editor.putBoolean("appDestroyed",true);
        editor.apply();
        if (serviceBound)
        {

                unbindService(serviceConnection);
                // service is active
                player.StopNotify();
                player.stopSelf();

//            StorageUtil storage = new StorageUtil(getApplicationContext());
//            Log.d(TAG,"Main_onDESTROY"+storage.loadAudioIndex());
//            preferences =getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
//
//            editor.putBoolean("appDestroyed",true);
//            editor.putString("audio_index", String.valueOf(audioIndex));
//            editor.putString("last_song_title", activeAudio.getTitle());
//            editor.putInt("songPos",player.mediaPlayer.getCurrentPosition());
//            editor.apply();
//
//            Log.d(TAG,"desPref"+audioIndex+"title"+activeAudio.getTitle());
//            Log.d(TAG,"onDes_appStartStatus"+isApprestarted);
//




        }


    }



    @Override
    public void onBackPressed() {
        Log.d("onBackPressed", "onBackPressed Called");
        moveTaskToBack(true);

        if (dl.isDrawerOpen(GravityCompat.START)) {
            dl.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressed) {
                this.doubleBackToExitPressed = false;
              //  Snackbar.make(findViewById(android.R.id.content), "Press again to exit...", Snackbar.LENGTH_LONG).show();
               // Toast.makeText(this,"Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

               // moveTaskToBack(true);

            } else {
                Log.d("onBackOUT", "onBackPressed"+doubleBackToExitPressed);

                super.onBackPressed();


            }
        }



    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    @SuppressLint("ResourceType")
    public void ShowAlbum()
    {
       // Log.d(TAG,"showAlb");

        MediaMetadataRetriever meta = new MediaMetadataRetriever();
       // StorageUtil storage = new StorageUtil(getApplicationContext());
        audioIndex = player.audioIndex;
        activeAudio =player.activeAudio;
        if (audioIndex != -1 && audioIndex < audioList.size()) {
            //index is in a valid range
            activeAudio = audioList.get(audioIndex);
        }
     try {
        // Log.d(TAG,"Image");
        meta.setDataSource(activeAudio.getData());
         byte[] imgdata = meta.getEmbeddedPicture();
       albumArt = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length);
      // imageView.setImageBitmap(albumArt);
       dock_image.setImageBitmap(albumArt);
       dock_song_name.setText(activeAudio.getTitle());
       dock_song_name.setSelected(true);
     } catch (Exception e) {
        albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.cover_art);
        dock_image.setImageBitmap(albumArt);
     }



    }

    @Override
    protected void onPause() {
        super.onPause();


        editor.putBoolean("paused",true);
        editor.putInt("Pos",currentPosition);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void save(ArrayList<String> mySongs){
        // Save the size so that you can retrieve the whole list later
        editor.putInt("listSize", mySongs.size());

        for(int i = 0; i < mySongs.size(); i++){
            // Save each song with its index in the list as a key
            editor.putString(String.valueOf(i), mySongs.get(i));
        }

        editor.commit();
    }

    public ArrayList<String> load() {
        // Create new array to be returned
        ArrayList<String> savedSongs = new ArrayList<String>();

        // Get the number of saved songs
        int numOfSavedSongs = preferences.getInt("listSize", 0);

        // Get saved songs by their index
        for (int i = 0; i < numOfSavedSongs; i++) {
            savedSongs.add(preferences.getString(String.valueOf(i),""));
        }

        return savedSongs;
    }

}