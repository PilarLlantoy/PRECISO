package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentDecisionTreeRepository extends JpaRepository<SegmentDecisionTree,Integer> {
    List<SegmentDecisionTree> findAll();
    List<SegmentDecisionTree> findAllById(Integer id);
}
