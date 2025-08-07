package com.weady.weady.domain.user.repository;

import com.weady.weady.domain.user.entity.UserTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {
}
