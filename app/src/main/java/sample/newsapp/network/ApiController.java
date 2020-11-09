package sample.newsapp.network;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sample.newsapp.network.response_models.SearchNewsResponse;

public class ApiController implements Callback<SearchNewsResponse> {

  static final String BASE_URL = "https://hn.algolia.com/api/v1/";
  private final String logTag = ApiController.class.getSimpleName();
  private ApiCallBack apiCallBack;

  public void getNewsListForSearch(String searchQuery, int page, ApiCallBack apiCallBack) {
    Gson gson = new GsonBuilder()
        .setLenient()
        .create();

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();

    this.apiCallBack = apiCallBack;

    ApiInterface apiInterface = retrofit.create(ApiInterface.class);

    Call<SearchNewsResponse> call = apiInterface.searchNews(searchQuery, page);
    call.enqueue(this);
  }

  /**
   * Invoked for a received HTTP response.
   * <p>
   * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
   * Call {@link Response#isSuccessful()} to determine if the response indicates success.
   */
  @Override public void onResponse(Call<SearchNewsResponse> call,
      Response<SearchNewsResponse> response) {
    if (response.isSuccessful() && response.body() != null) {
      Log.d(logTag, "nbPages: " + response.body().getNbPages());

      //callback to caller with response.body()

      apiCallBack.onSuccess(/*call,*/ response);
    } else {
      if (response.errorBody() != null) {
        Log.e(logTag, response.errorBody().toString());
        apiCallBack.onError(new Throwable(response.errorBody().toString()));
      } else {
        final String errorMsg = "API Error: response.errorBody() is null";
        Log.e(logTag, errorMsg);
        apiCallBack.onError(new Throwable(errorMsg));
      }
    }
  }

  /**
   * Invoked when a network exception occurred talking to the server or when an unexpected
   * exception occurred creating the request or processing the response.
   */
  @Override public void onFailure(Call<SearchNewsResponse> call, Throwable t) {
    t.printStackTrace();
    apiCallBack.onError(t);
  }
}
