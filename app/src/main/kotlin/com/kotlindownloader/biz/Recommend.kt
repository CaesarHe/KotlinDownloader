package com.kotlindownloader.biz

/**
 * Created by heyaokuai on 2017/10/12.
 */

data class Recommend(var id: Int = 0,
                     var resourceType: Int = 0,
                     var packageName: String? = null,
                     var name: String? = null,
                     var categoryId: Int = 0,
                     var categoryName: String? = null,
                     var versionName: String? = null,
                     var versionCode: Int = 0,
                     var size: Int = 0,
                     var downloadUrl: String? = null,
                     var iconUrl: String? = null,
                     var downloads: Int = 0,
                     var updateTime: Long = 0,
                     var versionId: Int = 0,
                     var hotLevel: Int = 0,
                     var editorRecommend: String? = null,
                     var searchCount: Int = 0,
                     var yrank: Int = 0,
                     var dbyrank: Int = 0,
                     var risingrate: Int = 0,
                     var isSignificant: Int = 0,
                     var listorder: Int = 0,
                     var isIsRecentRise: Boolean = false,
                     var cornerMark: Int = 0,
                     var isInstalled: Boolean = false) {

}
