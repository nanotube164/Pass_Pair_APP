package edu.nccu.mis.passpair.Homepage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.nccu.mis.passpair.R;

public class Login extends AppCompatActivity {
    EditText Username,Password;
    Button BtnLogin;
    final String TAG = this.getClass().getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference Ref_user = FirebaseDatabase.getInstance().getReference().child("User");
    private String time_str;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        getDateInfo();

        Username = (EditText) findViewById(R.id.LoginUsername);
        Password = (EditText) findViewById(R.id.LoginPassword);
        BtnLogin = (Button) findViewById(R.id.LoginButton);
        BtnLogin.setOnClickListener(BtnListener);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    Button.OnClickListener BtnListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            onSignIn();
        }
    };

    private void onSignIn(){
        String user = Username.getText().toString();
        String pass = Password.getText().toString();
        if (!user.isEmpty() && !pass.isEmpty()) {
            //建立QB User型態
            QBUser qbUser = new QBUser(user,pass);
            //登入QB伺服器
            QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
//                    Toast.makeText(getApplicationContext(),"Chat system Login Sucessfully",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(QBResponseException e) {
                    //取得錯誤訊息
                    Toast.makeText(getApplicationContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
            mAuth.signInWithEmailAndPassword(user, pass)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(Login.this,"登入成功"+authResult.getUser().getEmail(),Toast.LENGTH_SHORT).show();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                // The user's ID, unique to the Firebase project. Do NOT use this value to
                                // authenticate with your backend server, if you have one. Use
                                // FirebaseUser.getToken() instead.
                                String uid = user.getUid();

                                Ref_user.child(uid).child("任務").child(time_str).child("每日登入").setValue(1);

                                Intent intent = new Intent();
                                //以bundle物件進行打包
                                Bundle bundle=new Bundle();
                                bundle.putString("UID", uid);
                                intent.putExtras(bundle);
                                intent.setClass(Login.this, HomePage.class);
                                startActivity(intent);

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Login.this,"登入失敗",Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
    private void getDateInfo() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
        time_str = formatter.format(curDate);
    }
}
