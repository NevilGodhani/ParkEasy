package com.example.parkeasy;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.razorpay.PaymentResultListener;

public class MainActivity2 extends AppCompatActivity{

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        View decoreview= getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left=insets.getSystemWindowInsetLeft();
                int top=insets.getSystemWindowInsetTop();
                int right=insets.getSystemWindowInsetRight();
                int bottom=insets.getSystemWindowInsetBottom();
                v.setPadding(left,top,right,bottom);
                return insets.consumeSystemWindowInsets();
            }
        });


        toolbar=findViewById(R.id.toolbar_drawer);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        String drawer=getIntent().getStringExtra("drawer");
        if(drawer!=null){
            loadFragment(new DrawerFragment());
        }
        String like=getIntent().getStringExtra("like");
        if(like!=null){
            loadFragment(new LikeFragment());
        }
        String name=getIntent().getStringExtra("name");
        if(name != null){
            ParkingFragment fragment = new ParkingFragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", name);
            fragment.setArguments(bundle);
            loadFragment(fragment);
        }
        String register=getIntent().getStringExtra("register");
        if(register!=null){
            loadFragment(new RegisterFragment());
        }
        String add=getIntent().getStringExtra("add");
        if(add!=null){
            loadFragment(new AddParkingFragment());
        }
        String edit=getIntent().getStringExtra("edite");
        if(edit!=null){
            loadFragment(new ProfileFragment());
        }
        String bookingg=getIntent().getStringExtra("bookingg");
        if(bookingg !=null){
            loadFragment(new BookingsFragment());
        }
        String forgot=getIntent().getStringExtra("forgot");
        if(forgot !=null){
            loadFragment(new ForgotFragment());
        }
        String helpp=getIntent().getStringExtra("helpp");
        if(helpp !=null){
            loadFragment(new SupportFragment());
        }
        String token=getIntent().getStringExtra("token");
        if(token!=null){
            String parkName=getIntent().getStringExtra("name");
            String ddd=getIntent().getStringExtra("date");
            String sss=getIntent().getStringExtra("slot");
            TokenFragment fragment = new TokenFragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", parkName);
            bundle.putString("ddd", ddd);
            bundle.putString("sss", sss);
            fragment.setArguments(bundle);
            loadFragment(fragment);

        }
        String book=getIntent().getStringExtra("book");
        if(book!=null){
            String pname=getIntent().getStringExtra("name");
            String price2=getIntent().getStringExtra("price2");
            String price4=getIntent().getStringExtra("price4");
            BookFragment fragment = new BookFragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", pname);
            bundle.putString("price2", price2);
            bundle.putString("price4", price4);
            fragment.setArguments(bundle);
            loadFragment(fragment);
        }
        String ticket=getIntent().getStringExtra("ticket");
        if(ticket !=null){
            String ptname=getIntent().getStringExtra("pname");
            String date=getIntent().getStringExtra("date");
            String slot=getIntent().getStringExtra("slot");
            String ust=getIntent().getStringExtra("ust");
            String uet=getIntent().getStringExtra("uet");
            TicketFragment fragment = new TicketFragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", ptname);
            bundle.putString("date", date);
            bundle.putString("slot", slot);
            bundle.putString("ust", ust);
            bundle.putString("uet", uet);
            fragment.setArguments(bundle);
            loadFragment(fragment);
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.ActionFramlayout, fragment);
        fragmentTransaction.commit();
    }
}