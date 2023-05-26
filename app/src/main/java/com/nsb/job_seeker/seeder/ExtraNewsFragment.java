package com.nsb.job_seeker.seeder;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.nsb.job_seeker.R;
import com.nsb.job_seeker.adapter.FeedAdapter;
import com.nsb.job_seeker.common.AsyncTasks;
import com.nsb.job_seeker.model.RssObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtraNewsFragment extends Fragment {
    private View extraNewsView;
    private ListView lvJob;
    private ProgressBar pbLoading;
    private Toolbar toolbar;
    private RssObject rssObject;
    private String data = null;
    private FloatingActionButton fabMenu;
    private TextView tvAll, tvHCM, tvHN, tvContent;
    private List<String> listRSSLocationLinks, listRSSFieldLinks;
    private String[] listLocations, listFields;
    private AlertDialog.Builder builder;
    private int defaultPositionRadio = 0;
    private final String Rss_to_Json_API = "https://api.rss2json.com/v1/api.json?rss_url=";
    private String Rss_Link;
    private SwipeRefreshLayout refreshLayout;
    private boolean isLocationActionBar = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        extraNewsView = inflater.inflate(R.layout.fragment_seeker_extra_news, container, false);
        setHasOptionsMenu(true);

        setControl();
        setEvent();

        return extraNewsView;
    }

    private void setControl() {
        toolbar = extraNewsView.findViewById(R.id.toolbar);
        lvJob = extraNewsView.findViewById(R.id.lv_job);
        fabMenu = extraNewsView.findViewById(R.id.fab_menu);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.menu, null);
        tvAll = dialogView.findViewById(R.id.tvAll);
        tvHCM = dialogView.findViewById(R.id.tvHCM);
        tvHN = dialogView.findViewById(R.id.tvHN);
        refreshLayout = extraNewsView.findViewById(R.id.layout_refresh);
        tvContent = extraNewsView.findViewById(R.id.tv_content);

        listRSSLocationLinks = new ArrayList<>(Arrays.asList(
                "https://www.vinawork.com/tim-viec/rss/tinhthanh/73/toan-quoc.feed",
                "https://www.vinawork.com/tim-viec/rss/tinhthanh/2/ho-chi-minh.feed",
                "https://www.vinawork.com/tim-viec/rss/tinhthanh/1/ha-noi.feed",
                "https://www.vinawork.com/tim-viec/rss/tinhthanh/4/da-nang.feed",
                "https://www.vinawork.com/tim-viec/rss/tinhthanh/5/can-tho.feed",
                "https://www.vinawork.com/tim-viec/rss/tinhthanh/71/nuoc-ngoai.feed"));

        listRSSFieldLinks = new ArrayList<>(Arrays.asList(
                "https://www.vinawork.com/tim-viec/rss.html",
                "https://www.vinawork.com/tim-viec/rss/cong-nghe-thong-tin.feed",
                "https://www.vinawork.com/tim-viec/rss/ban-hang.feed",
                "https://www.vinawork.com/tim-viec/rss/xay-dung.feed",
                "https://www.vinawork.com/tim-viec/rss/ngan-hang.feed",
                "https://www.vinawork.com/tim-viec/rss/bao-chi,-bien-tap-vien.feed",
                "https://www.vinawork.com/tim-viec/rss/bat-dong-san.feed",
                "https://www.vinawork.com/tim-viec/rss/du-lich.feed"));

        listLocations = new String[]{"Toàn quốc", "Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Cần Thơ", "Nước ngoài"};
        listFields = new String[]{"Tất cả", "Công nghệ thông tin", "Kinh doanh", "Xây dựng", "Ngân hàng", "Báo chí", "Bất động sản", "Du lịch"};

        Rss_Link = listRSSLocationLinks.get(0);

        pbLoading = (ProgressBar) extraNewsView.findViewById(R.id.pb_loading);
    }

    private void setEvent() {
        toolbar.setTitle("Tin hot");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        refreshNewsJob();
        pbLoading.setVisibility(View.VISIBLE);
        loadRSS();
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationActionBar) {
                    openDialog("Tùy chọn khu vực", listLocations, listRSSLocationLinks);
                } else {
                    openDialog("Tùy chọn lĩnh vực", listFields, listRSSFieldLinks);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_extra_job, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        defaultPositionRadio = 0;
        switch (item.getItemId()) {
            case R.id.action_location:
                isLocationActionBar = true;
                Rss_Link = listRSSLocationLinks.get(defaultPositionRadio);
                tvContent.setText(listLocations[defaultPositionRadio]);
                break;
            case R.id.action_field:
                Rss_Link = listRSSFieldLinks.get(defaultPositionRadio);
                isLocationActionBar = false;
                tvContent.setText(listFields[defaultPositionRadio]);
        }
        pbLoading.setVisibility(View.VISIBLE);
        refreshLayout.setVisibility(View.GONE);
        loadRSS();
        return true;
    }

    private void refreshNewsJob() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadRSS();
            }
        });
    }

    private void openDialog(String title, String[] listTitle, List<String> RSSLink) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(title);

        builder.setSingleChoiceItems(listTitle, defaultPositionRadio, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                defaultPositionRadio = which;
                Rss_Link = RSSLink.get(which);
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(ExtraNewsActivity.this, Rss_Link, Toast.LENGTH_SHORT).show();
                pbLoading.setVisibility(View.VISIBLE);
                refreshLayout.setVisibility(View.GONE);
                tvContent.setText(listTitle[defaultPositionRadio]);
                loadRSS();

            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void loadRSS() {
        new AsyncTasks() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                StringBuilder url_get_data = new StringBuilder(Rss_to_Json_API);
                url_get_data.append(Rss_Link);
//                data = null;
                URL url = null;
                try {
                    url = new URL(Rss_to_Json_API + Rss_Link);
                    Log.d("ABC", "data: " + Rss_to_Json_API + Rss_Link);

                    HttpURLConnection urlConnection = null;
                    urlConnection = (HttpURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = r.readLine()) != null) {
                            sb.append(line);
                            data = sb.toString();
                        }
                        urlConnection.disconnect();
                        in.close();
                        r.close();
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onPostExecute() {
                rssObject = new Gson().fromJson(data, RssObject.class);
                Log.d("ABC", "data: " + data);
                Log.d("ABC", "rss link " + Rss_Link);
                pbLoading.setVisibility(View.GONE);
                refreshLayout.setRefreshing(false);
                refreshLayout.setVisibility(View.VISIBLE);
                setFeedAdapter();
            }
        }.execute();
    }

    private void setFeedAdapter() {
        if (getActivity() != null) {
            FeedAdapter adapter = new FeedAdapter(getActivity(), R.layout.item_listview_news, rssObject.getItems());
            lvJob.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            Log.d("ABC", rssObject.getItems().get(0).toString());

            lvJob.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(rssObject.getItems().get(position).getLink()));
                    startActivity(i);
                }
            });
        }
    }
}
