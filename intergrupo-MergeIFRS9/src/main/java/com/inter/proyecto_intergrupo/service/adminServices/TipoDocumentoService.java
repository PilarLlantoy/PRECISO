package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import com.inter.proyecto_intergrupo.repository.admin.TipoDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class TipoDocumentoService {
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    public List<TipoDocumento> findAll(){return tipoDocumentoRepository.findAll();}
    public TipoDocumento findTipoDocumentoById(int id){ return tipoDocumentoRepository.findById(id); }

    public List<TipoDocumento> findAllActiveTipoDocumento() {
        return tipoDocumentoRepository.findByEstado(true);
    }

    public TipoDocumento actualizarTipoDocumento(TipoDocumento cargo){
        return tipoDocumentoRepository.save(cargo);
    }

    public TipoDocumento findTipoDocumentoByNombre(String name){return tipoDocumentoRepository.findByNombre(name);}

}
