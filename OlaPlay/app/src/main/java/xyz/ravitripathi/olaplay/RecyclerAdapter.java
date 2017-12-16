package xyz.ravitripathi.olaplay;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import xyz.ravitripathi.olaplay.MainActivity;

import java.util.List;


/**
 * Created by Ravi on 15-12-2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.VH> {
    List<ResponsePOJO> songList;
    public static Context context;
    static int config;
    static UrlChangeListener listener;
    static int prev;



    public RecyclerAdapter(Context context, List<ResponsePOJO> songList, int config, UrlChangeListener listener) {
        this.songList = songList;
        this.context = context;
        this.config = config;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        VH holder = new VH(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.setData(songList.get(position));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        int i=0;
        CardView cardView;
        TextView song, artist;
        ImageView image;

        public VH(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.list_item_card);
            song = itemView.findViewById(R.id.song);
            artist = itemView.findViewById(R.id.artist);
            image = itemView.findViewById(R.id.image);
        }

        public void setData(final ResponsePOJO data) {
            song.setText(data.getSong());
            artist.setText(data.getArtists());

            song.setSelected(true);
            artist.setSelected(true);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_music_note_black_24dp);

            Glide.with(context)
                    .load(data.getCoverImage())
                    .apply(options)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Bitmap icon = drawableToBitmap(resource);
                            Palette.generateAsync(icon, new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch vibrant = palette.getVibrantSwatch();
                                    if (vibrant != null) {
                                        setColor(vibrant.getRgb());
                                    }
                                }
                            });
                            return false;
                        }
                    })
                    .into(image);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (data.getUrl() != null)
                        listener.updateUrl(data,i);
                }
            });
        }

        public void setColor(int l){
            i = l;
        }


        public static Bitmap drawableToBitmap (Drawable drawable) {
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if(bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }

            if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }


    }
}
