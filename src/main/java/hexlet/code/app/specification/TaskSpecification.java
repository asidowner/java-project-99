package hexlet.code.app.specification;

import hexlet.code.app.dto.TaskDTO.TaskFilterDTO;
import hexlet.code.app.model.Task;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {

    public Specification<Task> build(TaskFilterDTO filter) {
        return withTitleContains(filter.getTitleCont())
                .and(withAssigneeId(filter.getAssigneeId()))
                .and(withStatusSlug(filter.getStatus()))
                .and(withLabelId(filter.getLabelId()));
    }

    private Specification<Task> withTitleContains(String titleCont) {
        return ((root, query, criteriaBuilder) -> titleCont == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + titleCont.toLowerCase() + "%"));
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return ((root, query, criteriaBuilder) -> assigneeId == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId));
    }

    private Specification<Task> withStatusSlug(String slug) {
        return ((root, query, criteriaBuilder) -> slug == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("taskStatus").get("slug"), slug));
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, criteriaBuilder) -> labelId == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.join("labels", JoinType.INNER).get("id"), labelId);
    }
}
