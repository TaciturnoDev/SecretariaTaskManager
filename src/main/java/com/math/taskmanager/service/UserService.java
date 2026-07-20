package com.math.taskmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.math.taskmanager.dto.UserRequestDTO;
import com.math.taskmanager.dto.DelegationUserDTO;
import com.math.taskmanager.entity.Sector;
import com.math.taskmanager.entity.User;
import com.math.taskmanager.exception.BusinessRuleException;
import com.math.taskmanager.exception.ResourceNotFoundException;
import com.math.taskmanager.repository.UserRepository;

import com.math.taskmanager.entity.Role;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SectorService sectorService; //  NOVO

    /*
     
     * CRIAR USUÁRIO COM SETOR
  
     */
    public User create(UserRequestDTO dto) {

        // 🔒 senha obrigatória
        if (dto.password() == null || dto.password().isBlank()) {
            throw new BusinessRuleException("Senha é obrigatória.");
        }

        //  CPF duplicado
        userRepository.findByCpf(dto.cpf())
                .ifPresent(existing -> {
                    throw new BusinessRuleException("CPF já cadastrado no sistema.");
                });

        //  login duplicado
        if (userRepository.existsByLogin(dto.login())) {
            throw new BusinessRuleException("Login já está em uso.");
        }

        //  setor obrigatório
        if (dto.sectorId() == null) {
            throw new BusinessRuleException("Setor é obrigatório.");
        }

        Sector sector = sectorService.findById(dto.sectorId());

        User user = User.builder()
                .name(dto.name())
                .login(dto.login())
                .cpf(dto.cpf())
                .password(passwordEncoder.encode(dto.password()))
                .sector(sector) //  vínculo com setor
                .active(true)
                .build();

        return userRepository.save(user);
    }

    /*
     * ==============================
     * LISTAR TODOS
     * ==============================
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    
    
    
    /*
     * ==============================
     * BUSCAR POR ID
     * ==============================
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado com ID: " + id)
                );
    }

    /*
     * ==============================
     * BUSCAR ATIVO POR NOME
     * ==============================
     */
    public User findActiveByName(String name) {
        return userRepository.findByNameAndActiveTrue(name)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Funcionário ativo não encontrado com nome: " + name)
                );
    }

    /*
     * ==============================
     * BUSCAR POR LOGIN
     * ==============================
     */
    public User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado com login: " + login)
                );
    }

    /*
     * ==============================
     * BUSCAR SEGURO (NÃO QUEBRA)
     * ==============================
     */
    public User findByLoginSafe(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    /*
     * ==============================
     * VERIFICAR LOGIN
     * ==============================
     */
    public boolean loginExists(String login) {
        return userRepository.existsByLogin(login);
    }

    /*
     * ==============================
     * DESATIVAR USUÁRIO
     * ==============================
     */
    public void deactivate(Long id) {
        User user = findById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    /*
     * ==============================
     * DELETE REAL
     * ==============================
     */
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    /*
     * ==============================
     *  ALTERAR SETOR (FUTURO ADMIN)
     * ==============================
     */
    
    public void updateSector(Long userId, Long sectorId) {

        User user = findById(userId);

        Sector sector = sectorService.findById(sectorId);

        user.setSector(sector);

        userRepository.save(user);
    }
    
    
    public List<DelegationUserDTO> findUsersForDelegation(User currentUser) {

        return userRepository.findAll()
                .stream()
                .filter(User::getActive)
                .filter(user -> user.getSector() != null)
                .filter(user -> {

                    // SUPERADMIN pode delegar para qualquer setor
                    if (currentUser.getRole() == Role.SUPERADMIN) {
                        return true;
                    }

                    // USER e ADMIN somente no próprio setor
                    return currentUser.getSector() != null
                            && user.getSector().getId()
                            .equals(currentUser.getSector().getId());
                })
                .map(user -> new DelegationUserDTO(
                        user.getId(),
                        user.getName(),
                        user.getSector().getId(),
                        user.getSector().getName()
                ))
                .toList();
    }
}