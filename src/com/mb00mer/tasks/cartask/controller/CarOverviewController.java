package com.mb00mer.tasks.cartask.controller;

import com.mb00mer.tasks.cartask.bean.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Date;
import java.util.Optional;

/**
 * Класс-контроллер для основного окна приложения
 */
public class CarOverviewController {
    private Controller controller;

    @FXML
    private TableView<Car> carTable;
    @FXML
    private TableColumn<Car, String> nameColumn;
    @FXML
    private TableColumn<Car, Date> dateColumn;
    @FXML
    private TreeTableView<PartElement> carPartsTable;
    @FXML
    private TreeTableColumn<PartElement, Integer> idPartColumn;
    @FXML
    private TreeTableColumn<PartElement, String> namePartColumn;
    @FXML
    private TreeTableColumn<PartElement, Integer> countPartColumn;
    @FXML
    private TreeTableColumn<PartElement, Color> colorPartColumn;
    @FXML
    private TreeTableColumn<PartElement, String> materialPartColumn;
    @FXML
    private TreeTableColumn<PartElement, String> sizePartColumn;

    @FXML
    private void initialize() {
        // Инициализируем колонки таблицы с авто
        nameColumn.setCellValueFactory( cellData ->
                new ReadOnlyStringWrapper( cellData.getValue().getName()) );
        dateColumn.setCellValueFactory( cellData ->
                new ReadOnlyObjectWrapper<>( cellData.getValue().getDate()) );
        // Устанавливаем "слушателя" при изменении выбранной строки
        carTable.getSelectionModel().selectedItemProperty().addListener(
                (o, ov, nv) -> showCarDetails(nv) );

        // Инициализируем колонки таблицы с составом авто (правая)
        idPartColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>( cellData.getValue().getValue().getPart().getId() ));
        namePartColumn.setCellValueFactory( cellData -> new ReadOnlyStringWrapper( cellData.getValue().getValue().getPart().getName()) );
        colorPartColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>( cellData.getValue().getValue().getPart().getColor() ));
        materialPartColumn.setCellValueFactory( cellData -> new ReadOnlyStringWrapper( cellData.getValue().getValue().getPart().getMaterial()) );
        sizePartColumn.setCellValueFactory( cellData -> new ReadOnlyStringWrapper( cellData.getValue().getValue().getPart().getSize()) );
        countPartColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>( cellData.getValue().getValue().getCount() ));
    }

    /**
     * Устанавливает связь с моелью для данного контроллера
     * @param controller    Основной контроллер
     */
    public void setController(Controller controller) {
        this.controller = controller;
        // Устанавливаем источник данных для таблицы с авто
        refreshData();
        carTable.requestFocus();
        carTable.getSelectionModel().select(0);
    }

    /**
     * Вызывается при изменении выбранной строки в таблице с автомобилями
     * @param car   Car - текущий выбранный объект Car
     */
    private void showCarDetails(Car car) {
        if (car == null) {
            carPartsTable.setRoot(null);
            return;
        }
        // Строим и отображаем дерево деталей
        controller.buildPartsTree(carPartsTable, car.getPartList().asPartElementList());
    }

    /** Обработчик нажатия кнопки удаления автомобиля*/
    @FXML
    private void handleDeleteCar() {
        final int selectedIndex = carTable.getSelectionModel().getSelectedIndex();
        // Если выбраной строки нет, то ничего не делаем
        if (selectedIndex < 0) {
            return;
        }
        final Car car = carTable.getItems().get(selectedIndex);
        // Готовим диалог для подтверждения удаления авто
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтверждение удаления автомобиля");
        alert.setContentText(String.format("Вы уверены, что хотите удалить автомобиль '%s'?", car.getName()));
        // Меняем фокус по-умолчанию на кнопках
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
        // Отображаем диалог и обрабатываем результат
        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                // Удаляем авто в модели
                try {
                    controller.getModel().removeCar(car);
                    // Удаляем строку с авто из таблицы
                    carTable.getItems().remove(selectedIndex);
                }
                catch (Exception e) {
                    controller.showErrorMessage(e.getMessage());
                }
            }
        });
    }

    /** Обработчик нажатия кнопки добавления автомобиля*/
    @FXML
    private void handleAddCar() {
        // Готовим диалог для ввода названия авто
        Dialog dlg = new TextInputDialog("Автомобиль");
        dlg.setTitle("");
        dlg.setHeaderText("Введите название автомобиля:");
        // Отображаем диалог и ждем результата
        Optional<String> res = dlg.showAndWait();
        if (!res.isPresent())
            return;
        // Создаем новый автомобиль и передаем данные в модель
        Car newCar = controller.getModel().createCar(res.get());
        if (newCar == null) {
            controller.showErrorMessage("Не удалось создать автомобиль");
            return;
        }
        // Обновляем данные в таблице
        refreshData();
        carTable.getSelectionModel().select(newCar);
    }

    /**
     * Обновляет загруженные данные
     */
    public void refreshData() {
        carTable.setItems( controller.getCarData() );
    }

    /** Обработчик нажатия кнопки добавления детали в состав автомобиля */
    @FXML
    private void handleAddPartElement() {
        // Определяем выбранный автомобиль, в состав которго будет добавлена деталь
        // Если такого нет, то ничего не делаем
        int selectedIndex = carTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0)
            return;
        final Car car = carTable.getItems().get( selectedIndex );
        assert car != null;
        // Отображаем окно в режиме диалога
        PartElement partElement = controller.showPartsWindow(true);
        // Если пользователь ничего не выбрал - выходим
        if (partElement == null)
            return;
        // Пытаемся добавить деталь в состав. Если не получилось, то выводим сообщение об ошибке
        if ( !controller.getModel().addPartToCar(car, partElement) ) {
            controller.showErrorMessage("Не удалось добавить деталь в состав");
        }
        // Добавляем соответствующий объект в загруженные данные
        car.getPartList().addToPartList( partElement.getPart(), partElement.getCount() );
        // Перестраиваем дерево
        showCarDetails(car);
        // Возврашаем фокус на строку
//        carPartsTable.getSelectionModel().select( selectedIndex > carPartsTable.getExpandedItemCount()-1 ? carPartsTable.getExpandedItemCount()-1 : selectedIndex );
    }

    /** Обработчик нажатия кнопки удаления детали из состава автомобиля */
    @FXML
    private void handleRemovePartElement() {
        final int selectedIndex = carPartsTable.getSelectionModel().getSelectedIndex();
        // Если выбраной строки нет, то ничего не делаем
        if (selectedIndex < 0) {
            return;
        }
        final Car car = carTable.getItems().get( carTable.getSelectionModel().getSelectedIndex() );
        assert car !=null;
        final PartElement partElement = carPartsTable.getTreeItem(selectedIndex).getValue();
        // Пытаемся удалить деталь. Если не получилось, то выводим сообщение об ошибке
        if ( !controller.getModel().removePartFromCar(car, partElement) ) {
            controller.showErrorMessage("Не удалось удалить деталь из состава");
        }
        // Удаляем соответствующий объект из загруженных данных
        car.getPartList().removeFromPartList(partElement.getPart(), partElement.getCount());
        // Перестраиваем дерево
        showCarDetails(car);
        // Возврашаем фокус на строку
        carPartsTable.getSelectionModel().select( selectedIndex > carPartsTable.getExpandedItemCount()-1 ? carPartsTable.getExpandedItemCount()-1 : selectedIndex );
    }

    /** Обработчик нажатия кнопки отображения справочника деталей */
    @FXML
    private void showPartsWindow() {
        // Отображаем окно в режиме просмотра и редактирования справочника
        controller.showPartsWindow(false );
    }
}
