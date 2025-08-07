package org.example;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class UatClient {
    private static final String BASE_URL = "https://dice-uat.eka.io/";
    private static UatClient instance;
    private final SaasMcpApi api;
    
    private UatClient() {
        // Create OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // Create API interface
        this.api = retrofit.create(SaasMcpApi.class);
    }
    
    public static synchronized UatClient getInstance() {
        if (instance == null) {
            instance = new UatClient();
        }
        return instance;
    }
    
    public SaasMcpApi getApi() {
        return api;
    }
} 