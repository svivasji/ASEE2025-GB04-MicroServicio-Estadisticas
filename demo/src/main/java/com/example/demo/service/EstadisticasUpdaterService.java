package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Import necesario
import org.springframework.context.annotation.Lazy; // Import necesario
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.EstadisticaAlbumDocument;
import com.example.demo.model.EstadisticaCancionDocument;
import com.example.demo.model.ValoracionDocument;
import com.example.demo.repository.EstadisticaAlbumRepository;
import com.example.demo.repository.EstadisticaCancionRepository; 
import com.example.demo.repository.ReproduccionRepository;
import com.example.demo.repository.ValoracionRepository; 

@Service
public class EstadisticasUpdaterService {

    private final ContenidoService contenidoService;
    private final ReproduccionRepository reproduccionRepository;
    private final EstadisticaAlbumRepository albumRepository;
    private final EstadisticaCancionRepository cancionRepository;
    private final ValoracionRepository valoracionRepository; 

    // SOLUCIÓN: Auto-inyección con @Lazy para obtener el Proxy
    @Autowired
    @Lazy
    private EstadisticasUpdaterService self;

    public EstadisticasUpdaterService(
        ContenidoService contenidoService, 
        ReproduccionRepository reproduccionRepository, 
        EstadisticaAlbumRepository albumRepository,
        EstadisticaCancionRepository cancionRepository, 
        ValoracionRepository valoracionRepository 
    ) {
        this.contenidoService = contenidoService;
        this.reproduccionRepository = reproduccionRepository;
        this.albumRepository = albumRepository;
        this.cancionRepository = cancionRepository;
        this.valoracionRepository = valoracionRepository;
    }

    // ----------------------------------------------------
    // LÓGICA POST-REPRODUCCIÓN
    // ----------------------------------------------------
    
    @Transactional
    public void actualizarEstadisticasPostReproduccion(Integer idCancion) {
        
        // CORRECCIÓN: Usamos 'self' en lugar de la llamada directa (this)
        self.actualizarEstadisticasCancion(idCancion);
        
        Integer idAlbum = contenidoService.obtenerIdAlbumPorCancion(idCancion);
        
        if (idAlbum != null && idAlbum > 0) {
            // CORRECCIÓN: Usamos 'self' para pasar por el proxy transaccional
            self.actualizarReproduccionesTotalesAlbum(idAlbum);
        } else {
            System.out.println("La canción " + idCancion + " no pertenece a un álbum o el ID es cero/nulo. Solo se actualizan estadísticas de canción.");
        }
    }

    // ----------------------------------------------------
    // ACTUALIZACIÓN DE CANCIÓN
    // ----------------------------------------------------
    @Transactional
    public void actualizarEstadisticasCancion(Integer idCancion) {
        List<ValoracionDocument> valoraciones = valoracionRepository.findByIdSong(idCancion);

        EstadisticaCancionDocument estadistica = cancionRepository.findById(idCancion).orElse(new EstadisticaCancionDocument());
        estadistica.setIdCancion(idCancion);

        long reproduccionesTotales = reproduccionRepository.countByIdCancion(idCancion);
        estadistica.setReproduccionesTotales((long) reproduccionesTotales);

        if (valoraciones.isEmpty()) {
            estadistica.setValoracionMedia(0.0f);
            estadistica.setTotalValoraciones(0);
        } else {
            int totalValoraciones = valoraciones.size();
            double sumaDePuntuaciones = valoraciones.stream().mapToDouble(ValoracionDocument::getValoracion).sum();
            float mediaCalculada = (float) (sumaDePuntuaciones / totalValoraciones);

            estadistica.setValoracionMedia(mediaCalculada);
            estadistica.setTotalValoraciones(totalValoraciones);
        }
        cancionRepository.save(estadistica);
        System.out.println("Actualizadas estadísticas de la Canción " + idCancion);
    }
    
    // ----------------------------------------------------
    // ACTUALIZACIÓN DE ÁLBUM
    // ----------------------------------------------------

    @Transactional
    public void actualizarReproduccionesTotalesAlbum(Integer albumId) {
        List<Integer> idsCanciones = contenidoService.obtenerIdsCancionesPorAlbum(albumId);
        if (idsCanciones.isEmpty()) {
            System.out.println("Álbum " + albumId + " no tiene canciones registradas o hubo un error al obtener la lista.");
            return;
        }
        long reproduccionesTotales = 0;
        for (Integer idCancion : idsCanciones) {
            reproduccionesTotales += reproduccionRepository.countByIdCancion(idCancion); 
        }

        Optional<EstadisticaAlbumDocument> optEstadistica = albumRepository.findById(albumId);
        
        if (optEstadistica.isPresent()) {
            EstadisticaAlbumDocument estadistica = optEstadistica.get();
            estadistica.setReproduccionesTotales(reproduccionesTotales); 
            albumRepository.save(estadistica);
            
            System.out.println("Actualizadas reproducciones del Álbum " + albumId + ": " + reproduccionesTotales);
        } else {
            System.err.println("Error: No se encontró el documento de estadística para el Álbum ID: " + albumId);
        }
    }
    
    // ----------------------------------------------------
    // BORRADO Y ACTUALIZACIÓN DE ÁLBUM
    // ----------------------------------------------------
    
    @Transactional
    public void borrarEstadisticasCancionYActualizarAlbum(Integer idCancion) {
        
        Integer idAlbum = contenidoService.obtenerIdAlbumPorCancion(idCancion); 

        // --- BORRADO LOCAL ---
        if (cancionRepository.existsById(idCancion)) {
            cancionRepository.deleteById(idCancion);
        }
        
        reproduccionRepository.deleteByIdCancion(idCancion); 
        valoracionRepository.deleteByIdSong(idCancion); 
        
        // --- ACTUALIZACIÓN AGREGADA ---
        if (idAlbum != null && idAlbum > 0) {
            // CORRECCIÓN: Usamos 'self' aquí también
            self.actualizarReproduccionesTotalesAlbum(idAlbum); 
        } 
        
        System.out.println("Proceso de borrado de Canción ID " + idCancion + " finalizado. Álbum afectado: " + idAlbum);
    }

    // ... (El resto de métodos de registro de compras pueden quedarse igual, 
    // a menos que quieras hacerlos Transactional en el futuro)

    public void registrarCompraCancion(Integer idCancion, Double precio) {
        // ... (código existente)
        EstadisticaCancionDocument stats = cancionRepository.findById(idCancion)
            .orElse(new EstadisticaCancionDocument());
        
        // ... (lógica de inicialización)
        if (stats.getIdCancion() == null) {
             // ...
             stats.setIdCancion(idCancion);
             stats.setIngresos(0.0);
             // ...
        }

        double ingresosActuales = stats.getIngresos(); 
        stats.setIngresos(ingresosActuales + precio);

        cancionRepository.save(stats);
        
        System.out.println("💰 Ingresos actualizados Canción " + idCancion + ": +" + precio);

        // Cascada al Álbum
        Integer idAlbum = contenidoService.obtenerIdAlbumPorCancion(idCancion);
        if (idAlbum != null && idAlbum > 0) {
            // Nota: Si registrarIngresoAlbum fuera @Transactional, deberías usar self.registrarIngresoAlbum
            registrarIngresoAlbum(idAlbum, precio);
        }
    }

    public void registrarIngresoAlbum(Integer idAlbum, Double precio) {
        // ... (código existente igual que antes)
        EstadisticaAlbumDocument stats = albumRepository.findById(idAlbum)
            .orElse(new EstadisticaAlbumDocument());

        if (stats.getIdAlbum() == null) {
            stats.setIdAlbum(idAlbum);
            stats.setIngresos(0.0);
            // ...
        }

        double ingresosActuales = stats.getIngresos(); 
        stats.setIngresos(ingresosActuales + precio);

        albumRepository.save(stats);

        System.out.println("💰 Ingresos actualizados Álbum " + idAlbum + ": +" + precio);
    }
}