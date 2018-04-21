package com.mb00mer.tasks.cartask.bean;

import java.util.Objects;

/**
 * Класс <code>PartElement</code> представляет собой деталь как элемент состава, т.е. деталь + количество
 * Используется для передачи данных о составе СЕ
 */
public class PartElement implements Cloneable {
    private Part part;
    private int count;

    public PartElement(Part part, int count) {
        this.part = part;
        this.count = count;
    }

    public PartElement(Part part) {
        this(part, 1);
    }

    /** Два объекта PartElement равны, если равны их part */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartElement)) return false;
        PartElement that = (PartElement) o;
        return Objects.equals(part, that.part);
    }

    /** В рассмотрение при вычислении hash берется только part */
    @Override
    public int hashCode() {

        return Objects.hash(part);
    }

    /** Метод Clone переопределен для глубокого клонирования */
    @Override
    public PartElement clone() {
        PartElement cloned;
        // Клонируем сам объект
        try {
            cloned = (PartElement) super.clone();
            cloned.part = part.clone();
        } catch (CloneNotSupportedException ignored) {
            cloned = new PartElement(part.clone(), count);
        }
        // Клонируем список деталей
        return cloned;
    }

    public Part getPart() {
        return part;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return String.format("%s (%d шт)", part, count);
    }
}
