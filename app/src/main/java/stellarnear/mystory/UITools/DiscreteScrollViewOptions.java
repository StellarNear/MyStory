package stellarnear.mystory.UITools;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;

public class DiscreteScrollViewOptions {
   private static DiscreteScrollViewOptions instance;
   public static void init() {
      instance = new DiscreteScrollViewOptions();
   }

   private DiscreteScrollViewOptions() {
   }

   public static void smoothScrollToUserSelectedPosition(final DiscreteScrollView scrollView, View anchor) {
      PopupMenu popupMenu = new PopupMenu(scrollView.getContext(), anchor);
      Menu menu = popupMenu.getMenu();
      final RecyclerView.Adapter<?> adapter = scrollView.getAdapter();
      int itemCount = (adapter instanceof InfiniteScrollAdapter) ?
              ((InfiniteScrollAdapter<?>) adapter).getRealItemCount() :
              (adapter != null ? adapter.getItemCount() : 0);
      for (int i = 0; i < itemCount; i++) {
         menu.add(String.valueOf(i + 1));
      }
      popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            int destination = Integer.parseInt(String.valueOf(item.getTitle())) - 1;
            if (adapter instanceof InfiniteScrollAdapter) {
               destination = ((InfiniteScrollAdapter<?>) adapter).getClosestPosition(destination);
            }
            scrollView.smoothScrollToPosition(destination);
            return true;
         }
      });
      popupMenu.show();
   }

}
