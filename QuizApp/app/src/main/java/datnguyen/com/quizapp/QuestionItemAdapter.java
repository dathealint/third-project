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

	public void setSelectedChoices(HashSet<String> choices) {
		if (choices != null) {
			selectedChoices = choices;
		} else {
			selectedChoices = new HashSet<>();
		}
	}

	private HashSet<String> selectedChoices;

	public void setChoices(ArrayList<String> choices) {
		clear();
		if (choices != null) {
			addAll(choices);
		}
	}

	public QuestionItemAdapter(Context context, ArrayList<String>list) {
		super(context, R.layout.question_item_row, list);
		setSelectedChoices(null);
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
		viewHolder.checkbox.setTag(position);
		viewHolder.checkbox.setText(choice);
		viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				int selectedPosition = (Integer) compoundButton.getTag();
				String selectedChoice = getItem(selectedPosition);
				if (b) {
					selectedChoices.add(selectedChoice);
				} else {
					selectedChoices.remove(selectedChoice);
				}
			}
		});

		viewHolder.checkbox.setChecked(selectedChoices.contains(choice));

		return convertView;
	}

	// View Holder - for caching
	private static class ViewHolder {
		CheckBox checkbox;
	}

	public HashSet<String> selectedAnswers() {
		return selectedChoices;
	}

}
