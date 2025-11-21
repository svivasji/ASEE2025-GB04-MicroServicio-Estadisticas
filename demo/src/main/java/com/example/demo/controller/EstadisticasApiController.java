package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.EstadisticaAlbumDocument;
import com.example.demo.model.EstadisticaCancionDocument;
import com.example.demo.model.ReproduccionDocument;
import com.example.demo.model.ValoracionDocument;
import com.example.demo.repository.ReproduccionRepository;
import com.example.demo.repository.ValoracionRepository;
import com.example.demo.service.ContenidoService;
import com.example.demo.service.EstadisticasUpdaterService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EstadisticasApiController {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Autowired
    private ReproduccionRepository reproduccionRepository;

    @Autowired
    private EstadisticasUpdaterService updaterService;

    @Autowired
    private ContenidoService contenidoService;


    // ----------------------------------------------------
    // GET /estadisticas/canciones/{id}
    // ----------------------------------------------------
    @GetMapping("/estadisticas/canciones/{id}")
    public ResponseEntity<EstadisticaCancionDocument> obtenerEstadisticasCancion(@PathVariable("id") Integer id) {

        List<ValoracionDocument> valoraciones = valoracionRepository.findByIdSong(id);

        EstadisticaCancionDocument estadistica = new EstadisticaCancionDocument();
        estadistica.setIdCancion(id);

        // Reproducciones — SIEMPRE número correcto
        estadistica.setReproduccionesTotales(reproduccionRepository.countByIdCancion(id));

        // Valoraciones
        if (valoraciones.isEmpty()) {
            estadistica.setValoracionMedia(0.0f);
            estadistica.setTotalValoraciones(0);
        } else {
            int total = valoraciones.size();
            double suma = valoraciones.stream().mapToDouble(ValoracionDocument::getValoracion).sum();
            estadistica.setTotalValoraciones(total);
            estadistica.setValoracionMedia((float) (suma / total));
        }

        return ResponseEntity.ok(estadistica);
    }


    // ----------------------------------------------------
    // GET /estadisticas/canciones
    // ----------------------------------------------------
    @GetMapping("/estadisticas/canciones")
    public ResponseEntity<List<EstadisticaCancionDocument>> getEstadisticasCanciones() {

        // IDs de canciones que tienen reproducciones
        List<Integer> idsReproducciones = reproduccionRepository.findAllSongIdsDistinct();

        // IDs de canciones que tienen valoraciones
        List<Integer> idsValoraciones = valoracionRepository.findAll()
                .stream()
                .map(ValoracionDocument::getIdSong)
                .distinct()
                .collect(Collectors.toList());

        // Todos los IDs únicos
        Set<Integer> allSongIds = new HashSet<>();
        allSongIds.addAll(idsReproducciones);
        allSongIds.addAll(idsValoraciones);

        Map<Integer, EstadisticaCancionDocument> mapa = new HashMap<>();

        // Inicializar todas las estadísticas en 0
        for (Integer id : allSongIds) {
            EstadisticaCancionDocument e = new EstadisticaCancionDocument();
            e.setIdCancion(id);
            e.setValoracionMedia(0f);
            e.setTotalValoraciones(0);
            e.setReproduccionesTotales(0L);
            mapa.put(id, e);
        }

        // Reproducciones
        for (Integer id : allSongIds) {
            long totalRep = reproduccionRepository.countByIdCancion(id);
            mapa.get(id).setReproduccionesTotales(totalRep);
        }

        // Valoraciones
        List<ValoracionDocument> valoraciones = valoracionRepository.findAll();
        for (ValoracionDocument v : valoraciones) {
            EstadisticaCancionDocument e = mapa.get(v.getIdSong());
            if (e != null) {
                e.setTotalValoraciones(e.getTotalValoraciones() + 1);
                e.setValoracionMedia(e.getValoracionMedia() + v.getValoracion());
            }
        }

        // Calcular medias reales
        for (EstadisticaCancionDocument e : mapa.values()) {
            if (e.getTotalValoraciones() > 0) {
                e.setValoracionMedia(e.getValoracionMedia() / e.getTotalValoraciones());
            }
        }

        // Devolver ordenado por id
        List<EstadisticaCancionDocument> out = new ArrayList<>(mapa.values());
        out.sort(Comparator.comparing(EstadisticaCancionDocument::getIdCancion));

        return ResponseEntity.ok(out);
    }


    // ----------------------------------------------------
    // GET /estadisticas/albumes
    // ----------------------------------------------------
    
@GetMapping("/estadisticas/albumes")
    public ResponseEntity<List<EstadisticaAlbumDocument>> getEstadisticasAlbumes() {

        Map<Integer, EstadisticaAlbumDocument> mapa = new HashMap<>();

        // PASO 1: Buscar álbumes a través de las canciones que tienen REPRODUCCIONES
        // -------------------------------------------------------------------------
        List<Integer> songIdsConPlays = reproduccionRepository.findAllSongIdsDistinct();
        
        // Usamos un Set para no repetir álbumes
        Set<Integer> albumIdsActivos = new HashSet<>();

        for (Integer idCancion : songIdsConPlays) {
            // Llamamos al servicio de contenido para saber de qué álbum es la canción
            Integer idAlbum = contenidoService.obtenerIdAlbumPorCancion(idCancion);
            if (idAlbum != null && idAlbum > 0) {
                albumIdsActivos.add(idAlbum);
            }
        }

        // Inicializamos el mapa con estos álbumes
        for (Integer idAlbum : albumIdsActivos) {
            EstadisticaAlbumDocument est = new EstadisticaAlbumDocument();
            est.setIdAlbum(idAlbum);
            est.setReproduccionesTotales(0L); // Iniciamos en 0
            est.setTotalValoraciones(0);
            est.setValoracionMedia(0f); // O float, según tu modelo
            mapa.put(idAlbum, est);
        }

        // PASO 2: Añadir información de VALORACIONES (Tu lógica original mejorada)
        // -----------------------------------------------------------------------
        List<ValoracionDocument> valoraciones = valoracionRepository.findAll();
        for (ValoracionDocument v : valoraciones) {
            Integer idAlbum = v.getIdAlbum();
            if (idAlbum == null) continue;

            // Si el álbum no estaba por reproducciones, lo añadimos ahora
            mapa.putIfAbsent(idAlbum, new EstadisticaAlbumDocument());
            EstadisticaAlbumDocument est = mapa.get(idAlbum);
            
            if (est.getIdAlbum() == null) {
                est.setIdAlbum(idAlbum);
                est.setReproduccionesTotales(0L);
                est.setValoracionMedia(0f);
                est.setTotalValoraciones(0);
            }

            est.setTotalValoraciones(est.getTotalValoraciones() + 1);
            est.setValoracionMedia(est.getValoracionMedia() + v.getValoracion());
        }

        // PASO 3: Calcular REPRODUCCIONES TOTALES por álbum
        // -----------------------------------------------------------------------
        // Ahora recorremos el mapa completo (que ya tiene álbumes con plays Y álbumes con ratings)
        for (Integer idAlbum : mapa.keySet()) {
            List<Integer> idsCanciones = contenidoService.obtenerIdsCancionesPorAlbum(idAlbum);
            
            if (idsCanciones != null && !idsCanciones.isEmpty()) {
                long totalRep = 0L;
                for (Integer idCancion : idsCanciones) {
                    totalRep += reproduccionRepository.countByIdCancion(idCancion);
                }
                mapa.get(idAlbum).setReproduccionesTotales(totalRep);
            }
        }

        // PASO 4: Calcular media real de valoraciones
        // -----------------------------------------------------------------------
        for (EstadisticaAlbumDocument est : mapa.values()) {
            if (est.getTotalValoraciones() > 0) {
                est.setValoracionMedia(est.getValoracionMedia() / est.getTotalValoraciones());
            }
        }

        return ResponseEntity.ok(new ArrayList<>(mapa.values()));
    }


    // ----------------------------------------------------
    // POST /reproducciones
    // ----------------------------------------------------
    @PostMapping("/reproducciones")
    public ResponseEntity<ReproduccionDocument> postReproduccion(@RequestBody ReproduccionDocument reproduccion) {

        reproduccion.setFecha(LocalDateTime.now());
        ReproduccionDocument nueva = reproduccionRepository.save(reproduccion);

        // Actualizar estadísticas
        updaterService.actualizarEstadisticasPostReproduccion(reproduccion.getIdCancion());

        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }


    // ----------------------------------------------------
    // POST /estadisticas/albumes/{id}/actualizar-reproducciones
    // ----------------------------------------------------
    @PostMapping("/estadisticas/albumes/{id}/actualizar-reproducciones")
    public ResponseEntity<Void> actualizarReproduccionesAlbum(@PathVariable("id") Integer id) {
        updaterService.actualizarReproduccionesTotalesAlbum(id);
        return ResponseEntity.ok().build();
    }


    // ----------------------------------------------------
    // DELETE /estadisticas/canciones/{id}
    // ----------------------------------------------------
    @DeleteMapping("/estadisticas/canciones/{id}")
    public ResponseEntity<Void> borrarEstadisticasCancion(@PathVariable("id") Integer id) {
        updaterService.borrarEstadisticasCancionYActualizarAlbum(id);
        return ResponseEntity.noContent().build();
    }


    // ----------------------------------------------------
    // POST /estadisticas/canciones/reproducciones
    // ----------------------------------------------------
    @PostMapping("/estadisticas/canciones/reproducciones")
    public ResponseEntity<Map<String, Long>> obtenerSumaReproducciones(@RequestBody List<Integer> ids) {

        long suma = ids.stream()
                .mapToLong(id -> reproduccionRepository.countByIdCancion(id))
                .sum();

        Map<String, Long> r = new HashMap<>();
        r.put("reproducciones_totales", suma);
        return ResponseEntity.ok(r);
    }
}
