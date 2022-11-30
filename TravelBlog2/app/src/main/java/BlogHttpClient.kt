package com.travelblog

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.concurrent.Executors

object BlogHttpClient {

    private const val BASE_URL =
        "https://bitbucket.org/dmytrodanylyk/travel-blog-resources/raw/"
    private const val BLOG_ARTICLES_URL =
        BASE_URL + "8550ef2064bf14fcf3b9ff322287a2e056c7e153/blog_articles.json"

    private val executor = Executors.newFixedThreadPool(4)
    private val client = OkHttpClient()
    private val gson = Gson()




}

