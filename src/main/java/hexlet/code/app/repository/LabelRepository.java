package hexlet.code.app.repository;

import hexlet.code.app.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByName(String name);

    Set<Label> findAllByNameIn(Set<String> names);

    Set<Label> findAllByIdIn(Set<Long> ids);

    Boolean existsByName(String name);

}
