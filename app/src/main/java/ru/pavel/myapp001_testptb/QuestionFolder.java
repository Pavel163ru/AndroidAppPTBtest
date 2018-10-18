package ru.pavel.myapp001_testptb;

import java.util.List;

/**
 * Created by Pavel on 18.10.2018.
 * this is singleton for all questions
 */

public class QuestionFolder {
    private List<Question> questions;
    private int currentQuestion;
    private QuestionFolder instance;

    private QuestionFolder(){}

    public QuestionFolder make(){

        return instance;
    }

}
