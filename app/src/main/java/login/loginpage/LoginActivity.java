package login.loginpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    HttpURLConnection connection = null;
    BufferedReader  reader = null;
    EditText Name, Password;
    SessionManagement session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        Name = (EditText) findViewById(R.id.Name);
        Password = (EditText) findViewById(R.id.Password);
        session = new SessionManagement(getApplicationContext());

        /*-------------------------tombol Sign in----------------------------*/
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new ApiConnect().execute("http://private-17dd4-createtask3.apiary-mock.com/users");
            }
        });
        /*--------------------------------------------------------------------*/


        /*-------------------------tombol register----------------------------*/
        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new ApiRegister().execute("http://private-17dd4-createtask3.apiary-mock.com/users");
            }
        });
        /*--------------------------------------------------------------------*/

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    //this method for handle http connection
    public String get_data(String url_target){
        String line = "";
        try{
            URL url = new URL(url_target);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            while((line = reader.readLine())!=null){
                buffer.append(line);
            }
            return buffer.toString();
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(connection!=null) connection.disconnect();
            try{
                if(reader!=null)reader.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */


    public void post_json(String json_str)throws JSONException{
        try{
            JSONObject jsonobj = new JSONObject();

            jsonobj.put("Name", "Reyner");
            jsonobj.put("Email", "reynersubrata@gmail.com");
            jsonobj.put("Password", "123456");


        }
        catch(JSONException e){
            e.printStackTrace();
        }

    }

    public void process_json(String json_str)throws JSONException{
        String a = Name.getText().toString();
        String b = Password.getText().toString();

        try {

            //clean textview before append data
            boolean ceker = true;

            JSONObject api_json = new JSONObject(json_str);

            JSONArray users = api_json.getJSONArray("users");
            String[] arrname = new String[users.length()];
            String[] arrpassword = new String[users.length()];
            String[] arremail = new String[users.length()];
            for(int i = 0;i<users.length();i++){
                JSONObject user = users.getJSONObject(i);
                arrname[i] = String.valueOf(user.getString("name"));
                arrpassword[i] = String.valueOf(user.getString("password"));
                arremail[i] = String.valueOf(user.getString("email"));
                if(a.equals(arrname[i]) && b.equals(arrname[i])){
                    session.createLoginSession(a,arremail[i]);
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, WelcomeActivity.class);
                    startActivity(intent);
                    i = users.length();
                    finish();
                }else{

                }
                if(i == users.length()-1){
                    ceker = false;
                }


            }
            if(ceker==false){
                new AlertDialog.Builder(this)
                        .setTitle("Wrong input")
                        .setMessage("Wrong Username or Password")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(session.isLoggedIn()==false){
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }


    class ApiConnect extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            return get_data(params[0]);
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            try{
                process_json(s);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }



    }

    class ApiRegister extends AsyncTask<String, Void, String>{
    //apiregister buat tombol register, memanggil method POST
        @Override
        protected String doInBackground(String... params) {
            POST(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                post_json(s);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void POST(String url_target){
        InputStream inputStream = null;
        String result = "";

        /*-----------------------------bikin koneksi------------------*/
        try{
            URL url = new URL(url_target);
            HttpURLConnection con= (HttpURLConnection) url.openConnection();
            connection.connect();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");


        }catch(Exception e){

        }
    }

}

