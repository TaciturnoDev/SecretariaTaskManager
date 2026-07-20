package com.math.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.math.taskmanager.entity.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    //  Busca usuário pelo login (usado no Spring Security)
    Optional<User> findByLogin(String login);

    //  Verifica se login já existe
    boolean existsByLogin(String login);

    //  Busca por CPF (opcional - regra de negócio)
    Optional<User> findByCpf(String cpf);

    //  Busca usuário ativo por nome (opcional)
    Optional<User> findByNameAndActiveTrue(String name);
    
 // USUÁRIOS PARA DELEGAÇÃO
    // ==============================
    List<User> findBySectorIdAndActiveTrue(Long sectorId);
    
}