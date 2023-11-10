package stellarnear.mystory.UITools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;

import com.bumptech.glide.Glide;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

// based on https://android-arsenal.com/details/1/5480

public class ListBookAdapter extends RecyclerView.Adapter<ListBookAdapter.ViewHolder> {

    private DiscreteScrollView scrollView;
    private List<Book> data;

    public ListBookAdapter(List<Book> data, DiscreteScrollView scrollView) {
        this.data = data;
        this.scrollView = scrollView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_book_card, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        byte[] imageByte = data.get(position).getImage();
        if(imageByte==null || imageByte.length<1){
            try {
            String file = "res/raw/no_image.png";
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
            int nRead;
            byte[] data = new byte[16384];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            imageByte = buffer.toByteArray();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.smoothScrollToPosition(position);
            }
        });

        Glide.with(holder.itemView.getContext())
                .load(imageByte)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public Book getBook(int adapterPosition) {
        return data.get(adapterPosition);
    }

    public List<Book> getData() {
        return this.data;
    }

    public void reset() {
        data=new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);

        }
    }
}