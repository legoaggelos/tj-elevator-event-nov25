package org.togetherjava.event.elevator.elevators;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A single elevator that can serve a given amount of floors.
 * <p>
 * An elevator can take floor requests from either humans or the elevator system itself.
 * The elevator will eventually move towards the requested floor and transport humans to their destinations.
 */
public final class Elevator implements ElevatorPanel {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    private final int id;
    private final int minFloor;
    private final int floorsServed;
    private int currentFloor;
    private TravelDirection state; //UP = going up, DOWN = going down
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
        this.id = NEXT_ID.getAndIncrement();
        this.minFloor = minFloor;
        this.currentFloor = currentFloor;
        this.floorsServed = floorsServed;
        int minFloorDistance = Math.abs(currentFloor-minFloor);
        int maxFloorDistance = Math.abs(currentFloor- getTopFloor());
        if (minFloorDistance == 0 ) { //if we are are at the minFloor, we should go up.
            this.state = TravelDirection.UP;
        } else if (maxFloorDistance == 0) { //if we are are at the top floor, we should go down .
            this.state = TravelDirection.DOWN;
        }
    }

    public void setState(TravelDirection state) {
        this.state = state;
    }

    public TravelDirection getTravelDirection() {
        return this.state;
    }

    public int getTopFloor() {
        return minFloor + floorsServed - 1;
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

    @Override
    public void requestDestinationFloor(int destinationFloor) {
        // TODO Implement. This represents a human or the elevator system
        //  itself requesting this elevator to eventually move to the given floor.
        //  The elevator is supposed to memorize the destination in a way that
        //  it can ensure to eventually reach it.
        //empty for paternoster.
    }

    public void incrementFloorByOne() {
        if (currentFloor + 1 > getTopFloor()) {
            return;
        }
        currentFloor++;
    }

    public void decrementFloorByOne() {
        if (currentFloor - 1 < minFloor) {
            return;
        }
        currentFloor--;
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
        if (currentFloor == minFloor) {
            state = TravelDirection.UP; //if we are at the bottom, we go up
        } else if (currentFloor == getTopFloor()) {
            state = TravelDirection.DOWN; //if we are at the top, we need to start going down.
        }
        if (state == TravelDirection.UP) {
            this.incrementFloorByOne();
        } else {
            this.decrementFloorByOne();
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
