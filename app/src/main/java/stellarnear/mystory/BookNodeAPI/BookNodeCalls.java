package stellarnear.mystory.BookNodeAPI;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.BooksLibs.Autor;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.Serie;

public class BookNodeCalls {
    private static String baseUrl = "https://booknode.com/";

    public List<Book> getBooksFromSearch(String search) throws JSONException {

        new JsonTask().execute(baseUrl + "search-json?q=" + search.replace(" ", "+") + "&exclude_series_from_books=0");


        return null;

    }

    private OnDataRecievedEventListener mListener;
    private OnDataFailEventListener mListenerFail;

    public void setOnDataRecievedEventListener(OnDataRecievedEventListener eventListener) {
        mListener = eventListener;
    }

    public interface OnDataRecievedEventListener {
        void onEvent();
    }

    public void setOnDataFailEventListener(OnDataFailEventListener eventListener) {
        mListenerFail = eventListener;
    }

    public interface OnDataFailEventListener {
        void onEvent();
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        private ArrayList<Book> allBooks;
        private ArrayList<Autor> autors;
        private ArrayList<Serie> series;

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                JSONObject allResults = new JSONObject(result);

                //books
                this.allBooks = new ArrayList<>();
                JSONArray booksJson = (JSONArray) allResults.get("books");
                for(int i=0; i<booksJson.length();i++){
                    JSONObject bookJson = booksJson.getJSONObject(i);
                    JSONObject autorJson = bookJson.getJSONArray("authors").getJSONObject(0);
                    Autor autor = new Autor(autorJson.getLong("idauteur"),autorJson.getString("_prenom"),autorJson.getString("_nom"),autorJson.getString("nom"));
                    Book book = new Book(bookJson.getLong("id"),bookJson.getString("name"),bookJson.getString("cover_double"),autor);
                    allBooks.add(book);
                }


                //autors
                this.autors= new ArrayList<>();
                JSONArray autorsJson = (JSONArray) allResults.get("authors");
                for(int i=0; i<autorsJson.length();i++){
                    JSONObject autorJson = autorsJson.getJSONObject(i);
                    Autor autor = new Autor(autorJson.getLong("id"),autorJson.getString("firstname"),autorJson.getString("lastname"),autorJson.getString("name"));
                    autors.add(autor);
                }


                //series
                this.series= new ArrayList<>();
                JSONArray seriesJson = (JSONArray) allResults.get("series");
                for(int i=0; i<seriesJson.length();i++){
                    JSONObject serieJson = seriesJson.getJSONObject(i);
                    Serie serie = new Serie(serieJson.getLong("id"),serieJson.getString("name"),serieJson.getInt("book_count"));
                    series.add(serie);
                }


                if (mListener != null) {
                    mListener.onEvent();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                if (mListenerFail != null) {
                    mListenerFail.onEvent();
                }
            }
        }


    }

}
