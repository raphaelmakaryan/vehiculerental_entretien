package fr.vehiclerental.maintenance.service;

import fr.vehiclerental.maintenance.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceDAO extends JpaRepository<Maintenance, Integer> {
    List<Maintenance> findById(int id);

    List<Maintenance> findByIdVehicule(int id_vehicule);

    List<Maintenance> findAll();

    void delete(Maintenance maintenance);

    Maintenance save(Maintenance maintenance);
}