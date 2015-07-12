package com.contact.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.contact.R;
import com.contact.adapters.ContactsAdapterLollipop;
import com.contact.helpers.DividerItemDecoration;
import com.contact.models.ContactInfo;
import com.contact.models.Request;
import com.melnykov.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContactsListFragment extends Fragment {
    private static final String TAG = "ContactsListFragment";
    private List<ContactInfo> contacts = new ArrayList<>();

    private ContactClickListener listener;
    private ContactsAdapterLollipop contactsAdapter;

    @Bind(R.id.pbLoading) ProgressBar pb;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @Bind(R.id.rvContacts) RecyclerView rvContacts;
    @Bind(R.id.fabAddContact) FloatingActionButton fabAddContact;

    public interface ContactClickListener{
        void onSentRequestClick(String objectId);
        void onReceivedRequestClick(String objectId);
        void addContactButtonPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Contact);

        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View v = localInflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, v);

        // allows for optimizations
        //rvContacts.setHasFixedSize(true);

        rvContacts.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        // Unlike ListView, you have to explicitly give a LayoutManager to the RecyclerView to position items on the screen.
        // There are three LayoutManager provided at the moment: GridLayoutManager, StaggeredGridLayoutManager and LinearLayoutManager.
        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Create an adapter
        contactsAdapter = new ContactsAdapterLollipop(getActivity(), contacts);
        refreshList();

        // Bind adapter to list
        rvContacts.setAdapter(contactsAdapter);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        // Fab to launch Add Contact
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.addContactButtonPressed();
            }
        });

        return v;
    }

    public void refreshList(){
        contacts.clear();
        contactsAdapter.notifyDataSetChanged();
        rvContacts.removeAllViewsInLayout();
        pb.setVisibility(View.VISIBLE);
        ContactInfo.getContacts(new ContactInfo.OnContactsReturnedListener() {
            @Override
            public void receiveContacts(List<ContactInfo> contacts) {
                pb.setVisibility(View.INVISIBLE);
                swipeContainer.setRefreshing(false);
                addAll(contacts);
            }
        });

        Request.getRequestsInBackground(ParseUser.getCurrentUser().getUsername(), new Request.OnRequestsReturnedListener() {
            @Override
            public void receiveRequests(List<Request> requests) {
                swipeContainer.setRefreshing(false);
                for (Request r : requests){
                    ContactInfo c = r.getFromUser();
                    if (c == null) continue;
                    c.setRequestObjectId(r.getObjectId());
                    c.setRequestStatus(ContactInfo.RequestStatus.INCOMING);
                    c.fetchIfNeededInBackground(new GetCallback<ContactInfo>() {
                        @Override
                        public void done(ContactInfo contactInfo, ParseException e) {
                            if (e == null){
                                add(contactInfo);
                            } else {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
                }
            }
        });

        Request.getSentRequestsInBackground(ParseUser.getCurrentUser().getUsername(), new Request.OnRequestsReturnedListener() {
            @Override
            public void receiveRequests(List<Request> requests) {
                swipeContainer.setRefreshing(false);
                for (Request r : requests){
                    ContactInfo c = new ContactInfo();
                    c.setRequestObjectId(r.getObjectId());
                    c.setFirstName(r.getTo());
                    c.setRequestStatus(ContactInfo.RequestStatus.SENT);
                    add(c);
                }
            }
        });
    }

    private void add(ContactInfo c){
        contacts.add(c);
        contactsAdapter.notifyDataSetChanged();
    }

    private void addAll(List<ContactInfo> contacts){
        // Parse mush be caching these.  need to clear the status...
        for (ContactInfo c : contacts) {
            c.setRequestStatus(ContactInfo.RequestStatus.NA);
        }
        this.contacts.addAll(contacts);
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ContactClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ContactClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
