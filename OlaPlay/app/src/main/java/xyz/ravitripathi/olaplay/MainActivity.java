package xyz.ravitripathi.olaplay;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;

import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.wang.avi.AVLoadingIndicatorView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements UrlChangeListener {

    Context c;
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    AVLoadingIndicatorView avLoadingIndicatorView;
    SwipeRefreshLayout swipeRefreshLayout;
    SimpleExoPlayer player;
    DefaultBandwidthMeter bandwidthMeter;
    ExtractorsFactory extractorsFactory;
    DataSource.Factory dataSourceFactory;
    LinearLayout bar;
    RelativeLayout parent;
    TextView nowSong, artist;
    ImageView albumArt;
    Button play;
    int backgroundcolor=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.parent);
        bar = findViewById(R.id.bar);
        recyclerView = findViewById(R.id.recycle);
        avLoadingIndicatorView = findViewById(R.id.load);
        nowSong = findViewById(R.id.textNowPlaying);
        artist = findViewById(R.id.artist);
        albumArt = findViewById(R.id.imageViewAlbumArt);
        play = findViewById(R.id.btnPlay);

        c = this;

        swipeRefreshLayout = findViewById(R.id.swipe);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(c, 1));
            if(backgroundcolor!=0)
                parent.setBackgroundColor(backgroundcolor);
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(c, 2));
            if(backgroundcolor!=0)
                parent.setBackgroundColor(backgroundcolor);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                launch_call();
            }

        });

        initPlayer();
        launch_call();

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                if (player != null) {
                    boolean stat = player.getPlayWhenReady();
                    player.setPlayWhenReady(!stat);
                    play.setBackgroundResource(!stat ? R.drawable.pause : R.drawable.ic_play);
                }

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (player != null) {
                    boolean stat = player.getPlayWhenReady();
                    player.setPlayWhenReady(!stat);
                    play.setBackgroundResource(!stat ? R.drawable.pause : R.drawable.ic_play);
                }

            }
        });

    }



    private void initPlayer() {

        bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);


        dataSourceFactory = createDataSourceFactory(c, Util.getUserAgent(c, "OlaPlay"), bandwidthMeter);
    }


    public void updateUrl(ResponsePOJO data, int i) {
        bar.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse(data.getUrl());

        if(i!=0){
            parent.setBackgroundColor(i);
            backgroundcolor = i;
        }
        if (uri != null && dataSourceFactory != null && extractorsFactory != null) {
            MediaSource songSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
            player.prepare(songSource);
            player.setPlayWhenReady(true);
            updateUI(data.getSong(), data.getArtists(), data.getCoverImage());
        }
    }

    public void updateUI(String name, String art, String url) {
        nowSong.setText(name);
        artist.setText(art);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_music_note_black_24dp)
                .priority(Priority.HIGH);


        Glide.with(this)
                .load(url)
                .apply(options)
                .into(albumArt);

        play.setVisibility(View.VISIBLE);
        play.setBackgroundResource(R.drawable.pause);
    }


    public void launch_call() {
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        bar.setVisibility(View.GONE);
        player.setPlayWhenReady(false);
        final RetrofitCall NI = RetrofitCall.retrofit.create(RetrofitCall.class);
        Call<List<ResponsePOJO>> call = NI.getSongs();
        call.enqueue(new Callback<List<ResponsePOJO>>() {
            @Override
            public void onResponse(Call<List<ResponsePOJO>> call, Response<List<ResponsePOJO>> response) {

                try {
                    Collections.sort(response.body(), new Comparator<ResponsePOJO>() {
                        public int compare(ResponsePOJO one, ResponsePOJO two) {
                            return one.getSong().compareTo(two.getSong());
                        }
                    });
                    adapter = new RecyclerAdapter(c, response.body(), getResources().getConfiguration().orientation, MainActivity.this);
                    recyclerView.setAdapter(adapter);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error in fetching song list", Toast.LENGTH_SHORT).show();
                }

                swipeRefreshLayout.setRefreshing(false);
                recyclerView.setVisibility(View.VISIBLE);
                avLoadingIndicatorView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<ResponsePOJO>> call, Throwable t) {
                Toast.makeText(c, "Failed", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                recyclerView.setVisibility(View.VISIBLE);
                avLoadingIndicatorView.setVisibility(View.GONE);
            }
        });
    }


    public static DefaultDataSourceFactory createDataSourceFactory(Context context,
                                                                   String userAgent, TransferListener<? super DataSource> listener) {
        // Default parameters, except allowCrossProtocolRedirects is true
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent,
                listener,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true /* allowCrossProtocolRedirects */
        );

        return new DefaultDataSourceFactory(
                context,
                listener,
                httpDataSourceFactory
        );

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initPlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initPlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

}
