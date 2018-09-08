package in.edu.jaduniv.classroom.interfaces;

import java.io.Serializable;

/**
 * Interface to contains value event listener
 * Method (onValueChanged) - called when value is changed
 */

public interface ValueEventListener extends Serializable {

    void onValueChanged(Object newValue, Object oldValue);
}
