package stellarnear.mystory.UITools;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;

// based on https://android-arsenal.com/details/1/5480

public class ListBookAdapter extends RecyclerView.Adapter<ListBookAdapter.ViewHolder> {

    private DiscreteScrollView scrollView;
    private List<Book> data;
    private boolean small=false;

    public ListBookAdapter(List<Book> data, DiscreteScrollView scrollView,boolean... small) {
        this.data = data;
        this.scrollView = scrollView;
        if(small.length>0 && small[0]){
            this.small=true;
        }
    }

    public void setSmallViews(){
        this.small=true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        if(small){
            v = inflater.inflate(R.layout.item_book_card_small, parent, false);
        } else {
            v= inflater.inflate(R.layout.item_book_card, parent, false);
        }

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = data.get(position);
        byte[] imageByte = book.getImage();
        if (imageByte == null || imageByte.length < 1) {
            //on a pas réussi à avoir d'image on affiche l'image broken et on lance un retry pour une prochaine utilisation
            try {
                String file = "res/raw/no_image.png";
                InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
                int nRead;
                byte[] dataBytes = new byte[16384];
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((nRead = in.read(dataBytes, 0, dataBytes.length)) != -1) {
                    buffer.write(dataBytes, 0, nRead);
                }
                imageByte = buffer.toByteArray();

                new BookNodeCalls().refreshImage(book);
                book.setOnImageRefreshedEventListener(new Book.OnImageRefreshedEventListener() {
                    @Override
                    public void onEvent() {
                        onBindViewHolder(holder,position);
                    }
                });
            } catch (Exception e) {
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
        data = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);

        }
    }
}