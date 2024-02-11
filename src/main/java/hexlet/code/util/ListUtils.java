package hexlet.code.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class ListUtils {

    public static PageRequest getPageRequest(Integer start, Integer end, String orderDirection, String orderProperty) {
        var dir = Sort.Direction.fromString(orderDirection);
        var sort = Sort.by(dir, orderProperty);
        return PageRequest.of(start, end, sort);
    }
}
