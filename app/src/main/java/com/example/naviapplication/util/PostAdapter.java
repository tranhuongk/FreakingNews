package com.example.naviapplication.util;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.naviapplication.CommentActivity;
import com.example.naviapplication.object.Post;
import com.example.naviapplication.R;
import com.example.naviapplication.object.User;
import com.example.naviapplication.service.ip;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.RecyclerViewHolder> {
    private ArrayList<Post> listPost;
    private Context context;
    private LayoutInflater layoutInflater;
    private int idUser;
    private ip ip = new ip();
    private User user;
    private SetDate setDate;

    public PostAdapter(Context context, ArrayList<Post> listPost) {
        this.context = context;
        user = new User(context);
        layoutInflater = LayoutInflater.from(context);
        this.listPost = listPost;
        this.idUser = user.getId();
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.item_post, viewGroup, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        final Post post = listPost.get(position);
        if(post.getStatus() > 0){
            holder.upBtn.setImageResource(R.mipmap.voted_up);
            holder.downBtn.setImageResource(R.mipmap.vote_down);
        }
        if(post.getStatus() < 0){
            holder.upBtn.setImageResource(R.mipmap.vote_up);
            holder.downBtn.setImageResource(R.mipmap.voted_down);
        }
        if(post.getStatus() == 0){
            holder.upBtn.setImageResource(R.mipmap.vote_up);
            holder.downBtn.setImageResource(R.mipmap.vote_down);
        }
        holder.name.setText(post.getName());
        setDate = new SetDate(post.getDate());
        holder.date.setText(setDate.getDate());
        holder.content.setText(post.getContent());
        holder.vote.setText(""+post.getVote());
        Picasso.get().load(post.getUrl_avatar()).into(holder.avatar);
        holder.imagePost.setAdapter(new ImageAdapter(post.getListImage()));
        holder.imagePost.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if(post.getListImage().size() == 1){
            holder.positionImage.setBackgroundColor(0x00000000);
            holder.positionImage.setText("");
        }
        else {
            holder.positionImage.setBackgroundResource(R.drawable.position);
            holder.positionImage.setText(1+holder.imagePost.getCurrentItem()+"/"+post.getListImage().size());
            holder.imagePost.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int i) {
                    holder.positionImage.setText(++i+"/"+post.getListImage().size());
                }
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                }
                @Override
                public void onPageScrollStateChanged(int i) {
                }
            });
        }
        holder.upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.getStatus() == -1){
                    setStt("http://"+ip.getIp()+"/FreakingNews/setStatus.php",post.getId(),1);
                    holder.downBtn.setImageResource(R.mipmap.vote_down);
                    holder.upBtn.setImageResource(R.mipmap.voted_up);
                    post.setStatus(1);
                    post.setVote(post.getVote()+2);
                }
                else if(post.getStatus() == 0){
                    setStt("http://"+ip.getIp()+"/FreakingNews/setStatus.php",post.getId(),1);
                    holder.upBtn.setImageResource(R.mipmap.voted_up);
                    post.setStatus(1);
                    post.setVote(post.getVote()+1);
                }
                else {
                    setStt("http://"+ip.getIp()+"/FreakingNews/setStatus.php",post.getId(),0);
                    holder.upBtn.setImageResource(R.mipmap.vote_up);
                    post.setStatus(0);
                    post.setVote(post.getVote()-1);
                }
                holder.vote.setText(post.getVote()+"");
            }
        });
        holder.downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.getStatus() == 1){
                    setStt("http://"+ip.getIp()+"/FreakingNews/setStatus.php",post.getId(),-1);
                    holder.downBtn.setImageResource(R.mipmap.voted_down);
                    holder.upBtn.setImageResource(R.mipmap.vote_up);
                    post.setStatus(-1);
                    post.setVote(post.getVote()-2);
                }
                else if(post.getStatus() == 0){
                    setStt("http://"+ip.getIp()+"/FreakingNews/setStatus.php",post.getId(),-1);
                    holder.downBtn.setImageResource(R.mipmap.voted_down);
                    post.setStatus(-1);
                    post.setVote(post.getVote()-1);
                }
                else {
                    setStt("http://"+ip.getIp()+"/FreakingNews/setStatus.php",post.getId(),-1);
                    holder.downBtn.setImageResource(R.mipmap.vote_down);
                    post.setStatus(0);
                    post.setVote(post.getVote()+1);
                }
                holder.vote.setText(post.getVote()+"");
            }
        });
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("idPost", ""+post.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPost.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView name, date, content, vote, positionImage;
        ImageView avatar, upBtn, downBtn;
        Button commentButton;
        ViewPager imagePost;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name_post);
            date = (TextView) itemView.findViewById(R.id.date);
            content = (TextView) itemView.findViewById(R.id.content);
            vote = (TextView) itemView.findViewById(R.id.vote);
            positionImage = (TextView) itemView.findViewById(R.id.position);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            upBtn = (ImageView) itemView.findViewById(R.id.upBtn);
            downBtn = (ImageView) itemView.findViewById(R.id.downBtn);
            commentButton = (Button) itemView.findViewById(R.id.commentButton);
            imagePost = (ViewPager) itemView.findViewById(R.id.imagePost);
        }

    }

    private void setStt(String url, int idPost, int setStatus){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url+"?idUser="+idUser+"&idPost="+idPost+"&status="+setStatus,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Toast.makeText(context,response,Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,"Kết nối server không thành công!",Toast.LENGTH_LONG);
                    }
                });
        requestQueue.add(stringRequest);
    }
}
