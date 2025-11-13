package fr.vehiclerental.maintenance.exception;

public class VehicleType extends RuntimeException {
    public VehicleType() {
        super("Le type de véhicule n'est pas autorisé a avoir ce soucis !");
    }
}
