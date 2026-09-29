package com.sena.backend.service.impl;

import com.sena.backend.domain.doctor.CreateDoctorRequest;
import com.sena.backend.domain.doctor.DoctorResponse;
import com.sena.backend.domain.doctor.UpdateDoctorRequest;
import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.entity.Doctor;
import com.sena.backend.entity.Role;
import com.sena.backend.entity.User;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.DoctorRepository;
import com.sena.backend.repository.RoleRepository;
import com.sena.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserServiceImpl.
 *
 * Cubre la gestión de cuentas del personal: asignación de roles, alta de
 * médicos (usuario ROLE_DOCTOR + perfil clínico) y recepcionistas
 * (usuario ROLE_RECEPTION), consulta/actualización de médicos y activación
 * o desactivación de cuentas. La contraseña nunca debe persistirse en texto
 * plano: siempre pasa por el Argon2PasswordEncoder.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private Argon2PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Role adminRole;
    private Role doctorRole;
    private Role receptionRole;
    private User doctorUser;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().id(1).name("ROLE_ADMIN").description("Administrador").build();
        doctorRole = Role.builder().id(2).name("ROLE_DOCTOR").description("Médico").build();
        receptionRole = Role.builder().id(3).name("ROLE_RECEPTION").description("Recepción").build();

        doctorUser = User.builder()
                .id(10L)
                .username("dr.garcia")
                .password("hashed-password")
                .role(doctorRole)
                .isActive(true)
                .build();

        doctor = Doctor.builder()
                .id(4L)
                .documentNumber("1094000111")
                .fullName("Dr. Andrés García")
                .email("agarcia@clinica.com")
                .phone("573001112233")
                .specialty("Dermatología")
                .user(doctorUser)
                .build();
    }

    private CreateDoctorRequest validDoctorRequest() {
        CreateDoctorRequest req = new CreateDoctorRequest();
        req.setUsername("dr.garcia");
        req.setPassword("Secret123");
        req.setDocumentNumber("1094000111");
        req.setFullName("Dr. Andrés García");
        req.setEmail("agarcia@clinica.com");
        req.setPhone("573001112233");
        req.setSpecialty("Dermatología");
        return req;
    }

    private CreateReceptionistRequest validReceptionistRequest() {
        CreateReceptionistRequest req = new CreateReceptionistRequest();
        req.setUsername("recepcion.ana");
        req.setPassword("Secret123");
        req.setFullName("Ana Recepción");
        req.setIdentityDocument("1094999888");
        req.setPhone("573009998877");
        return req;
    }

    private UpdateDoctorRequest validUpdateRequest(boolean active) {
        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setFullName("Dr. Andrés García Actualizado");
        req.setEmail("nuevo@clinica.com");
        req.setPhone("573114445566");
        req.setSpecialty("Cirugía plástica");
        req.setIsActive(active);
        return req;
    }

    // ------------------------------------------------------------------
    // assignRoleToUser
    // ------------------------------------------------------------------

    @Test
    @DisplayName("assignRoleToUser: con usuario y rol existentes asigna el rol y guarda el usuario")
    void assignRoleToUser_withValidData_setsRoleAndSaves() {
        User user = User.builder().id(11L).username("nuevo.usuario").password("x").role(receptionRole).isActive(true).build();
        when(userRepository.findByUsername("nuevo.usuario")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        userService.assignRoleToUser("nuevo.usuario", "ROLE_ADMIN");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isSameAs(adminRole);
        assertThat(captor.getValue().getUsername()).isEqualTo("nuevo.usuario");
    }

    @Test
    @DisplayName("assignRoleToUser: usuario inexistente lanza ResourceNotFoundException")
    void assignRoleToUser_withUnknownUser_throwsResourceNotFound() {
        when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRoleToUser("fantasma", "ROLE_ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("fantasma");

        verifyNoInteractions(roleRepository);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignRoleToUser: rol inexistente lanza ResourceNotFoundException y no modifica al usuario")
    void assignRoleToUser_withUnknownRole_throwsResourceNotFound() {
        User user = User.builder().id(11L).username("nuevo.usuario").password("x").role(receptionRole).isActive(true).build();
        when(userRepository.findByUsername("nuevo.usuario")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRoleToUser("nuevo.usuario", "ROLE_INEXISTENTE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ROLE_INEXISTENTE");

        assertThat(user.getRole()).isSameAs(receptionRole);
        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // createDoctor
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createDoctor: crea el usuario ROLE_DOCTOR con contraseña cifrada y el perfil clínico vinculado")
    void createDoctor_withValidData_createsUserAndDoctorProfile() {
        when(userRepository.findByUsername("dr.garcia")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createDoctor(validDoctorRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("dr.garcia");
        assertThat(savedUser.getPassword()).isEqualTo("hashed-password").isNotEqualTo("Secret123");
        assertThat(savedUser.getRole()).isSameAs(doctorRole);
        assertThat(savedUser.getIsActive()).isTrue();

        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());
        Doctor savedDoctor = doctorCaptor.getValue();

        assertThat(savedDoctor.getDocumentNumber()).isEqualTo("1094000111");
        assertThat(savedDoctor.getFullName()).isEqualTo("Dr. Andrés García");
        assertThat(savedDoctor.getEmail()).isEqualTo("agarcia@clinica.com");
        assertThat(savedDoctor.getPhone()).isEqualTo("573001112233");
        assertThat(savedDoctor.getSpecialty()).isEqualTo("Dermatología");
        assertThat(savedDoctor.getUser()).isSameAs(savedUser);
    }

    @Test
    @DisplayName("createDoctor: un username ya existente lanza BusinessRuleException y no crea nada")
    void createDoctor_withExistingUsername_throwsBusinessRuleException() {
        when(userRepository.findByUsername("dr.garcia")).thenReturn(Optional.of(doctorUser));

        assertThatThrownBy(() -> userService.createDoctor(validDoctorRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("dr.garcia");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(roleRepository, passwordEncoder, doctorRepository);
    }

    @Test
    @DisplayName("createDoctor: si el rol ROLE_DOCTOR no existe lanza ResourceNotFoundException y no guarda nada")
    void createDoctor_whenDoctorRoleMissing_throwsResourceNotFound() {
        when(userRepository.findByUsername("dr.garcia")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createDoctor(validDoctorRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ROLE_DOCTOR");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, doctorRepository);
    }

    // ------------------------------------------------------------------
    // createReceptionist
    // ------------------------------------------------------------------

//    @Test
//    @DisplayName("createReceptionist: crea un usuario ROLE_RECEPTION activo con contraseña cifrada")
//    void createReceptionist_withValidData_createsActiveUserWithReceptionRole() {
//        when(userRepository.findByUsername("recepcion.ana")).thenReturn(Optional.empty());
//        when(roleRepository.findByName("ROLE_RECEPTION")).thenReturn(Optional.of(receptionRole));
//        when(passwordEncoder.encode("Secret123")).thenReturn("hashed-reception");
//        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        userService.createReceptionist(validReceptionistRequest());
//
//        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
//        verify(userRepository).save(captor.capture());
//        User saved = captor.getValue();
//
//        assertThat(saved.getUsername()).isEqualTo("recepcion.ana");
//        assertThat(saved.getPassword()).isEqualTo("hashed-reception");
//        assertThat(saved.getRole()).isSameAs(receptionRole);
//        assertThat(saved.getIsActive()).isTrue();
//
//        verifyNoInteractions(doctorRepository);
//    }
//
//    @Test
//    @DisplayName("createReceptionist: un username ya existente lanza BusinessRuleException")
//    void createReceptionist_withExistingUsername_throwsBusinessRuleException() {
//        when(userRepository.findByUsername("recepcion.ana")).thenReturn(Optional.of(doctorUser));
//
//        assertThatThrownBy(() -> userService.createReceptionist(validReceptionistRequest()))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("recepcion.ana");
//
//        verify(userRepository, never()).save(any());
//        verifyNoInteractions(roleRepository, passwordEncoder);
//    }
//
//    @Test
//    @DisplayName("createReceptionist: si el rol ROLE_RECEPTION no existe lanza ResourceNotFoundException")
//    void createReceptionist_whenReceptionRoleMissing_throwsResourceNotFound() {
//        when(userRepository.findByUsername("recepcion.ana")).thenReturn(Optional.empty());
//        when(roleRepository.findByName("ROLE_RECEPTION")).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> userService.createReceptionist(validReceptionistRequest()))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("ROLE_RECEPTION");
//
//        verify(userRepository, never()).save(any());
//        verifyNoInteractions(passwordEncoder);
//    }

    // ------------------------------------------------------------------
    // getDoctorById
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getDoctorById: retorna el DTO con los datos del médico y de su cuenta de usuario")
    void getDoctorById_withExistingId_returnsMappedResponse() {
        when(doctorRepository.findById(4L)).thenReturn(Optional.of(doctor));

        DoctorResponse response = userService.getDoctorById(4L);

        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getDocumentNumber()).isEqualTo("1094000111");
        assertThat(response.getFullName()).isEqualTo("Dr. Andrés García");
        assertThat(response.getEmail()).isEqualTo("agarcia@clinica.com");
        assertThat(response.getPhone()).isEqualTo("573001112233");
        assertThat(response.getSpecialty()).isEqualTo("Dermatología");
        assertThat(response.getUsername()).isEqualTo("dr.garcia");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("getDoctorById: médico inexistente lanza ResourceNotFoundException")
    void getDoctorById_withUnknownId_throwsResourceNotFound() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getDoctorById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ------------------------------------------------------------------
    // updateDoctor
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateDoctor: actualiza los datos del perfil y guarda el médico")
    void updateDoctor_withValidData_updatesProfileFieldsAndSaves() {
        when(doctorRepository.findById(4L)).thenReturn(Optional.of(doctor));

        userService.updateDoctor(4L, validUpdateRequest(true));

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor saved = captor.getValue();

        assertThat(saved.getFullName()).isEqualTo("Dr. Andrés García Actualizado");
        assertThat(saved.getEmail()).isEqualTo("nuevo@clinica.com");
        assertThat(saved.getPhone()).isEqualTo("573114445566");
        assertThat(saved.getSpecialty()).isEqualTo("Cirugía plástica");
        // El número de documento no es editable
        assertThat(saved.getDocumentNumber()).isEqualTo("1094000111");
        assertThat(saved.getUser().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("updateDoctor: con isActive=false deshabilita la cuenta de usuario y bloquea el login")
    void updateDoctor_withInactiveFlag_disablesUserAccount() {
        when(doctorRepository.findById(4L)).thenReturn(Optional.of(doctor));

        userService.updateDoctor(4L, validUpdateRequest(false));

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());

        assertThat(captor.getValue().getUser().getIsActive()).isFalse();
        assertThat(captor.getValue().getUser().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("updateDoctor: médico inexistente lanza ResourceNotFoundException y no guarda")
    void updateDoctor_withUnknownId_throwsResourceNotFound() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateDoctor(999L, validUpdateRequest(true)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // toggleDoctorStatus
    // ------------------------------------------------------------------

    @Test
    @DisplayName("toggleDoctorStatus: con false desactiva la cuenta de usuario del médico")
    void toggleDoctorStatus_withFalse_deactivatesUser() {
        when(doctorRepository.findById(4L)).thenReturn(Optional.of(doctor));

        userService.toggleDoctorStatus(4L, false);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("toggleDoctorStatus: con true reactiva la cuenta de usuario del médico")
    void toggleDoctorStatus_withTrue_reactivatesUser() {
        doctorUser.setActive(false);
        when(doctorRepository.findById(4L)).thenReturn(Optional.of(doctor));

        userService.toggleDoctorStatus(4L, true);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("toggleDoctorStatus: médico inexistente lanza ResourceNotFoundException y no guarda")
    void toggleDoctorStatus_withUnknownId_throwsResourceNotFound() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.toggleDoctorStatus(999L, false))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // getAllDoctors
    // ------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getAllDoctors: arma la paginación con orden ascendente y mapea cada médico a DoctorResponse")
    void getAllDoctors_buildsAscendingPageableAndMapsResults() {
        Pageable expected = PageRequest.of(0, 10, Sort.by("fullName").ascending());
        Page<Doctor> page = new PageImpl<>(List.of(doctor), expected, 1);
        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<DoctorResponse> result = userService.getAllDoctors(0, 10, "fullName", "garc", "derma", true);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(doctorRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isZero();
        assertThat(used.getPageSize()).isEqualTo(10);
        assertThat(used.getSort().getOrderFor("fullName")).isNotNull();
        assertThat(used.getSort().getOrderFor("fullName").getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Dr. Andrés García");
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("dr.garcia");
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getAllDoctors: sin filtros ni resultados retorna una página vacía")
    void getAllDoctors_withNoResults_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<DoctorResponse> result = userService.getAllDoctors(0, 10, "id", null, null, null);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}