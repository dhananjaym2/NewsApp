package sample.newsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
  private int pageIndex;
  private boolean alreadyLoadingNextPage = false;
  private boolean isInErrorState = false;

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
          if (adapter != null) {
            if (hits != null) {
              hits.clear();
            }
            adapter.notifyItemRangeRemoved(0, 0);
          }
          getNewsListForSearch(editTextSearchFromUser.getText().toString().trim(), 0);
        } else {
          showToastMessage(getString(R.string.emptySearch));
        }
      } else {
        showToastMessage(getString(R.string.internetNotConnected));
      }
    }
  }

  private void getNewsListForSearch(String searchQuery, int pageNum) {
    showProgressDialog();
    // start API call on NON main thread
    ApiController apiController = new ApiController();
    pageIndex = pageNum;
    alreadyLoadingNextPage = true;
    isInErrorState = false;
    apiController.getNewsListForSearch(searchQuery, pageNum, this);
  }

  @Override public void onSuccess(final Response<SearchNewsResponse> response) {
    dismissProgressDialog();
    if (response.body() != null && response.body().getNbPages() != 0) {
      // TODO SAVE data for offline
      if (adapter != null) {
        // notify new data has been added.
        int currentHitsCount = hits.size();
        if (response.body() != null && response.body().getHits() != null) {
          hits.addAll(response.body().getHits());
          adapter.notifyItemRangeInserted(currentHitsCount, response.body().getHits().size());
        }
      } else {
        hits = response.body().getHits();
        adapter = new NewsListAdapter(this, hits, this);
        // set adapter of recycler
        recyclerViewNewsList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNewsList.setAdapter(adapter);
        recyclerViewNewsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
          /**
           * Callback method to be invoked when the RecyclerView has been
           * scrolled. This will be
           * called after the scroll has completed.
           * <p>
           * This callback will also be called if visible item range changes after
           * a layout
           * calculation. In that case, dx and dy will be 0.
           *
           * @param recyclerView The RecyclerView which scrolled.
           * @param dx The amount of horizontal scroll.
           * @param dy The amount of vertical scroll.
           */
          @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!hasMoreContent()) {
              return;
            }
            if (currentlyLoadingInitialRequest()) {
              return;
            }
            if (alreadyLoadingNextPage()) {
              return;
            }
            if (isInErrorState()) {
              return;
            }
            LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
            if (linearLayoutManager != null) {
              int total = linearLayoutManager.getItemCount();
              int currentLastItem = linearLayoutManager.findLastVisibleItemPosition();
              if (currentLastItem == total - 1) {
                //requestNextPage
                getNewsListForSearch(editTextSearchFromUser.getText().toString().trim(),
                    ++pageIndex);
              }
            }
          }

          private boolean alreadyLoadingNextPage() {
            return alreadyLoadingNextPage;
          }

          private boolean isInErrorState() {
            return isInErrorState;
          }

          private boolean currentlyLoadingInitialRequest() {
            return (pageIndex == 0 && (hits != null && hits.size() == 0));
          }

          private boolean hasMoreContent() {
            return response.body().getNbPages() != 0;
          }
        });
      }
    } else {
      showToastMessage(getString(R.string.noMoreSearchResults));
    }
    isInErrorState = false;
    alreadyLoadingNextPage = false;
  }

  @Override public void onError(Throwable t) {
    dismissProgressDialog();
    alreadyLoadingNextPage = false;
    isInErrorState = true;
    showToastMessage(getString(R.string.somethingWentWrong));
  }

  @Override public void onItemClick(String url) {
    if (url == null) {
      showToastMessage(getString(R.string.urlNotAvailable));
      return;
    }

    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    List<ResolveInfo> intentResolveList = getPackageManager().queryIntentActivities(intent, 0);
    if (intentResolveList.size() > 0) {
      startActivity(intent);
    } else {
      showToastMessage(getString(R.string.unableToGetResolvingApps));
    }
  }

  private void showProgressDialog() {
    progressDialog = new ProgressDialog(this);
    progressDialog.setMessage(getString(R.string.loading));
    progressDialog.setCancelable(false);
    progressDialog.show();
  }

  private void dismissProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  private void showToastMessage(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }
}