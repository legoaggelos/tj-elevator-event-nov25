package org.togetherjava.event.elevator.elevators;

import org.togetherjava.event.elevator.humans.ElevatorListener;

import java.util.ArrayList;
import java.util.List;

/**
 * System controlling all elevators of a building.
 * <p>
 * Once all elevators and humans have been registered via {@link #registerElevator(Elevator)}
 * and {@link #registerElevatorListener(ElevatorListener)} respectively,
 * the system can be made ready using {@link #ready(List)}.
 */
public final class ElevatorSystem implements FloorPanelSystem {
    private final List<Elevator> elevators = new ArrayList<>();
    private final List<ElevatorListener> elevatorListeners = new ArrayList<>();
    private boolean isFirstElev = false;

    public void registerElevator(Elevator elevator) {
        if (!isFirstElev) {
            isFirstElev = true;
            elevators.forEach(elevator1 -> elevator1.setSoleElevator(false));
        }
        elevators.add(elevator);
    }

    public void registerElevatorListener(ElevatorListener listener) {
        elevatorListeners.add(listener);
    }

    public int distanceFromBottom(Elevator elevator) {
        return Math.abs(elevator.getCurrentFloor()-1);
    }

    public int distanceFromTop(Elevator elevator) {
        return Math.abs(elevator.getCurrentFloor()-elevator.getFloorsServed()+elevator.getMinFloor()-1);
    }

    /**
     * Upon calling this, the system is ready to receive elevator requests. Elevators may now start moving.
     */
    public void ready(List<Integer> startingFloors) {
        elevatorListeners.forEach(listener -> {
            listener.onElevatorSystemReady(this);
        });
        for (Elevator elevator : elevators) {
            for (int startingFloor : startingFloors) {
                elevator.addStartingFloor(startingFloor);
            }
        }
        if(elevators.size() == 1) {
            return;
        }
        Elevator upElevator = elevators.getFirst();
        Elevator secondUpElevator = elevators.get(1);
        Elevator downElevator = upElevator;
        for (Elevator elev: elevators) {
            if (distanceFromTop(elev) < distanceFromTop(upElevator)) {
                secondUpElevator = upElevator;
                upElevator = elev;
            }
            else if (distanceFromBottom(elev) < distanceFromBottom(downElevator)) {
                downElevator = elev;
            }
        }
        if (downElevator.equals(upElevator)) {
            assert downElevator != secondUpElevator;
            upElevator = secondUpElevator;
        }
        upElevator.setSoleElevator(false);
        upElevator.setRole(Elevator.Role.DOWN_TRAVELLER);
        downElevator.setSoleElevator(false);
        downElevator.setRole(Elevator.Role.UP_TRAVELLER);
    }

    public static int floorAndElevatorDistance(int floor, Elevator elevator) {
        return Math.abs(elevator.getCurrentFloor()-floor);
    }
    @Override
    public Elevator bestElevator(int atFloor, TravelDirection desiredTravelDirection) {
        assert !elevators.isEmpty();
        Elevator bestElevator = elevators.getFirst();
        if (elevators.size() == 1) {
            return bestElevator;
        }
        for (var elevator : elevators) {
            if (elevator.getStartingFloors().size() < bestElevator.getStartingFloors().size()) {
                bestElevator = elevator;
            }
        }
        return bestElevator;
    }

    public void requestElevator(Elevator elevator, int atFloor) {
        elevator.requestDestinationFloor(atFloor);
    }

    @Override
    public void requestElevator(int atFloor, TravelDirection desiredTravelDirection) {
        // TODO Implement. This represents a human standing in the corridor,
        //  requesting that an elevator comes to pick them up for travel into the given direction.
        //  The system is supposed to make sure that an elevator will eventually reach this floor to pick up the human.
        //  The human can then enter the elevator and request their actual destination within the elevator.
        //  Ideally this has to select the best elevator among all which can reduce the time
        //  for the human spending waiting (either in corridor or in the elevator itself).
        bestElevator(atFloor, desiredTravelDirection).requestDestinationFloor(atFloor);
    }

    public void moveOneFloor() {
        elevators.forEach(elevator -> elevatorListeners.forEach(listener -> listener.onElevatorArrivedAtFloor(elevator)));
        elevators.forEach(Elevator::moveOneFloor);

    }
}
