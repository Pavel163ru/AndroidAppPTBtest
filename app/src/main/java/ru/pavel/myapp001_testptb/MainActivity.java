package ru.pavel.myapp001_testptb;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;



public class MainActivity extends AppCompatActivity {

    //private static final String FILENAME = "file";
    private static final String TAG = "MyApp";
    private static final String FILE_NAME = "quiz.txt";

    static final int MENU_TEST = 1;
    static final int MENU_TEACH = 2;
    static final int MENU_EXIT = 0;
    static final int MENU_STATISTIC = 3;
    static final int MENU_SETTINGS = 4;
    static final String MENU_RESULT = "result";

    static final String PREF_SCANNED = "scanned"; //boolean
    static final String PREF_QUESTIONS = "questions"; //integer
    private SharedPreferences sharedPreferences;
    boolean prefScanned = false;

    final String DB_STARTPOINT = "startpoint";
    final String DB_GRADE = "grade";
    final String DB_DATE = "lastdate";
    final String DB_MILLIS_DATE = "millis_date";
    final String DB_NAME = "grades";

    private String fileQuiz; //file with questions convert to string for next operation with it
    private Question newQuestion; //class for make question
    //private int countTest = 0; //number questions was show on display
    private int countPassQuestions = 0; //count number question pass right now
    private int rightAnswers = 0; //count right answers on questions

    ArrayList<Integer> pointQuestions = new ArrayList<>(); //array with all start point questions
    private int currentPointQuestion;
    private int currentGrade;
    private int currentMode = 1;
    //ArrayList<Boolean> arrayRepeat = new ArrayList<>(); //array for off repeat questions
    private int numberQuestions = 0; //общее количество вопросов

    ArrayList<Integer> pointQuestionsGrade0 = new ArrayList<>();
    ArrayList<Integer> pointQuestionsGrade1 = new ArrayList<>();
    ArrayList<Integer> pointQuestionsGrade2 = new ArrayList<>();
    ArrayList<Integer> pointQuestionsGrade3 = new ArrayList<>();
    ArrayList<Integer> pointQuestionsGrade4 = new ArrayList<>();
    ArrayList<Integer> pointQuestionsGrade5 = new ArrayList<>();


    TextView mTvInfo;
    TextView mTvQuestion;
    TextView mTvWrong;
    TextView mTvRight;
    Button mButton;
    CheckBox mChAnswer1;
    CheckBox mChAnswer2;
    CheckBox mChAnswer3;
    CheckBox mChAnswer4;
    CheckBox mChAnswer5;
    CheckBox mChAnswer6;
    //ArrayList<CheckBox> mAnswers = new ArrayList<>();
    CheckBox[] mChAnswers;

    boolean viewNewQuestion = true;

    DBHelper dbHelper;

    View.OnClickListener onClickNextButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            log("Button pressed");


            //loopTeachMode();
            //loopLogic();
            modeSwitch();
        }
    };

    //todo всплывающее окно с результатом теста и началом нового
    //method unity all logic program. Draw new question and draw right answer
    void loopLogic(){

        //проверяем нужно выводить новый вопрос или проверить ответ пользователя
        if (viewNewQuestion){
            //doQuiz(pointQuestions.get(randomIndexQuestion()));
            //randomIndexQuestion();
            //drawQuestion();

            /*
            //for only one time show each quiz
            int index = randomIndexQuestion();
            boolean repeat = arrayRepeat.get(index);
            if (repeat){
                log("It's quiz already pass");
                index = -1;
                while(repeat&&(pointQuestions.size()>(index+1))){
                    index++;
                    log("Search new quiz in index = "+index);
                    repeat = arrayRepeat.get(index);
                }
                if (((index+1) == pointQuestions.size())&&repeat){
                    countTest = 0;
                    countPassQuestions = 0;
                    rightAnswers = 0;
                    index = 0;
                    log("Pass all quiz, start from first question");
                    for (int i=0; arrayRepeat.size()>i; i++){
                        arrayRepeat.set(i, false);
                    }
                }
            }
            */
            //готовим случайный вопрос
            int index;
            if(pointQuestions.size()>1){
                Random random = new Random();
                int max = pointQuestions.size()-1;
                int min = 0;
                index = random.nextInt(((max-min)+1)+min);
            }else if(pointQuestions.size()==1){
                index = 0;
            }else{
                //countTest = 0;
                countPassQuestions = 0;
                rightAnswers = 0;
                index = 0;
                log("Pass all quiz, start from first question");
                searchAllQuestions();
            }
            log("Random index = "+index+" of "+pointQuestions.size());
            currentPointQuestion = pointQuestions.get(index);
            pointQuestions.remove(index);
            log("There are "+pointQuestions.size()+" questions left");


            //newQuestion = makeQuiz(randomPointQuestion(),fileQuiz);
            //newQuestion = makeQuiz(pointQuestions.get(index),fileQuiz);

            newQuestion = makeQuiz(currentPointQuestion,fileQuiz);//создаем вопрос с ответами
            //arrayRepeat.set(index, true); //mark pass quiz

            drawQuestion(newQuestion);//выводим вопрос на экран
            //countTest++;


        }else{
            //checkAnswersAndDrawRight();
            checkAnswersAndDrawRight(newQuestion);
            countPassQuestions++;


            //log how more quiz left
            /*
            int countQuizForShow=0;
            for(int i=0; arrayRepeat.size()>i; i++){
                if(!arrayRepeat.get(i)){
                    countQuizForShow++;
                }
            }
            log("quiz left: "+countQuizForShow);
            */
        }
        viewNewQuestion = !viewNewQuestion;
        drawInfo();

    }

    //todo проверить работоспсобность
    //сохраняем новую оценку и время в таблицу
    void saveCurrentGrade(){
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        //поиск строки для изменения
        String selection = DB_STARTPOINT+ " = ?";
        String[] selectionArg = new String[]{String.valueOf(currentPointQuestion)};

        ContentValues contentValues = new ContentValues();
        long currentMillis = System.currentTimeMillis();
        contentValues.put(DB_GRADE, currentGrade);
        contentValues.put(DB_MILLIS_DATE, String.valueOf(currentMillis));

        DateFormat df = new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss", Locale.US);
        String date = df.format(new Date());
        contentValues.put(DB_DATE, date);

        int result = sqLiteDatabase.update(DB_NAME, contentValues , selection, selectionArg);
        log("Строк изменено = "+result);

        dbHelper.close();
    }

    void readGrades(){
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        Cursor cursor;
        /*
        cursor = sqLiteDatabase.query(DB_NAME, null, "grade = 3", null, null, null, null);
        if (cursor.moveToFirst()){
            log("question with grade = 1 was found");
            log("row "+cursor.getPosition()+" is last: "+cursor.isLast());
            cursor.moveToLast();
            log("last row is "+cursor.getPosition());
            log("id " + cursor.getInt(0));

            log("grades 0 is "+pointQuestionsGrade0.size());

        }else log("there is not question with grade = 1");
        */

        //массив с позициями вопросов групированный по оценкам
        ArrayList[] arrayLists = new ArrayList[]{pointQuestionsGrade0, pointQuestionsGrade1,
                pointQuestionsGrade2,pointQuestionsGrade3,pointQuestionsGrade4,pointQuestionsGrade5};
        long[] timeIntervals = new long[]{0L, 20L, 180L, 1440L, 4320L, 43200L}; //задаем интервал повтора вопроса для каждой оценки
        //получаем текущее время в милисекундах
        long currentMillis = System.currentTimeMillis();

        //экперименты с классом Date и форматированием даты
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss", Locale.US);
        Date inDate = new Date();
        inDate.setTime(currentMillis);
        String date = df.format(inDate);

        log("Current time = "+currentMillis+" or date "+date);

        for (int i=0; i<6; i++){
            arrayLists[i].clear();

            long timeInterval = currentMillis-(timeIntervals[i]*60000L);

            inDate.setTime(timeInterval);
            date = df.format(inDate);
            log("Time interval = "+timeInterval+" or date "+date);

            String selection = DB_GRADE + " = ? and "+DB_MILLIS_DATE+" < ?";
            //log("searching "+String.valueOf(timeInterval));
            String[] selectionArg = new String[]{String.valueOf(i), String.valueOf(timeInterval)};

            cursor = sqLiteDatabase.query(DB_NAME, null, selection, selectionArg, null, null, null);
            if (cursor.moveToFirst()){
                do{
                    arrayLists[i].add(cursor.getInt(cursor.getColumnIndex(DB_STARTPOINT)));
                }while(cursor.moveToNext());
            }
            cursor.close();
        }

        for (int i=0; i<6; i++){
            log("Questions with grade "+ i +" is "+arrayLists[i].size());
        }
        log("Quantity questions = "+(pointQuestionsGrade0.size()+pointQuestionsGrade1.size()
                +pointQuestionsGrade2.size()+pointQuestionsGrade3.size()+pointQuestionsGrade4.size()+pointQuestionsGrade5.size()));

        /*проврка считывания таблицы
        cursor = sqLiteDatabase.query(DB_NAME, null, null, null, null, null, null);
        ArrayList<Integer> testArray = new ArrayList<>();
        if(cursor.moveToFirst()){
            int point;
            do{
                point = cursor.getInt(cursor.getColumnIndex(DB_STARTPOINT));
                testArray.add(point);
                log("Read point "+point);
            }while(cursor.moveToNext());
            log("test read database, read "+testArray.size()+" row");
        }
        */


        //cursor.close();
        dbHelper.close();
    }

    //запуск сканирования файлов с вопросами и создание таблицы с оценками
    //выполняется при первом запуске
    void firstStartMakeSQLiteTable (){
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        //очищаем таблицу на всякий случай
        log( "--- Clear table of grades: ---");
        // удаляем все записи
        int clearCount = sqLiteDatabase.delete("grades", null, null);
        log("deleted rows count = " + clearCount);

        //берем текущее время для записи в таблицу
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss", Locale.US);
        String date = df.format(new Date());

        long millis = System.currentTimeMillis();
        String millisString = millis+"";

        ContentValues contentValues = new ContentValues();
        for (Integer point: pointQuestions){
            contentValues.put(DB_STARTPOINT, point);

            //todo убрать случайную генерацию оценок
            Random rand = new Random();
            int max = 5;
            int min = 0;
            int random = rand.nextInt(((max-min)+1)+min);
            random = 0;


            contentValues.put(DB_GRADE, random);
            contentValues.put(DB_DATE, date);
            contentValues.put(DB_MILLIS_DATE, millisString);
            sqLiteDatabase.insert(DB_NAME, null, contentValues);
        }
        dbHelper.close();
        prefScanned = true;
        savePrefs();
    }
    void printSQLiteTable (){
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        //---чтение таблицы
        log("--- Row in table of grade: ---");
        Cursor c = sqLiteDatabase.query(DB_NAME, null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()){
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("grade");
            int numberColIndex = c.getColumnIndex("startpoint");
            int dateColIndex = c.getColumnIndex("lastdate");
            int millisColIndex = c.getColumnIndex(DB_MILLIS_DATE);
            do {
                log("ID = " + c.getInt(idColIndex) +
                        ", grade = " + c.getInt(nameColIndex) +
                        ", point = " + c.getInt(numberColIndex)+
                        ", date = " + c.getString(dateColIndex)+
                        ", millis = " + c.getString(millisColIndex));
            } while (c.moveToNext());
        } else  log("0 rows");
        c.close();
        dbHelper.close();
    }

    //временный метод для проверки sqlite
    void testSQLite (DBHelper dbHelper){
        ContentValues cv = new ContentValues();
        String name = "just text";
        int number = 777;

        //эксперименты со временем
        /*
        long timeMillis = System.currentTimeMillis();
        int timeSeconds = (int) timeMillis/1000;
        log ("Current time millis = "+timeMillis+ " or Seconds = "+timeSeconds);

        Calendar rightNow = Calendar.getInstance();
        log (rightNow.toString());
        */
        //better use
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss", Locale.US);
        String date = df.format(new Date()); // new Date() создает объект с текущим временем, его и переводим в строку
        log("Right now: "+date);
        //convert string to date with same pattern
        Date convertedDate = new Date();
        try {
            convertedDate = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        log("String to Date: "+convertedDate.toString());


        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //---запись в таблицу
        //подготовка данных ключ-данные, для зиписи в таблицу
        cv.put("name", name);
        cv.put("startpoint", number);
        cv.put("lastdate", date);
        //пишем строку в таблицу
        long rowID = db.insert("mytable", null, cv);
        log("row inserted, ID = " + rowID);

        //---чтение таблицы
        log("--- Row in mytable: ---");
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()){
            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int numberColIndex = c.getColumnIndex("startpoint");
            int dateColIndex = c.getColumnIndex("lastdate");
            do {
                log("ID = " + c.getInt(idColIndex) +
                        ", name = " + c.getString(nameColIndex) +
                        ", number = " + c.getInt(numberColIndex)+
                        ", date = " + c.getString(dateColIndex));
            } while (c.moveToNext());
        } else  log("0 rows");

        c.close(); //закрываем курсор

        /*
        //очистка таблицы
        log( "--- Clear mytable: ---");
        // удаляем все записи
        int clearCount = db.delete("mytable", null, null);
        log("deleted rows count = " + clearCount);
        */

        dbHelper.close();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent intent = new Intent(this, MainMenu.class);
        //startActivityForResult(intent, 1);

        mTvInfo = (TextView) findViewById(R.id.mTvInfo);
        mTvQuestion = findViewById(R.id.mTvQuestin);
        mButton = findViewById(R.id.mBtNext);
        mChAnswer1 = (CheckBox) findViewById(R.id.mCbAnswer1);
        mChAnswer2 = (CheckBox) findViewById(R.id.mCbAnswer2);
        mChAnswer3 = (CheckBox) findViewById(R.id.mCbAnswer3);
        mChAnswer4 = (CheckBox) findViewById(R.id.mCbAnswer4);
        mChAnswer5 = findViewById(R.id.mCbAnswer5);
        mChAnswer6 = findViewById(R.id.mCbAnswer6);
        //make array of Answer CheckBox
        mChAnswers = new CheckBox[] {mChAnswer1,mChAnswer2,mChAnswer3,mChAnswer4,mChAnswer5,mChAnswer6};

        mTvWrong = findViewById(R.id.tvWrong);
        mTvRight = findViewById(R.id.tvRight);



        //writeFile();




        //newQuestion = makeQuiz(randomPointQuestion(),fileQuiz);
        //drawQuestion(newQuestion);
        //drawInfo();
        //doQuiz(0);
        //drawQuestion();


        mButton.setOnClickListener(onClickNextButton);

        fileQuiz = readQuizRaw(); //читаем файл с вопросами


        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

        startApp();

        //проверка sqlite, сохранение, чтение и т.д.
        //testSQLite(dbHelper);



        //loopTeachMode();//режим обечения
        //loopLogic();//запуск тестирования
    }

    //test write file
    void writeFile() {
        try {
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILE_NAME, MODE_PRIVATE)));
            // пишем данные
            bw.write("Содержимое файла \r\n Новая строка \n");
            // закрываем поток
            bw.close();
            Log.i(TAG, "Файл записан");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //old version read quiz file
    String readQuizFiles() {
        FileInputStream file = null;
        String text = null;
        try {
            file = openFileInput(FILE_NAME);
            byte[] bytes = new byte[file.available()];
            file.read(bytes);
            text = new String(bytes);
            Log.i(TAG, text);
        }
        catch(IOException ex) {

            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{

            try{
                if(file!=null)
                    file.close();
            }
            catch(IOException ex){

                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return text;

    }

    //read text file with quiz and save to String variable
    String readQuizRaw(){
        String text = null;
        Resources res = this.getResources();
        InputStream file = res.openRawResource(R.raw.quizall);
        try {
            byte[] bytes = new byte[file.available()];
            file.read(bytes);
            text = new String(bytes);
        }
        catch(IOException ex){
        }
        try {
            file.close();
        }
        catch (IOException e){
        }
        return text;
    }



    // test method for parse quiz for question and answer
    boolean[] answerMark = new boolean[10];
    String[] answerText = new String[10];
    String question;
    // in - fileQuiz - search questions and answers
    // line is start point for search QUESTION
    @Deprecated
    void doQuiz(int line){
        int countAnswer=0;
        char chLookFor;
        int pointStartQuestion;
        int pointEndQuestion;
        boolean searchQuestion = true;
        boolean searchTicket = true;
        boolean searchText = true;
        boolean newLine = true;
        answerText = new String[10];

        for (int i=line; (i<fileQuiz.length())&&searchTicket; i++){
            chLookFor = fileQuiz.charAt(i);
            if (chLookFor=='\n'){
                newLine = true;
            }
            if ((chLookFor == '?')&&(searchQuestion)){
                newLine = false;
                Log.i(TAG, "Вопрос найден");

                while(chLookFor!='\n'){
                    i++;
                    chLookFor = fileQuiz.charAt(i);
                }
                i++;
                pointStartQuestion = i;
                for (searchText = true;(i<fileQuiz.length())&&searchText; i++){
                    chLookFor = fileQuiz.charAt(i);
                    if (chLookFor == '\n'){
                        pointEndQuestion = i-1;
                        question = fileQuiz.substring(pointStartQuestion, pointEndQuestion);
                        searchText = false;
                        searchQuestion = false;
                    }
                }
            }
            if ((chLookFor=='-')||(chLookFor=='+')){
                newLine = false;
                Log.i(TAG,"Найден ответ "+countAnswer);
                if (chLookFor=='-') answerMark[countAnswer]=false;
                if (chLookFor=='+') answerMark[countAnswer]=true;
                pointStartQuestion = i+1;

                for(searchText = true;(i<fileQuiz.length())&&searchText; i++){
                    chLookFor = fileQuiz.charAt(i);
                    if (chLookFor == '\n'){
                        pointEndQuestion = i;
                        answerText[countAnswer] = fileQuiz.substring(pointStartQuestion, pointEndQuestion);
                        countAnswer++;
                        searchText = false;
                        i--;
                    }
                }
            }
            if((chLookFor=='?')&&newLine) searchTicket = false;
        }

        //view result in log
        Log.i(TAG, question);
        for (int i=0;i<10;i++){
            if (answerText[i]==null) return;
            //if (answerText[i].isEmpty()) return;
            Log.i(TAG, answerMark[i]+"  "+answerText[i]);
        }
    }

    //новая версия с использованием класса Question
    //line - начало вопроса, text - весь текст с вопросами и ответами
    Question makeQuiz(int line, String text){
        Question question = new Question();
        if (line<text.length()){
            char chLookFor;
            int pointStartQuestion;
            int pointEndQuestion;
            boolean makingQuestion = true;
            boolean markAnswer = false;

            for (int i=line; i<text.length()&&makingQuestion; i++ ){
                chLookFor = text.charAt(i);
                //search start question
                if (chLookFor!='\n'&&(chLookFor!='\r')&&(chLookFor!=' ')&&(chLookFor!='?')){
                    pointStartQuestion = i;
                    //search end question
                    for (;i<text.length()&&makingQuestion; i++){
                        chLookFor = text.charAt(i);
                        if (chLookFor=='\n'){
                            pointEndQuestion = i;
                            question.setQuestion(text.substring(pointStartQuestion, pointEndQuestion));
                            //log("Question is found");
                            //search all answers
                            for (;i<text.length()&&makingQuestion; i++){
                                chLookFor = text.charAt(i);
                                if (chLookFor=='-'||(chLookFor=='+')){
                                    if (chLookFor=='-') markAnswer = false;
                                    if (chLookFor=='+') markAnswer = true;
                                    pointStartQuestion = i+1;
                                    //search end answer
                                    while(i<text.length()&&(chLookFor!='\n')){
                                        i++;
                                        chLookFor = text.charAt(i);
                                    }
                                    pointEndQuestion = i;
                                    //add new answer with mark
                                    question.addAnswer(text.substring(pointStartQuestion,pointEndQuestion), markAnswer);
                                    //log ("Answer is found");

                                // all answers end
                                }else if (chLookFor!='\n'&&(chLookFor!='\r')&&(chLookFor!=' ')) {
                                    makingQuestion = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        //вывод готового вопроса в консоль
        log("Вопрос : "+question.getQuestion());
        for (int i=0; i<question.getAnswerQuantity(); i++){
            log("Ответ "+i+question.getАnswerMark(i)+question.getAnswer(i));
        }

        return question;
    }

    //pointQuestions ArrayList
    void searchAllQuestions(){
        log("Search questions ...");
        pointQuestions.clear();

        boolean newLine = true;
        char chLookFor;
        for (int i=1; i<fileQuiz.length(); i++){
            chLookFor = fileQuiz.charAt(i);
            if ((chLookFor=='?')&&newLine){
                pointQuestions.add(i);
                //log("point found = "+i);
                //arrayRepeat.add(false); //add repeat mark
            }else if (chLookFor=='\n'){
                newLine=true;
            }else if (chLookFor!=' '){
                newLine=false;
            }

        }
        numberQuestions = pointQuestions.size();
        Log.i(TAG, "Вопросов найдено : "+numberQuestions);
    }
    void log(String text){
        Log.i(TAG,text);
    }

    //return random index of question
    int randomIndexQuestion(){
        int random = 0;
        if(pointQuestions.size()>3){
            Random rand = new Random();
            int max = pointQuestions.size()-1;
            int min = 0;
            random = rand.nextInt(((max-min)+1)+min);
        }
        log("Random index = "+random+" of "+pointQuestions.size());
        return random;
    }
    //return point question for use in drawQuestion;
    int randomPointQuestion(){
        int random = pointQuestions.get(randomIndexQuestion());
        return random;
    }

    //todo добавить всплывающее окно когда вопросы закончатся
    void loopTeachMode(){
        if(viewNewQuestion){
            Random random = new Random();
            int max = 100;
            int min = 0;
            int randomNumber = random.nextInt(((max-min)+1)+min);
            log("Random grade = "+randomNumber);

            do { //немного криво написанное сканирование, нужно будет доработать
                if (randomNumber > 40 && !(pointQuestionsGrade1.size() == 0)) {
                    //оценка 1
                    currentPointQuestion = pointQuestionsGrade1.get(pointQuestionsGrade1.size() - 1);
                    pointQuestionsGrade1.remove(pointQuestionsGrade1.size() - 1);
                    randomNumber = 150;
                } else if (randomNumber > 20 && !(pointQuestionsGrade0.size() == 0)) {
                    //grade 0
                    currentPointQuestion = pointQuestionsGrade0.get(pointQuestionsGrade0.size() - 1);
                    pointQuestionsGrade0.remove(pointQuestionsGrade0.size() - 1);
                    randomNumber = 150;
                } else if (randomNumber > 10 && !(pointQuestionsGrade2.size() == 0)) {
                    //grade 2
                    currentPointQuestion = pointQuestionsGrade2.get(pointQuestionsGrade2.size() - 1);
                    pointQuestionsGrade2.remove(pointQuestionsGrade2.size() - 1);
                    randomNumber = 150;
                } else if (randomNumber > 5 && !(pointQuestionsGrade3.size() == 0)) {
                    //grade 3
                    currentPointQuestion = pointQuestionsGrade3.get(pointQuestionsGrade3.size() - 1);
                    pointQuestionsGrade3.remove(pointQuestionsGrade3.size() - 1);
                    randomNumber = 150;
                } else if (randomNumber > 2 && !(pointQuestionsGrade4.size() == 0)) {
                    //grade 4
                    currentPointQuestion = pointQuestionsGrade4.get(pointQuestionsGrade4.size() - 1);
                    pointQuestionsGrade4.remove(pointQuestionsGrade4.size() - 1);
                    randomNumber = 150;
                } else if (!(pointQuestionsGrade5.size() == 0)) {
                    //grade 5
                    currentPointQuestion = pointQuestionsGrade5.get(pointQuestionsGrade5.size() - 1);
                    pointQuestionsGrade5.remove(pointQuestionsGrade5.size() - 1);
                    randomNumber = 150;
                } else {

                    //todo реализовать сценарий когда вопросы для обучения закончились

                    if(randomNumber==101){
                        log("There isn't more question for teaching");
                        randomNumber = 150;//завершаем поиск

                        //Если больше нет вопросов выкидываем сообщение
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Вопросы для обучения закончились").setTitle("Запустить Тест?");

                        // Add the buttons
                        builder.setPositiveButton("Тестирование", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                currentMode = 1;
                                startApp();
                                return;
                            }
                        });
                        builder.setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                finish();
                            }
                        });
                        // Set other dialog properties


                        // Create the AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else{
                        randomNumber = 101;//проверяем все оставшиеся вопросы
                        log("let's try to find another question");
                    }
                }
            }while(randomNumber!=150);

            log("Grades 5*="+pointQuestionsGrade5.size()+" 4*="+pointQuestionsGrade4.size()
                    +" 3*="+pointQuestionsGrade3.size()+" 2*="+pointQuestionsGrade2.size()
                    +" 1*="+pointQuestionsGrade1.size()+" 0*="+pointQuestionsGrade0.size());

            readCurrentGrade();

            newQuestion = makeQuiz(currentPointQuestion,fileQuiz);//создаем вопрос с ответами
            drawQuestion(newQuestion);//выводим вопрос на экран

        }else{
            boolean result = checkAnswersAndDrawRight(newQuestion);
            if(result){
                changeCurrentGrade(1);
            }else{
                changeCurrentGrade(-1);
            }
            saveCurrentGrade();
        }
        viewNewQuestion = !viewNewQuestion;
        drawTeachInfo();
    }
    void startApp(){
        viewNewQuestion = true;
        countPassQuestions = 0;
        rightAnswers = 0;

        searchAllQuestions(); //поиск всех вопросов в файле
        loadPrefs(); //загрузка настроек программы
        if(!prefScanned){
            log("Launch first scanning ...");
            firstStartMakeSQLiteTable();//создание базы данных с оценками при первом запуске
        }

        printSQLiteTable();//проверочная печать базы данных
        readGrades(); //чтение оценок из базы данных
        modeSwitch(); //запуск логики приложения в первый раз
    }

    void modeSwitch(){
        switch (currentMode){
            case 1:
                loopLogic();
                break;
            case 2:
                loopTeachMode();
                break;
            case 0:
                finish();
                break;
            default:
                break;
        }
    }
    void readCurrentGrade (){

        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor;
        String selection = DB_STARTPOINT + " = ?";
        String[] selectionArg = new String[]{String.valueOf(currentPointQuestion)};
        //String[] columns = new String[]{DB_GRADE};
        log("current point = "+currentPointQuestion);

        cursor = sqLiteDatabase.query(DB_NAME,null , selection, selectionArg, null, null, null);

        cursor.moveToFirst();
        currentGrade = cursor.getInt(cursor.getColumnIndex(DB_GRADE));
        log("Current grade "+currentGrade);

        cursor.close();
        dbHelper.close();
    }

    //меняем оценку
    void changeCurrentGrade(int value){
        if(value==1){
            if(currentGrade==0){
                currentGrade = 2;//
            }else if(currentGrade<5) {
                currentGrade += 1;
            }
        }else if(value==-1){
            if(currentGrade > 3){
                currentGrade = 1;
            }else if(currentGrade>1){
                currentGrade -= 1;
            }
        }else{
            log("Ошибка ввода!!! оценка не изменена");
        }
    }

    @Deprecated
    @SuppressLint("ResourceType")
    void drawQuestion (){
        mTvQuestion.setText(question);
        mTvRight.setVisibility(View.GONE);
        mTvWrong.setVisibility(View.GONE);
        log(question);
        for (int i=0;i<mChAnswers.length;i++){
            mChAnswers[i].setTextColor(Color.parseColor(this.getResources().getString(R.color.colorAnswer)));
            mChAnswers[i].setChecked(false);
            if (answerText[i]!=null){
                mChAnswers[i].setText(answerText[i]);
                mChAnswers[i].setVisibility(View.VISIBLE);
                log(answerText[i]);
            }else {
                mChAnswers[i].setVisibility(View.GONE);
            }
        }
    }

    // new draw for question class
    @SuppressLint("ResourceType")
    void drawQuestion (Question question){
        if (question!=null){
            mTvQuestion.setText(question.getQuestion());
            mTvRight.setVisibility(View.GONE);
            mTvWrong.setVisibility(View.GONE);
            for (int i=0;i<mChAnswers.length;i++){
                mChAnswers[i].setTextColor(Color.parseColor(this.getResources().getString(R.color.colorAnswer)));
                mChAnswers[i].setChecked(false);
                if (i<question.getAnswerQuantity()){
                    mChAnswers[i].setText(question.getAnswer(i));
                    mChAnswers[i].setVisibility(View.VISIBLE);
                }else {
                    mChAnswers[i].setVisibility(View.GONE);
                }
            }
        }
    }

    @Deprecated
    @SuppressLint("ResourceType")
    boolean checkAnswersAndDrawRight (){
        boolean correctAnswer;
        boolean checkAnswer;
        boolean choice = true;
        for (int i=0;(i<mChAnswers.length)&&(answerText[i]!=null);i++){
            checkAnswer = mChAnswers[i].isChecked();
            correctAnswer = answerMark[i];
            if (correctAnswer!=checkAnswer) {
                choice = false;
                if (correctAnswer == true) {
                    mChAnswers[i].setTextColor(Color.parseColor(this.getResources().getString(R.color.colorNotChoice)));
                } else {
                    mChAnswers[i].setTextColor(Color.parseColor(this.getResources().getString(R.color.colorWrongChoice)));
                }
            }
        }
        if (choice){
            mTvRight.setVisibility(View.VISIBLE);
        }else{
            mTvWrong.setVisibility(View.VISIBLE);
        }


        return choice;
    }

    //проверяем ответ, выводим верное решение
    //возвращаем результат ответа
    @SuppressLint("ResourceType")
    boolean checkAnswersAndDrawRight (Question question){
        boolean correctAnswer;
        boolean checkAnswer;
        boolean choice = true; //результат Истина - правильный
        for (int i=0;(i<mChAnswers.length)&&(i<question.getAnswerQuantity());i++){
            checkAnswer = mChAnswers[i].isChecked();
            correctAnswer = question.getАnswerMark(i);
            if (correctAnswer!=checkAnswer) {
                choice = false;
                if (correctAnswer == true) {
                    mChAnswers[i].setTextColor(Color.parseColor(this.getResources().getString(R.color.colorNotChoice)));
                } else {
                    mChAnswers[i].setTextColor(Color.parseColor(this.getResources().getString(R.color.colorWrongChoice)));
                }
            }
        }
        if (choice){
            mTvRight.setVisibility(View.VISIBLE);
            rightAnswers++;
        }else{
            mTvWrong.setVisibility(View.VISIBLE);
        }

        return choice;
    }

    void drawInfo (){
        String info;
        String result;
        if (countPassQuestions>0){
            result = " ("+((rightAnswers)*100/countPassQuestions)+"%)";
        }else result = " ";

        info = "Вопрос "+(numberQuestions-pointQuestions.size())+" из "+numberQuestions+". Верных ответов "+rightAnswers+result;
        mTvInfo.setText(info);
    }
    void drawTeachInfo (){
        String info;
        int quantity = (pointQuestionsGrade0.size()+pointQuestionsGrade1.size()
                +pointQuestionsGrade2.size()+pointQuestionsGrade3.size()
                +pointQuestionsGrade4.size()+pointQuestionsGrade5.size());

        info = "Осталось: "+quantity+" вопр. Оценка: "+currentGrade+"*  ";
        mTvInfo.setText(info);
    }

    void loadPrefs(){
        sharedPreferences = getPreferences(MODE_PRIVATE);
        prefScanned = sharedPreferences.getBoolean(PREF_SCANNED, false);
        log("Prefs loaded");
    }

    void savePrefs(){
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editorPrefs = sharedPreferences.edit();
        editorPrefs.putBoolean(PREF_SCANNED, prefScanned);
        editorPrefs.commit();
        log("Prefs saved");
    }

    void changeDBgrades(int point, int grade, String millisDate){
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB_GRADE, grade);
        contentValues.put(DB_MILLIS_DATE, millisDate);

        String whereClause = DB_STARTPOINT+" = ?";
        String[] whereArgs = new String[]{point+""};

        sqLiteDatabase.update(DB_NAME, contentValues, whereClause, whereArgs);

        dbHelper.close();
    }

    //обработка результата работы со вторым Активити
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){return;}
        int result = data.getIntExtra(MainActivity.MENU_RESULT, 0);
        //выход
        if (result==MainActivity.MENU_EXIT){
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 101, Menu.NONE, "Тестирование");
        menu.add(Menu.NONE, 102, Menu.NONE, "Обучение");
        menu.add(Menu.NONE,100,Menu.NONE, "Выход");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 101:
                currentMode = 1;
                startApp();
                break;
            case 102:
                currentMode = 2;
                startApp();
                break;
            case 100:
                currentMode = 0;
                modeSwitch();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    //используем sqlite
    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context){
            super(context, "myDB", null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "--- onCreate database ---" );
            db.execSQL("create table mytable ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "startpoint integer,"
                + "grade integer,"
                + "lastdate text"+ ");");
            db.execSQL("create table grades ("
                    + "id integer primary key autoincrement,"
                    + "startpoint integer,"
                    + "grade integer,"
                    + "lastdate text,"
                    + "millis_date text"+ ");"); //создаем таблицу с рейтингом вопросов
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(newVersion == 2){
                log("--- onUpgrade database v2---");
                db.execSQL("ALTER TABLE grades ADD COLUMN millis_date text");
            }

        }
    }
}


