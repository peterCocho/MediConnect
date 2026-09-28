package com.sena.backend.service.impl;

import com.sena.backend.entity.Role;
import com.sena.backend.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for RoleServiceImpl.
 *
 * Cubre la creación y el listado de roles del sistema (ROLE_ADMIN,
 * ROLE_DOCTOR, etc.). El servicio delega la persistencia al repositorio;
 * la unicidad del nombre se garantiza a nivel de base de datos
 * (@Column unique = true), por lo que aquí se verifica que la excepción
 * de integridad se propague sin ser tragada.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    @DisplayName("createRole: construye el rol con nombre y descripción y lo persiste")
    void createRole_withValidData_savesRoleWithGivenFields() {
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setId(1);
            return r;
        });

        Role result = roleService.createRole("ROLE_DOCTOR", "Médico de la clínica");

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        Role saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("ROLE_DOCTOR");
        assertThat(saved.getDescription()).isEqualTo("Médico de la clínica");

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("ROLE_DOCTOR");
        assertThat(result.getDescription()).isEqualTo("Médico de la clínica");
    }

    @Test
    @DisplayName("createRole: la descripción es opcional y se persiste como null")
    void createRole_withNullDescription_savesRoleWithoutDescription() {
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = roleService.createRole("ROLE_RECEPTION", null);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("ROLE_RECEPTION");
        assertThat(captor.getValue().getDescription()).isNull();
        assertThat(result.getDescription()).isNull();
    }

    @Test
    @DisplayName("createRole: un nombre duplicado propaga DataIntegrityViolationException del repositorio")
    void createRole_withDuplicateName_propagatesIntegrityViolation() {
        when(roleRepository.save(any(Role.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> roleService.createRole("ROLE_ADMIN", "Administrador"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("listRoles: retorna todos los roles que entrega el repositorio")
    void listRoles_returnsAllRolesFromRepository() {
        Role admin = Role.builder().id(1).name("ROLE_ADMIN").description("Administrador").build();
        Role doctor = Role.builder().id(2).name("ROLE_DOCTOR").description("Médico").build();
        when(roleRepository.findAll()).thenReturn(List.of(admin, doctor));

        List<Role> result = roleService.listRoles();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Role::getName)
                .containsExactly("ROLE_ADMIN", "ROLE_DOCTOR");
        verify(roleRepository).findAll();
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    @DisplayName("listRoles: sin roles registrados retorna una lista vacía")
    void listRoles_whenNoRoles_returnsEmptyList() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<Role> result = roleService.listRoles();

        assertThat(result).isEmpty();
    }
}