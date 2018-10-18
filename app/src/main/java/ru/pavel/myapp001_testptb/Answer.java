package ru.pavel.myapp001_testptb;

/**
 * Created by Pavel on 18.10.2018.
 * answer only include text answer and mark correct or incorrect
 */

public class Answer {
    public Answer(String answer, boolean mark){
        this.answer = answer;
        this.mark = mark;
    }

    private String answer;
    private boolean mark;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isCorrect() {
        return mark;
    }

    public void setCorrect(boolean mark) {
        this.mark = mark;
    }
}
