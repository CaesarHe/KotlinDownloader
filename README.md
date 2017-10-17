# KotlinDownloader
```
 val request = DownloadRequest(
                     url = downloadUrl,
                     fileName = "$packageName.apk",
                     localPath = "${Environment.getExternalStorageDirectory()}/kotlin/download/$packageName.apk"
             )
 DownloadManager.getInstance().runCmd(DownloadManager.CMD_NEW, request)
 ```
 ![image](https://github.com/CaesarHe/KotlinDownloader/blob/master/device-2017-10-13-162408.png?raw=true)