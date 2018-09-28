package ru.pavel.myapp001_testptb;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Pavel on 20.01.2018.
 */

public class Question implements Serializable {
    String question; //вопрос
    String answer; //один ответ для передачи
    int grade; //текущая оценка
    long lastdate; //дата последнего показа
    int startpoint; //начальная позиция для считывания вопросов и ответов

    ArrayList<String> answers = new ArrayList<>(); //Массив ответов
    ArrayList<Boolean> answersMark = new ArrayList<>(); //Массив меток правильного ответа
    //int answerIndex;
    int answerQuantity;

    public int getAnswerQuantity() {
        answerQuantity = answers.size();
        return answerQuantity;
    }

    private boolean answerMark;


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer(int index) {
        if (index<answers.size()){
            answer = answers.get(index);
        }else answer = "error";
        return answer;
    }

    public boolean getАnswerMark(int index) {
        if (index<answersMark.size()){
            answerMark = answersMark.get(index).booleanValue();
        }
        return answerMark;
    }

    public void addAnswer(String answer, boolean mark){
        if(answer!=null){
            answers.add(answer);
            answersMark.add(mark);
        }
    }


}
