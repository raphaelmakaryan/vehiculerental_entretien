package fr.vehiclerental.maintenance.controller;

import fr.vehiclerental.maintenance.entity.*;
import fr.vehiclerental.maintenance.exception.*;
import fr.vehiclerental.maintenance.service.MaintenanceDAO;
import fr.vehiclerental.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

@RestController
public class WebAppController {
    private final MaintenanceDAO maintenanceDao;
    private final MaintenanceService maintenanceService;
    private final ReservationDTO reservationDTO;

    public WebAppController(MaintenanceDAO maintenanceDao, MaintenanceService maintenanceService, ReservationDTO reservationDTO) {
        this.maintenanceDao = maintenanceDao;
        this.maintenanceService = maintenanceService;
        this.reservationDTO = reservationDTO;
    }

    @Operation(summary = "Voir toute les entretiens de la base de données ", description = "Requête pour la récupération de toute les entretiens de la base de données ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Opération réussi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Maintenance.class)))})
    @GetMapping("/maintenance")
    public List<Maintenance> maintenance() {
        return maintenanceDao.findAll();
    }


    @Operation(summary = "Voir un entretien spécifique de la base de données", description = "Requête pour la récupération d'un entretien de la base de données")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Opération réussi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Maintenance.class))), @ApiResponse(responseCode = "405", description = "Échec de l'opération ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MaintenanceNotFind.class)))})
    @RequestMapping(path = "/maintenance/{id}", method = RequestMethod.GET)
    public List<Maintenance> getMaintenance(@Parameter(description = "Identifiant de l'entretien", required = true) @PathVariable(value = "id") int id) {
        try {
            return maintenanceDao.findById(id);
        } catch (Exception e) {
            throw new MaintenanceNotFind();
        }
    }

/*
    @Operation(summary = "Crée un nouvel entretien dans la base de données", description = "Requête pour crée/ajouter un entretien dans la base de données")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Opération réussi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationAdd.class))), @ApiResponse(responseCode = "405", description = "Erreur métier",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "Client introuvable",
                                    value = """
                                            {
                                              "timestamp": "2025-11-06T15:00:00",
                                              "status": 404,
                                              "error": "Client introuvable",
                                              "message": "Client not found with ID : 1"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Client déjà réservé",
                                    value = """
                                            {
                                              "timestamp": "2025-11-06T15:00:00",
                                              "status": 404,
                                              "error": "Client déjà réservé",
                                              "message": "Ce client dispose déjà d'une réservation en cours"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Véhicule déjà réservé",
                                    value = """
                                            {
                                              "timestamp": "2025-11-06T15:00:00",
                                              "status": 404,
                                              "error": "Véhicule déjà réservé",
                                              "message": "Le véhicule 12 est déjà réservé pour cette période"
                                            }
                                            """
                            )
                    }
            )
    )})

 */

    @RequestMapping(value = "/maintenance", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addMaintenance(@Validated @RequestBody RequiredMaintenance informations) {
        List<VehicleDTO> vehicleVerification = maintenanceService.requestVehicle(informations.getId_vehicle());
        if (!vehicleVerification.isEmpty()) {
            VehicleDTO vehicleDTO = vehicleVerification.getFirst();
            List<UnavailabilityDTO> unavailabilityVerification = maintenanceService.requestUnavaibility(informations.getId_unavailability());
            if (!unavailabilityVerification.isEmpty()) {
                UnavailabilityDTO unavailability = unavailabilityVerification.getFirst();
                if (maintenanceService.requestReservation(informations.getId_vehicle())) {
                    if (maintenanceService.typeVerificationUnavaibility(unavailability.getTypeVehicle(), vehicleDTO.getType())) {
                        Map<String, Object> response = new HashMap<>();
                        Maintenance maintenance = new Maintenance();
                        maintenance.setIdVehicule(vehicleDTO.getId());
                        maintenance.setIdUnavailability(unavailability.getId());
                        maintenanceDao.save(maintenance);
                        response.put("success", true);
                        response.put("message", "Votre entretien a été ajouté !");
                        return ResponseEntity.ok(response);
                    } else {
                        throw new VehicleType();
                    }
                } else {
                    throw new VehicleAlreadyReserved();
                }
            } else {
                throw new UnavailabilityNotFind();
            }
        } else {
            throw new VehicleNotFind();
        }
    }


    @Operation(summary = "Mettre à jour une maintenance dans la base de données", description = "Requête pour mettre a jour une maintenance dans la base de données ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Opération réussi", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\n" + "    \"success\": true,\n" + "    \"message\": \"Votre reservation a été modifié !\"\n" + "}"))), @ApiResponse(responseCode = "405", description = "Échec de l'opération ", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Erreur générale", value = "{\n" + "  \"localDateTime\": \"2025-11-03T08:25:00\",\n" + "  \"message\": \"Maintenance not found with ID : 1\",\n" + "  \"status\": 404\n" + "}")
    }))})
    @PutMapping("/maintenance/{id}")
    public ResponseEntity<Map<String, Object>> editMaintenance(
            @Parameter(description = "Identifiant de la maintenance", required = true) @PathVariable(value = "id") int idMaintenance,
            @Validated @RequestBody Maintenance maintenanceRequest) {
        try {
            System.out.println(maintenanceRequest.toString());
            List<Maintenance> maintenance = maintenanceService.getMaintenanceOrThrow(idMaintenance, maintenanceDao);
            VehicleDTO vehicle = maintenanceService.getVehicleOrThrow(maintenanceRequest.getIdVehicule());
            UnavailabilityDTO unavailability = maintenanceService.getUnavailabilityOrThrow(maintenanceRequest.getIdUnavailability());
            if (!maintenanceService.typeVerificationUnavaibility(unavailability.getTypeVehicle(), vehicle.getType())) {
                throw new VehicleType();
            } else {
                maintenanceService.editMaintenance(maintenance.getFirst(), maintenanceRequest, maintenanceDao);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Votre maintenance a été modifié !");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Operation(summary = "Supprimer une maintenance de la base de données", description = "Requête pour supprimer une maintenance de la base de données")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Opération réussi", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\n" + "    \"success\": true,\n" + "    \"message\": \"Votre reservation a été supprimé !\"\n" + "}"))), @ApiResponse(responseCode = "405", description = "Échec de l'opération ", content = @Content(mediaType = "application/json", examples = {@ExampleObject(name = "Erreur générale", value = "{\n" + "  \"localDateTime\": \"2025-11-03T08:25:00\",\n" + "  \"message\": \"Reservation not found with ID : 1 \",\n" + "  \"status\": 404\n" + "}")
    }))})
    @DeleteMapping("/maintenance/{id}")
    public ResponseEntity<Map<String, Object>> deleteMaintenance(
            @Parameter(description = "Identifiant de la maintenance", required = true) @PathVariable(value = "id") int idMaintenance) {
        List<Maintenance> maintenances = maintenanceDao.findById(idMaintenance);
        if (maintenances == null || maintenances.isEmpty()) {
            throw new MaintenanceNotFind();
        } else {
            maintenanceDao.delete(maintenances.getFirst());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Votre maintenance a été supprimé !");
            return ResponseEntity.ok(response);
        }
    }

    /*
    @Operation(summary = "Voir un entretien spécifique de la base de données", description = "Requête pour la récupération d'un entretien de la base de données")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Opération réussi", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Maintenance.class))), @ApiResponse(responseCode = "405", description = "Échec de l'opération ", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MaintenanceNotFind.class)))})
     */
    @RequestMapping(path = "/maintenance/vehicle/{id}", method = RequestMethod.GET)
    public List<Maintenance> getMaintenanceVehicleId(@Parameter(description = "Identifiant du vehicule", required = true) @PathVariable(value = "id") int idVehicle) {
        try {
            return maintenanceDao.findByIdVehicule(idVehicle);
        } catch (Exception e) {
            throw new MaintenanceNotFind();
        }
    }
}

