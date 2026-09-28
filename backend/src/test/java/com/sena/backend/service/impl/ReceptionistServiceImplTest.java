package com.sena.backend.service.impl;

import com.sena.backend.domain.receptionist.CreateReceptionistRequest;
import com.sena.backend.domain.receptionist.ReceptionistResponse;
import com.sena.backend.domain.receptionist.UpdateReceptionistRequest;
import com.sena.backend.entity.Receptionist;
import com.sena.backend.entity.Role;
import com.sena.backend.entity.User;
import com.sena.backend.exception.BusinessRuleException;
import com.sena.backend.exception.ResourceNotFoundException;
import com.sena.backend.repository.ReceptionistRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

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
 * Unit tests for ReceptionistServiceImpl.
 *
 * Cubre el alta completa de recepcionistas (cuenta de usuario ROLE_RECEPTION
 * + perfil en la tabla receptionists), su consulta, actualización y
 * activación/desactivación. La entidad Receptionist persiste el User por
 * cascada (CascadeType.ALL), por lo que el servicio guarda únicamente el
 * Receptionist. La contraseña siempre debe pasar por el PasswordEncoder.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReceptionistServiceImpl")
class ReceptionistServiceImplTest {

    @Mock
    private ReceptionistRepository receptionistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ReceptionistServiceImpl receptionistService;

    private Role receptionRole;
    private User receptionUser;
    private Receptionist receptionist;

    @BeforeEach
    void setUp() {
        receptionRole = Role.builder().id(3).name("ROLE_RECEPTION").description("Recepción").build();

        receptionUser = User.builder()
                .id(20L)
                .username("recepcion.ana")
                .password("hashed-password")
                .role(receptionRole)
                .isActive(true)
                .build();

        receptionist = Receptionist.builder()
                .id(7L)
                .user(receptionUser)
                .identityDocument("1094999888")
                .fullName("Ana Recepción")
                .phone("573009998877")
                .build();
    }

    private CreateReceptionistRequest validCreateRequest() {
        CreateReceptionistRequest req = new CreateReceptionistRequest();
        req.setUsername("recepcion.ana");
        req.setPassword("Secret123");
        req.setFullName("Ana Recepción");
        req.setIdentityDocument("1094999888");
        req.setPhone("573009998877");
        return req;
    }

    private UpdateReceptionistRequest updateRequest(Boolean isActive) {
        return UpdateReceptionistRequest.builder()
                .fullName("Ana María Recepción")
                .phone("573118887766")
                .identityDocument("1094777666")
                .isActive(isActive)
                .build();
    }

    // ------------------------------------------------------------------
    // createReceptionist
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createReceptionist: crea el perfil con su usuario ROLE_RECEPTION activo y contraseña cifrada")
    void createReceptionist_withValidData_savesProfileWithEncodedUser() {
        when(userRepository.findByUsername("recepcion.ana")).thenReturn(Optional.empty());
        when(receptionistRepository.findByIdentityDocument("1094999888")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_RECEPTION")).thenReturn(Optional.of(receptionRole));
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed-password");

        receptionistService.createReceptionist(validCreateRequest());

        ArgumentCaptor<Receptionist> captor = ArgumentCaptor.forClass(Receptionist.class);
        verify(receptionistRepository).save(captor.capture());
        Receptionist saved = captor.getValue();

        assertThat(saved.getIdentityDocument()).isEqualTo("1094999888");
        assertThat(saved.getFullName()).isEqualTo("Ana Recepción");
        assertThat(saved.getPhone()).isEqualTo("573009998877");

        User savedUser = saved.getUser();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("recepcion.ana");
        assertThat(savedUser.getPassword()).isEqualTo("hashed-password").isNotEqualTo("Secret123");
        assertThat(savedUser.getRole()).isSameAs(receptionRole);
        assertThat(savedUser.getIsActive()).isTrue();

        // El usuario se persiste por cascada desde Receptionist
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReceptionist: un username ya existente lanza BusinessRuleException y no consulta nada más")
    void createReceptionist_withExistingUsername_throwsBusinessRuleException() {
        when(userRepository.findByUsername("recepcion.ana")).thenReturn(Optional.of(receptionUser));

        assertThatThrownBy(() -> receptionistService.createReceptionist(validCreateRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("usuario");

        verifyNoInteractions(receptionistRepository, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("createReceptionist: un documento de identidad ya registrado lanza BusinessRuleException")
    void createReceptionist_withExistingIdentityDocument_throwsBusinessRuleException() {
        when(userRepository.findByUsername("recepcion.ana")).thenReturn(Optional.empty());
        when(receptionistRepository.findByIdentityDocument("1094999888")).thenReturn(Optional.of(receptionist));

        assertThatThrownBy(() -> receptionistService.createReceptionist(validCreateRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("documento");

        verify(receptionistRepository, never()).save(any());
        verifyNoInteractions(roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("createReceptionist: si el rol ROLE_RECEPTION no existe lanza ResourceNotFoundException")
    void createReceptionist_whenReceptionRoleMissing_throwsResourceNotFound() {
        when(userRepository.findByUsername("recepcion.ana")).thenReturn(Optional.empty());
        when(receptionistRepository.findByIdentityDocument("1094999888")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_RECEPTION")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receptionistService.createReceptionist(validCreateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ROLE_RECEPTION");

        verify(receptionistRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    // ------------------------------------------------------------------
    // getReceptionistById
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getReceptionistById: retorna el DTO con los datos del perfil y de su cuenta de usuario")
    void getReceptionistById_withExistingId_returnsMappedResponse() {
        when(receptionistRepository.findById(7L)).thenReturn(Optional.of(receptionist));

        ReceptionistResponse response = receptionistService.getReceptionistById(7L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getUsername()).isEqualTo("recepcion.ana");
        assertThat(response.getIdentityDocument()).isEqualTo("1094999888");
        assertThat(response.getFullName()).isEqualTo("Ana Recepción");
        assertThat(response.getPhone()).isEqualTo("573009998877");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("getReceptionistById: recepcionista inexistente lanza ResourceNotFoundException")
    void getReceptionistById_withUnknownId_throwsResourceNotFound() {
        when(receptionistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receptionistService.getReceptionistById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ------------------------------------------------------------------
    // getAllReceptionists
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAllReceptionists: arma la paginación con orden ascendente y mapea cada recepcionista")
    void getAllReceptionists_buildsAscendingPageableAndMapsResults() {
        Pageable expected = PageRequest.of(0, 10, Sort.by("fullName").ascending());
        Page<Receptionist> page = new PageImpl<>(List.of(receptionist), expected, 1);
        when(receptionistRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ReceptionistResponse> result = receptionistService.getAllReceptionists(0, 10, "fullName");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(receptionistRepository).findAll(pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isZero();
        assertThat(used.getPageSize()).isEqualTo(10);
        assertThat(used.getSort().getOrderFor("fullName")).isNotNull();
        assertThat(used.getSort().getOrderFor("fullName").getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("recepcion.ana");
    }

    @Test
    @DisplayName("getAllReceptionists: sin recepcionistas registrados retorna una página vacía")
    void getAllReceptionists_withNoResults_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        when(receptionistRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ReceptionistResponse> result = receptionistService.getAllReceptionists(0, 10, "id");

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    // ------------------------------------------------------------------
    // updateReceptionist
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateReceptionist: actualiza los datos del perfil y la cuenta cuando isActive viene informado")
    void updateReceptionist_withValidData_updatesProfileAndAccountStatus() {
        when(receptionistRepository.findById(7L)).thenReturn(Optional.of(receptionist));

        receptionistService.updateReceptionist(7L, updateRequest(false));

        ArgumentCaptor<Receptionist> captor = ArgumentCaptor.forClass(Receptionist.class);
        verify(receptionistRepository).save(captor.capture());
        Receptionist saved = captor.getValue();

        assertThat(saved.getFullName()).isEqualTo("Ana María Recepción");
        assertThat(saved.getPhone()).isEqualTo("573118887766");
        assertThat(saved.getIdentityDocument()).isEqualTo("1094777666");
        assertThat(saved.getUser().getIsActive()).isFalse();
        assertThat(saved.getUser().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("updateReceptionist: con isActive nulo no modifica el estado de la cuenta")
    void updateReceptionist_withNullActiveFlag_keepsAccountStatus() {
        when(receptionistRepository.findById(7L)).thenReturn(Optional.of(receptionist));

        receptionistService.updateReceptionist(7L, updateRequest(null));

        ArgumentCaptor<Receptionist> captor = ArgumentCaptor.forClass(Receptionist.class);
        verify(receptionistRepository).save(captor.capture());

        assertThat(captor.getValue().getFullName()).isEqualTo("Ana María Recepción");
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("updateReceptionist: recepcionista inexistente lanza ResourceNotFoundException y no guarda")
    void updateReceptionist_withUnknownId_throwsResourceNotFound() {
        when(receptionistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receptionistService.updateReceptionist(999L, updateRequest(true)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(receptionistRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // toggleReceptionistStatus
    // ------------------------------------------------------------------

    @Test
    @DisplayName("toggleReceptionistStatus: con false desactiva la cuenta de usuario")
    void toggleReceptionistStatus_withFalse_deactivatesUser() {
        when(receptionistRepository.findById(7L)).thenReturn(Optional.of(receptionist));

        receptionistService.toggleReceptionistStatus(7L, false);

        ArgumentCaptor<Receptionist> captor = ArgumentCaptor.forClass(Receptionist.class);
        verify(receptionistRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("toggleReceptionistStatus: con true reactiva la cuenta de usuario")
    void toggleReceptionistStatus_withTrue_reactivatesUser() {
        receptionUser.setActive(false);
        when(receptionistRepository.findById(7L)).thenReturn(Optional.of(receptionist));

        receptionistService.toggleReceptionistStatus(7L, true);

        ArgumentCaptor<Receptionist> captor = ArgumentCaptor.forClass(Receptionist.class);
        verify(receptionistRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("toggleReceptionistStatus: recepcionista inexistente lanza ResourceNotFoundException y no guarda")
    void toggleReceptionistStatus_withUnknownId_throwsResourceNotFound() {
        when(receptionistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receptionistService.toggleReceptionistStatus(999L, false))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(receptionistRepository, never()).save(any());
    }
}