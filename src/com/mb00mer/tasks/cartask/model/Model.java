package com.mb00mer.tasks.cartask.model;

import com.mb00mer.tasks.cartask.bean.Car;
import com.mb00mer.tasks.cartask.bean.Color;
import com.mb00mer.tasks.cartask.bean.Part;
import com.mb00mer.tasks.cartask.bean.PartElement;

import java.util.List;

/**
 *  Интерфейс доступа к модели предметной области
 */
public interface Model extends AutoCloseable {
    // ToDo Возможно, стоило разделить интерфейсы для Car и Parts
    // Возвращает полный список деталей
    List<PartElement> getAllParts();
    // Возвращает деталь с заданным id
    Part getPartById(int id);
    // Удаляет существующую деталь
    void deletePart(int id);
    // Создает новую деталь или изменяет данные по уже существующей
    Part createOrUpdatePart(int id, String name, Color color, String material, String size);
    // Создает новую деталь или изменяет данные по уже существующей
    default Part createOrUpdatePart(Part part) {
        // Вызываем альтернативный метод
        return createOrUpdatePart(part.getId(), part.getName(), part.getColor(), part.getMaterial(), part.getSize());
    }
    // Добавляет деталь в состав сборки
    void addSubPart(Part part, PartElement partElement);
    // Удаляет деталь из состава сборки
    void deleteSubPart(Part part, PartElement partElement);

    // Возвращает полный список деталей
    List<Car> getAllCars();
    // Удаляет автомобиль
    void removeCar(Car car);
    // Создает автомобиль
    Car createCar(String carName);
    // Удаляет деталь из состава автомобиля
    boolean removePartFromCar(Car car, PartElement partElement);
    // Добавляет деталь в состав автомобиля
    boolean addPartToCar(Car car, PartElement partElement);
}
