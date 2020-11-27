package cn.chper.hengji.api;

import com.google.gson.annotations.SerializedName;

public class SimpleResponse {

    @SerializedName("code")
    public int code;

    @SerializedName("data")
    public Object data;

}
