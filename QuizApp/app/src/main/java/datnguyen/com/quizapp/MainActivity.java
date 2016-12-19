package datnguyen.com.quizapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import static datnguyen.com.quizapp.Constants.FILE_JSON;
import static datnguyen.com.quizapp.Constants.QUIZ_QUESTIONS_KEY;
import static datnguyen.com.quizapp.Constants.QUIZ_QUESTION_CHOICES_KEY;
import static datnguyen.com.quizapp.Constants.QUIZ_QUESTION_CORRECT_KEY;
import static datnguyen.com.quizapp.Constants.QUIZ_QUESTION_ID_KEY;
import static datnguyen.com.quizapp.Constants.QUIZ_QUESTION_KEY;
import static datnguyen.com.quizapp.Constants.QUIZ_QUESTION_TYPE_KEY;

public class MainActivity extends AppCompatActivity {

	public  static  final  String VIEW_TAG = "MainActivity";

    private TextView        tvQuestion = null;
    private TextView        tvQuestionIndex = null;
    private RadioGroup      radioGroupChoices = null;
    private ListView		lvCheckboxGroup = null;
    private EditText        txtInput = null;
	private ViewGroup       viewResult = null;
	private TextView        tvResultDescription = null;
	private TextView        tvResult = null;
	private Button			btnNext = null;
	private Button			btnWatchAnswers = null;


    ArrayList<Question>     questions = new ArrayList<>();
    private     int         currentIndex = 0;
	private     int         correctAnswer = 0;

	private     boolean     watchAnswersMode;

	QuestionItemAdapter 	adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // grab controls
        tvQuestion = (TextView)findViewById(R.id.tvQuestion);
        tvQuestionIndex = (TextView)findViewById(R.id.tvCurrentPageIndex);
        radioGroupChoices = (RadioGroup)findViewById(R.id.radioGroupChoices);
		lvCheckboxGroup = (ListView) findViewById(R.id.lvCheckboxGroup);
        txtInput = (EditText)findViewById(R.id.txtInput);
		viewResult = (ViewGroup)findViewById(R.id.viewResult);
		tvResultDescription = (TextView)findViewById(R.id.tvResultDescription);
		tvResult = (TextView)findViewById(R.id.tvResult);
		btnNext = (Button)findViewById(R.id.btnNext);
		btnWatchAnswers = (Button)findViewById(R.id.btnWatchAnswers);

		// handle click events
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClickNext();
			}
		});

		btnWatchAnswers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClickWatchAnswers();
			}
		});


		// setup adapter for listview checkbox group
		adapter = new QuestionItemAdapter(this, new ArrayList<String>());
		lvCheckboxGroup.setAdapter(adapter);

        // initialize values
        currentIndex = 0;
		tvResultDescription.setText(R.string.txt_completed);

		// load questions from json
		loadJSONQuestions();

		startQuiz();
    }

	private String stringFromFile(String fileName) {
		// load content from json file
		String jsonContent = null;
		try {
			InputStream is = this.getAssets().open(fileName);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			// get string content out of buffer, using UTF-8 encoding
			jsonContent = new String(buffer, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonContent;
	}

    private void loadJSONQuestions() {
		String jsonString = stringFromFile(FILE_JSON);

		// now parse to JSON object
		try {
			JSONObject jsonRoot = new JSONObject(jsonString);
			JSONArray questionJSONList = jsonRoot.getJSONArray(QUIZ_QUESTIONS_KEY);

			// clear old values
			questions.clear();
			for (int i = 0; i < questionJSONList.length(); i++) {
				JSONObject questionJSONObj = (JSONObject)questionJSONList.get(i);

				int questionId = questionJSONObj.optInt(QUIZ_QUESTION_ID_KEY);
				String questionText = questionJSONObj.optString(QUIZ_QUESTION_KEY);
				String choices = questionJSONObj.optString(QUIZ_QUESTION_CHOICES_KEY);
				String type = questionJSONObj.optString(QUIZ_QUESTION_TYPE_KEY);
				String answer = questionJSONObj.optString(QUIZ_QUESTION_CORRECT_KEY);

				Question newQuestion = new Question(questionId, questionText, type, choices, answer);
				questions.add(newQuestion);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void startQuiz() {
		// reload content
		currentIndex = 0;
		correctAnswer = 0;
		displayQuestion(currentIndex);
	}

	private void onClickWatchAnswers() {
		watchAnswersMode = true;
		// go home, reset value
		startQuiz();
	}

	private void resetViewData() {
		// clear current answer
		adapter.setChoices(null);
		adapter.setSelectedChoices(null);
		adapter.notifyDataSetChanged();
		radioGroupChoices.clearCheck();
		txtInput.setText("");
	}

	private void onClickNext() {
		//dismiss keyboard if showing
		hideSoftKeyboard();

		if (!watchAnswersMode && currentIndex < questions.size()) {
			Question question = questions.get(currentIndex);
			// check answer
			boolean isCorrect = question.isCorrectAnswer(currentAnswers());
			if (isCorrect) {
				correctAnswer += 1;
			}
		}

		// reset current selection on views
		resetViewData();

		// increase index
		currentIndex++;

		if (currentIndex < questions.size()) {
			displayQuestion(currentIndex);
        } else if (currentIndex == questions.size()) {
			// show finish view
			displayResultView();
		} else {
			// go home, reset value
			watchAnswersMode = false;
			startQuiz();
		}
    }

	private HashSet<String> currentAnswers() {
		HashSet<String> answers = null;
		try {
			Question question = questions.get(currentIndex);

			if (question.getType().equals(Question.Type.MULTIPLE)) {
				answers = adapter.selectedAnswers();
			} else if (question.getType().equals(Question.Type.SINGLE)) {
				int selectedRadio = radioGroupChoices.getCheckedRadioButtonId();
				String choice = question.getChoices().get(selectedRadio);
				answers = new HashSet<>();
				answers.add(choice);
			} else if (question.getType().equals(Question.Type.INPUT)) {
				answers = new HashSet<>();
				String choice = txtInput.getText().toString().trim().toLowerCase();
				answers.add(choice);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return answers;
		}
	}

    private void displayQuestion(int index) {
		// check if this question is using checkbox or radio box or input
		if (index >= questions.size()) {
			Log.v(VIEW_TAG, "INDEX OUT OF BOUND, NOT SHOWING");
			return;
		}

		if (index == questions.size() - 1) {
			// final question, change text to Finish
			btnNext.setText(getString(R.string.btn_finish));
		} else {
			btnNext.setText(getString(R.string.btn_next));
		}

		Question question = questions.get(index);

		tvQuestionIndex.setVisibility(View.VISIBLE);
		tvQuestion.setVisibility(View.VISIBLE);
		viewResult.setVisibility(View.GONE);
		radioGroupChoices.setVisibility(View.GONE);
		txtInput.setVisibility(View.GONE);
		lvCheckboxGroup.setVisibility(View.GONE);
		btnWatchAnswers.setVisibility(View.GONE);

		tvQuestion.setText(question.getQuestionText());
		tvQuestionIndex.setText(String.format("%d / %d", currentIndex + 1, questions.size()));

		if (question.getType().equals(Question.Type.MULTIPLE)) {
			// Hide radiogroup, input view, show checkbox group
			lvCheckboxGroup.setVisibility(View.VISIBLE);
			loadDataCheckboxes(question.getChoices());

		} else if (question.getType().equals(Question.Type.SINGLE)) {
			// Hide checkbox group, input view, Show radio group
			radioGroupChoices.setVisibility(View.VISIBLE);
			loadDataRadioGroup(question.getChoices());

		} else if (question.getType().equals(Question.Type.INPUT)) {
			// Hide radiogroup, checkbox group, show input view
			txtInput.setVisibility(View.VISIBLE);
			loadDataTextInput(question.getAnswers().toArray()[0].toString());

		}
	}

	private void loadDataCheckboxes(ArrayList<String> choices) {
		adapter.setChoices(choices);

		Question question = questions.get(currentIndex);
		// show answer if in watch answer mode
		if (watchAnswersMode) {
			adapter.setSelectedChoices(question.getAnswers());
		} else {
			adapter.setSelectedChoices(null);
		}

		adapter.notifyDataSetChanged();
	}

	private void loadDataRadioGroup(ArrayList<String> choices) {
		radioGroupChoices.clearCheck();
		radioGroupChoices.removeAllViews();

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

		Question question = questions.get(currentIndex);

		for(int i = 0; i < choices.size(); i++) {
			String choice = choices.get(i);

			RadioButton radioButton = new RadioButton(this);
			radioButton.setLayoutParams(layoutParams);
			radioButton.setText(choice);
			radioButton.setId(i);

			radioGroupChoices.addView(radioButton);

			// show answer if in watch answer mode
			if (watchAnswersMode && question.getAnswers().contains(choice)) {
				radioButton.setChecked(true);
			}
		}
	}

	private void loadDataTextInput(String text) {
		if (watchAnswersMode) {
			txtInput.setText(text);
		} else {
			txtInput.setText("");
		}

	}

	private void displayResultView() {
		viewResult.setVisibility(View.VISIBLE);
		txtInput.setVisibility(View.GONE);
		lvCheckboxGroup.setVisibility(View.GONE);
		radioGroupChoices.setVisibility(View.GONE);
		tvQuestionIndex.setVisibility(View.GONE);
		tvQuestion.setVisibility(View.GONE);

		btnWatchAnswers.setVisibility(View.VISIBLE);

		// change text to Try Again
		btnNext.setText(getString(R.string.btn_try_again));

		// update text result
		if (watchAnswersMode) {
			tvResult.setText("");
			tvResultDescription.setText(getString(R.string.txt_watch_result_completed));
		} else {
			tvResult.setText(String.format("%d / %d", correctAnswer, questions.size()));
			tvResultDescription.setText(getString(R.string.txt_completed));
		}
	}

	public void hideSoftKeyboard() {
		if (txtInput.hasFocus()) {
			InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(txtInput.getWindowToken(), 0);
		}
	}

}
