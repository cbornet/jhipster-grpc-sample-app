package io.github.jhipster.grpcsample.service;

import io.github.jhipster.grpcsample.domain.Label;
import io.github.jhipster.grpcsample.repository.LabelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Label.
 */
@Service
@Transactional
public class LabelService {

    private final Logger log = LoggerFactory.getLogger(LabelService.class);
    
    private final LabelRepository labelRepository;

    public LabelService(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    /**
     * Save a label.
     *
     * @param label the entity to save
     * @return the persisted entity
     */
    public Label save(Label label) {
        log.debug("Request to save Label : {}", label);
        Label result = labelRepository.save(label);
        return result;
    }

    /**
     *  Get all the labels.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Label> findAll(Pageable pageable) {
        log.debug("Request to get all Labels");
        Page<Label> result = labelRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one label by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Label findOne(Long id) {
        log.debug("Request to get Label : {}", id);
        Label label = labelRepository.findOne(id);
        return label;
    }

    /**
     *  Delete the  label by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Label : {}", id);
        labelRepository.delete(id);
    }
}
