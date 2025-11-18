package org.togetherjava.event.elevator.elevators;

import org.togetherjava.event.elevator.humans.Human;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A single elevator that can serve a given amount of floors.
 * <p>
 * An elevator can take floor requests from either humans or the elevator system itself.
 * The elevator will eventually move towards the requested floor and transport humans to their destinations.
 */
public final class Elevator implements ElevatorPanel {
    public enum Role {
        UP_TRAVELLER,
        DOWN_TRAVELLER,
        SOLE_ELEVATOR
    }

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    private final int id;
    private final int minFloor;
    private final int floorsServed;
    private int currentFloor;
    private boolean shouldGoUp;
    private boolean isSoleElevator = true;
    private Role role = Role.SOLE_ELEVATOR;
    private boolean hasReachedLimit = false;

    public void setRole(Role role) {
        this.role = role;
    }

    public void setSoleElevator(boolean soleElevator) {
        isSoleElevator = soleElevator;
    }

    private final List<Integer> startingFloors = new ArrayList<>();
    private final List<Integer> destFloors = new ArrayList<>();

    public List<Integer> getStartingFloors() {
        return startingFloors;
    }

    public List<Integer> getDestFloors() {
        return destFloors;
    }

    /**
     * Creates a new elevator.
     *
     * @param minFloor     the minimum floor that the elevator can serve, must be greater than or equal to 1.
     * @param floorsServed the amount of floors served in total by this elevator, must be greater than or equal to 2.
     *                     Together with the minFloor this forms a consecutive range of floors with no gaps in between.
     * @param currentFloor the floor the elevator starts at, must be within the defined range of floors served by the elevator
     */
    public Elevator(int minFloor, int floorsServed, int currentFloor) {
        if (minFloor <= 0 || floorsServed < 2) {
            throw new IllegalArgumentException("Min floor must at least 1, floors served at least 2.");
        }
        if (currentFloor < minFloor || currentFloor >= minFloor + floorsServed) {
            throw new IllegalArgumentException("The current floor must be between the floors served by the elevator.");
        }
        this.shouldGoUp = currentFloor < minFloor + floorsServed - 1;
        this.id = NEXT_ID.getAndIncrement();
        this.minFloor = minFloor;
        this.currentFloor = currentFloor;
        this.floorsServed = floorsServed;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getMinFloor() {
        return minFloor;
    }

    public int getFloorsServed() {
        return floorsServed;
    }

    @Override
    public int getCurrentFloor() {
        return currentFloor;
    }

    public void addStartingFloor(int startingFloor) {
        if (startingFloor < minFloor || minFloor + floorsServed - 1 < startingFloor || startingFloors.contains(startingFloor)) { //invalid floor; too big or too little
            return;
        }
        startingFloors.add(startingFloor);
    }

    @Override
    public void requestDestinationFloor(int destinationFloor) {
        // TODO Implement. This represents a human or the elevator system
        //  itself requesting this elevator to eventually move to the given floor.
        //  The elevator is supposed to memorize the destination in a way that
        //  it can ensure to eventually reach it.
        if (destinationFloor < minFloor || minFloor + floorsServed - 1 < destinationFloor || destFloors.contains(destinationFloor)) {
            return;
        }
        destFloors.add(destinationFloor);
    }

    public static TravelDirection getDirectionFromHuman(Human human) {
        if (human.getStartingFloor() == human.getDestinationFloor()) {
            return TravelDirection.STATIONARY;
        }
        if (human.getDestinationFloor() - human.getStartingFloor() > 0) {
            return TravelDirection.UP;
        } else {
            return TravelDirection.DOWN;
        }
    }

    @Override
    public boolean shouldJoin(Human human) {
        TravelDirection humanTravelDirection = getDirectionFromHuman(human);
        if (humanTravelDirection == TravelDirection.STATIONARY) return false;
        if (isSoleElevator) return true;
        if ((humanTravelDirection == TravelDirection.UP && role == Role.UP_TRAVELLER) ||
            (humanTravelDirection == TravelDirection.DOWN && role == Role.DOWN_TRAVELLER)
        ) {
            return true;
        }
        return false;
    }

    public void incrementFloorByOne() {
        if (currentFloor+1 > minFloor + floorsServed - 1) {
            return;
        }
        currentFloor++;
    }

    public void decrementFloorByOne() {
        if (currentFloor-1 < minFloor) {
            return;
        }
        currentFloor--;
    }

    public void paternosterMoveOneFloor() {
        if (currentFloor == minFloor) {
            shouldGoUp = true;
        } else if (currentFloor == minFloor + floorsServed - 1) {
            shouldGoUp = false;
        }
        if (shouldGoUp) {
            incrementFloorByOne();
        } else {
            decrementFloorByOne();
        }
    }

    public void moveOneFloor() {
        // TODO Implement. Essentially there are three possibilities:
        //  - move up one floor
        //  - move down one floor
        //  - stand still
        //  The elevator is supposed to move in a way that it will eventually reach
        //  the floors requested by Humans via requestDestinationFloor(), ideally "fast" but also "fair",
        //  meaning that the average time waiting (either in corridor or inside the elevator)
        //  is minimized across all humans.
        //  It is essential that this method updates the currentFloor field accordingly.
        if (role == Role.SOLE_ELEVATOR) {
            paternosterMoveOneFloor();
            return;
        }
        if ((role == Role.DOWN_TRAVELLER && currentFloor == minFloor + floorsServed - 1) ||
                (role == Role.UP_TRAVELLER && currentFloor == minFloor)) {
            hasReachedLimit = true;
        }
        if (hasReachedLimit) {
            if (role == Role.DOWN_TRAVELLER) {
                decrementFloorByOne();
            } else {
                incrementFloorByOne();
            }
        } else {
            if (role == Role.DOWN_TRAVELLER) {
                incrementFloorByOne();
            } else {
                decrementFloorByOne();
            }
        }

    }

    @Override
    public synchronized String toString() {
        return new StringJoiner(", ", Elevator.class.getSimpleName() + "[", "]").add("id=" + id)
                .add("minFloor=" + minFloor)
                .add("floorsServed=" + floorsServed)
                .add("currentFloor=" + currentFloor)
                .toString();
    }
}
