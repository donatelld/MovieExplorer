/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.alvin.movie.explorer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alvin.movie.explorer.entity.Movie;
import com.alvin.movie.explorer.view.MovieInfoView;
import com.alvin.movie.explorer.view.WrapLinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {
    private static List<String> supportMovieFormats = Arrays.asList("ISO", "MKV", "MP4", "TS");
    private List<Movie> movieList = null;
    private WrapLinearLayout mainLayout;
    private Context context;
    private int selectedIndex;
    private static final int ROW_SIZE = 8;
    private int currentRow = 1;
    private static final String REMOTE_IP = "192.168.0.110";
    private static final String REOMTE_PORT = "80";
    private static final String TAG = "MainActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.main_layout);
        mainLayout = findViewById(R.id.main_layout);

        if(movieList != null) return;
        Subscription subscribe = Observable.just("")
                .map(new Func1<String, List<Movie>>() {
                    @Override
                    public List<Movie> call(String s) {
                        return loadRemoteMovies();
                    }
                }).subscribeOn(Schedulers.io())//把工作线程指定为了IO线程
                .observeOn(AndroidSchedulers.mainThread())//把回调线程指定为了UI线程
                .subscribe(new Action1<List<Movie>>() {
                    @Override
                    public void call(List<Movie> movies) {
                        if (movies == null){
                            movieList = new ArrayList<>();
                            try {
                                new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                                    .setTitle("错误")
                                    .setMessage("未连接到电脑，请检查电脑是否开机")
                                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            System.exit(0);
                                        }
                                    }).create().show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }else{
                            movieList = movies;
                        }
                        boolean first = true;
                        for (Movie movie : movieList) {
                            try {
                                MovieInfoView view = new MovieInfoView(context);
                                view.getMovieNameView().setText(movie.getResolution() + " " + movie.getTitle());
                                ImageView movieImageView = view.getMovieImageView();
                                if(movie.getPhotoDrawable() != null) {
                                    movieImageView.setBackground(movie.getPhotoDrawable());
                                }
                                mainLayout.addView(view);
                                if(first){
                                    view.getMovieLayout().setBackgroundResource(R.drawable.selected_border);
                                    view.requestFocus();
                                }
                                first = false;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private List<Movie> loadRemoteMovies() {
        StringBuilder builder = new StringBuilder();
        long start = System.currentTimeMillis();
        try {
            URL url = new URL("http://" + REMOTE_IP + ":" + REOMTE_PORT + "/getMovies.html");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(3000);
            OutputStream os = connection.getOutputStream();
            connection.connect();
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "获取电影列表失败", e);
            return null;
        }
        try{
            final List<Movie> movies = JSON.parseArray(builder.toString(), Movie.class);
            if(movies != null) {
                final CountDownLatch latch = new CountDownLatch(movies.size());
                final AtomicInteger queueSize = new AtomicInteger(movies.size());//等待队列大小
                final AtomicInteger size = new AtomicInteger(0);//等待队列大小
                final int MAX_SIZE = 10;
                final Queue<Movie> queue = new LinkedList<>();
                for(final Movie movie : movies) {
                    queue.add(movie);
                }
                while(queueSize.intValue() > 0) {
                    try {
                        if(size.intValue() >= MAX_SIZE){
                            System.out.println("thread is full");
                            continue;
                        }
                        final Movie movie = queue.poll();
                        System.out.println("queueSize: " + queueSize.intValue());
                        queueSize.decrementAndGet();
                        size.incrementAndGet();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if(movie.getPhoto() != null && !movie.getPhoto().equals("")) {
                                        movie.setPhotoDrawable(Drawable.createFromStream(
                                                new URL("http://" + REMOTE_IP + ":" + REOMTE_PORT + movie.getPhoto()).openStream(), movie.getPhoto()));
                                    }
                                    size.decrementAndGet();
                                    latch.countDown();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("total cost: " + (System.currentTimeMillis() - start));
            return movies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getRemotePhoto(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//设置超时
            conn.setDoInput(true);
            conn.setUseCaches(false);//不缓存
            conn.connect();
            InputStream is = conn.getInputStream();//获得图片的数据流
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    private List<Movie> loadSmbMovies() {
        try {
            List<Movie> movieList = new ArrayList<>();
            String[] movieFolders = new String[]{"smb://192.168.0.110/H/Movie"};
            String ip = "192.168.0.110";
            UniAddress uniAddress = UniAddress.getByName(ip);
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, null, null);
            SmbSession.logon(uniAddress, auth);
            for(String folder : movieFolders) {
                SmbFile movieFolder = new SmbFile(folder, auth);
                for(SmbFile file : movieFolder.listFiles()) {
                    if (file.isHidden()) continue;
                    String format = "";
                    if (file.isDirectory()) {
                        for (SmbFile child : file.listFiles()) {
                            if (child.getName().equalsIgnoreCase("BDMV")) {
                                format = "BDMV";
                                break;
                            }
                        }
                        if (!format.equals("BDMV"))continue;
                    } else {
                        if (file.length() < 1024 * 1024 * 1014) continue;//小于1G的文件跳过
                        String fileFormat = file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase();
                        if (!supportMovieFormats.contains(fileFormat)) {
                            continue;
                        }//不支持的视频文件格式
                        format = fileFormat;
                    }
                    String title = "";
                    int index = file.getName().indexOf(" ");
                    if (index < 0) {
                        title = file.getName();
                    } else {
                        title = file.getName().substring(0, index);
                    }
                    Movie movie = new Movie();
                    movie.setTitle(title);
                    movieList.add(movie);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieList;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int nextIndex = -1;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP :
                if (selectedIndex == -1) {
                    nextIndex = 0;
                } else {
                    if (selectedIndex - ROW_SIZE >= 0) {
                        nextIndex = selectedIndex - ROW_SIZE;
                        if(currentRow % 3 == 1) {
                            mainLayout.scrollTo(0, (currentRow - 4) * 350);
                        }
                        currentRow--;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN :
                if (selectedIndex == -1) {
                    nextIndex = 0;
                } else {
                    if (selectedIndex + ROW_SIZE < movieList.size()) {
                        nextIndex = selectedIndex + ROW_SIZE;
                        currentRow++;
                        if(currentRow % 3 == 1) {
                            mainLayout.scrollTo(0, (currentRow - 1) * 350);
                        }
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT :
                if (selectedIndex == -1) {
                    nextIndex = 0;
                } else {
                    if (selectedIndex - 1 >= 0) {
                        nextIndex = selectedIndex - 1;
                        if((nextIndex + 1) % ROW_SIZE == 0) {
                            if(currentRow % 3 == 1) {
                                mainLayout.scrollTo(0, (currentRow - 4) * 350);
                            }
                            currentRow--;
                        }
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT :
                if (selectedIndex == -1) {
                    nextIndex = 0;
                } else {
                    if (selectedIndex + 1 < movieList.size()) {
                        nextIndex = selectedIndex + 1;
                        if((nextIndex + 1) % ROW_SIZE == 1) {
                            currentRow++;
                            if(currentRow % 3 == 1) {
                                mainLayout.scrollTo(0, (currentRow - 1) * 350);
                            }
                        }
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER :
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);
                Movie movie = movieList.get(selectedIndex);
                String path = movie.getFilePath();
                path = path.replaceAll(":", "").replace("\\", "/");
                if(movie.getFormat().equals("BDMV")){
                    path += "/BDMV/index.bdmv";
                }
                path = "smb://" + REMOTE_IP + "/" + path;
                        intent.setDataAndType(Uri.parse(path), "video/*");
                startActivity(intent);
                return super.onKeyDown(keyCode, event);
        }
        if(nextIndex != -1)
        {
            int beforeRows = selectedIndex / ROW_SIZE;
            int afterRows = nextIndex / ROW_SIZE;
            MovieInfoView currentView = (MovieInfoView)mainLayout.getChildAt(selectedIndex);
            MovieInfoView nextView = (MovieInfoView)mainLayout.getChildAt(nextIndex);
            currentView.getMovieLayout().setBackgroundResource(R.drawable.default_border);
            nextView.getMovieLayout().setBackgroundResource(R.drawable.selected_border);
            nextView.requestFocus();
            selectedIndex = nextIndex;
//            if (beforeRows < afterRows)
//            {
//                if(afterRows > 2)
//                {
//                    scrollBar.ScrollToVerticalOffset(scrollBar.VerticalOffset + 350);
//                }
//            }
//            else if(beforeRows > afterRows)
//            {
//                scrollBar.ScrollToVerticalOffset(scrollBar.VerticalOffset - 350);
//            }
//
//            selectedMovie(getMovieInfo(nextIndex));
        }
        return super.onKeyDown(keyCode, event);
    }
}
