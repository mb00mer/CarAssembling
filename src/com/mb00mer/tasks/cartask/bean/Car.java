package com.mb00mer.tasks.cartask.bean;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Класс <code>Car<code/> представляет автомобиль.
 */
public class Car implements Cloneable {
    // Наименование авто
    private String name;
    // Дата создания авто
    private Date date;
    // Cписок входящих деталей
    private PartList partList = new PartList();

    public Car(String name) {
        this.name = name;
        this.date = new Date();
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Возвращает список деталей, входящих в авто
     * @return      PartList - Список деталей в авто
     */
    public PartList getPartList() {
        return partList;
    }

    /** Два объекта Car равны, если равны их name */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car)) return false;
        Car car = (Car) o;
        return Objects.equals(name, car.name);
    }

    /** В рассмотрение при вычислении hash берется только name */
    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    /** Метод Clone переопределен для глубокого клонирования */
    @Override
    public Car clone() {
        Car cloned;
        // Клонируем сам объект
        try {
            cloned = (Car) super.clone();
        } catch (CloneNotSupportedException ignored) {
            cloned = new Car(name);
        }
        // Клонируем список деталей
        cloned.partList = partList.clone();
        return cloned;
    }

    @Override
    public String toString() {
        return String.format("Авто '%s'", name);
    }
}
