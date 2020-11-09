package sample.newsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Response;
import sample.newsapp.list.adapter.NewsItemClick;
import sample.newsapp.list.adapter.NewsListAdapter;
import sample.newsapp.network.ApiCallBack;
import sample.newsapp.network.ApiController;
import sample.newsapp.network.response_models.Hit;
import sample.newsapp.network.response_models.SearchNewsResponse;
import sample.newsapp.utils.NetworkUtils;

public class NewsListActivity extends AppCompatActivity implements View.OnClickListener,
    ApiCallBack, NewsItemClick {

  private EditText editTextSearchFromUser;
  private RecyclerView recyclerViewNewsList;
  private Button btnSearchNews;
  private final String logTag = NewsListActivity.class.getSimpleName();
  private NewsListAdapter adapter;
  private List<Hit> hits;
  private ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_news_list);

    initViews();
  }

  private void initViews() {
    editTextSearchFromUser = findViewById(R.id.editTextSearchFromUser);
    recyclerViewNewsList = findViewById(R.id.recyclerViewNewsList);
    btnSearchNews = findViewById(R.id.btnSearchNews);
    btnSearchNews.setOnClickListener(this);
  }

  @Override public void onClick(View view) {
    if (view.getId() == R.id.btnSearchNews) {
      if (NetworkUtils.isConnectedToInternet(this)) {
        if (editTextSearchFromUser.getText().toString() != null
            && editTextSearchFromUser.getText().toString().trim().length() >= 0) {
          // TODO show progress loader
          progressDialog = getProgressDialogToShow();
          progressDialog.show();
          getNewsListForSearch(editTextSearchFromUser.getText().toString().trim(), 0);
        } else {
          showToastMessage(getString(R.string.emptySearch));
        }
      } else {
        showToastMessage(getString(R.string.internetNotConnected));
      }
    }
  }

  private ProgressDialog getProgressDialogToShow() {
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage(getString(R.string.loading));
    progressDialog.setCancelable(false);
    return progressDialog;
  }

  private void getNewsListForSearch(String searchQuery, int pageNum) {
    // start API call on NON main thread
    ApiController apiController = new ApiController();
    apiController.getNewsListForSearch(searchQuery, pageNum, this);
  }

  private void showToastMessage(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }

  @Override public void onSuccess(Response<SearchNewsResponse> response) {
    progressDialog.dismiss();
    //Log.d(logTag, "onSuccess");
    if (response.body() != null && response.body().getNbPages() != 0) {
      // TODO SAVE data for offline
      if (adapter != null) {
        // notify new data has been added.
        int currentHitsCount = hits.size();
        hits.addAll(response.body().getHits());
        adapter.notifyItemRangeInserted(currentHitsCount, response.body().getHits().size());
      } else {
        hits = response.body().getHits();
        adapter = new NewsListAdapter(this, hits, this);
        // set adapter of recycler
        recyclerViewNewsList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNewsList.setAdapter(adapter);
      }
    } else {
      showToastMessage(getString(R.string.noMoreSearchResults));
    }
  }

  @Override public void onError(Throwable t) {
    progressDialog.dismiss();
  }

  @Override public void onItemClick(String url) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    startActivity(intent);
  }
}
