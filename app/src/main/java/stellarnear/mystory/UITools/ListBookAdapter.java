package stellarnear.mystory.UITools;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;

// based on https://android-arsenal.com/details/1/5480

public class ListBookAdapter extends RecyclerView.Adapter<ListBookAdapter.ViewHolder> {

    private final DiscreteScrollView scrollView;
    private List<Book> data;
    private boolean small = false;

    private OnImageRefreshedEventListener mListner;

    public interface OnImageRefreshedEventListener {
        void onEvent(int pos);
    }

    public void setOnImageRefreshedEventListener(OnImageRefreshedEventListener listner) {
        this.mListner = listner;
    }

    public ListBookAdapter(List<Book> data, DiscreteScrollView scrollView, boolean... small) {
        this.data = data;
        this.scrollView = scrollView;
        if (small.length > 0 && small[0]) {
            this.small = true;
        }
    }

    public void setSmallViews() {
        this.small = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        if (small) {
            v = inflater.inflate(R.layout.item_book_card_small, parent, false);
        } else {
            v = inflater.inflate(R.layout.item_book_card, parent, false);
        }
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = data.get(position);
        if (book.getImage() == null || book.getImage().length < 7) {
            //on a pas réussi à avoir d'image on affiche l'image broken et on lance un retry pour une prochaine utilisation
            holder.image.setImageDrawable(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.no_image));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new BookNodeCalls().refreshImage(book);
                }
            });

            book.setOnImageRefreshedEventListener(new Book.OnImageRefreshedEventListener() {
                @Override
                public void onEvent() {
                    if (mListner != null) {
                        mListner.onEvent(holder.getAdapterPosition());
                    }
                }
            });
        } else {
            Drawable image = new BitmapDrawable(holder.itemView.getContext().getResources(), BitmapFactory.decodeByteArray(book.getImage(), 0, book.getImage().length));
            holder.image.setImageDrawable(image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.smoothScrollToPosition(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public Book getBook(int adapterPosition) {
        return data.get(adapterPosition);
    }

    public void reset() {
        data = new ArrayList<>();
    }

    public List<Book> getBooks() {
        return data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }

        public ImageView getImageView() {
            return image;
        }
    }
}