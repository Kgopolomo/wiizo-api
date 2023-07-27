package za.co.wiizo.wiizoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import za.co.wiizo.wiizoapi.entity.UserProfile;


@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long>, JpaSpecificationExecutor<UserProfile> {

    UserProfile findByEmail(String email);
    boolean existsByEmail(String email);
}
