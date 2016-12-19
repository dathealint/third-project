package datnguyen.com.quizapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by datnguyen on 12/19/16.
 */

public class QuestionItemAdapter extends ArrayAdapter<String> {

	private HashSet<String> selectedChoices;

	public void setChoices(ArrayList<String> choices) {
		clear();
		addAll(choices);
		this.selectedChoices = new HashSet<>();
	}

	public QuestionItemAdapter(Context context, ArrayList<String>list) {
		super(context, R.layout.question_item_row, list);
		setChoices(list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		// create a new view
		if (convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater layoutInflater = LayoutInflater.from(getContext());
			convertView = layoutInflater.inflate(R.layout.question_item_row, parent, false);
			viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.chkQuestion);

			// cache viewholder
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// bind data
		final String choice = getItem(position);
		viewHolder.checkbox.setText(choice);
		viewHolder.checkbox.setChecked(selectedChoices.contains(choice));
		viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (b) {
					selectedChoices.add(choice);
				} else {
					selectedChoices.remove(choice);
				}
			}
		});

		return convertView;
	}

	// View lookup cache
	private static class ViewHolder {
		CheckBox checkbox;
	}

	public HashSet<String> selectedAnswers() {
		return selectedChoices;
	}
}
