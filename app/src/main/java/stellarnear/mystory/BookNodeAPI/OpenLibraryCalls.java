package stellarnear.mystory.BookNodeAPI;

import android.os.AsyncTask;

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
import java.util.SortedSet;
import java.util.TreeSet;

import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.Log.CustomLog;


/*
THIS CLASS IS NOT USED ANYMORE DON'T CARE ABOUT AUTO FETCH PAGES

 */
public class OpenLibraryCalls {

    //LOOK AT https://openlibrary.org/developers/api

    private final transient CustomLog log = new CustomLog(OpenLibraryCalls.class);

    private static final String baseUrl = "https://openlibrary.org";
    private Book book;

    public void addExtraMetadatas(Book book) throws JSONException {
        this.book = book;
        new JsonTaskGetMetaData().execute();
    }

    private class JsonTaskGetMetaData extends AsyncTask<String, String, SortedSet<Integer>> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected SortedSet<Integer> doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            HttpURLConnection connectionData = null;
            BufferedReader readerData = null;
            try {
                URL url = new URL(baseUrl + "/search.json?title=" + book.getName().replace(" ", "+") + "&author=" + book.getAutor().getFullName().replace(" ", "+"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }


                JSONObject allResults = new JSONObject(buffer.toString());

                //books
                JSONArray booksJsonArray = (JSONArray) allResults.get("docs");
                SortedSet<Integer> maxPagesFounds = new TreeSet<>();
                for (int i = 0; i < booksJsonArray.length(); i++) {
                    JSONObject booksJson = booksJsonArray.getJSONObject(i);
                    for (int j = 0; j < booksJson.getJSONArray("seed").length(); j++) {
                        String value = booksJson.getJSONArray("seed").getString(j);
                        if (value.startsWith("/books/")) {
                            Integer maxPageFound = null;
                            URL urlGetData = new URL(baseUrl + value + ".json");
                            connectionData = (HttpURLConnection) urlGetData.openConnection();
                            connectionData.setConnectTimeout(15000);
                            connectionData.connect();

                            InputStream streamData = connectionData.getInputStream();

                            readerData = new BufferedReader(new InputStreamReader(streamData));

                            StringBuffer bufferData = new StringBuffer();
                            String lineData = "";

                            while ((lineData = readerData.readLine()) != null) {
                                bufferData.append(lineData + "\n");
                                //Log.d("ResponseData: ", "> " + lineData);   //here u ll get whole response...... :-)
                            }

                            //dothing with meta on book then call listner

                            JSONObject bookJsonData = new JSONObject(bufferData.toString());

                            if (bookJsonData.has("pagination")) {
                                maxPageFound = Integer.parseInt(bookJsonData.getString("pagination").replaceAll("[^\\d]", ""));
                            } else if (bookJsonData.has("number_of_pages")) {
                                maxPageFound = Integer.parseInt(bookJsonData.getString("number_of_pages").replaceAll("[^\\d]", ""));
                            }
                            if (maxPageFound != null) {
                                maxPagesFounds.add(maxPageFound);
                            }
                        }
                    }
                }
                return maxPagesFounds;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                log.err("Error in jsonTaskGetMetaData background", e);
            } catch (Exception e) {
                e.printStackTrace();
                log.err("Error in jsonTaskGetMetaData background", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (connectionData != null) {
                    connectionData.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (readerData != null) {
                        readerData.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.err("Error in jsonTaskGetMetaData background", e);
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(SortedSet<Integer> result) {
            super.onPostExecute(result);

            try {
                if (result != null && result.size() > 0) {
                    book.addMultipleMaxPagesFound(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.err("Error in jsonTaskGetMetaData on post", e);
            }
        }


    }

}
