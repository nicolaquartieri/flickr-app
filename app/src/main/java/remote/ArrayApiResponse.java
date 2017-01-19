package remote;

/**
 * This utility class makes easy to parse responses which contains an array.
 * This response are a json object with a property called "elements" which contains the array.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
class ArrayApiResponse<T> {
    /** The array. */
    private T[] elements;

    /**
     * Rrturns the array.
     * @return the array.
     */
    public T[] getElements() {
        return elements;
    }
}
