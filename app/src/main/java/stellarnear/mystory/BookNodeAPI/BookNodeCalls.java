package stellarnear.mystory.BookNodeAPI;

import android.os.AsyncTask;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import stellarnear.mystory.BooksLibs.Autor;
import stellarnear.mystory.BooksLibs.Book;

public class BookNodeCalls {


    private static final String baseUrl = "https://booknode.com/";

    public void getBooksFromSearch(String search) throws JSONException {
        new JsonTaskGetBook().execute(baseUrl + "search-json?q=" + search.replace(" ", "+") + "&exclude_series_from_books=0");
    }

    private OnDataRecievedEventListener mListener;
    private OnDataFailEventListener mListenerFail;

    public void setOnDataRecievedEventListener(OnDataRecievedEventListener eventListener) {
        mListener = eventListener;
    }

    public interface OnDataRecievedEventListener {
        void onEvent(List<Book> allBooks);
    }

    public void setOnDataFailEventListener(OnDataFailEventListener eventListener) {
        mListenerFail = eventListener;
    }

    public interface OnDataFailEventListener {
        void onEvent();
    }

    public void refreshImage(Book book) {
        new JsonTaskRefreshImage().execute(book);
    }


    public class JsonTaskGetBook extends AsyncTask<String, String, List<Book>> {


        //private Set<Autor> autors;
        // private Set<Serie> series;

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected List<Book> doInBackground(String... params) {

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
                    // Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }


                JSONObject allResults = new JSONObject(buffer.toString());

                //books
                List<Book> allBooks = new ArrayList<>();
                JSONArray booksJson = (JSONArray) allResults.get("books");
                for (int i = 0; i < booksJson.length(); i++) {
                    JSONObject bookJson = booksJson.getJSONObject(i);
                    JSONObject autorJson = bookJson.getJSONArray("authors").getJSONObject(0);
                    Autor autor = new Autor(autorJson.getLong("idauteur"), autorJson.getString("_prenom"), autorJson.getString("_nom"), autorJson.getString("nom"));
                    Book book = new Book(bookJson.getLong("id"), bookJson.getString("name"), bookJson.getString("cover_double"), autor);
                    getImage(book);
                    if (bookJson.has("href") && bookJson.getString("href") != null) {
                        book.setHref(bookJson.getString("href"));
                    }
                    if (i < 3) {
                        getSummary(book);
                    }
                    new OpenLibraryCalls().addExtraMetadatas(book);
                    allBooks.add(book);
                }


                /*
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
                }*/


                return allBooks;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
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

        public void getImage(Book book) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream stream = null;
            try {
                byte[] chunk = new byte[16384];
                int bytesRead;
                stream = new URL(book.getCover_url()).openStream();
                while ((bytesRead = stream.read(chunk)) > 0) {
                    outputStream.write(chunk, 0, bytesRead);
                }
                if (outputStream.toByteArray().length > 0) {
                    book.setImageByte(outputStream.toByteArray());
                }
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected void onPostExecute(List<Book> allBooksFound) {
            super.onPostExecute(allBooksFound);

            try {
                if (mListener != null) {
                    mListener.onEvent(allBooksFound);
                }
                if (allBooksFound.size() > 3) {
                    //le premier livre a été fait en forcé
                    for (int i = 3; i < allBooksFound.size(); i++) {
                        new JsonTaskSummary().execute(allBooksFound.get(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mListenerFail != null) {
                    mListenerFail.onEvent();
                }
            }
        }


    }


    private class JsonTaskSummary extends AsyncTask<Book, String, Void> {
        private Book book;

        //private Set<Autor> autors;
        // private Set<Serie> series;

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Book... books) {
            this.book = books[0];
            getSummary(book);
            return null;
        }
    }

    private void getSummary(Book book) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream stream = null;
        try {
            byte[] chunk = new byte[16384];
            int bytesRead;
            stream = new URL(book.getHref()).openStream();
            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }

            TagNode tagNode = new HtmlCleaner().clean(outputStream.toString());
            org.w3c.dom.Document doc = new DomSerializer(
                    new CleanerProperties()).createDOM(tagNode);

            XPath xpath = XPathFactory.newInstance().newXPath();

            XPathExpression expr = xpath.compile("//span[descendant::span[@class='resume-title']]/p");
            NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            String summary = "";
            if (list.getLength() > 1) {
                for (int i = 1; i < list.getLength(); i++) {
                    try {
                        summary += list.item(i).getTextContent() + "\n";
                    } catch (Exception e) {
                        //skip paragraph
                    }
                }
                if (summary != null && summary.trim().length() > 1) {
                    book.setSummary(summary.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class JsonTaskRefreshImage extends AsyncTask<Book, String, byte[]> {
        private Book book;

        //private Set<Autor> autors;
        // private Set<Serie> series;

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected byte[] doInBackground(Book... books) {
            this.book = books[0];
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                InputStream stream = null;
                try {
                    byte[] chunk = new byte[16384];
                    int bytesRead;
                    stream = new URL(book.getCover_url()).openStream();
                    while ((bytesRead = stream.read(chunk)) > 0) {
                        outputStream.write(chunk, 0, bytesRead);
                    }
                    return outputStream.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] byteArray) {
            super.onPostExecute(byteArray);
            if (byteArray != null && byteArray.length > 1) {
                book.setImageByte(byteArray);
            }
        }

    }
}
