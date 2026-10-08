package cr.ac.una.smartroute.business;

import cr.ac.una.smartroute.business.exception.LugarInexistenteOInactivoException;
import cr.ac.una.smartroute.business.exception.ResenaDuplicadaException;
import cr.ac.una.smartroute.business.exception.ResenaInexistenteException;
import cr.ac.una.smartroute.data.entity.Lugar;
import cr.ac.una.smartroute.data.entity.Resena;
import cr.ac.una.smartroute.data.entity.Usuario;
import cr.ac.una.smartroute.data.repository.LugarRepository;
import cr.ac.una.smartroute.data.repository.ResenaRepository;
import cr.ac.una.smartroute.data.repository.UsuarioRepository;
import cr.ac.una.smartroute.data.specification.ResenaSpecifications;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;

    public ResenaService(
            ResenaRepository resenaRepository, LugarRepository lugarRepository, UsuarioRepository usuarioRepository) {
        this.resenaRepository = resenaRepository;
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Resena crear(Long usuarioId, Long lugarId, short calificacion, String comentario) {
        Lugar lugar = lugarRepository
                .findById(lugarId)
                .filter(Lugar::isActivo)
                .orElseThrow(() -> new LugarInexistenteOInactivoException(lugarId));

        if (resenaRepository.exists(ResenaSpecifications.conFiltrosOpcionales(lugarId, usuarioId))) {
            throw new ResenaDuplicadaException(lugarId);
        }

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        Resena resena = new Resena();
        resena.setUsuario(usuario);
        resena.setLugar(lugar);
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setCreadoEn(LocalDateTime.now());

        return resenaRepository.save(resena);
    }

    @Transactional(readOnly = true)
    public Page<Resena> listarPaginado(Specification<Resena> filtro, Pageable pageable) {
        return resenaRepository.findAll(filtro, pageable);
    }

    @Transactional(readOnly = true)
    public Resena obtener(Long id) {
        return resenaRepository.findById(id).orElseThrow(() -> new ResenaInexistenteException(id));
    }
}
