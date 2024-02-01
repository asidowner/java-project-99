package hexlet.code.app.controller.api;

import hexlet.code.app.dto.UserDTO.UserCreateDTO;
import hexlet.code.app.dto.UserDTO.UserDTO;
import hexlet.code.app.dto.UserDTO.UserUpdateDTO;
import hexlet.code.app.exception.ResourceForbiddenException;
import hexlet.code.app.service.UserService;
import hexlet.code.app.util.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserUtils userUtils;

    @GetMapping
    public ResponseEntity<List<UserDTO>> index(
            @RequestParam(defaultValue = "0", name = "_start") Integer start,
            @RequestParam(defaultValue = "10", name = "_end") Integer end,
            @RequestParam(defaultValue = "ASC", name = "_order") String orderDirection,
            @RequestParam(defaultValue = "id", name = "_sort") String orderProperty
    ) {
        var users = userService.getAll(start, end, orderDirection, orderProperty);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(userService.countAll()))
                .body(users);
    }

    @GetMapping("/{id}")
    public UserDTO show(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO data) {
        return userService.create(data);
    }

    @PutMapping("/{id}")
    public UserDTO update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO data) {
        var currentUser = userUtils.getCurrentUser();

        if (!currentUser.getId().equals(id)) {
            throw new ResourceForbiddenException();
        }
        return userService.update(id, data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        var currentUser = userUtils.getCurrentUser();

        if (!currentUser.getId().equals(id)) {
            throw new ResourceForbiddenException();
        }
        userService.delete(id);
    }
}
