package com.mb00mer.tasks.cartask.model;

import com.mb00mer.tasks.cartask.bean.Car;
import com.mb00mer.tasks.cartask.bean.Color;
import com.mb00mer.tasks.cartask.bean.Part;
import com.mb00mer.tasks.cartask.bean.PartElement;
import com.mb00mer.tasks.cartask.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainModel implements Model {
    private Dao dao = new Dao( this );
    private List<Part> parts = new ArrayList<>();
    private List<Car> cars = new ArrayList<>();
    private int MAX_ID;

    // Тестовая инициализация без подключения к БД
    {
        Part part1 = new Part(1, "Кузов");
        Part part2 = new Part(2, "Колесо");
        Part part3 = new Part(3, "Дверь");
        Part part4 = new Part(4, "Двигатель");
        Part part5 = new Part(5, "Цилиндр");
        Part part6 = new Part(6, "Поршень");
        Part part7 = new Part(7, "Коробка передач");
        Part part8 = new Part(8, "Руль");
        Part part9 = new Part(9, "Тормозная система");
        Part part10 = new Part(10, "Система зажигания");
        Part part11 = new Part(11, "Свеча");

        part4.getPartList().addToPartList(part5, 4);
        part4.getPartList().addToPartList(part6, 4);
        part4.getPartList().addToPartList(part10, 1);
        part10.getPartList().addToPartList(part11, 4);
        parts.addAll( Arrays.asList(part1, part2, part3, part4, part5, part6, part7, part8, part9, part10, part11) );

        Car car1 = new Car("Авто 1");
        Car car2 = new Car("Вело 2");
        Car car3 = new Car("Авто 3");
        Car car4 = new Car("Авто 4");

        car1.getPartList().addToPartList(part1, 1);
        car1.getPartList().addToPartList(part2, 4);
        car1.getPartList().addToPartList(part3, 4);
        car1.getPartList().addToPartList(part4, 1);
        car1.getPartList().addToPartList(part7, 1);
        car1.getPartList().addToPartList(part8, 1);
        car1.getPartList().addToPartList(part9, 1);
        car2.getPartList().addToPartList(part2, 2);
        cars.addAll( Arrays.asList(car1, car2, car3, car4) );
    }

    public MainModel() {
        openModel();
    }

    /**
     * Инициализирует модель для работы
     */
    private void openModel() {
        // Открываем подключение к БД
        dao.openConnection();
        try {
            // Заполняем справочник деталей
            parts = dao.loadPartsData();
            // Определяем макисмальный id
            MAX_ID = parts.stream()
                    .mapToInt(Part::getId)
                    .max()
                    .orElse(0);
            // Заполняем список автомобилей
            cars = dao.loadCarsData();
        }
        catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении данных из БД", e);
        }
    }

    /**
     * Корректно закрывает модель при завершении работы
     */
    private void closeModel() {
        // Закрываем подключение к БД
        dao.closeConnection();
    }

    /**
     * Метод для интерфейса AutoCloseable. Вызывает closeModel()
     * @throws Exception
     */
    @Override
    public void close() {
        closeModel();
    }

    /**
     * Возвращает список всех деталей. Объекты в списке являются копией (clone) хранящихся в модели оригиналов
     * @return      Список всех деталей (List<Part>)
     */
    @Override
    public List<PartElement> getAllParts() {
        return parts.stream()
                .map( p -> new PartElement(p.clone()) )
                .collect(Collectors.toList());
    }

    /**
     * Ищет и возвращает деталь <code>Part<code/> с заданным id. Если деталь не найдена, то возвращет null
     * @param id    id детали, которую необходимо найти
     * @return      Объект Деталь (Part) с заданным id, если найдена, иначе null
     */
    public Part getOriginalById(int id) {
        return parts.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Ищет и возвращает деталь <code>Part<code/> с заданным id. Возвращается копия объекта (clone). Если деталь не
     * найдена, то возвращет null
     * @param id    id детали, которую необходимо найти
     * @return      Копия объекта Деталь (Part) с заданным id, если найдена, иначе null
     */
    @Override
    public Part getPartById(int id) {
        return getOriginalById(id).clone();
    }

    /**
     * Удаляет существующую деталь, если это возможно. Если на деталь имеется ссылка из состава каких-либо сборочных
     * единиц либо автомобилей, то выбрасывается исключение
     * @param partId        id детали, которую необходимо удалить
     */
    @Override
    public void deletePart(int partId) {
        // Проверяем в БД - можно ли удалить деталь из справочника
        try {
            if (dao.isPartReferenced(partId))
                throw new RuntimeException("На деталь ссылаются другие сборки");
            // Удаление из БД
            dao.saveDeletePart(partId);
        }
        catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении детали: " + e.getMessage(), e);
        }
        // Обновляем модель
        parts = parts.stream()
                .filter(p -> p.getId() != partId)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param id
     * @param name
     * @param color
     * @param material
     * @param size
     * @return
     */
    @Override
    public Part createOrUpdatePart(int id, String name, Color color, String material, String size) {
        // Ищем - существует ли деталь с таким id
        Part part = getOriginalById(id);
        boolean isNew = false;
        // Обновляем модель
        if (part == null) {
            isNew = true;
            part = new Part(++MAX_ID);
            parts.add(part);
        }
        part.setName(name);
        part.setColor(color);
        part.setMaterial(material);
        part.setSize(size);
        // Добавление или обновление в БД
        try {
            if (isNew)
                dao.saveInsertPart(part.getId(), part.getName(), part.getColor(), part.getMaterial(), part.getSize());
            else
                dao.saveUpdatePart(part.getId(), part.getName(), part.getColor(), part.getMaterial(), part.getSize());
        }
        catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении БД", e);
        }
        // Возвращаем клонированный объект
        return part.clone();
    }

    /**
     * Добавляет деталь в состав сборки
     * @param part          Сборка, в состав которой добавляется деталь
     * @param partElement   Добавляемая деталь
     */
    @Override
    public void addSubPart(Part part, PartElement partElement) {
        if ((part == null) || (partElement == null) || (partElement.getCount() <= 0))
            return;
        // Ищем оригинальную деталь
        Part original = getOriginalById(part.getId());
        if (original == null)
            return;
        // Добавляем деталь в модель
        int result = original.getPartList().addToPartList(getOriginalById(partElement.getPart().getId()), partElement.getCount());
        if (result == -1)
            return;

        // Добавляем в БД
        try {
            if (result == 0)
                dao.saveInsertSubPart(original.getId(), partElement.getPart().getId(), partElement.getCount());
            else
                dao.saveUpdateSubPart(original.getId(), partElement.getPart().getId(), result);
        }
        catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении БД", e);
        }
    }

    /**
     * Удаляет деталь из состава сборки
     * @param part          Сборка, из состава которой удаляется деталь
     * @param partElement   Удаляемая деталь
     */
    @Override
    public void deleteSubPart(Part part, PartElement partElement) {
        if ((part == null) || (partElement == null) || (partElement.getCount() <= 0))
            return;
        // Ищем оригинальную деталь
        Part original = getOriginalById(part.getId());
        if (original == null)
            return;
        // Удаляем деталь в модели
        int result = original.getPartList().removeFromPartList(getOriginalById(partElement.getPart().getId()), partElement.getCount());
        if (result == -1)
            return;
        // Удаляем в БД
        try {
            if (result == 0)
                dao.saveDeleteSubPart(original.getId(), partElement.getPart().getId());
            else
                dao.saveUpdateSubPart(original.getId(), partElement.getPart().getId(), result);
        }
        catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении БД", e);
        }
    }

    @Override
    public List<Car> getAllCars() {
        return cars.stream()
                .map(Car::clone)
                .collect(Collectors.toList());

    }

    /**
     * Удаляет существующий автомобиль
     * @param car   Автомобиль, который требуется удалить
     */
    @Override
    public void removeCar(Car car) {
        if (car == null)
            return;
        if (cars.remove(car)) {
            try {
                // Удаляем в БД
                dao.saveDeleteCar(car.getName());
            }
            catch (SQLException e) {
                throw new RuntimeException("Ошибка при обновлении БД", e);
            }
        }
    }

    /**
     * Создает новый автомобиль с заданным именем, если такого еще нет.
     * @param carName   Имя автомобиля
     * @return          Если авто с таким именем уже существует, возвращает null, иначе - вновь созданный объект Car
     */
    @Override
    public Car createCar(String carName) {
        // ToDo По идее надо кидать Exception и пользователю давать более вразумительную информацию о причинах отказа в создании
        // Если имя авто не задано, то возвращаем null
        if (carName == null)
            return null;
        carName = carName.trim();
        // Если заданное имя пустое или его длина превышает максимальное значение, то возвращаем null
        if ((carName.isEmpty()) || (carName.length() > 20))
            return null;
        Car car = new Car(carName);
        // Если авто с таким именем уже существует, то возвращаем null
        if (cars.contains(car))
            return null;
        // Добавляем авто в список
        cars.add(car);
        // Добавляем в БД
        try {
            dao.saveInsertCar(car.getName(), car.getDate());
        }
        catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return car.clone();
    }

    /**
     * Удаляет деталь из состава автомобиля
     * @param car           Автомобиль, в составе которого удаляется деталь
     * @param partElement   Удаляемая деталь
     * @return              true если деталь успешно удалена, иначе false
     */
    @Override
    public boolean removePartFromCar(Car car, PartElement partElement) {
        if ((car == null) || (partElement == null))
            return false;
        // Ищем объект car в модели
        Car original = cars.stream()
                .filter(c -> c.getName().equals(car.getName()))
                .findFirst()
                .orElse(null);
        // Если не нашли, то ничего не делаем
        if (original == null)
            return false;
        // Удаляем из состава автомобиля деталь
        int result = original.getPartList().removeFromPartList(partElement.getPart(), partElement.getCount());
        if (result == -1)
            return false;
        // Обновляем БД
        try {
            if (result == 0)
                dao.saveDeleteCarPart(original.getName(), partElement.getPart().getId());
            else
                dao.saveUpdateCarPart(original.getName(), partElement.getPart().getId(), result);
        }
        catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении БД", e);
        }
        return true;
    }

    /**
     * Добавляет деталь в состав автомобиля.
     * @param car           Автомобиль, в состав которого добавляется деталь
     * @param partElement   Добавляемая деталь
     * @return              true если деталь успешно добавлена, иначе false
     */
    @Override
    public boolean addPartToCar(Car car, PartElement partElement) {
        if ((car == null) || (partElement == null) || (partElement.getCount() <= 0))
            return false;
        // Ищем объект car в модели
        Car original = cars.stream()
                .filter(c -> c.getName().equals(car.getName()))
                .findFirst()
                .orElse(null);
        // Если не нашли, то ничего не делаем
        if (original == null)
            return false;
        // Добавляем в состав автомобиля деталь
        int result = original.getPartList().addToPartList( getOriginalById(partElement.getPart().getId()), partElement.getCount() );
        if (result == -1)
            return false;
        // Обновляем БД
        try {
            if (result == 0)
                dao.saveInsertCarPart(original.getName(), partElement.getPart().getId(), partElement.getCount());
            else
                dao.saveUpdateCarPart(original.getName(), partElement.getPart().getId(), result);
        }
        catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении БД", e);
        }
        return true;
    }
}
