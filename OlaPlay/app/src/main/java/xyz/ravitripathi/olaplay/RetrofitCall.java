package xyz.ravitripathi.olaplay;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Ravi on 15-12-2017.
 */

public interface RetrofitCall {
    @GET("studio")
    Call<List<ResponsePOJO>> getSongs();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://starlord.hackerearth.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
