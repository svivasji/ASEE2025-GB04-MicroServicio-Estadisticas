package com.example.demo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.model.EstadisticaAlbumDocument;
import com.example.demo.model.EstadisticaCancionDocument;
import com.example.demo.repository.EstadisticaAlbumRepository;
import com.example.demo.repository.EstadisticaCancionRepository;
import com.example.demo.service.ContenidoService;
import com.example.demo.service.EstadisticasUpdaterService;



@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    private final ContenidoService contenidoService;
    private final EstadisticaCancionRepository cancionRepository;
    private final EstadisticaAlbumRepository albumRepository;
    private final EstadisticasUpdaterService updaterService;

    public DatabaseInitializer(
        ContenidoService contenidoService, 
        EstadisticaCancionRepository cancionRepository,
        EstadisticaAlbumRepository albumRepository,
        EstadisticasUpdaterService updaterService
    ) {
        this.contenidoService = contenidoService;
        this.cancionRepository = cancionRepository;
        this.albumRepository = albumRepository;
        this.updaterService = updaterService;
    }

    @Override
    public void run(String... args) throws Exception {
        inicializarEstadisticasCanciones();
        inicializarEstadisticasAlbumes();
        actualizarEstadisticasExistentes();
    }
    // ------------------------------------
    // INICIALIZACIÓN DE CANCIONES
    // ------------------------------------
    private void inicializarEstadisticasCanciones() {
        logger.info("--- Inicializando Estadísticas de Canciones ---");

        List<Integer> idsCanciones = contenidoService.obtenerIdsCanciones(); 
        
        if (idsCanciones != null && !idsCanciones.isEmpty()) {
            for (Integer id : idsCanciones) {
                if (id != null && !cancionRepository.existsById(id)) {
                    
                    EstadisticaCancionDocument nuevaEstadistica = new EstadisticaCancionDocument();
                    nuevaEstadistica.setIdCancion(id);
                    
                    cancionRepository.save(nuevaEstadistica);
                    // Usamos {} como placeholder para mejorar el rendimiento
                    logger.info("Creada estadística inicial para canción ID: {}", id);
                }
            }
        } else {
             logger.info("No se encontraron IDs de canciones para inicializar.");
        }
    }

    // ------------------------------------
    // INICIALIZACIÓN DE ÁLBUMES
    // ------------------------------------
    private void inicializarEstadisticasAlbumes() {
        logger.info("--- Inicializando Estadísticas de Álbumes ---");

        List<Integer> idsAlbumes = contenidoService.obtenerIdsAlbumes(); 
        
        if (idsAlbumes != null && !idsAlbumes.isEmpty()) {
            for (Integer id : idsAlbumes) {
                if (id != null && !albumRepository.existsById(id)) {
                    
                    EstadisticaAlbumDocument nuevaEstadistica = new EstadisticaAlbumDocument();
                    nuevaEstadistica.setIdAlbum(id);
                    
                    albumRepository.save(nuevaEstadistica);
                    logger.info("Creada estadística inicial para álbum ID: {}", id);
                }
            }
        } else {
             logger.info("No se encontraron IDs de álbumes para inicializar.");
        }
    }
    
    // ------------------------------------
    // ACTUALIZACIÓN FORZADA DE ESTADÍSTICAS
    // ------------------------------------
    private void actualizarEstadisticasExistentes() {
        logger.info("--- Actualizando todas las estadísticas existentes ---");

        List<Integer> idsCanciones = contenidoService.obtenerIdsCanciones();
        if (idsCanciones != null && !idsCanciones.isEmpty()) {
            for (Integer id : idsCanciones) {
                updaterService.actualizarEstadisticasCancion(id);
            }
        }

        List<Integer> idsAlbumes = contenidoService.obtenerIdsAlbumes();
        if (idsAlbumes != null && !idsAlbumes.isEmpty()) {
            for (Integer id : idsAlbumes) {
                updaterService.actualizarReproduccionesTotalesAlbum(id);
            }
        }
    }
}