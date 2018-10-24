package com.example.mcresswell.project01.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mcresswell.project01.activities.DashboardActivity;
import com.example.mcresswell.project01.activities.ProfileEntryActivity;
import com.example.mcresswell.project01.R;
import com.example.mcresswell.project01.viewmodel.FitnessProfileViewModel;
import com.example.mcresswell.project01.viewmodel.UserListViewModel;
import com.example.mcresswell.project01.viewmodel.UserViewModel;
import com.example.mcresswell.project01.db.entity.User;
import com.example.mcresswell.project01.util.Constants;

import java.time.Instant;
import java.util.Date;

import static com.example.mcresswell.project01.util.Constants.SAVE_INSTANCE_STATE;
import static com.example.mcresswell.project01.util.Constants.VIEW_STATE_RESTORED;
import static com.example.mcresswell.project01.util.ValidationUtils.isNotNullOrEmpty;
import static com.example.mcresswell.project01.util.ValidationUtils.isValidAlphaChars;
import static com.example.mcresswell.project01.util.ValidationUtils.isValidEmail;

/**
 * Fragment class for the screen that user sees when the Settings button is pressed from the main 
 * dashboard.
 */
public class AccountSettingsFragment extends Fragment {

    private static final String LOG_TAG = AccountSettingsFragment.class.getSimpleName();

    private Button m_btn_updateAccount, m_btn_deleteAccount;
    private EditText m_email, m_password, m_firstName, m_lastName;
    private UserViewModel userViewModel;
    private FitnessProfileViewModel fitnessProfileViewModel;
    private UserListViewModel userListViewModel;

    public AccountSettingsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, Constants.CREATE);
        super.onCreate(savedInstanceState);

    }

    private void configureViewModels() {
        userListViewModel = ViewModelProviders.of(this).get(UserListViewModel.class);
        userListViewModel.getUserList().observe(this, userList -> {
            if (userList != null) {
                Log.d(LOG_TAG, "UserListViewModel observer for getUserList() now contains userList data");
            }
        });

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        userViewModel.getUser().observe(this, user -> {
            Log.d(LOG_TAG, "UserViewModel observer for getUser()");
            if (user != null) {
                m_email.setText(user.getEmail());
                m_password.setText(user.getPassword());
                m_firstName.setText(user.getFirstName());
                m_lastName.setText(user.getLastName());
                
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, Constants.CREATE_VIEW);

        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        initializeFragmentView(view);

        configureViewModels();

        setOnClickListeners();
        return view;
    }

    private void initializeFragmentView(View view){
        m_email = view.findViewById(R.id.text_email_update_account);
        m_password = view.findViewById(R.id.text_password_update_account);
        m_firstName = view.findViewById(R.id.text_first_name_update_account);
        m_lastName = view.findViewById(R.id.text_last_name_update_account);
        m_btn_updateAccount = view.findViewById(R.id.btn_update_account);
        m_btn_deleteAccount = view.findViewById(R.id.btn_delete_account);
    }

    private void setOnClickListeners() {
        m_btn_updateAccount.setOnClickListener(listener -> updateAccountHandler());
        m_btn_deleteAccount.setOnClickListener(listener -> deleteAccountHandler());
    }

    private void deleteAccountHandler() {
        Log.d(LOG_TAG, "delete account button pressed");
        if (userViewModel.getUser().getValue() != null) {
            User userToDelete = userViewModel.getUser().getValue();

            Log.d(LOG_TAG,
                    String.format("Deleting user account for the following user (id: %d): %s\t%s\t%s",
                            userToDelete.getId(), userToDelete.getEmail(), userToDelete.getFirstName(), userToDelete.getLastName()));

            Log.d(LOG_TAG, "JUST KIDDING, NOT IMPLEMENTED YET!");
            //TODO: make call to delete corresponding fitness profile record FIRST, to prevent FK constraint violation!
//            userViewModel.deleteUser(userToDelete);
        }

    }

    private void updateAccountHandler() {
        if (isAccountDataValid()) {
            if (!isUniqueUserLogin(m_email.getText().toString())) {
                Toast.makeText(getContext(), "A user account with that email already exists.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (userViewModel.getUser().getValue() != null) {
                User userToUpdate = userViewModel.getUser().getValue();

                Log.d(LOG_TAG,
                        String.format("Updating user account for the following user (id: %d): %s\t%s\t%s",
                                userToUpdate.getId(), userToUpdate.getEmail(), userToUpdate.getFirstName(), userToUpdate.getLastName()));

                userToUpdate.setEmail(m_email.getText().toString());
                userToUpdate.setPassword(m_password.getText().toString());
                userToUpdate.setFirstName(m_firstName.getText().toString());
                userToUpdate.setLastName(m_lastName.getText().toString());
                //Keep join date the same as it was intiially

                Log.d(LOG_TAG,
                        String.format("Updating user account with the following account updates (id: %d): %s\t%s\t%s",
                                userToUpdate.getId(), userToUpdate.getEmail(), userToUpdate.getFirstName(), userToUpdate.getLastName()));

                //Update user
                userViewModel.updateUser(userToUpdate);

                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                startActivity(intent);
            }
        }
    }

    private boolean isUniqueUserLogin(String email) {
        LiveData<User> userResult = userViewModel.findUser(email);
        if (userResult.getValue() != null && userResult.getValue().getEmail().equals(email)) {
            Log.d(LOG_TAG, "Email is not unique. A user with that email already exists.");

//            Log.d(LOG_TAG, "EMAIL: " + userResult.getValue().getEmail());
//            Log.d(LOG_TAG, "PASSOWRD: " + userResult.getValue().getPassword());
//            Log.d(LOG_TAG, "First name: " + userResult.getValue().getFirstName());
//            Log.d(LOG_TAG, "Last Name: " + userResult.getValue().getLastName());
//            Log.d(LOG_TAG, "Date joined: " + userResult.getValue().getJoinDate());

            return false;
        }
        return true;
    }

    private boolean isAccountDataValid () {
        if (!isValidEmail(m_email.getText().toString())) {
            Toast.makeText(getContext(), "Invalid email.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isNotNullOrEmpty(m_password.getText().toString())) {
            Toast.makeText(getContext(), "Invalid password.", Toast.LENGTH_SHORT).show();

        }
        else if (!isValidAlphaChars(m_firstName.getText().toString())) {
            Toast.makeText(getContext(), "Invalid first name.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!isValidAlphaChars(m_lastName.getText().toString())) {
            Toast.makeText(getContext(), "Invalid last name.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onViewStateRestored (Bundle savedInstanceState) {
        Log.d(LOG_TAG, VIEW_STATE_RESTORED);
        super.onViewStateRestored(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        Log.d(LOG_TAG, SAVE_INSTANCE_STATE);

        super.onSaveInstanceState(bundle);
    }
}
