package com.mb00mer.tasks.cartask.controller;

import com.mb00mer.tasks.cartask.bean.Color;
import com.mb00mer.tasks.cartask.bean.Part;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.util.Arrays;

/**
 * Класс-контроллер для диалога ввода/редактирования детали
 */
public class PartEditOverviewController {
    private Controller controller;
    private Part part;
    private boolean isNew;

    @FXML
    TextField textPartName;
    @FXML
    TextField textMaterial;
    @FXML
    TextField textSize;
    @FXML
    ChoiceBox<Color> comboColor;

    @FXML
    private void initialize() {
        comboColor.setItems( new ObservableListWrapper<>(Arrays.asList( Color.values() )));
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setPart(Part part) {
        this.part = part;
        this.isNew = part == null;

        textPartName.setText( part == null ? "" : part.getName() );
        textMaterial.setText( part == null ? "" : part.getMaterial() );
        textSize.setText( part == null ? "" : part.getSize() );
        comboColor.setValue( part == null ? null : part.getColor() );
    }

    public Part getPart() {
        return part;
    }

    @FXML
    private void handleCancel() {
        part = null;
        controller.getPartEditStage().close();
    }

    @FXML
    private void handleOK() {
        part = new Part( isNew ? -1 : part.getId(), textPartName.getText() );
        part.setMaterial( textMaterial.getText() );
        part.setSize( textSize.getText() );
        part.setColor( comboColor.getValue() );
        controller.getPartEditStage().close();
    }
}
