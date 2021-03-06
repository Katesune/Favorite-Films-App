package com.example.favoritefilmsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AllMovieActivity extends AppCompatActivity {


    LinearLayout ln;

    Gson gson = new GsonBuilder().create();
    String api_key = "2afab14a5f39728f8f613d627e5dd9bb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_movie);

        ln = (LinearLayout) findViewById(R.id.all_movies_lin);

        String api_key = "2afab14a5f39728f8f613d627e5dd9bb";

        //new getImagesTask().execute();

        Call<Movies> getFilmCall = getFilmsCall();

        Callback<Movies> filmcallback = new Callback<Movies>() {
            @Override
            public void onResponse(Call<Movies> call, retrofit2.Response<Movies> response) {

                //Log.d("mytag","response.raw().request().url();"+response.raw().request().url());
                Movies movies = response.body();

                if (movies!=null) {
                    setPopularFilms(movies);
                    //Log.d("mytag", movies.results[0].backdrop_path);
                    //Log.d("mytag", String.valueOf(movies.results[0].id));
                    //showResult(r);
                } //else error.setText("Results Not Found");
            }

            @Override
            public void onFailure(Call<Movies> call, Throwable t) {
                Log.d("mytag", "fail:" + t.getLocalizedMessage());
            }
        };

        getFilmCall.enqueue(filmcallback);

    }

    public void addNewItem(int id, int category) {
        UserMoviesDB db = UserMoviesDB.create(this, false);
        UsersDB users_db;

        new Thread() {
            @Override
            public void run() {
                int film_id = db.movie_manager().getFilmId(id);
                if (film_id==0) {
                    int count = db.movie_manager().getNumberOfRows() + 1;
                    db.movie_manager().insert(new MovieList(count, id, 1, category, "06.06.2021"));
                    Log.d("mytag", "insert");
                } else {
                    db.movie_manager().delete(new MovieList(film_id, id, 1, category, "06.06.2021"));
                    Log.d("mytag", "delete");
                }
            }
        }.start();
    }

    public void addToFav(View v) {
        Button butt = (Button)v;
        String str_id = butt.getText().toString();
        int id = Integer.parseInt(str_id);
        addNewItem(id, 1);
    }

    public void goToFilm(View v) {
        Intent intent = new Intent(AllMovieActivity.this, MovieActivity.class);
        TextView butt_v = (TextView)v;
        String movie_id = butt_v.getText().toString();
        intent.putExtra("movie_id", Integer.parseInt(movie_id));
        startActivity(intent);
    }

    public Call<Movies> getFilmsCall () {
        String url = "https://api.themoviedb.org/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        //1043758
        Manager api = retrofit.create(Manager.class);

        Call <Movies> call_film = api.getAll(api_key, "ru", "popularity.desc");
        return call_film;
    }

    public void setPopularFilms(Movies movies) {
        LayoutInflater ltInflater = getLayoutInflater();

        for (Movie m : movies.results) {
            String url = "https://www.themoviedb.org/t/p/w1280/";
            View new_movie = ltInflater.inflate(R.layout.popular_movie, ln, false);

            TextView title = new_movie.findViewById(R.id.title);
            TextView original_title = new_movie.findViewById(R.id.original_title);
            TextView release_date = new_movie.findViewById(R.id.release_date);
            TextView popularity = new_movie.findViewById(R.id.popularity);
            TextView overview = new_movie.findViewById(R.id.overview);

            ImageView poster = new_movie.findViewById(R.id.poster);

            TextView id = new_movie.findViewById(R.id.id);
            id.setText(Integer.toString(m.id));

            Button fav = new_movie.findViewById(R.id.simpleButton1);
            fav.setText(Integer.toString(m.id));

            title.setText("????????????????: " + m.title);
            original_title.setText("???????????? ????????????????: " + m.original_title);
            release_date.setText("???????? ????????????: " + m.release_date);
            overview.setText("????????????????: " + m.overview);
            popularity.setText("????????????????????????: " + String.valueOf(m.popularity));

            if (m.backdrop_path != null) {
                Uri uri_pic = Uri.parse(url + m.backdrop_path);
                Picasso p = new Picasso.Builder(getApplicationContext()).build();
                p.load(uri_pic).into(poster);
            }

            if (m.poster_path != null) {
                Uri uri_pic = Uri.parse(url + m.poster_path);
                Picasso p = new Picasso.Builder(getApplicationContext()).build();
                p.load(uri_pic).into(poster);
            }

            ln.addView(new_movie);
        }

    }


        public class getImagesTask extends AsyncTask<Void , Void, Movies> {

            @Override
            protected Movies doInBackground(Void... voids) {
                Call<Movies> getFilmCall = getFilmsCall();

                return getImageTask(getFilmCall);
            }

            @Override
            protected void onPostExecute(Movies result) {
                setPopularFilms(result);
            }

            private Movies getImageTask(Call<Movies> getFilmCall) {
                try {
                retrofit2.Response<Movies> response =  getFilmCall.execute();
                    return response.body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }


}