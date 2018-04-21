package com.mb00mer.tasks.cartask.dao;

import com.mb00mer.tasks.cartask.bean.Car;
import com.mb00mer.tasks.cartask.bean.Color;
import com.mb00mer.tasks.cartask.bean.Part;
import com.mb00mer.tasks.cartask.model.MainModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  Управляет подключением к БД. Формирует запросы и обновления БД
 */
public class Dao {
    private static final String H2_DRIVER_NAME = "org.h2.Driver";
    private static final String CONN_STRING = "jdbc:h2:./db/Cars_DB";

    // ToDo Запросы к БД. Можно было бы вынести в ресурсы..
    private static final String SQL_SELECT_PARTS = "SELECT PARTID, PARTNAME, COLOR, MATERIAL, SIZE FROM PARTS";
    private static final String SQL_SELECT_CARS = "SELECT CARNAME, DT FROM CARS";
    private static final String SQL_SELECT_PARTSTRUCT = "SELECT PARTID, PARTID_CHILD, COUNT FROM PARTSTRUCT";
    private static final String SQL_SELECT_CARSTRUCT = "SELECT CARNAME, PARTID, COUNT FROM CARSTRUCT";
    private static final String SQL_SELECT_CHECK_STRUCT = "SELECT PARTID FROM PARTSTRUCT WHERE PARTID_CHILD = %1$d UNION SELECT PARTID FROM CARSTRUCT WHERE PARTID = %1$d";

    private static final String SQL_INSERT_PARTS = "INSERT INTO PARTS(PARTID, PARTNAME, COLOR, MATERIAL, SIZE) VALUES(%d, '%s', %d, '%s', '%s')";
    private static final String SQL_INSERT_CARS = "INSERT INTO CARS(CARNAME, DT) VALUES('%s', '%2$tY-%2$tm-%2$td')";
    private static final String SQL_INSERT_PARTSTRUCT = "INSERT INTO PARTSTRUCT(PARTID, PARTID_CHILD, COUNT) VALUES(%d, %d, %d)";
    private static final String SQL_INSERT_CARSTRUCT = "INSERT INTO CARSTRUCT(CARNAME, PARTID, COUNT) VALUES('%s', %d, %d)";

    private static final String SQL_DELETE_PARTS = "DELETE FROM PARTS WHERE PARTID = %d";
    private static final String SQL_DELETE_CARS = "DELETE FROM CARS WHERE CARNAME = '%s'";
    private static final String SQL_DELETE_PARTSTRUCT = "DELETE FROM PARTSTRUCT WHERE PARTID = %d AND PARTID_CHILD = %d";
    private static final String SQL_DELETE_PARTSTRUCT_ALL = "DELETE FROM PARTSTRUCT WHERE PARTID = %d";
    private static final String SQL_DELETE_CARSTRUCT = "DELETE FROM CARSTRUCT WHERE CARNAME = '%s' AND PARTID = %d";
    private static final String SQL_DELETE_CARSTRUCT_ALL = "DELETE FROM CARSTRUCT WHERE CARNAME = '%s'";

    private static final String SQL_UPDATE_PARTS = "UPDATE PARTS SET PARTNAME = '%s', COLOR = %d, MATERIAL = '%s', SIZE = '%s' WHERE PARTID = %d";
    private static final String SQL_UPDATE_PARTSTRUCT = "UPDATE PARTSTRUCT SET COUNT = %d WHERE PARTID = %d AND PARTID_CHILD = %d";
    private static final String SQL_UPDATE_CARSTRUCT = "UPDATE CARSTRUCT SET COUNT = %d WHERE CARNAME = '%s' AND PARTID = %d";

    private Connection conn;

    // ToDo Спроектировано плохо, пришлось сослаться на модель. Методы построения структур надо выносить из dao в model
    private MainModel model;

    public Dao(MainModel model) {
        this.model = model;
    }

    /**
     * Открывает соединение с БД
     */
    public void openConnection() {
        try {
            // Создаем соединение с БД H2 (embedded)
            Class.forName(H2_DRIVER_NAME);
            conn = DriverManager.getConnection(CONN_STRING);
        }
        catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Ошибка при соединении с БД", e);
        }
    }

    /**
     * Закрывает соединение с БД
     */
    public void closeConnection() {
        // Закрываем соединение с БД
        try {
            if (conn != null)
                conn.close();
        }
        catch (SQLException e) {
            throw new RuntimeException("Ошибка при закрытии соединения с БД", e);
        }
    }

    /** Загружает данные справочника деталей */
    public List<Part> loadPartsData() throws SQLException {
        List<Part> list = new ArrayList<>();
        try ( Statement st = conn.createStatement() ) {
            // Запрашиваем данные таблицы PARTS
            try ( ResultSet res = st.executeQuery(SQL_SELECT_PARTS ) ) {
                Color[] colors = Color.values();
                Part part;
                while (res.next()) {
                    part = new Part( res.getInt("PARTID"), res.getString("PARTNAME") );
                    part.setColor(  colors[res.getInt("COLOR")] );
                    part.setMaterial( res.getString("MATERIAL") );
                    part.setSize( res.getString("SIZE") );
                    list.add(part);
                }
            }
            // Запрашиваем данные таблицы PARTSTRUCT
            try ( ResultSet res = st.executeQuery(SQL_SELECT_PARTSTRUCT) ) {
                Part part, part_child;
                while (res.next()) {
                    int partID = res.getInt("PARTID");
                    part = //model.getOriginalById(partID);
                            list.stream()
                            .filter(p -> p.getId() == partID)
                            .findFirst()
                            .orElse(null);
                    int partID_child = res.getInt("PARTID_CHILD");
                    part_child = //model.getOriginalById(partID_child);
                            list.stream()
                            .filter(p -> p.getId() == partID_child)
                            .findFirst()
                            .orElse(null);
                    int count = res.getInt("COUNT");
                    if (part != null)
                        part.getPartList().addToPartList(part_child, count);
                }
            }
        }
        return list;
    }

    /** Загружает данные справочника автомобилей */
    public List<Car> loadCarsData() throws SQLException {
        List<Car> list = new ArrayList<>();
        try ( Statement st = conn.createStatement() ) {
            // Запрашиваем данные таблицы CARS
            try ( ResultSet res = st.executeQuery(SQL_SELECT_CARS) ) {
                Car car;
                while (res.next()) {
                    car = new Car( res.getString("CARNAME") );
                    car.setDate( res.getDate("DT") );
                    list.add(car);
                }
            }
            // Запрашиваем данные таблицы CARSTRUCT
            try ( ResultSet res = st.executeQuery(SQL_SELECT_CARSTRUCT) ) {
                Car car;
                Part part;
                while (res.next()) {
                    String carName = res.getString("CARNAME");
                    car = list.stream()
                            .filter(c -> c.getName().equals(carName))
                            .findFirst()
                            .orElse(null);
                    int partId = res.getInt("PARTID");
                    part = model.getOriginalById(partId);
                    int count = res.getInt("COUNT");
                    if (car != null)
                        car.getPartList().addToPartList(part, count);
                }
            }
        }
        return list;
    }


    public void saveUpdateSubPart(int partId, int partId_Child, int count) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_UPDATE_PARTSTRUCT, count, partId, partId_Child));
        }
    }

    public void saveInsertSubPart(int partId, int partId_Child, int count) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_INSERT_PARTSTRUCT, partId, partId_Child, count));
        }
    }

    public void saveDeleteSubPart(int partId, int partId_Child) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_DELETE_PARTSTRUCT, partId, partId_Child));
        }
    }

    public void saveDeleteCar(String name) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            try {
                st.execute("SET AUTOCOMMIT=OFF");
                st.execute(String.format(SQL_DELETE_CARS, name));
                st.execute(String.format(SQL_DELETE_CARSTRUCT_ALL, name));
                st.execute("COMMIT");
            }
            catch (SQLException e) {
                st.execute("ROLLBACK");
                throw e;
            }
            finally {
                st.execute("SET AUTOCOMMIT=ON");
            }
        }
    }

    public void saveInsertCar(String carName, Date dt) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_INSERT_CARS, carName, dt));
        }
    }


    public void saveDeleteCarPart(String carName, int partId) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_DELETE_CARSTRUCT, carName, partId));
        }
    }

    public void saveUpdateCarPart(String carName, int partId, int count) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_UPDATE_CARSTRUCT, count, carName, partId));
        }
    }

    public void saveInsertCarPart(String carName, int partId, int count) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_INSERT_CARSTRUCT, carName, partId, count));
        }
    }

    public boolean isPartReferenced(int partId) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            try ( ResultSet res = st.executeQuery(String.format(SQL_SELECT_CHECK_STRUCT, partId)) ) {
                return res.next();
            }
        }
    }

    public void saveDeletePart(int partId) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            try {
                st.execute("SET AUTOCOMMIT=OFF");
                st.execute(String.format(SQL_DELETE_PARTSTRUCT_ALL, partId));
                st.execute(String.format(SQL_DELETE_PARTS, partId));
                st.execute("COMMIT");
            }
            catch (SQLException e) {
                st.execute("ROLLBACK");
                throw e;
            }
            finally {
                st.execute("SET AUTOCOMMIT=ON");
            }
        }
    }

    public void saveInsertPart(int partId, String partName, Color color, String material, String size) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_INSERT_PARTS, partId, partName, color.ordinal(), material, size));
        }
    }

    public void saveUpdatePart(int partId, String partName, Color color, String material, String size) throws SQLException {
        try ( Statement st = conn.createStatement() ) {
            st.execute(String.format(SQL_UPDATE_PARTS, partName, color.ordinal(), material, size, partId));
        }
    }
}
