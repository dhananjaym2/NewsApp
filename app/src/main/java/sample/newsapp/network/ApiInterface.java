package sample.newsapp.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import sample.newsapp.network.response_models.SearchNewsResponse;

public interface ApiInterface {

  @GET("search") Call<SearchNewsResponse> searchNews(@Query("query") String query,
      @Query("page") int page);
}
