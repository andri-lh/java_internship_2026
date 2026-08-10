package al.lhind.internship.exception;

public class RoomCapacityExceededException extends RuntimeException {
    public RoomCapacityExceededException(String message) {
        super(message);
    }
}
