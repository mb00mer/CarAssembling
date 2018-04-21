package com.mb00mer.tasks.cartask.controller;

import com.mb00mer.tasks.cartask.bean.Color;
import com.mb00mer.tasks.cartask.bean.Part;
import com.mb00mer.tasks.cartask.bean.PartElement;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

/**
 * Класс-контроллер для окна просмотра справочника деталей
 */
public class PartsOverviewController {
    private Controller controller;
    private boolean chooseMode;
    private PartElement selectedPart;

    @FXML
    private TreeTableView<PartElement> partTreeTable;
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
    private ToolBar editToolBar;
    @FXML
    private ButtonBar chooseToolBar;
    @FXML
    private BorderPane borderPane;

    @FXML
    private void initialize() {
        // Инициализируем колонки таблицы
        idPartColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>( cellData.getValue().getValue().getPart().getId() ));
        namePartColumn.setCellValueFactory( cellData -> new ReadOnlyStringWrapper( cellData.getValue().getValue().getPart().getName()) );
        colorPartColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>( cellData.getValue().getValue().getPart().getColor() ));
        materialPartColumn.setCellValueFactory( cellData -> new ReadOnlyStringWrapper( cellData.getValue().getValue().getPart().getMaterial()) );
        sizePartColumn.setCellValueFactory( cellData -> new ReadOnlyStringWrapper( cellData.getValue().getValue().getPart().getSize()) );
        countPartColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>( cellData.getValue().getValue().getCount() ));

        // Устанавливаем "слушателя" при изменении выбранной строки
        partTreeTable.getSelectionModel().selectedItemProperty().addListener( (o, ov, nv) -> {
            if (nv == null)
                selectedPart = null;
            else
                selectedPart = nv.getValue();
        } );
        // Устанавливаем "слушателя" на двойной клик мыши (если находимся в режиме диалога выбора)
        partTreeTable.setOnMouseClicked(event -> {
            if ((chooseMode) && (event.getClickCount() == 2))
                handleOk();
        });
    }

    /**
     * Устанавливает связь с моелью для данного контроллера
     * @param controller    Основной контроллер
     */
    public void setController(Controller controller) {
        this.controller = controller;
        // Строим дерево для справочника деталей
        controller.buildPartsTree(partTreeTable, controller.getPartsData());
//        partTreeTable.getSelectionModel().select(0);
    }

    /**
     * Устанавливает режим отображения - диалог выбора или просмотр/редактирование справочника
     * @param chooseMode    true - режим диалога; false - режим просмотра/редактирования справочника
     */
    public void setChooseMode(boolean chooseMode) {
        this.chooseMode = chooseMode;
        editToolBar.setVisible(!chooseMode);
        chooseToolBar.setVisible(chooseMode);
    }

    /** Обработчик нажатия кнопки Отмена */
    @FXML
    private void handleCancel() {
        assert controller.getPartsStage() !=null;
        selectedPart = null;
        controller.getPartsStage().close();
    }

    /** Отображает диалог ввода коичества деталей */
    private int showGettingCountDlg() {
        // Готовим диалог для ввода количества деталей
        Dialog dlg = new TextInputDialog("1");
        dlg.setTitle("");
        dlg.setHeaderText("Введите количество деталей:");
        // Отображаем диалог и ждем результата
        Optional<String> res = dlg.showAndWait();
        if (!res.isPresent())
            return 0;
        //
        int count;
        try {
            count = Integer.parseInt(res.get());
        }
        catch (NumberFormatException ignored) {
            count = 1;
        }
        return count;
    }

    /** Обработчик нажатия кнопки Ok */
    @FXML
    private void handleOk() {
        assert controller.getPartsStage() !=null;
        if (selectedPart != null) {
            int count = showGettingCountDlg();
            if (count > 0)
                selectedPart = new PartElement(selectedPart.getPart(), count);
            else
                selectedPart = null;
        }
        controller.getPartsStage().close();
    }

    /**
     * Возвращает текущий выбранный элемент таблицы
     * @return
     */
    public PartElement getSelectedPart() {
        return selectedPart;
    }

    /** Обработчик нажатия кнопки 'Добавить деталь' */
    @FXML
    private void handleAddPart() {
        // Отображаем диалог ввода новой детали
        Part newPart = controller.showPartEditWindow( null );
        if (newPart == null)
            return;
        // Обновляем данные в модели
        controller.getModel().createOrUpdatePart( newPart );
        // Перестраиваем дерево
        controller.buildPartsTree(partTreeTable, controller.getPartsData());
        // Обновляем данные в главном окне
        controller.refreshMainWindow();
        partTreeTable.requestFocus();
    }

    /** Обработчик нажатия кнопки 'Редактировать деталь' */
    @FXML
    private void handleEditPart() {
        final int selectedIndex = partTreeTable.getSelectionModel().getSelectedIndex();
        // Если выбраной строки нет, то ничего не делаем
        if (selectedIndex < 0) {
            return;
        }
        final Part part = partTreeTable.getTreeItem(selectedIndex).getValue().getPart();
        // Отображаем диалог редактирования детали
        Part newPart = controller.showPartEditWindow( part );
        if (newPart == null)
            return;
        // Обновляем данные в модели
        controller.getModel().createOrUpdatePart( newPart );
        // Перестраиваем дерево
        controller.buildPartsTree(partTreeTable, controller.getPartsData());
        // Обновляем данные в главном окне
        controller.refreshMainWindow();
        partTreeTable.requestFocus();
    }

    /** Обработчик нажатия кнопки 'Удалить деталь' */
    @FXML
    private void handleDeletePart() {
        final int selectedIndex = partTreeTable.getSelectionModel().getSelectedIndex();
        // Если выбраной строки нет, то ничего не делаем
        if (selectedIndex < 0) {
            return;
        }
        final PartElement partElement = partTreeTable.getTreeItem(selectedIndex).getValue();
        // Удаляем деталь
        try {
            // Определяем - выбранная деталь является головной или входит в другую сборку
            // В зависимости от этого вызываем разные методы
            TreeItem<PartElement> treeItem = partTreeTable.getTreeItem(selectedIndex);
            if (partTreeTable.getRoot() == treeItem.getParent())
                // Это "головная" деталь справочника
                controller.getModel().deletePart(partElement.getPart().getId());
            else
                // Это деталь, входящая в состав сборки
                controller.getModel().deleteSubPart(treeItem.getParent().getValue().getPart(), partElement);
        }
        catch (Exception e) {
            controller.showErrorMessage(e.getMessage());
        }
        // Перестраиваем дерево
        controller.buildPartsTree(partTreeTable, controller.getPartsData());
        // Обновляем данные в главном окне
        controller.refreshMainWindow();
        partTreeTable.requestFocus();
        // ToDo Прыгает фокус
    }

    /** Обработчик нажатия кнопки 'Добавить деталь в состав сборки' */
    @FXML
    private void handleAddSubPart() {
        // Определяем выбранную деталь, в состав которой будет добавлена входящая
        // Если такой нет, то ничего не делаем
        int selectedIndex = partTreeTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0)
            return;
        final PartElement part = partTreeTable.getTreeItem( selectedIndex ).getValue();
        assert part != null;
        // Отображаем окно в режиме диалога
        PartElement partElement = controller.showPartsWindow(true);
        // Если пользователь ничего не выбрал - выходим
        if (partElement == null)
            return;
        // Пытаемся добавить деталь в состав. Если не получилось, то выводим сообщение об ошибке
        try {
            controller.getModel().addSubPart(part.getPart(), partElement);
        }
        catch (Exception e) {
            controller.showErrorMessage("Не удалось добавить деталь в состав: \n" + e.getMessage());
        }
        // Добавляем соответствующий объект в загруженные данные
        part.getPart().getPartList().addToPartList( partElement.getPart(), partElement.getCount() );
        // Перестраиваем дерево
        controller.buildPartsTree(partTreeTable, controller.getPartsData());
        // Возврашаем фокус на строку
        if (partTreeTable.getTreeItem( selectedIndex ) != null)
            partTreeTable.getTreeItem( selectedIndex ).setExpanded(true);
        // Обновляем данные в главном окне
        controller.refreshMainWindow();
        partTreeTable.requestFocus();
    }
}
