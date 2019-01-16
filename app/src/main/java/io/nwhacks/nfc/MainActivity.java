package io.nwhacks.nfc;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FirebaseAuth mAuth;
    private NFCManager nfcMgr;
    private static final int RC_SIGN_IN = 123;
    private ViewPager mViewPager;
    private BottomNavigationView navigation;
    private List<NFCFragment> fragmentsList;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            navigation.getMenu().findItem(item.getItemId());
            Menu menu = navigation.getMenu();
            for (int i = 0; i<menu.size(); i++) {
                if (menu.getItem(i).getItemId() == item.getItemId()) {
                    mViewPager.setCurrentItem(i);
                }
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mViewPager = findViewById(R.id.pager);

        fragmentsList = new ArrayList<>();
        fragmentsList.add(new ReadFragment());
        fragmentsList.add(new WriteFragment());
        fragmentsList.add(new EventsFragment());


        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentsList));
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                navigation.getMenu().getItem(position).setChecked(true);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        nfcMgr = new NFCManager(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            firebaseLogin();
        } else {
            loggedIn(currentUser);
        }
    }

    @Override
    public void onPause() {
        NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
        nfcAdpt.disableForegroundDispatch(this);
        super.onPause();
    }

    private NFCFragment activeFragment() {
        return fragmentsList.get(mViewPager.getCurrentItem());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        System.out.println(intent.getAction());
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            activeFragment().tagDiscovered(nfcMgr, intent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        if (!nfcMgr.verifyNFC()) {
            toast(this, "No NFC Found!");
        }
        Intent nfcIntent = new Intent(this, getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] intentFiltersArray = new IntentFilter[] {tagDetected, techDetected};
        String[][] techList = nfcMgr.getTechList();
        NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
        nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);

    }

    private void loggedIn(FirebaseUser user) {
        if (!user.isEmailVerified()) {
            user.sendEmailVerification();
            toast(this, "You need to verify your email!");
            return;
        }
        for (NFCFragment f : fragmentsList) {
            f.loggedIn(user);
        }
    }

    private void firebaseLogin() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
                );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);


    }

    @Override
    // called when sign-in flow is complete
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                loggedIn(user);
            } else {
                // Sign in failed, check response for error code
                toast(this, "Signin failed!");
            }
        }
    }

    public static void toast (Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    public static void toast (Context ctx, String msg, Integer duration) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        if (duration > 0){
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(duration);
        }
    }
}
