package fr.vehiclerental.maintenance.exception;

public class VehicleNotFind extends RuntimeException {
    public VehicleNotFind() {
        super("The vehicle not existed.");
    }
}
