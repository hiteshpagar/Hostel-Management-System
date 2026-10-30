package com.hms.repository;

import com.hms.common.enums.InvoiceStatus;
import com.hms.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByHostelIdAndInvoiceNumber(Long hostelId, String invoiceNumber);
    List<Invoice> findByStudentId(Long studentId);
    List<Invoice> findByHostelIdAndStatus(Long hostelId, InvoiceStatus status);
}
