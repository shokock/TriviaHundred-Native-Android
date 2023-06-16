package com.apps.meirovichomer.triviagame.leaderboardFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.apps.meirovichomer.triviagame.DividerItemDecoration;
import com.apps.meirovichomer.triviagame.R;
import com.apps.meirovichomer.triviagame.RetrofitTriviaInterface;
import com.apps.meirovichomer.triviagame.data;
import com.apps.meirovichomer.triviagame.recyclerAdatpers.personalAdapter;
import com.apps.meirovichomer.triviagame.retroClasses.SessionsClass;
import com.apps.meirovichomer.triviagame.retroClasses.sessionInfoClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonalRecordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PersonalRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalRecordFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    //shared prefs
    private SharedPreferences prefs;

    private RecyclerView recyclerView;
    private personalAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    public PersonalRecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalRecordFragment.
     */
    public static PersonalRecordFragment newInstance(String param1, String param2) {
        PersonalRecordFragment fragment = new PersonalRecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personal_record, container, false);
    }

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
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sessionInfoRetrofit();


    }


    // Get the sessions ArrayList and populate the recycler_view.
    private void populateRecycleView(ArrayList<sessionInfoClass> allSessions) {

        recyclerView = (RecyclerView) getActivity().findViewById(R.id.personal_recycler_view);
        mAdapter = new personalAdapter(allSessions);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }


    // render the epoch time long to a string date.
    private String formatTimeStamp(long dateStamp) {

        // Date needs to be in MILISECONDS in order to be rendered and not in epoch time.
        long fixedDate = dateStamp * 1000L;

        Date cDate = new Date(fixedDate);
        String fDate = new SimpleDateFormat("dd-MM-yyyy").format(cDate);

        return fDate;
    }

    // Get properties from user into the shared prefs.
    private void sessionInfoRetrofit() {
        RetrofitTriviaInterface mApiService = this.getInterfaceService();
        // Get shared prefs
        Context context = getActivity();
        prefs = context.getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String userId = prefs.getString(getResources().getString(R.string.prefs_user_id), null);
        String uuid = prefs.getString(getString(R.string.user_uuid), null);
        data mapRequest = new data();
        mapRequest.putParam("id", userId);
        mapRequest.putParam("uuid", uuid);

        final Call<SessionsClass> mService = mApiService.getSessionInfo(mapRequest.getParamsData("getSessionsInfo"));
        mService.enqueue(new Callback<SessionsClass>() {
            @Override
            public void onResponse(@NonNull Call<SessionsClass> call, @NonNull Response<SessionsClass> response) {
                SessionsClass mSessions = response.body();
                if (mSessions.getSessions().size() > 0) {
                    populateRecycleView(formatDateArray(mSessions.getSessions()));
                } else {
                    Toast.makeText(getActivity(), "שחק בכדי לראות תוצאות!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<SessionsClass> call, @NonNull Throwable t) {

                Log.e("ERR", "ERROR HAS OCCURED IN RETUREND RESPONSE PROPRTEIES");

            }

        });
    }

    private RetrofitTriviaInterface getInterfaceService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://meirovich-ghost.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final RetrofitTriviaInterface mInterfaceService = retrofit.create(RetrofitTriviaInterface.class);
        return mInterfaceService;
    }

    // Format given epoch timeStamp to the format DATE : DD/MM/YYYY
    private ArrayList<sessionInfoClass> formatDateArray(ArrayList<sessionInfoClass> allSessions) {

        int arrSize = allSessions.size();
        for (int i = 0; i < arrSize; i++) {

            long longDate = Long.parseLong(allSessions.get(i).getSessionTime());
            allSessions.get(i).setSessionTime(formatTimeStamp(longDate));
        }

        return allSessions;
    }


}
