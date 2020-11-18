package me.sergiomartin.tvshowmovietracker.moviesModule.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.sergiomartin.tvshowmovietracker.R;

public class UserProfileFragment extends Fragment {

    @BindView(R.id.tb_user_profile)
    Toolbar tbUserProfile;
    @BindView(R.id.ctl_user_profile)
    CollapsingToolbarLayout ctlUserProfile;
    @BindView(R.id.fab_user_profile)
    FloatingActionButton fabUserProfile;
    @BindView(R.id.fab_settings)
    FloatingActionButton fabSettings;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        super.onStop();
        // mostrar la Toolbar
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        // ocultar la Toolbar
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ocultar la Toolbar principal al iniciar View
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @OnClick({R.id.fab_user_profile, R.id.fab_settings})
    public void onClick(View view) {
        Bundle bundle = new Bundle();

        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle);

        switch (view.getId()) {
            case R.id.fab_user_profile:
                break;
            case R.id.fab_settings:
                requireActivity().getSupportFragmentManager().beginTransaction()
                        // ((ViewGroup)getView().getParent()).getId() -> es el id del fragment actual
                        .replace(((ViewGroup) requireView().getParent()).getId(), settingsFragment, "FragmentPreferences")
                        .addToBackStack(null)
                        .commit();
        }
    }
}