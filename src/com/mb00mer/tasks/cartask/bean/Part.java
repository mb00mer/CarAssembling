package com.mb00mer.tasks.cartask.bean;

import java.util.*;

/**
 * Класс <code>Part<code/> представляет деталь автомобиля.
 */
public class Part implements Cloneable {
    private static final String NO_NAME = "Без названия";
    private static final String NO_DATA = "Нет данных";
    private static final Color DEF_COLOR = Color.P_BLACK;

    // Уникальный идентификатор детали
    private int id;
    // Наименование детали
    private String name;
    // Цвет детали
    private Color color = DEF_COLOR;
    // Материал, из которого изготовлена деталь
    private String material = NO_DATA;
    // Размер детали
    private String size = NO_DATA;
    // Состав сборки - список входящих деталей
    private PartList partList = new PartList();

    public Part(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Part(int id) {
        this(id, NO_NAME);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaterial() {
        return material;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Показывает - является деталь сборкой, или нет
     * @return      true если в составе детали есть другие детали, иначе - false
     */
    public boolean hasStructure() {
        return !partList.isEmpty();
    }

    /**
     * Возвращает состав сборки
     * @return      PartList - состав сборки
     */
    public PartList getPartList() {
        return partList;
    }

    /** Два объекта Part равны, если равны их id */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Part)) return false;
        Part part = (Part) o;
        return id == part.id;
    }

    /** В рассмотрение при вычислении hash берется только id */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s %d: %s; Цвет: %s; Материал: %s; Размер: %s",
                hasStructure() ? "Сборка" : "Деталь", id, name, color, material, size);
    }

    /** Метод Clone переопределен для глубокого клонирования */
    @Override
    public Part clone() {
        Part cloned;
        // Клонируем сам объект
        try {
            cloned = (Part) super.clone();
        } catch (CloneNotSupportedException ignored) {
            cloned = new Part(id, name);
            cloned.setMaterial(material);
            cloned.setColor(color);
            cloned.setSize(size);
        }
        // Клонируем список деталей
        cloned.partList = partList.clone();
        return cloned;
    }
}
