package za.co.wiizo.wiizoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import za.co.wiizo.wiizoapi.entity.UserProfile;
import za.co.wiizo.wiizoapi.entity.VerificationCode;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long>, JpaSpecificationExecutor<VerificationCode> {
    VerificationCode findByCode(String code);
    VerificationCode findByUserProfile(UserProfile user);
}
