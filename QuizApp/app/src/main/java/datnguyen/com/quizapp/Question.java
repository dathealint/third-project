package datnguyen.com.quizapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Created by datnguyen on 12/19/16.
 */

public class Question {

	private int questionId;
	private String questionText;
	private Type type;
	private ArrayList<String> choices;
	private HashSet<String> answers;

	public Question(int iQuestionId, String sQuestion, String sType, String sChoices, String sAnswers) {
		this.questionId = iQuestionId;
		this.questionText = sQuestion;
		this.type = Type.getTypeEnumByStringValue(sType);

		// parse choices
		String[] strChoices = sChoices.split(Pattern.quote(Constants.QUIZ_JSON_SEPARATOR));
		this.choices = new ArrayList<String>(Arrays.asList(strChoices));

		// parse answers
		String[] strAnswers = sAnswers.split(Pattern.quote(Constants.QUIZ_JSON_SEPARATOR));
		this.answers = new HashSet<>(Arrays.asList(strAnswers));
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public ArrayList<String> getChoices() {
		return choices;
	}

	public void setChoices(ArrayList<String> choices) {
		this.choices = choices;
	}

	public HashSet<String> getAnswers() {
		return answers;
	}

	public void setAnswers(HashSet<String> answers) {
		this.answers = answers;
	}

	// inner class
	public enum Type {
		MULTIPLE(Constants.QUIZ_QUESTION_TYPE_MULTIPLE),
		SINGLE(Constants.QUIZ_QUESTION_TYPE_SINGLE),
		INPUT(Constants.QUIZ_QUESTION_TYPE_INPUT);

		private String value;

		Type(String typeValue) {
			this.value = typeValue;
		}

		public static Type getTypeEnumByStringValue(String typeValue) {
			for (Type type: values()) {
				if (type.value.equalsIgnoreCase(typeValue)) {
					return type;
				}
			}
			return INPUT;
		}

	}

	public boolean isCorrectAnswer(HashSet<String> answers) {
		if (answers == null) {
			return false;
		}

		if (type.equals(Question.Type.MULTIPLE)) {
			return answers.equals(this.answers);
		} else if (type.equals(Question.Type.SINGLE)) {
			return answers.equals(this.answers);
		} else if (type.equals(Question.Type.INPUT)) {
			return answers.equals(this.answers);
		}
		return false;
	}

}
