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

package explorer.movie.alvin.com.movieexplorer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.net.MalformedURLException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        textView = findViewById(R.id.textView);

        System.setProperty("jcifs.smb.client.dfs.disabled", "true");//默认false
        System.setProperty("jcifs.smb.client.soTimeout", "1000000");//超时
        System.setProperty("jcifs.smb.client.responseTimeout", "30000");//超时

        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = "192.168.0.110";//pc地址
                String username = "alvin";//账户密码
                String password = "";
                UniAddress mDomain = null;
                try {
                    //登录授权
                    mDomain = UniAddress.getByName(ip);
                    NtlmPasswordAuthentication mAuthentication = new NtlmPasswordAuthentication(null, null, null);
                    SmbSession.logon(mDomain, mAuthentication);
                    //登录授权结束
                    String rootPath = "smb://" + ip + "/";
                    SmbFile mRootFolder;
                    try {
//                        mRootFolder = new SmbFile(rootPath, mAuthentication);
//                        try {
//                            SmbFile[] files;
//                            files = mRootFolder.listFiles();
//                            StringBuilder sb = new StringBuilder();
//                            for (SmbFile smbfile : files) {
//                                sb.append(smbfile.getName()).append("\n");
//                                Log.e("文件名称----", smbfile.getCanonicalPath());//这个就能获取到共享文件夹了
//                            }
//                            textView.setText(sb.toString());
//                        } catch (SmbException e) {
//                            e.printStackTrace();
//                        }
                        SmbFile file = new SmbFile("smb://192.168.0.110/h/Movie/复仇者联盟4 Avengers.Endgame.2019.UHD.BluRay.2160p.HEVC.Atmos.TrueHD7.1-LKReborn@CHDBits", mAuthentication);
                        Intent intent = new Intent();

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        intent.setAction(android.content.Intent.ACTION_VIEW);

                        String path = "smb://192.168.0.110/h/Movie/豪勇七蛟龙 The.Magnificent.Seven.2016.BluRay.1080p.AVC.DTS-HD.MA7.1-bb@HDSky.iso";;

                        intent.setDataAndType(Uri.parse(path), "video/*");

                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
