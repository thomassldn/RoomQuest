package roomquest.cse.csusb.edu.roomquest;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Author: thomasmsaldana
 * File:Grid.java
 * Date:9/14/16.
 * Description:This is the grid of the csusb main activity. It is the dialog
 * fragment that comes out when the user clicks on the grid icon on the application
 * in the MainActivity.
 *
 * I followed some of the things concepts of this tutorial:
 * https://www.youtube.com/watch?v=bkUHeXCX8XM
 *
 */

public class Grid extends DialogFragment implements View.OnClickListener{


    Button one, two, three, four, five, six, seven,
            eight, nine, ten, eleven, twelve ;//grid buttons
    Communicator communicator;
    int gridBoxNum = 0 ; //this will be passed to main activity
    //it is the box number for the grid

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communicator = (Communicator) activity;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View  view = inflater.inflate(R.layout.grid,null);
        //set title on dialogFrag
        //getDialog().setTitle("Menu");
       //getDialog().setTitle(Html.fromHtml("<font color = '#ffffff'> Menu </font>"));
       //int dividerId = getDialog().getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        //View divider = getDialog().findViewById(dividerId);
        //divider.setBackgroundColor(getResources().getColor(R.color.justWhite));

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

        setCancelable(false);//prevents user from canceling the grid

        one = (Button) view.findViewById(R.id.grid_button_one);
        one.setOnClickListener(this);

        two = (Button) view.findViewById(R.id.grid_button_two);
        two.setOnClickListener(this);

        three= (Button) view.findViewById(R.id.grid_button_three);
        three.setOnClickListener(this);

        four = (Button) view.findViewById(R.id.grid_button_four);
        four.setOnClickListener(this);

        five = (Button) view.findViewById(R.id.grid_button_five);
        five.setOnClickListener(this);

        six = (Button) view.findViewById(R.id.grid_button_six);
        six.setOnClickListener(this);

        seven = (Button) view.findViewById(R.id.grid_button_seven);
        seven.setOnClickListener(this);

        eight = (Button) view.findViewById(R.id.grid_button_eight);
        eight.setOnClickListener(this);
        nine = (Button) view.findViewById(R.id.grid_button_nine);
        nine.setOnClickListener(this);

        ten = (Button) view.findViewById(R.id.grid_button_ten);
        ten.setOnClickListener(this);

        eleven = (Button) view.findViewById(R.id.grid_button_eleven);
        eleven.setOnClickListener(this);

        twelve = (Button) view.findViewById(R.id.grid_button_twelve);
        twelve.setOnClickListener(this);






        return view; //inflater.inflate(R.layout.grid, null);
    }

    public void onClick(View view){

        if(view.getId() == R.id.grid_button_one){
            communicator.onDialogMessage(1);
            dismiss();

        }
        else if(view.getId() == R.id.grid_button_two){
            communicator.onDialogMessage(2);
            dismiss();
        }else if(view.getId() == R.id.grid_button_three){
            communicator.onDialogMessage(3);
            dismiss();
        }else if(view.getId() == R.id.grid_button_four){
            communicator.onDialogMessage(4);
            dismiss();
        }else if(view.getId() == R.id.grid_button_five){
            communicator.onDialogMessage(5);
            dismiss();
        }else if(view.getId() == R.id.grid_button_six){
            communicator.onDialogMessage(6);
            dismiss();
        }else if(view.getId() == R.id.grid_button_seven){
            communicator.onDialogMessage(7);
            dismiss();

        }else if(view.getId() == R.id.grid_button_eight){
            communicator.onDialogMessage(8);
            dismiss();

        }else if(view.getId() == R.id.grid_button_nine){
            communicator.onDialogMessage(9);
            dismiss();

        }else if(view.getId() == R.id.grid_button_ten){
            communicator.onDialogMessage(10);
            dismiss();

        }else if(view.getId() == R.id.grid_button_eleven){
            communicator.onDialogMessage(11);
            dismiss();

        }else if(view.getId() == R.id.grid_button_twelve){
            communicator.onDialogMessage(12);
            dismiss();

        }

    }


    interface  Communicator {
        public void onDialogMessage(int boxNum);
    }
}//END CLASS GRID
