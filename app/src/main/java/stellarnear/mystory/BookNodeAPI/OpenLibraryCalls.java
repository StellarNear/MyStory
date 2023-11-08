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
import java.util.HashSet;
import java.util.Set;

import stellarnear.mystory.BooksLibs.Book;

public class OpenLibraryCalls {

    //LOOK AT https://openlibrary.org/developers/api

    private static String baseUrl = "https://openlibrary.org";
    private Book book;

    public void addExtraMetadatas(Book book) throws JSONException {
        this.book = book;
        new JsonTaskGetMetaData().execute();
        return;
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


    private class JsonTaskGetMetaData extends AsyncTask<String, String, Set<Integer>> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Set<Integer> doInBackground(String... params) {

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
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }


                JSONObject allResults = new JSONObject(buffer.toString());

                //books
                JSONArray booksJsonArray = (JSONArray) allResults.get("docs");
                Set<Integer> maxPagesFounds = new HashSet<>();
                for(int i=0 ;i<booksJsonArray.length();i++) {
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
                                Log.d("ResponseData: ", "> " + lineData);   //here u ll get whole response...... :-)
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
            } catch (Exception e) {
                e.printStackTrace();
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
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Set<Integer> result) {
            super.onPostExecute(result);

            try {
                if (result.size() > 0) {
                    book.addMultipleMaxPagesFound(result);
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
