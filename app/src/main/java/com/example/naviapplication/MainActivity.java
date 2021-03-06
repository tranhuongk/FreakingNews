package com.example.naviapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bumptech.glide.Glide;
import com.example.naviapplication.fragments.BusinessFragment;
import com.example.naviapplication.fragments.ErrorFragment;
import com.example.naviapplication.fragments.HomeFragment;
import com.example.naviapplication.fragments.SavedNewsFragment;
import com.example.naviapplication.fragments.SportFragment;
import com.example.naviapplication.fragments.TechFragment;
import com.example.naviapplication.object.Article;
import com.example.naviapplication.object.User;
import com.example.naviapplication.service.XMLDOMParser;
import com.example.naviapplication.service.ip;
import com.example.naviapplication.util.CustomAdapter;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<Article> articles;
    SwipeMenuListView listView;
    CustomAdapter customAdapter;
    private static String cate = "https://www.24h.com.vn/upload/rss/tintuctrongngay.rss";
    ImageView c_avatar;
    TextView c_name, c_email;
    private int idUser;
    ip ip = new ip();
    Menu menu;
    MenuItem auth, home, business, sport, tech, saved;
    private String title_news, link_news, url_news;
    private GoogleSignInClient mGoogleSignInClient;
    String urlSave ="http://"+ip.getIp()+"/FreakingNews/getSave.php";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        menu = navigationView.getMenu();
        auth = menu.findItem(R.id.nav_auth);
        home = menu.findItem(R.id.nav_home);
        home.setCheckable(true);
        business = menu.findItem(R.id.nav_business);
        sport = menu.findItem(R.id.nav_sport);
        tech = menu.findItem(R.id.nav_tech);
        saved = menu.findItem(R.id.nav_saved);
        listView = (SwipeMenuListView) findViewById(R.id.listView);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        c_avatar = (ImageView) headerView.findViewById(R.id.c_avatar);
        c_name = (TextView) headerView.findViewById(R.id.c_name);
        c_email = (TextView) headerView.findViewById(R.id.c_email);

        User user = new User(this);
        idUser=user.getId();
        c_name.setText(user.getName());
        c_email.setText(user.getEmail());
        if(user.getUrl_avatar().equals("null")){
            auth.setTitle("Đăng nhập/Đăng ký");
            c_avatar.setImageResource(R.mipmap.ic_launcher_round);
        }
        else{
            auth.setTitle("Đăng xuất");
            Glide.with(this).load(user.getUrl_avatar()).into(c_avatar);
        }

        articles = new ArrayList<Article>();

        //nếu có kết nối Internet
        if (check_internet()) {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new ReadData().execute(cate);
                }
            });

            //tạo menu trượt
            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // tạo nút share
                    SwipeMenuItem shareItem = new SwipeMenuItem(
                            MainActivity.this);
                    // nút share background
                    shareItem.setBackground(new ColorDrawable(Color.rgb(66, 134, 244)));
                    // width của nút share
                    shareItem.setWidth(170);
                    // tạo icon
                    shareItem.setIcon(R.drawable.ic_share_white);
                    // thêm vào menu
                    menu.addMenuItem(shareItem);

                    // tạo nút save
                    SwipeMenuItem saveItem = new SwipeMenuItem(
                            MainActivity.this);
                    //background
                    saveItem.setBackground(new ColorDrawable(Color.rgb(66, 244, 83)));
                    // width
                    saveItem.setWidth(170);
                    // tạo icon
                    saveItem.setIcon(R.drawable.ic_menu_save);
                    // thêm vào menu
                    menu.addMenuItem(saveItem);
                }
            };
            listView.setMenuCreator(creator);

            listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        case 0:
                            //khi click vào nút share, gọi hàm shareIt() để share
//                            Toast.makeText(MainActivity.this, articles.get(position).image, Toast.LENGTH_LONG).show();
                            shareIt(articles.get(position));
                            break;
                        case 1:
                            User user = new User(MainActivity.this);
                            if(user.getEmail()=="Freakingnews@freakingnews.com"){
                                articles.clear();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                MainActivity.this.startActivity(intent);
                                finish();
                                Toast.makeText(MainActivity.this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else {
                                title_news = articles.get(position).title;
                                link_news =  articles.get(position).link;
                                url_news = articles.get(position).image;
                                addSave(urlSave);
                            }
                            //urlSave la cai nao day
//                            Toast.makeText(MainActivity.this, title_news, Toast.LENGTH_LONG).show();
                            //TODO 1: sự kiện longclick để lưu tin vào data, thêm code ở đây
//                            Toast.makeText(MainActivity.this, "Tin đã được lưu", Toast.LENGTH_LONG).show();
                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra("link", articles.get(position).link);
                    startActivity(intent);
                }
            });
        } else {    //Nếu không có internet, hiển thị thông báo lỗi
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ErrorFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            }
        }
    }

    //    dang xuat
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //điều hướng menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (!check_internet()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ErrorFragment()).commit();
        } else {
            switch (id) {
                case R.id.nav_home: {
                    cate = "https://www.24h.com.vn/upload/rss/tintuctrongngay.rss";
                    articles.clear();
                    home.setCheckable(true);
                    customAdapter.notifyDataSetChanged();
                    new ReadData().execute(cate);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new HomeFragment()).commit();
                    break;
                }
                case R.id.nav_business: {
                    cate = "https://24h.com.vn/upload/rss/taichinhbatdongsan.rss";
                    articles.clear();
                    business.setCheckable(true);
                    customAdapter.notifyDataSetChanged();
                    new ReadData().execute(cate);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new BusinessFragment()).commit();
                    break;
                }
                case R.id.nav_sport: {
                    sport.setCheckable(true);
                    cate = "https://24h.com.vn/upload/rss/thethao.rss";
                    articles.clear();
                    customAdapter.notifyDataSetChanged();
                    new ReadData().execute(cate);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new SportFragment()).commit();
                    break;
                }
                case R.id.nav_tech: {
                    tech.setCheckable(true);
                    cate = "https://24h.com.vn/upload/rss/congnghethongtin.rss";
                    articles.clear();
                    customAdapter.notifyDataSetChanged();
                    new ReadData().execute(cate);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new TechFragment()).commit();
                    break;
                }
                case R.id.nav_saved: {
                    User user = new User(this);
                    if(user.getEmail()=="Freakingnews@freakingnews.com"){
                        articles.clear();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(intent);
                        finish();
                        Toast.makeText(MainActivity.this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else {
                        articles.clear();
                        saved.setCheckable(true);
                        customAdapter.notifyDataSetChanged();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new SavedNewsFragment()).commit();
                        break;
                    }

                }
                case R.id.nav_story: {
                    User user = new User(this);
                    if(user.getEmail()=="Freakingnews@freakingnews.com"){
                        articles.clear();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(intent);
                        finish();
                        Toast.makeText(MainActivity.this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else{
                        articles.clear();
                        Intent intent = new Intent(MainActivity.this, PostActivity.class);
                        MainActivity.this.startActivity(intent);
                        finish();
                        break;
                    }
                }
                case R.id.nav_auth: {
                    User user = new User(this);
                    if(user.getEmail()=="Freakingnews@freakingnews.com"){
                        articles.clear();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        MainActivity.this.startActivity(intent);
                        finish();
                        break;
                    }
                    else {
                        SharedPreferences sharedPreferences = getSharedPreferences("DoUSer", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        signOut();
                        AccessToken.setCurrentAccessToken(null);
                        user= new User(this);
                        c_name.setText(user.getName());
                        c_email.setText(user.getEmail());
                        c_avatar.setImageResource(R.mipmap.ic_launcher_round);
                        auth.setTitle("Đăng nhập/Đăng ký");
                        break;
                    }

                }
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //function kiểm tra có internet đang kết nối không
    private boolean check_internet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    //    luu tin
    private void addSave(String url){
//        Toast.makeText(MainActivity.this, title_news, Toast.LENGTH_LONG).show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Đã lưu", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                        Log.d("@@@@@@????AAA","Loi!\n"+error.toString());
                    }
                }
        ){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
//                params.put("usernameApp", txtEmail.getText().toString().trim());
//                params.put("nameApp", txtName.getText().toString().trim());

                params.put("idUser", ""+idUser);
                params.put("title_news", title_news);
                params.put("link_news", link_news);
                params.put("url_news", url_news);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    //class aynctask để xử lý rss được đọc về
    class ReadData extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {

            return docNoiDung_Tu_URL(strings[0]);
        }
        @Override
        protected void onPostExecute(String s) {
            XMLDOMParser parser = new XMLDOMParser();
            Document document = parser.getDocument(s);
            NodeList nodeList = document.getElementsByTagName("item");
            NodeList nodeListDescription = document.getElementsByTagName("description");
            String title = "";
            String link = "";
            String image = "";
            String pubDate = "";
            for (int i=0; i < nodeList.getLength(); i++) {
                String cdata = nodeListDescription.item(i+1).getTextContent();
                Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
                Matcher m = p.matcher(cdata);
                if (m.find()) {
                    image = m.group(1);
                    Log.d("hinhanh", image);
                }
                Element element = (Element) nodeList.item(i);
                title = parser.getValue(element, "title");
                link = parser.getValue(element, "link");
                pubDate = parser.getValue(element, "pubDate");
                articles.add(new Article(title, link, image, pubDate));
            }

            customAdapter = new CustomAdapter(MainActivity.this, android.R.layout.simple_list_item_1, articles);
            listView.setAdapter(customAdapter);
            listView.invalidateViews();

            super.onPostExecute(s);
        }
    }

    //function đọc nội dung từ URL
    private String docNoiDung_Tu_URL(String theUrl){
        StringBuilder content = new StringBuilder();
        try {
            // create a url object
            URL url = new URL(theUrl);

            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();

            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null){
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    //function để chia sẻ tin trên các nền tảng
    public void shareIt(Article p) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String sharebody = p.link;
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, sharebody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
