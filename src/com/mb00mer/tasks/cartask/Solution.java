package com.mb00mer.tasks.cartask;

import com.mb00mer.tasks.cartask.controller.Controller;
import com.mb00mer.tasks.cartask.model.*;
import javafx.application.Application;
import javafx.stage.Stage;


public class Solution extends Application {
    private Controller controller;
    private static Model model;

    /** Метод main: создает модель и запускает приложение */
    public static void main(String[] args) throws Exception {
        // Создаем модель. try-with-resources обеспечивает ее корректное завершение
        try( Model model = new MainModel() ) {
            Solution.model = model;
            launch(args);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Создаем основной контроллер, который запускает главное окно
        controller = new Controller(Solution.model, primaryStage);
    }
}
