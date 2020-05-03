package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.List;

public interface ShipService {
    List<Ship> getAllShips(
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating
    );

    List<Ship> sortShips(List<Ship> ships, ShipOrder order);

    List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize);

    Ship createShip(Ship ship);

    Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException;

    Ship saveShip(Ship ship);

    void deleteShip(Ship ship);

    Ship getShipById(Long id);

    boolean isShipValid(Ship ship);

    double computeRating(Ship ship);
}
