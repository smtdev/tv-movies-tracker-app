package me.smt.mediaddict.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.smt.mediaddict.R;
import me.smt.mediaddict.model.data.TMDbRepositoryAPI;
import me.smt.mediaddict.common.Constants;
import me.smt.mediaddict.adapter.MoviesAdapter;
import me.smt.mediaddict.model.Movie;
import me.smt.mediaddict.model.dataAccess.action.OnMoviesClickCallback;
import me.smt.mediaddict.model.dataAccess.get.OnGetMoviesCallback;

/**
 * Clase que se encarga de cargar el Fragment principal
 * con las diferentes listas de películas
 * @author Sergio Martín Teruel
 * @version 1.0
 * @see Fragment
 **/
public class HomeListFragment extends Fragment {

    @BindView(R.id.rv_home_popular_movies_recyclerview)
    RecyclerView rvHomePopularMoviesRecyclerview;
    @BindView(R.id.rv_home_toprated_movies_recyclerview)
    RecyclerView rvHomeTopratedMoviesRecyclerview;
    @BindView(R.id.rv_home_upcoming_movies_recyclerview)
    RecyclerView rvHomeUpcomingMoviesRecyclerview;
    @BindView(R.id.btn_home_list_popular_viewall_textview)
    AppCompatButton btnHomeListPopularViewallTextview;
    @BindView(R.id.btn_home_list_toprated_viewall_textview)
    AppCompatButton btnHomeListTopratedViewallTextview;
    @BindView(R.id.btn_home_list_upcoming_viewall_textview)
    AppCompatButton btnHomeListUpcomingViewallTextview;
    @BindView(R.id.pb_home_list)
    ProgressBar pbHomeList;

    //private MoviesDbHelper moviesDbHelper;
    //private List<Movie> savedMovieList;
    //private boolean isFavoriteChecked = false;

    /**
     * Atributo que crea una nueva instancia del adapter.
     */
    private MoviesAdapter adapter;

    /**
     * Atributo que crea una instancia de la clase gestora de API.
     */
    private TMDbRepositoryAPI mTMDbRepositoryAPI;

    /**
     * Atributo que indica la lista por defecto a abrir.
     */
    private String sortBy;

    /**
     * Atributo mediante el cual indicamos en qué página inicializa
     * el listado extraido de la API. Cada vez que se haga scroll al 50%
     * del listado de películas, se incrementará +1.
     */
    private int currentPage = 1;

    /**
     * Constructor de la clase.
     */
    public HomeListFragment() {
        // Required empty public constructor
    }

    /**
     * Método para retornar un nuevo Fragment de tipo HomeListFragment.
     * @return fragment devuelto.
     */
    public static HomeListFragment newInstance() {
        return new HomeListFragment();
    }

    /**
     * Método llamado al iniciar la creación del Fragment.
     * @param savedInstanceState estado de la instancia.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // para poder filtrar por el botón de búsqueda
        setHasOptionsMenu(true);

        mTMDbRepositoryAPI = TMDbRepositoryAPI.getInstance();
    }

    /**
     * Método llamado para que el Fragment cree una instancia de la
     * vista a inflar.
     * @param inflater vista a inflar.
     * @param container contenedor de vistas.
     * @param savedInstanceState estado de la instancia.
     * @return la vista.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_list, container, false);

        ButterKnife.bind(this, view);

        rvHomePopularMoviesRecyclerview = view.findViewById(R.id.rv_home_popular_movies_recyclerview);
        rvHomeTopratedMoviesRecyclerview = view.findViewById(R.id.rv_home_toprated_movies_recyclerview);
        rvHomeUpcomingMoviesRecyclerview = view.findViewById(R.id.rv_home_upcoming_movies_recyclerview);

        rvHomePopularMoviesRecyclerview.setVisibility(View.GONE);
        rvHomeTopratedMoviesRecyclerview.setVisibility(View.GONE);
        rvHomeUpcomingMoviesRecyclerview.setVisibility(View.GONE);

        initRecyclerView();

        return view;
    }

    /**
     * Método llamado inmediatamente después de que OnCreateView termine pero
     * antes de que se haya restaurado cualquier estado a la vista.
     * @param view vista.
     * @param savedInstanceState estado de la instancia.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displaySortedMovieList(rvHomePopularMoviesRecyclerview);
        displaySortedMovieList(rvHomeTopratedMoviesRecyclerview);
        displaySortedMovieList(rvHomeUpcomingMoviesRecyclerview);
    }

    /**
     * Método para instanciar los diferentes RecyclerView donde
     * se cargarán cada una de las películas obtenidas de la API.
     */
    private void initRecyclerView() {

        // inicializando ArrayList de películas marcadas como favoritas
        //savedMovieList = new ArrayList<>();

        // Configuración del RecyclerView
        rvHomePopularMoviesRecyclerview.setHasFixedSize(true);
        rvHomeTopratedMoviesRecyclerview.setHasFixedSize(true);
        rvHomeUpcomingMoviesRecyclerview.setHasFixedSize(true);
        //rvHomePopularMoviesRecyclerview.setAdapter(null);

        LinearLayoutManager managerPopular = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager managerToprated = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager managerUpcoming = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        rvHomePopularMoviesRecyclerview.setLayoutManager(managerPopular);
        rvHomeTopratedMoviesRecyclerview.setLayoutManager(managerToprated);
        rvHomeUpcomingMoviesRecyclerview.setLayoutManager(managerUpcoming);
    }

    /**
     * Método para inicializar el contenido del menú de opciones y añadir
     * el botón de búsqueda que será utilizado por SearchFragment.
     * @param menu menú referenciado.
     * @param inflater vista donde instanciar el menú.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.app_bar_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);

        SearchView searchView = (SearchView) item.getActionView();

        /*
         * Info sacada de: https://guides.codepath.com/android/Extended-ActionBar-Guide#adding-searchview-to-actionbar
         */
        // Modificando el icono de búsqueda del SearchView de la AppBar
        int searchImgId = R.id.search_button;
        ImageView v = searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_baseline_search_24);

        // maximizando tamaño hasta ocupar toda la pantalla
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // controlando dinámicamente el botón "X"
        ImageView searchClose = searchView.findViewById(R.id.search_close_btn);
        searchClose.setColorFilter(Color.WHITE);
        searchClose.setVisibility(View.VISIBLE);

        // Cambiando el style al SearchView
        int searchEditId = R.id.search_src_text;
        EditText et = searchView.findViewById(searchEditId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            et.setTextColor(requireActivity().getBaseContext().getResources().getColor(R.color.colorAccent, requireActivity().getBaseContext().getTheme()));
            et.setBackgroundColor(requireActivity().getBaseContext().getResources().getColor(R.color.gray_800, requireActivity().getBaseContext().getTheme()));
        } else {
            et.setTextColor(requireActivity().getBaseContext().getResources().getColor(R.color.colorAccent));
            et.setBackgroundColor(requireActivity().getBaseContext().getResources().getColor(R.color.gray_800));
        }

        et.setHint(R.string.searchview_text);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Bundle bundle = new Bundle();

                SearchFragment searchFragment = new SearchFragment();
                searchFragment.setArguments(bundle);

                bundle.putString("queryValue", query);

                if (requireView().getParent()!= null) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            // ((ViewGroup)getView().getParent()).getId() -> es el id del fragment actual
                            .replace(((ViewGroup) requireView().getParent()).getId(), searchFragment, "HomeListFragmentToSearchFragment")
                            .addToBackStack(null)
                            .commit();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    /**
     * Método para cargar las películas recibidas de las llamadas REST.
     * @param page número de página.
     * @param sortBy tipo de listado.
     * @param rv RecyclerView en cuestion.
     */
    private void getHomeMovies(int page, String sortBy, RecyclerView rv) {
        currentPage = 1;
        mTMDbRepositoryAPI.getMovies(page, sortBy, new OnGetMoviesCallback() {
            @Override
            public void onSuccess(int page, List<Movie> movies) {
                Log.d("HomeListFragment-gMovie", "Current Page = " + page);
                adapter = new MoviesAdapter(movies, callback);
                rv.setAdapter(adapter);
                adapter.appendMovies(movies);
                currentPage = page;
                pbHomeList.setVisibility(View.GONE);
                rvHomePopularMoviesRecyclerview.setVisibility(View.VISIBLE);
                rvHomeTopratedMoviesRecyclerview.setVisibility(View.VISIBLE);
                rvHomeUpcomingMoviesRecyclerview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                showError();
            }
        });
    }

    /**
     * Método para abrir los detalles de una película al hacer clic sobre ella.
     */
    OnMoviesClickCallback callback = (movie, moviePosterImageView) -> {
        //isFavoriteChecked = false;
        /*
         * Enviar información entre activities y fragments para manejarla y mostrarla
         * https://developer.android.com/training/basics/fragments/pass-data-between
         */
        Intent intent = new Intent(HomeListFragment.this.getContext(), MovieDetailsActivity.class);

        /*
         * Recuperando películas guardadas para saber si ya están marcadas como favoritas
         * y mantener marcado el fab de MovieDetailsActivity
         */
        /*getFavoriteMovies();

        for(Movie savedMovie : savedMovieList) {
            if(savedMovie.getId() == movie.getId()) {
                isFavoriteChecked = true;
            }
        }*/

        intent.putExtra(Constants.MOVIE_ID, movie.getId());
        /*intent.putExtra(Constants.MOVIE_TITLE, movie.getTitle());
        intent.putExtra(Constants.MOVIE_BACKDROP, movie.getBackdrop());
        intent.putExtra(Constants.MOVIE_RATING, movie.getRating());
        intent.putExtra(Constants.MOVIE_OVERVIEW, movie.getOverview());
        intent.putExtra(Constants.MOVIE_POSTERPATH, movie.getPosterPath());
        intent.putExtra(Constants.MOVIE_RELEASE_DATE, movie.getReleaseDate());
        intent.putExtra(Constants.MOVIE_GENRES_ID, TextUtils.join(",", movie.getGenreIds()));

        intent.putExtra("movie_favorite_status", isFavoriteChecked);*/

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                HomeListFragment.this.getActivity(),
                moviePosterImageView,
                "fromHomeToMovieDetails"
        );
        HomeListFragment.this.startActivity(intent, options.toBundle());
    };

    /**
     * Método que da un valor a sortBy en base al RecyclerView al
     * que va asociado.
     * @param rv el RecyclerView.
     */
    private void displaySortedMovieList(RecyclerView rv) {
        currentPage = 1; // Volvemos al inicio cada vez que entremos en una de las listas

        switch (rv.getId()) {
            case R.id.rv_home_popular_movies_recyclerview:
                sortBy = Constants.POPULAR;
                break;
            case R.id.rv_home_toprated_movies_recyclerview:
                sortBy = Constants.TOP_RATED;
                break;
            case R.id.rv_home_upcoming_movies_recyclerview:
                sortBy = Constants.UPCOMING;
                break;
        }
        getHomeMovies(currentPage, sortBy, rv);
    }

    /**
     * Método para mostrar un Snackbar al aparecer algún error.
     * Más info: https://stackoverflow.com/questions/49289281/android-support-library-27-1-0-new-methods-requireactivity-requirecontext
     */
    public void showError() {
        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.error_message_loading_movies_panel, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.bottom_navigation)
                .show();
    }

    /**
     * Método para manejar los clics de los elementos asociados al @OnClick.
     * @param view
     */
    @OnClick({R.id.btn_home_list_popular_viewall_textview, R.id.btn_home_list_toprated_viewall_textview, R.id.btn_home_list_upcoming_viewall_textview})
    public void onClick(View view) {
        String sortFilter = null;
        // Enviar parámetros para que reciba la info el nuevo fragment
        // https://stackoverflow.com/a/40949016/1552146
        Bundle bundle = new Bundle();

        MovieListFragment movieList = new MovieListFragment();
        movieList.setArguments(bundle);
        switch (view.getId()) {
            case R.id.btn_home_list_popular_viewall_textview:
                sortFilter = Constants.POPULAR;
                break;
            case R.id.btn_home_list_toprated_viewall_textview:
                sortFilter = Constants.TOP_RATED;
                break;
            case R.id.btn_home_list_upcoming_viewall_textview:
                sortFilter = Constants.UPCOMING;
                break;
        }
        bundle.putString("movieFilter", sortFilter);

        requireActivity().getSupportFragmentManager().beginTransaction()
                // ((ViewGroup)getView().getParent()).getId() -> es el id del fragment actual
                .replace(((ViewGroup) requireView().getParent()).getId(), movieList, "FragmentMovieListFiltered")
                .addToBackStack(null)
                .commit();
    }

    /*
     * Recuperar la lista de películas almacenadas en la SQLite
     */
    /*private void getFavoriteMovies() {
        moviesDbHelper = new MoviesDbHelper(requireActivity().getApplicationContext());

        savedMovieList.clear();
        savedMovieList.addAll(moviesDbHelper.getSavedMovies());
    }*/
}
