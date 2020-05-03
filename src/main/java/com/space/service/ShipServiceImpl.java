package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {
    private ShipRepository shipRepository;

    public ShipServiceImpl() {}

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getAllShips(String name,
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
                                  Double maxRating) {

        List<Ship> ships = shipRepository.findAll();

        if (name != null) {
            ships = ships.stream().filter(ship -> ship.getName().toLowerCase()
                    .contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (planet != null) {
            ships = ships.stream().filter(ship -> ship.getPlanet().toLowerCase()
                    .contains(planet.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (shipType != null) {
            ships = ships.stream().filter(ship -> ship.getShipType().equals(shipType))
                    .collect(Collectors.toList());
        }

        if (after != null) {
            ships = ships.stream().filter(ship -> ship.getProdDate().after(new Date(after)))
                    .collect(Collectors.toList());
        }

        if (before != null) {
            ships = ships.stream().filter(ship -> ship.getProdDate().before(new Date(before)))
                    .collect(Collectors.toList());
        }

        if (isUsed != null) {
            ships = ships.stream().filter(ship -> ship.getUsed().equals(isUsed))
                    .collect(Collectors.toList());
        }

        if (minSpeed != null) {
            ships = ships.stream().filter(ship -> ship.getSpeed() >= minSpeed)
                    .collect(Collectors.toList());
        }

        if (maxSpeed != null) {
            ships = ships.stream().filter(ship -> ship.getSpeed() <= maxSpeed)
                    .collect(Collectors.toList());
        }

        if (minCrewSize != null) {
            ships = ships.stream().filter(ship -> ship.getCrewSize() >= minCrewSize)
                    .collect(Collectors.toList());
        }

        if (maxCrewSize != null) {
            ships = ships.stream().filter(ship -> ship.getCrewSize() <= maxCrewSize)
                    .collect(Collectors.toList());
        }

        if (minRating != null) {
            ships = ships.stream().filter(ship -> ship.getRating() >= minRating)
                    .collect(Collectors.toList());
        }

        if (maxRating != null) {
            ships = ships.stream().filter(ship -> ship.getRating() <= maxRating)
                    .collect(Collectors.toList());
        }

        return ships;
    }

    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID:
                        return ship1.getId().compareTo(ship2.getId());
                    case SPEED:
                        return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE:
                        return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING:
                        return ship1.getRating().compareTo(ship2.getRating());
                    default:
                        return 0;
                }
            });
        }
        return ships;
    }

    @Override
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        Integer page = pageNumber == null ? 0 : pageNumber;
        Integer size = pageSize == null ? 3 : pageSize;

        int first = page * size;
        int last = first + size;

        if (last > ships.size()) {
            last = ships.size();
        }
        return ships.subList(first, last);
    }

    @Override
    public Ship createShip(Ship ship) {
        if (ship.getName() == null || ship.getPlanet() == null
            || ship.getShipType() == null || ship.getProdDate() == null
            || ship.getSpeed() == null || ship.getCrewSize() == null) {

            throw new IllegalArgumentException();
        }

        if (!isShipValid(ship)) {
            throw new IllegalArgumentException();
        }

        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }

        Double r = computeRating(ship);
        ship.setRating(r);

        return shipRepository.saveAndFlush(ship);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean changeRating = false;

        if (newShip.getName() != null ) {
            if (isNameLengthValid(newShip.getName())) {
                oldShip.setName(newShip.getName());
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getPlanet() != null) {
            if (isPlanetLengthValid(newShip.getPlanet())) {
                oldShip.setPlanet(newShip.getPlanet());
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }

        if (newShip.getProdDate() != null) {
            if (isDateValid(newShip.getProdDate())) {
                oldShip.setProdDate(newShip.getProdDate());
                changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            changeRating = true;
        }

        if (newShip.getSpeed() != null) {
            if (isSpeedValid(newShip.getSpeed())) {
                oldShip.setSpeed(newShip.getSpeed());
                changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getCrewSize() != null) {
            if (isCrewSizeValid(newShip.getCrewSize())) {
                oldShip.setCrewSize(newShip.getCrewSize());
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (changeRating) {
            oldShip.setRating(computeRating(oldShip));
        }

        shipRepository.save(oldShip);
        return oldShip;
    }

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public Ship getShipById(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public boolean isShipValid(Ship ship) {
        return ship != null
                && isNameLengthValid(ship.getName())
                && isPlanetLengthValid(ship.getPlanet())
                && isSpeedValid(ship.getSpeed())
                && isDateValid(ship.getProdDate())
                && isCrewSizeValid(ship.getCrewSize());
    }

    @Override
    public double computeRating(Ship ship){
        int year = getYearFromDate(ship.getProdDate());
        double k = ship.getUsed() ? 0.5 : 1;
        double r = (80 * ship.getSpeed() * k / (3019 - year + 1));
        return round(r);
    }

    private double round(double value) {
        return Math.round(value * 100) / 100D;
    }

    public boolean isNameLengthValid(String name) {
        return name.length() >= 1 && name.length() <= 50;
    }

    public boolean isPlanetLengthValid(String planet) {
        return planet.length() >= 1 && planet.length() <= 50;
    }

    public boolean isSpeedValid(Double speed){
        return round(speed) >= 0.01 && round(speed) <= 0.99;
    }

    private boolean isDateValid(Date prodDate) {
        return getYearFromDate(prodDate) >= 2800 && getYearFromDate(prodDate) <= 3019;
    }

    public static int getYearFromDate(Date date) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = cal.get(Calendar.YEAR);
        }
        return result;
    }

    private boolean isCrewSizeValid(Integer crewSize) {
        return  crewSize >= 1 && crewSize <= 9999;
    }
}