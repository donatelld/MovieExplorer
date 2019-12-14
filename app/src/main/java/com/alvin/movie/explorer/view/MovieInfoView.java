package com.alvin.movie.explorer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alvin.movie.explorer.R;

/**
 * Created by Alvin on 2019/12/14.
 */

public class MovieInfoView extends LinearLayout {
    private LinearLayout movieLayout;
    private ImageView movieImageView;
    private TextView movieNameView;

    public MovieInfoView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.movie_info,this,true);
    }

    /**
     * 此方法会在所有的控件都从xml文件中加载完成后调用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public ImageView getMovieImageView() {
        if(movieImageView == null)
            movieImageView = findViewById(R.id.movieImageView);
        return movieImageView;
    }

    public TextView getMovieNameView() {
        if(movieNameView == null)
            movieNameView = findViewById(R.id.movieNameView);
        return movieNameView;
    }

    public LinearLayout getMovieLayout() {
        if(movieLayout == null)
            movieLayout = findViewById(R.id.movieLayout);
        return movieLayout;
    }
}
