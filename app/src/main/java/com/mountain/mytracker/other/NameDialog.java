package com.mountain.mytracker.other;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mountain.mytracker.activity.R;

public class NameDialog extends DialogFragment{
	
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(String titlu);
	}
	
	NoticeDialogListener mListener;
	EditText mEditText;
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try {
			mListener = (NoticeDialogListener) activity;
			Log.v("in onAttach", "s-o facut legatura "+ activity.getLocalClassName());
		}
		catch (ClassCastException e ){
			throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(getString(R.string.new_track_dialog));
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View mView = inflater.inflate(R.layout.dialog_new_track, null);
		builder.setView(mView);
		mEditText = (EditText) mView.findViewById(R.id.dialog_new_track);
		builder.setPositiveButton(getString(R.string.new_track_dialog_ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.v("cand dai click ok",mEditText.getText().toString());
				mListener.onDialogPositiveClick(mEditText.getText().toString());
			}
		});
		builder.setNegativeButton(getString(R.string.new_track_dialog_cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				NameDialog.this.getDialog().cancel();
			}
		});
		
		
		return builder.create();
	}
	
}
