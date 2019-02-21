# Tinker_APP
Android热更新Tinker + 多渠道打包 + 加固的流程详解demo
## 前言
最近在两个月时间内同时维护上线的六个项目，有很多是小问题，却只有更新版本上线耗费的人力物力实在太大，于是发一天时间集成了微信开源的热更新库Tinker，并将学习demo开源供初学热更新的码农使用。
## 热更新框架对比表
![image](https://github.com/wangfeng19930909/Tinker_APP/blob/master/screenshot/0.png)
## 步骤
####  第一步 添加 gradle 插件依赖

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.2'
        // TinkerPatch 插件
        classpath "com.tinkerpatch.sdk:tinkerpatch-gradle-plugin:1.1.8"
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
```
####  第二步 集成TinkerPatch SDK
在app中的gradle添加denpendencies 依赖，注意：这两个gradle不是同一个gradle，上面的那个build gradle 是整个项目的，下面这个build gradle是在app里面的，注意区分。

```
 dependencies {
    // 若使用annotation需要单独引用,对于tinker的其他库都无需再引用
    provided("com.tinkerpatch.tinker:tinker-android-anno:1.8.0")
    compile("com.tinkerpatch.sdk:tinkerpatch-android-sdk:1.1.8")
 }
```
注意,若使用 annotation 自动生成 Application， 需要单独引入 Tinker 的 tinker-android-anno 库。除此之外，我们无需再单独引入 tinker 的其他库。

为了简单方便，我们将 TinkerPatch 相关的配置都放于 tinkerpatch.gradle 中, 我们需要将其引入：

apply from: 'tinkerpatch.gradle'
####  第三步 配置 tinkerpatchSupport 参数
好了，接下来我们就在app目录下创建这个tinkerpatch.gradle，如图所示的目录：

![image](https://github.com/wangfeng19930909/Tinker_APP/blob/master/screenshot/3.png)

打开tinkerpatch.gradle，将 TinkerPatch 相关的配置都放于tinkerpatch.gradle中。

```
apply plugin: 'tinkerpatch-support'

/**
 * TODO: 请按自己的需求修改为适应自己工程的参数
 */

//基包路径
def bakPath = file("${buildDir}/bakApk/")
//基包文件夹名（打补丁包的时候，需要修改）
def baseInfo = "app-1.0.1-0221-11-01-38"
//版本名称
def variantName = "release"

/**
 * 对于插件各参数的详细解析请参考
 *
 */
tinkerpatchSupport {
    //可以在debug的时候关闭 tinkerPatch
    tinkerEnable = true
    //是否使用一键接入功能 默认为false  是否反射 Application 实现一键接入；
    // 一般来说，接入 Tinker 我们需要改造我们的 Application, 若这里为 true， 即我们无需对应用做任何改造即可接入。
    reflectApplication = true
    //将每次编译产生的 apk/mapping.txt/R.txt 归档存储的位置
    autoBackupApkPath = "${bakPath}"
    appKey = "582e640cae57f603"// 注意！！！  需要修改成你的appkey

    /** 注意: 若发布新的全量包, appVersion一定要更新 **/
    appVersion = "1.0.1"

    protectedApp=true//使用加固

    def pathPrefix = "${bakPath}/${baseInfo}/${variantName}/"
    def name = "${project.name}-${variantName}"
    /**
     * 基准包的文件路径, 对应 tinker 插件中的 oldApk 参数;编译补丁包时，
     * 必需指定基准版本的 apk，默认值为空，则表示不是进行补丁包的编译
     */
    baseApkFile = "${pathPrefix}/${name}.apk"

    /**
     * 基准包的 Proguard mapping.txt 文件路径, 对应 tinker 插件 applyMapping 参数；在编译新的 apk 时候，
     * 我们希望通过保持基准 apk 的 proguard 混淆方式，
     * 从而减少补丁包的大小。这是强烈推荐的，编译补丁包时，我们推荐输入基准 apk 生成的 mapping.txt 文件。
     */
    baseProguardMappingFile = "${pathPrefix}/${name}-mapping.txt"
    /**
     * 基准包的资源 R.txt 文件路径, 对应 tinker 插件 applyResourceMapping 参数；在编译新的apk时候，
     * 我们希望通基准 apk 的 R.txt 文件来保持 Resource Id 的分配，这样不仅可以减少补丁包的大小，
     * 同时也避免由于 Resource Id 改变导致 remote view 异常
     */
    baseResourceRFile = "${pathPrefix}/${name}-R.txt"
    /**
     *  若有编译多flavors需求, 可以参照： https://github.com/TinkerPatch/tinkerpatch-flavors-sample
     *  注意: 除非你不同的flavor代码是不一样的,不然建议采用zip comment或者文件方式生成渠道信息（相关工具：walle 或者 packer-ng）
     **/
}

/**
 * 用于用户在代码中判断tinkerPatch是否被使能
 */
android {
    defaultConfig {
        buildConfigField "boolean", "TINKER_ENABLE", "${tinkerpatchSupport.tinkerEnable}"
    }
}
/**
 * 一般来说,我们无需对下面的参数做任何的修改
 * 对于各参数的详细介绍请参考:
 * https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97
 */
tinkerPatch {
    ignoreWarning = false
    useSign = true  //是否需要签名，打正式包如果这里是true，则要配置签名，否则会编译不过去
    dex {
        dexMode = "jar"
        pattern = ["classes*.dex"]
        loader = []
    }
    lib {
        pattern = ["lib/*/*.so"]
    }

    res {
        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        ignoreChange = []
        largeModSize = 100
    }
    packageConfig {
    }
    sevenZip {
        zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
//        path = "/usr/local/bin/7za"
    }
    buildConfig {
        keepDexApply = false
    }
}
```
##### 参数配置图


![image](https://github.com/wangfeng19930909/Tinker_APP/blob/master/screenshot/1.png)

这里还需要注意一个地方，就是appKey，在我们登录tinker的官网，并且添加一个app版本以后，都会生成一个appkey，我们要把自己的appkey填到上面的配置中，附上一张图：


![image](https://github.com/wangfeng19930909/Tinker_APP/blob/master/screenshot/2.png)

#### 第四步 初始化 TinkerPatch SDK

```
package com.barnettwong.tinkerdemo;



import android.app.Application;

import com.tencent.tinker.loader.app.ApplicationLike;
import com.tinkerpatch.sdk.TinkerPatch;
import com.tinkerpatch.sdk.loader.TinkerPatchApplicationLike;


/**
 * Created by wang on 2019-2-20.
 * reflectApplication = true 时
 */

public class tinkerApplication extends Application {

    private ApplicationLike tinkerApplicationLike;
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.TINKER_ENABLE) {
            // 我们可以从这里获得Tinker加载过程的信息
            tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();

            // 初始化TinkerPatch SDK, 更多配置可参照API章节中的,初始化SDK
            TinkerPatch.init(tinkerApplicationLike)
                    .reflectPatchLibrary()
                    .setPatchRollbackOnScreenOff(true)
                    .setPatchRestartOnSrceenOff(true);

            // 每隔3个小时去访问后台时候有更新,通过handler实现轮训的效果
            new FetchPatchHandler().fetchPatchWithInterval(3);
        }
    }
}

```
最后在加上FetchPatchHandler这个类：

```
package com.barnettwong.tinkerdemo;


import android.os.Handler;
import android.os.Message;

import com.tinkerpatch.sdk.TinkerPatch;

/**
 *  Created by wang on 2019-2-20.
 */

public class FetchPatchHandler extends Handler {

    public static final long HOUR_INTERVAL = 3600 * 1000;
    private long checkInterval;


    /**
     * 通过handler, 达到按照时间间隔轮训的效果
     */
    public void fetchPatchWithInterval(int hour) {
        //设置TinkerPatch的时间间隔
        TinkerPatch.with().setFetchPatchIntervalByHours(hour);
        checkInterval = hour * HOUR_INTERVAL;
        //立刻尝试去访问,检查是否有更新
        sendEmptyMessage(0);
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        //这里使用false即可
        TinkerPatch.with().fetchPatchUpdate(false);
        //每隔一段时间都去访问后台, 增加10分钟的buffer时间
        sendEmptyMessageDelayed(0, checkInterval + 10 * 60 * 1000);
    }
}

```

最后将AndroidManifest.xml中的添加上相应的网络和SD的权限，还要在application中加上 android:name=".tinkerApplication"，附上代码：
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.barnettwong.tinkerdemo">

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>


    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:name=".tinkerApplication"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>

#### 编译基准包

#### 使用360加固助手对apk进行多渠道配置和加固

#### 发布补丁
具体打包和发布补丁过程在此不详述，如有问题可联系wangfengkxhp@163.com

MIT License
=================================== 
Copyright 2019, wangfeng19930909

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

