package ru.pavel.myapp001_testptb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends AppCompatActivity implements OnClickListener {
    Button buttonTest;
    Button buttonExit;
    Button buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        //присоединяем слежение нажатия
        buttonTest = findViewById(R.id.btn_menu_test);
        buttonExit = findViewById(R.id.btn_menu_exit);
        buttonSettings = findViewById(R.id.btn_menu_settings);
        buttonTest.setOnClickListener(this);
        buttonExit.setOnClickListener(this);
        buttonSettings.setOnClickListener(this);
    }

    //обработка нажатия на View
    @Override
    public void onClick (View view){
        Intent intent;
        switch (view.getId()){
            case R.id.btn_menu_test:
                intent = new Intent();
                intent.putExtra(MainActivity.MENU_RESULT, MainActivity.MENU_TEST);
                setResult(RESULT_OK, intent);
                this.finish();
                break;
            case R.id.btn_menu_exit:
                intent = new Intent();
                intent.putExtra(MainActivity.MENU_RESULT, MainActivity.MENU_EXIT);
                setResult(RESULT_OK, intent);
                this.finish();
                break;
            case  R.id.btn_menu_settings:
                intent = new Intent(this, MainSettings.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
