package cn.chper.hengji.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceImpl {

    public static final ServiceImpl instance = new ServiceImpl();

    public Service service;

    public String token;

    public String uic;

    ServiceImpl() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.2.112:4523/mock/365548/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(Service.class);
    }

}

