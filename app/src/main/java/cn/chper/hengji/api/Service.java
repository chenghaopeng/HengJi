package cn.chper.hengji.api;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Service {

    @Multipart
    @POST("user/getCode")
    Call<SimpleResponse> getCode(@Part("phone") String phone);

    @Multipart
    @POST("user/login")
    Call<SimpleResponse> login(@Part("phone") String phone, @Part("code") String code);

    @Multipart
    @POST("uic/refresh")
    Call<SimpleResponse> refreshUic(@Part("token") String token);

    @POST("uic/risk")
    Call<SimpleResponse> riskUic();

    @Multipart
    @POST("uic/judge")
    Call<SimpleResponse> judgeUic(@Part("uic") String uic);

}
