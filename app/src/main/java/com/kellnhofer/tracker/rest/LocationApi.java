package com.kellnhofer.tracker.rest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LocationApi {

    String BASE_PATH = "/api/v1";

    @GET(BASE_PATH + "/loc")
    Call<List<ApiLocation>> getLocations(@Query("change_time") Long changeTime);

    @POST(BASE_PATH + "/loc")
    Call<ApiLocation> createLocation(@Body ApiLocation location);

    @GET(BASE_PATH + "/loc/{location_id}")
    Call<ApiLocation> getLocation(@Path("location_id") Long locationId);

    @PUT(BASE_PATH + "/loc/{location_id}")
    Call<ApiLocation> changeLocation(@Path("location_id") Long locationId, @Body ApiLocation location);

    @DELETE(BASE_PATH + "/loc/{location_id}")
    Call<Void> deleteLocation(@Path("location_id") Long locationId);

    @GET(BASE_PATH + "/loc/deleted")
    Call<List<Long>> getDeletedLocations(@Query("change_time") Long changeTime);

}
