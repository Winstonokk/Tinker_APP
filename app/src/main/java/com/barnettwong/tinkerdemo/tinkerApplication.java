package com.barnettwong.tinkerdemo;



import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.tencent.tinker.entry.ApplicationLike;
import com.tinkerpatch.sdk.TinkerPatch;
import com.tinkerpatch.sdk.loader.TinkerPatchApplicationLike;


/**
 * Created by wang on 2019-2-20.
 * reflectApplication = true 时
 */

public class tinkerApplication extends Application {

    private ApplicationLike tinkerApplicationLike;

    public tinkerApplication() {

    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //在app/build.grade中配置multiDexEnabled true即可
        //you must install multiDex whatever tinker is installed!
        //MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initTinkerPatch();
    }

    /**
     * 我们需要确保至少对主进程跟patch进程初始化 TinkerPatch
     */
    private void initTinkerPatch() {
        // 我们可以从这里获得Tinker加载过程的信息
        if (BuildConfig.TINKER_ENABLE) {
            tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();
            // 初始化TinkerPatch SDK
            TinkerPatch.init(
                    tinkerApplicationLike
//                new TinkerPatch.Builder(tinkerApplicationLike)
//                    .requestLoader(new OkHttp3Loader())
//                    .build()
            )
                    .reflectPatchLibrary()
                    .setPatchRollbackOnScreenOff(true)
                    .setPatchRestartOnSrceenOff(true)
                    .setFetchPatchIntervalByHours(3)
            ;
            // 获取当前的补丁版本
            Log.d("wang", "当前获取补丁：Current patch version is " + TinkerPatch.with().getPatchVersion());

            // fetchPatchUpdateAndPollWithInterval 与 fetchPatchUpdate(false)
            // 不同的是，会通过handler的方式去轮询
            TinkerPatch.with().fetchPatchUpdateAndPollWithInterval();
        }
    }
}
