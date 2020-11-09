package sample.newsapp.network;

import retrofit2.Response;
import sample.newsapp.network.response_models.SearchNewsResponse;

public interface ApiCallBack {

  void onSuccess(
      Response<SearchNewsResponse> response);

  void onError(Throwable t);
}
