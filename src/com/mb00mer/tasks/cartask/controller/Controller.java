package com.mb00mer.tasks.cartask.controller;

import com.mb00mer.tasks.cartask.Solution;
import com.mb00mer.tasks.cartask.bean.*;
import com.mb00mer.tasks.cartask.model.*;
import javafx.collections.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 *  Класс <code>Controller<code/> управляет доступом к данным модели предметной области и выбором представления для
 *  отображения пользователю.
 */
public class Controller {
    private final String RES_MAINWINDOW = "/com/mb00mer/tasks/cartask/view/MainWindow.fxml";
    private final String RES_PARTSWINDOW = "/com/mb00mer/tasks/cartask/view/PartsWindow.fxml";
    private final String RES_PARTEDITWINDOW = "/com/mb00mer/tasks/cartask/view/PartEditWindow.fxml";

    private Stage primaryStage;
    private Stage partsStage;
    private Stage partEditStage;
    private Model model;
    private CarOverviewController mainWindowController;

    public Controller(Model model, Stage stage) {
        this.model = model;
        this.primaryStage = stage;
        this.primaryStage.setTitle("Сборка автомобилей");
        // Инициализируем и отображаем главное окно
        initMainWindow();
    }

    /**
     * Инициализирует и отображает главное окно приложения
     */
    private void initMainWindow() {
        try {
            // Загружаем главный макет из файла
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation( Solution.class.getResource(RES_MAINWINDOW) );
            Parent mainLayout = loader.load();

            mainWindowController = loader.getController();
            mainWindowController.setController( this );

            // Отображаем сцену, содержащую корневой макет
            primaryStage.setScene( new Scene( mainLayout ) );
            primaryStage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPartsStage() {
        return partsStage;
    }

    public Stage getPartEditStage() {
        return partEditStage;
    }

    /** Данные для показа списка автомобилей */
    public ObservableList<Car> getCarData() {
        return FXCollections.observableArrayList( model.getAllCars() );
    }

    /** Данные для показа справочника деталей */
    public ObservableList<PartElement> getPartsData() {
        return FXCollections.observableArrayList( model.getAllParts() );
    }

    /** Модель данных */
    public Model getModel() {
        return model;
    }

    /**
     * Отображает окно со справочником деталей
     * @param chooseMode    true - справочник отображается в режиме выбора, иначе - в режиме просмотра и редактирования
     */
    public PartElement showPartsWindow(boolean chooseMode) {
        FXMLLoader loader = new FXMLLoader();
        Parent partsLayout;
        // Загружаем макет из файла
        try {
            loader.setLocation(Solution.class.getResource(RES_PARTSWINDOW));
            partsLayout = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Создаем новое окно, но не отображаем
        partsStage = new Stage();
        partsStage.setScene( new Scene( partsLayout ) );
        partsStage.setTitle( chooseMode ? "Выберите деталь" : "Справочник деталей" );
        partsStage.initOwner( primaryStage );

        // Устанавливаем контроллер
        PartsOverviewController controller = loader.getController();
        controller.setController( this );
        controller.setChooseMode(chooseMode);

        // Инициализируем модальность окна в зависимости от переданного флага
        if (chooseMode) {
            partsStage.initModality(Modality.WINDOW_MODAL);
            partsStage.showAndWait();
            return controller.getSelectedPart();
        }
        else {
            partsStage.show();
            return null;
        }
    }

    /**
     * Отображает окно ввода или редактирования детали
     * @param part  Деталь, которая редактируется. Если null, то вводится новая
     * @return      Part - объект с заполненными полями. Для новой детали Id = -1
     */
    public Part showPartEditWindow(Part part) {
        FXMLLoader loader = new FXMLLoader();
        Parent partEditLayout;
        // Загружаем макет из файла
        try {
            loader.setLocation(Solution.class.getResource(RES_PARTEDITWINDOW));
            partEditLayout = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // Создаем новое окно, но не отображаем
        partEditStage = new Stage();
        partEditStage.setScene( new Scene( partEditLayout ) );
        partEditStage.setTitle( part == null ? "Ввод новой детали" : "Редактирование детали" );
        partEditStage.initOwner( primaryStage );
        partEditStage.initModality(Modality.WINDOW_MODAL);

        // Устанавливаем контроллер
        PartEditOverviewController controller = loader.getController();
        controller.setController( this );
        controller.setPart( part );

        partEditStage.showAndWait();
        return controller.getPart();
    }

    /**
     * Строит и отображает дерево состава
     * @param tree          Компонент TreeTableView, в который строится дерево
     * @param partElements  Список деталей
     */
    public void buildPartsTree(TreeTableView<PartElement> tree, List<PartElement> partElements) {
        if ((tree == null) || (partElements == null)) {
            return;
        }
        // Создаем корневой элемент дерева (root)
        TreeItem<PartElement> root = new TreeItem<>( new PartElement( new Part(0, "root") ) );
        // Строим дерево (рекурсивно)
        buildTreeLevel( root, partElements );
        root.setExpanded(true);
        // Отображаем дерево
        tree.setShowRoot(false);
        tree.setRoot( root );
        tree.getSelectionModel().select(0);
    }

    /**
     * Служит для построения одного уровня дерева состава. Вызывает сам себя рекурсивно для следующих уровней
     * @param treeItem      Элемент дерева, для которого строится подчиненный уровень
     * @param partElements  Список деталей для уровня
     */
    private void buildTreeLevel(TreeItem<PartElement> treeItem, List<PartElement> partElements) {
        for (PartElement partElement : partElements) {
            TreeItem<PartElement> ti = new TreeItem<>(partElement);
            treeItem.getChildren().add( ti );
            buildTreeLevel(ti, partElement.getPart().getPartList().asPartElementList());
        }
    }

    /** Показывает окно с сообщением об ошибке */
    public void showErrorMessage(String msg) {
        String errText = "Ошибка";
        if (msg == null)
            msg = errText;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(errText);
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    /** Инициирует обновление данных в главном окне */
    public void refreshMainWindow() {
        mainWindowController.refreshData();
    }
}
