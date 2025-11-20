package org.togetherjava.event.elevator.humans;

import org.togetherjava.event.elevator.elevators.Elevator;
import org.togetherjava.event.elevator.elevators.ElevatorPanel;
import org.togetherjava.event.elevator.elevators.FloorPanelSystem;
import org.togetherjava.event.elevator.elevators.TravelDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.StringJoiner;

/**
 * A single human that starts at a given floor and wants to
 * reach a destination floor via an elevator.
 * <p>
 * The class mainly acts upon given elevator events it listens to,
 * for example requesting an elevator, eventually entering and exiting them.
 */
public final class Human implements ElevatorListener {
    private State currentState;
    private final int startingFloor;
    private final int destinationFloor;
    /**
     * If the human is currently inside an elevator, this is its unique ID.
     * Otherwise, this is {@code null} to indicate that the human is currently on the corridor.
     */
    private Integer currentEnteredElevatorId;
    private List<HumanArrivedListener> listeners = new ArrayList<>();

    /**
     * Creates a new human.
     * <p>
     * It is supported that starting and destination floors are equal.
     * The human will then not travel with an elevator at all.
     *
     * @param startingFloor    the floor the human currently stands at, must be greater than or equal to 1
     * @param destinationFloor the floor the human eventually wants to reach, must be greater than or equal to 1
     */
    public Human(int startingFloor, int destinationFloor) {
        if (startingFloor <= 0 || destinationFloor <= 0) {
            throw new IllegalArgumentException("Floors must be at least 1");
        }

        this.startingFloor = startingFloor;
        this.destinationFloor = destinationFloor;

        currentState = State.IDLE;
    }

    public void addListener(HumanArrivedListener listener) {
        this.listeners.add(listener);
    }

    private void setArrived() {
        if (currentState == State.ARRIVED) {
            return; //dont want to notify listeners again for our arrival
        }
        this.currentState = State.ARRIVED;
        for (var listener : listeners) {
            listener.onHumanArrived(this);
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public int getStartingFloor() {
        return startingFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public TravelDirection getTravelDirection() {
        return destinationFloor > startingFloor ? TravelDirection.UP : TravelDirection.DOWN;
    }

    @Override
    public void onElevatorSystemReady(FloorPanelSystem floorPanelSystem) {
        // TODO Implement. The system is now ready and the human should leave
        //  their initial IDLE state, requesting an elevator by clicking on the buttons of
        //  the floor panel system. The human will now enter the WAITING_FOR_ELEVATOR state.
        if (getCurrentState() != State.IDLE) { //if the human isnt idle nothing should be done
            return;
        }
        this.currentState = State.WAITING_FOR_ELEVATOR;
        if (destinationFloor == startingFloor) { //human is alr at the correct floor? Arrived!
            this.setArrived();
        }
    }

    @Override
    public void onElevatorArrivedAtFloor(ElevatorPanel elevatorPanel) {
        // TODO Implement. If the human is currently waiting for an elevator and
        //  this event represents arrival at the humans current floor, the human can now enter the
        //  elevator and request their actual destination floor. The state has to change to TRAVELING_WITH_ELEVATOR.
        //  If the human is currently traveling with this elevator and the event represents
        //  arrival at the human's destination floor, the human can now exit the elevator.
        if ( //arrived or on an irrelevant floor? do nothing
                (this.getDestinationFloor() != elevatorPanel.getCurrentFloor() && this.getStartingFloor() != elevatorPanel.getCurrentFloor())
                || getCurrentState() == State.ARRIVED
        ) {
            return;
        }
        if (getCurrentState() == State.WAITING_FOR_ELEVATOR && elevatorPanel.getCurrentFloor() == startingFloor && currentEnteredElevatorId == null
                && (getTravelDirection() == elevatorPanel.getTravelDirection() || elevatorPanel.getTopFloor() == elevatorPanel.getCurrentFloor() || elevatorPanel.getCurrentFloor() == elevatorPanel.getMinFloor())
        ) {
            //elevator in our floor and travelling in the same direction? hop in. If they both are at the top or min floor they can join no matter the travel direction, because they have only one option.
            this.currentEnteredElevatorId = elevatorPanel.getId();
            this.currentState = State.TRAVELING_WITH_ELEVATOR;
            return;
        }
        //elevator reached our floor and we are travelling with it? hop out
        if (getCurrentState() == State.TRAVELING_WITH_ELEVATOR && this.currentEnteredElevatorId != null && this.currentEnteredElevatorId == elevatorPanel.getId() && destinationFloor == elevatorPanel.getCurrentFloor()) {
            this.setArrived();
            this.currentEnteredElevatorId = null;
        }
    }

    public OptionalInt getCurrentEnteredElevatorId() {
        return currentEnteredElevatorId == null
                ? OptionalInt.empty()
                : OptionalInt.of(currentEnteredElevatorId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Human.class.getSimpleName() + "[", "]")
                .add("currentState=" + currentState)
                .add("startingFloor=" + startingFloor)
                .add("destinationFloor=" + destinationFloor)
                .add("currentEnteredElevatorId=" + currentEnteredElevatorId)
                .toString();
    }

    public enum State {
        IDLE,
        WAITING_FOR_ELEVATOR,
        TRAVELING_WITH_ELEVATOR,
        ARRIVED
    }
}
