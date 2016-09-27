package com.runzii.blog.repositories;

import com.runzii.blog.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Raysmond<i@raysmond.com>.
 */
public interface TagRepository extends JpaRepository<Tag, Long>{
    Tag findByName(String name);
}
