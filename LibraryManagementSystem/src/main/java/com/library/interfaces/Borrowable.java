package com.library.interfaces;

/**
 * Interface for borrowable items in the library
 * Demonstrates interface implementation
 */
public interface Borrowable {
    /**
     * Returns the duration in days for which the item can be borrowed
     * @return number of days
     */
    int borrowDuration();
}

