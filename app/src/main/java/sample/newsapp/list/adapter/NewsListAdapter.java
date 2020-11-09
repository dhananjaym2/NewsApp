package sample.newsapp.list.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import sample.newsapp.NewsListActivity;
import sample.newsapp.R;
import sample.newsapp.network.response_models.Hit;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsItemViewHolder> {

  private List<Hit> hits;
  private  NewsItemClick newsItemClick;

  public NewsListAdapter(NewsListActivity newsListActivity, List<Hit> hits, NewsItemClick newsItemClick) {
    this.hits = hits;
    this.newsItemClick = newsItemClick;
  }

  @NonNull @Override public NewsItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item,
        parent, false);
    return new NewsItemViewHolder(itemView);
  }

  @Override public void onBindViewHolder(@NonNull NewsItemViewHolder holder, final int position) {

    holder.newsItemParent.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        // open next page with URL
        if (newsItemClick != null) {
          newsItemClick.onItemClick(hits.get(position).getUrl());
        }
      }
    });
    holder.newsItemTitle.setText(hits.get(position).getTitle());
  }

  /**
   * Returns the total number of items in the data set held by the adapter.
   *
   * @return The total number of items in this adapter.
   */
  @Override public int getItemCount() {
    if (hits == null) {
      return 0;
    } else {
      return hits.size();
    }
  }

  static class NewsItemViewHolder extends RecyclerView.ViewHolder {
    ConstraintLayout newsItemParent;
    TextView newsItemTitle;

    public NewsItemViewHolder(View itemView) {
      super(itemView);
      newsItemParent = itemView.findViewById(R.id.newsItemParent);
      newsItemTitle = itemView.findViewById(R.id.newsItemTitle);
    }
  }
}
