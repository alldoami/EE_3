package allisondoami.ee3door3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class MyDialogFragment extends DialogFragment implements OnClickListener {
    int numClicks = 0;
    int password[];

    Toast leftToast;
    Toast rightToast;
    Toast passwordSet;

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_left:
                if (numClicks < 4) {
                    leftToast.show();
                    password[numClicks] = 0;
                    Log.v("password string", "0");
                    numClicks++;
                    String num = "" + numClicks;
                    Log.v("numberofclicks", num);
                }
                if (numClicks == 4) {
                    passwordSet.show();
                    ((MainActivity)getActivity()).savePassword(password);
                    getDialog().dismiss();
                }
                break;
            case R.id.button_right:
                if (numClicks < 4) {
                    rightToast.show();
                    password[numClicks] = 1;
                    Log.v("password string", "1");
                    numClicks++;
                    String num1 = "" + numClicks;
                    Log.v("numberofclicks", num1);
                }
                if (numClicks == 4) {
                    passwordSet.show();
                    ((MainActivity)getActivity()).savePassword(password);
                    getDialog().dismiss();
                }
                break;
        }
    }


  //  Button leftButton = (Button)findViewById(R.layout.dialogfragment.button3);
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialogfragment, container,
                false);
        password = new int[4];

        Context context = rootView.getContext();
        CharSequence text = "Left Clicked";
        CharSequence text2 = "Right Clicked";
        CharSequence text3 = "Password Set";
        int duration = Toast.LENGTH_SHORT;

        leftToast = Toast.makeText(context, text, duration);
        rightToast = Toast.makeText(context, text2, duration);
        passwordSet = Toast.makeText(context, text3, duration);

        Button leftButton = (Button) rootView.findViewById(R.id.button_left);
        Button rightButton = (Button) rootView.findViewById(R.id.button_right);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        getDialog().setTitle("Enter a password");
        // Do something else
        return rootView;
    }
}