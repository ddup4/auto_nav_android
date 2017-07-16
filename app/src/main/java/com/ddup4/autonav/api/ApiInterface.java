package com.ddup4.autonav.api;

import com.ddup4.autonav.api.entity.GpsInfo;
import com.ddup4.autonav.api.entity.Response;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by idonans on 2017/7/16.
 */

public interface ApiInterface {

    @GET("/gpsinfo/getByPhone")
    Single<Response<GpsInfo>> getGpsInfo(@Query("phone") String phone);

}
