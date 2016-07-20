package login.loginpage;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    SessionManagement session;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button logout_button = (Button) findViewById(R.id.logout_button);
        logout_button.setOnClickListener(this);
        TextView lblname = (TextView) findViewById(R.id.welcome_name);
        TextView lblemail = (TextView) findViewById(R.id.welcome_email);
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();

        // name
        String name = user.get(SessionManagement.KEY_NAME);

        // email
        String email = user.get(SessionManagement.KEY_EMAIL);
        lblname.setText(name);
        lblemail.setText(email);


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

    @Override
    public void onClick(View view) {
        session.logoutUser();
    }
}
