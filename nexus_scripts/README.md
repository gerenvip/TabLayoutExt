## 使用已发布的私有库
首先打开project的build.gradle,添加Maven仓库地址

```
allprojects {
    repositories {
        xxx
        maven { url "http://47.94.132.220:8081/nexus/content/repositories/releases" }
        maven { url 'http://47.94.132.220:8081/nexus/content/repositories/thirdparty' }
    }
}
```

然后打开app的build.gradle, 添加依赖
```
dependencies {
    implementation "com.suapp:ad-client:1.0"
}
```

ok, 大功告成, 您可以愉快的使用私有库, 想做啥就做啥！

## 需要拷贝的部分
如果您想自己打aar包并实时发布到Nexus OSS,   
请把以下部分配置拷贝到您的project根目录下的gradle.properties

```
# the following can move to ~/.gradle/gradle.properties
NEXUS_USERNAME=admin
NEXUS_PASSWORD=jiandaola
RELEASE_REPOSITORY_URL=http://47.94.132.220:8081/nexus/content/repositories/releases
SNAPSHOT_REPOSITORY_URL=http://47.94.132.220:8081/nexus/content/repositories/snapshots

PROJ_GROUP=com.suapp

PROJ_WEBSITEURL=http://kvh.io
PROJ_ISSUETRACKERURL=https://github.com/kevinho/Embrace-Android-Studio-Demo/issues
PROJ_VCSURL=https://github.com/kevinho/Embrace-Android-Studio-Demo.git
PROJ_DESCRIPTION=demo apps for embracing android studio

PROJ_LICENCE_NAME=The Apache Software License, Version 2.0
PROJ_LICENCE_URL=http://www.apache.org/licenses/LICENSE-2.0.txt
PROJ_LICENCE_DEST=repo

DEVELOPER_ID=panzhilong
DEVELOPER_NAME=panzhilong
DEVELOPER_EMAIL=panzhilong@jiandaola.com
```

注释project下的settings.gradle中的如下脚本
```
exec {
    commandLine "git", "submodule", "update", "--init", "--recursive"
}
```

待aar打包结束后, 再还原.

## aar打包步骤
1. 进入gradle.properties, 修改PROJ_VERSION字段
2. 返回上一级,执行命令:
 ./gradlew -p ad-client clean build uploadArchives --info
3. aar打包完成并自动上传到Nexus OSS
