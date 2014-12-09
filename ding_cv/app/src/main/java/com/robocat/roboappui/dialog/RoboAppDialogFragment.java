package com.robocat.roboappui.dialog;

import com.robocat.roboappui.R;
import android.os.Bundle;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


/**
 * This class is used to display a dialog pop up box that the user can enter a byte
 * that can be sent to the Robo Cat
 * @author Joey Phelps
 *
 */
public class RoboAppDialogFragment extends DialogFragment {
	
	private String data;
	
	/**
	 * Returns the string data
	 * @return data
	 */
	public String getData() {
		return new String(data);
	}
	
	/**
	 * Interface classes that creat a RoboAppDialogFragment have to implement
	 *
	 */
	public interface RoboAppDialogListener {
		public void onCommandSubmitClick(DialogFragment dialog);
		public void onCancelClick(DialogFragment dialog);
	}
	
	RoboAppDialogListener commandListener;
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            commandListener = (RoboAppDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RoboAppDialogListener");
        }

	}
	
    /**
     * Sets the layout for the dialog box and specifies the functions to call when certain
     * buttons are clicked
     * @param savedInstaceState - 
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View myLayout = inflater.inflate(R.layout.command_parameter_submit, null);
        
        builder.setView(myLayout)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	EditText c = (EditText) myLayout.findViewById(R.id.manual_data);
                    	
                    	data = c.getText().toString();
                    	//data = "0x5F";
                    	commandListener.onCommandSubmitClick(RoboAppDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        commandListener.onCancelClick(RoboAppDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
    
    
}
