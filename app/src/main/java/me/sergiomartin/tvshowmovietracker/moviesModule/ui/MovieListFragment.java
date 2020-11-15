package me.sergiomartin.tvshowmovietracker.moviesModule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.sergiomartin.tvshowmovietracker.R;
import me.sergiomartin.tvshowmovietracker.common.model.dataAccess.TMDbRepositoryAPI;
import me.sergiomartin.tvshowmovietracker.common.utils.Constants;
import me.sergiomartin.tvshowmovietracker.moviesModule.adapter.MoviesAdapter;
import me.sergiomartin.tvshowmovietracker.moviesModule.model.Genre;
import me.sergiomartin.tvshowmovietracker.moviesModule.model.Movie;
import me.sergiomartin.tvshowmovietracker.moviesModule.model.dataAccess.action.OnMoviesClickCallback;
import me.sergiomartin.tvshowmovietracker.moviesModule.model.dataAccess.get.OnGetGenresCallback;
import me.sergiomartin.tvshowmovietracker.moviesModule.model.dataAccess.get.OnGetMoviesCallback;

public class MovieListFragment extends Fragment {

    @BindView(R.id.srl_fragment_movie_list)
    SwipeRefreshLayout srlFragmentMovieList;
    @BindView(R.id.rv_fragment_movie_list)
    RecyclerView rvFragmentMovieList;

    private MoviesAdapter adapter;
    private TMDbRepositoryAPI mTMDbRepositoryAPI;

    private List<Genre> movieGenres;
    private String sortBy = Constants.POPULAR;

    /**
     * Determina si está cerca la siguiente página de la API.
     * Se utiliza para evitar duplicidad y mostrar siempre
     * las mismas películas al hacer scroll
     */
    private boolean isFetchingMovies;
    /**
     * Mediante esta variable indicamos en qué página inicializa
     * el listado extraido de la API. Cada vez que se haga scroll al 50%
     * del listado de películas, se incrementará +1
     */
    private int currentPage = 1;

    public MovieListFragment() {
        // Required empty public constructor
    }

    public static MovieListFragment newInstance() {
        return new MovieListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTMDbRepositoryAPI = TMDbRepositoryAPI.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);

        // Recoger parámetros enviados por el fragment anterior
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            sortBy = bundle.getString("movieFilter");
        }

        ButterKnife.bind(this, view);

        rvFragmentMovieList = view.findViewById(R.id.rv_fragment_movie_list);
        rvFragmentMovieList.setHasFixedSize(true);

        rvFragmentMovieList.setLayoutManager(new LinearLayoutManager(getContext()));

        initRecyclerViewAndScrolling(sortBy);

        getGenres(sortBy);

        srlFragmentMovieList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initRecyclerViewAndScrolling(sortBy);
                srlFragmentMovieList.setRefreshing(false);
                srlFragmentMovieList.setColorSchemeColors(
                        getActivity().getResources().getColor(R.color.colorAccent),
                        getActivity().getResources().getColor(R.color.text_light_blue)
                );
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    private void initRecyclerViewAndScrolling(String sortByFilter) {
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rvFragmentMovieList.setLayoutManager(manager);
        rvFragmentMovieList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = manager.getItemCount();
                int visibleItemCount = manager.getChildCount();
                int firstVisibleItem = manager.findFirstVisibleItemPosition();

                if (firstVisibleItem + visibleItemCount >= totalItemCount / 2) {
                    if (!isFetchingMovies) {
                        getMovies(currentPage + 1, sortByFilter);
                    }
                }
            }
        });
    }

    private void getGenres(String sortByFilter) {
        mTMDbRepositoryAPI.getGenres(new OnGetGenresCallback() {
            @Override
            public void onSuccess(List<Genre> genres) {
                movieGenres = genres;
                getMovies(currentPage, sortByFilter);
            }

            @Override
            public void onError() {
                showError();
            }
        });
    }

    private void getMovies(int page, String sortByFilter) {
        isFetchingMovies = true;
        mTMDbRepositoryAPI.getMovies(page, sortByFilter, new OnGetMoviesCallback() {
            @Override
            public void onSuccess(int page, List<Movie> movies) {
                Log.d("FragmentMovie-getMovies", "Current Page = " + page);
                if (adapter == null) {
                    adapter = new MoviesAdapter(movies, movieGenres, callback);
                    rvFragmentMovieList.setAdapter(adapter);
                } else {
                    if (page == 1) {
                        adapter.clearMovies();
                    }
                    adapter.appendMovies(movies);
                }
                currentPage = page;
                isFetchingMovies = false;

                //setTitle();
            }

            @Override
            public void onError() {
                showError();
            }
        });
    }

    OnMoviesClickCallback callback = new OnMoviesClickCallback() {
        @Override
        public void onClick(Movie movie, ImageView movieImageView) {
            Intent intent = new Intent(MovieListFragment.this.getContext(), MovieDetailsActivity.class);
            intent.putExtra(Constants.MOVIE_ID, movie.getId());
            intent.putExtra(Constants.MOVIE_TITLE, movie.getTitle());
            intent.putExtra(Constants.MOVIE_THUMBNAIL, movie.getBackdrop());
            intent.putExtra(Constants.MOVIE_RATING, movie.getRating());
            intent.putExtra(Constants.MOVIE_SUMMARY, movie.getOverview());
            intent.putExtra(Constants.MOVIE_POSTERPATH, movie.getPosterPath());

            MovieListFragment.this.startActivity(intent);
        }
    };

    /*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/


    public void showError() {
        /*
         * Context from: https://stackoverflow.com/questions/49289281/android-support-library-27-1-0-new-methods-requireactivity-requirecontext
         */
        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.error_message_loading_movies_panel, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.bottom_navigation)
                .show();
    }
}