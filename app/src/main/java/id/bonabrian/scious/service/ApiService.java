package id.bonabrian.scious.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import id.bonabrian.scious.source.dao.Articles;
import id.bonabrian.scious.source.dao.BaseModel;
import id.bonabrian.scious.source.dao.Measurements;
import id.bonabrian.scious.source.dao.Recommended;
import id.bonabrian.scious.source.dao.User;
import id.bonabrian.scious.util.AppConstant;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface ApiService {

    @FormUrlEncoded
    @POST("index.php?page=api&type=login")
    Observable<User.UserList> loginEmail(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("index.php?page=api&type=loginwithgoogle")
    Observable<User.UserList> loginWithGoogle(@Field("email") String email, @Field("name") String name);

    @FormUrlEncoded
    @POST("index.php?page=api&type=register")
    Observable<BaseModel> registerUser(@Field("name") String name,
                                       @Field("email") String email,
                                       @Field("password") String password,
                                       @Field("confirm_password") String confirmPassword,
                                       @Field("weight") String weight,
                                       @Field("height") String height,
                                       @Field("birthday") String birthday);

    @FormUrlEncoded
    @POST("index.php?page=api&type=savemeasurement")
    Observable<BaseModel> saveMeasurement(@Field("user_id") String user_id,
                                          @Field("stress_level") String stress_level,
                                          @Field("sdnn") double sdnn,
                                          @Field("mean_hr") double mean_hr,
                                          @Field("mean_rr") double mean_rr,
                                          @Field("time") String time);

    @GET("index.php?page=api&type=articles")
    Observable<Articles.ListArticles> getListArticles(@Query("offset") int offset);

    @GET("index.php?page=api&type=history")
    Observable<Measurements.ListMeasurements> getListMeasurements(@Query("user_id") String user_id, @Query("offset") int offset);

    @GET("index.php?page=api&type=recommended")
    Observable<Recommended.ListRecommended> getListRecommended(@Query("user_id") String user_id, @Query("offset") int offset);

    @GET("index.php?page=api&type=seerecommendation")
    Observable<Recommended.ListRecommended> seeRecommendation(@Query("stress_level") String stress_level, @Query("offset") int offset);

    @FormUrlEncoded
    @POST("index.php?page=api&type=updateuserdata")
    Observable<User.UserData> editUserName(@Query("user_id") String userId, @Field("name") String name);

    @FormUrlEncoded
    @POST("index.php?page=api&type=updateuserdata")
    Observable<User.UserData> editUserEmail(@Query("user_id") String userId, @Field("email") String email);

    @FormUrlEncoded
    @POST("index.php?page=api&type=updateuserdata")
    Observable<User.UserData> editUserWeight(@Query("user_id") String userId, @Field("weight") double weight);

    @FormUrlEncoded
    @POST("index.php?page=api&type=updateuserdata")
    Observable<User.UserData> editUserHeight(@Query("user_id") String userId, @Field("height") double height);

    @FormUrlEncoded
    @POST("index.php?page=api&type=updateuserdata")
    Observable<User.UserData> editUserBirthday(@Query("user_id") String userId, @Field("birthday") String birthday);

    class Factory {
        public static ApiService create() {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.readTimeout(20, TimeUnit.SECONDS);
            builder.connectTimeout(10, TimeUnit.SECONDS);
            builder.writeTimeout(10, TimeUnit.SECONDS);

            OkHttpClient client = builder.build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(AppConstant.Api.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            return retrofit.create(ApiService.class);
        }
    }
}
