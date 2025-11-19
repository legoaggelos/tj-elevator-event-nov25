package org.togetherjava.event.elevator.elevators;

import org.togetherjava.event.elevator.humans.ElevatorListener;

import java.util.ArrayList;
import java.util.List;

/**
 * System controlling all elevators of a building.
 * <p>
 * Once all elevators and humans have been registered via {@link #registerElevator(Elevator)}
 * and {@link #registerElevatorListener(ElevatorListener)} respectively,
 * the system can be made ready using {@link #ready()}.
 */
public final class ElevatorSystem implements FloorPanelSystem {
    private boolean smartInitialDirection = true;

    private final List<Elevator> elevators = new ArrayList<>();
    private final List<ElevatorListener> elevatorListeners = new ArrayList<>();

    public void registerElevator(Elevator elevator) {
        elevators.add(elevator);
    }

    public void registerElevatorListener(ElevatorListener listener) {
        elevatorListeners.add(listener);
    }


    /*
     * This works by calculating the amount of people who need to go up and are up compared to the elevator
     * and the amount of people who need to go down and are down compared to the elevator
     * Then, if more people need to go up, it goes up, because it will serve more people that way, resulting in higher arrived%, but more steps.
     * Same situation if more people wanna go down
     */
    public TravelDirection findOptimalDirectionForElevator(Elevator elevator) {
        int amountOfPeopleGoingUp = 0;
        int amountOfPeopleGoingDown = 0;
        int amountOfPeopleOverElev = 0;
        int amountOfPeopleUnderElev = 0;
        for (ElevatorListener human : elevatorListeners) {
            boolean overElev = human.getStartingFloor() > elevator.getCurrentFloor();
            boolean underElev = human.getStartingFloor() < elevator.getCurrentFloor(); //we have 2 variables because we dont want to consider people who are at the same floor as the elevator. If we did !overElev, people at the same level as it would be considered under it
            if (overElev && human.getTravelDirection() == TravelDirection.UP) {//over the elevator and going up
                amountOfPeopleGoingUp++;
            } else if (underElev && human.getTravelDirection() == TravelDirection.DOWN) { //under the elevator and going down
                amountOfPeopleGoingDown++;
            }
            if (overElev) {
                amountOfPeopleOverElev++;
            } else if(underElev) {
                amountOfPeopleUnderElev++;
            }
        }
        if (amountOfPeopleGoingDown == amountOfPeopleGoingUp) {
            //if they are equal, we compare amountOfPeopleOver/under. If those are equal too, it doesnt matter and we can return whatever
            return amountOfPeopleOverElev > amountOfPeopleUnderElev ? TravelDirection.UP : TravelDirection.DOWN;
        }
        return amountOfPeopleGoingDown > amountOfPeopleGoingUp ? TravelDirection.DOWN : TravelDirection.UP;
    }

    /**
     * Upon calling this, the system is ready to receive elevator requests. Elevators may now start moving.
     */
    public void ready() {
        elevatorListeners.forEach(listener -> listener.onElevatorSystemReady(this));
        for (Elevator elevator : elevators) {
            if (smartInitialDirection) {
                elevator.setState(findOptimalDirectionForElevator(elevator));
            } else {
                elevator.setState(TravelDirection.UP);
            }
        }
    }

    @Override
    public void requestElevator(int atFloor, TravelDirection desiredTravelDirection) {
        // TODO Implement. This represents a human standing in the corridor,
        //  requesting that an elevator comes to pick them up for travel into the given direction.
        //  The system is supposed to make sure that an elevator will eventually reach this floor to pick up the human.
        //  The human can then enter the elevator and request their actual destination within the elevator.
        //  Ideally this has to select the best elevator among all which can reduce the time
        //  for the human spending waiting (either in corridor or in the elevator itself).
        //nothing for paternoster.
    }

    @Override
    public void setUseSmartInitialDirection(boolean useSmartInitialDirection) {
        this.smartInitialDirection = useSmartInitialDirection;
    }


    public void moveOneFloor() {
        //these 2 are swapped, because first the people should go where they should, then the elevators should move.
        //This is to it make so if on step 1 a human is next to an elevator, he goes in, and then all the elevators move.
        //This makes one sanity test fail, but it has been corrected.
        elevators.forEach(elevator -> elevatorListeners.forEach(listener -> listener.onElevatorArrivedAtFloor(elevator)));
        elevators.forEach(Elevator::moveOneFloor);
    }
}
