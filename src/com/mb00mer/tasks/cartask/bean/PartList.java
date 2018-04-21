package com.mb00mer.tasks.cartask.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Класс <code>PartList<code/> представляет список деталей с количеством
 */
public class PartList implements Cloneable {
    // Список деталей. Ключ - деталь, значение - количество
    private Map<Part, Integer> partList = new HashMap<>();

    /**
     * Добавляет в состав сборки новую деталь с количеством = 1. Если деталь уже была в составе, то увеличивает
     * количество на 1
     * @param part      Деталь, которая должна войти в состав сборки
     */
    public void addToPartList(Part part) {
        addToPartList(part, 1);
    }

    /**
     * Добавляет в состав сборки новую деталь с заданным количеством. Если деталь уже была в составе, то увеличивает
     * количество на заданную величину
     * @param part      Деталь, которая должна войти в состав сборки
     * @param count     Количество деталей. Если <=0, то ничего не выполняется
     * @return          -1 - если модель необновлена, 0 - если деталей еще не было в составе, иначе - новое количество деталей
     */
    public int addToPartList(Part part, int count) {
        if ((part == null) || (count <= 0))
            return -1;
        // Вычисляем новое количество деталей в составе.
        int newCount = partList.containsKey(part) ? partList.get(part) + count : count;
        // Кладем в состав сборки деталь с вычисленным количеством
        partList.put(part, newCount);
        return newCount == count ? 0 : newCount;
    }

    /**
     * Уменьшает количество указанных деталей в составе на заданную величину. Если итоговое количество <=0, то деталь
     * удаляется из состава сборки. Если указанной детали не имеется в составе, то ничего не выполняется
     * @param part      Деталь, по которой необходимо уменьшить количество
     * @param count     Количество, на которое необходимо уменьшить состав
     * @return          -1 - если модель необновлена, 0 - если деталь полностью удалена из состава, иначе - новое количество деталей
     */
    public int removeFromPartList(Part part, int count) {
        if ((part == null) || (count <= 0) || !partList.containsKey(part))
            return -1;
        // Вычисляем новое количество деталей в составе.
        int newCount = partList.get(part) - count;
        // Если вычисленное количество <=0, то удаляем деталь из состава, иначе - уменьшаем количество
        if (newCount <= 0) {
            partList.remove(part);
            return 0;
        }
        else {
            partList.put(part, newCount);
            return newCount;
        }
    }

    /**
     * Показывает - пустой список или нет
     * @return      true если список пуст, иначе - false
     */
    public boolean isEmpty() {
        return partList.size() == 0;
    }

    /** Метод Clone переопределен для "глубокого" клонирования */
    @Override
    public PartList clone() {
        PartList cloned;
        // Клонируем сам объект
        try {
            cloned = (PartList) super.clone();
        }
        catch (CloneNotSupportedException ignored) {
            cloned = new PartList();
        }
        // Клонируем список деталей
        cloned.partList = cloneEntry();
        return cloned;
    }

    /**
     * Получает "глубокую" копию списка деталей. Каждый объект Part также клонируется
     * @return      "Глубокая" копия исходного partList
     */
    private Map<Part, Integer> cloneEntry() {
        return this.partList.entrySet().stream()
                .collect( Collectors.toMap(
                        e -> e.getKey().clone(),
                        e -> e.getValue()
                ));
    }

    /**
     * Возвращает список элементов типа PartElement
     * @return      Копия списка деталей
     */
    public List<PartElement> asPartElementList() {
        return this.partList.entrySet().stream()
                .map((entry) -> new PartElement(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
