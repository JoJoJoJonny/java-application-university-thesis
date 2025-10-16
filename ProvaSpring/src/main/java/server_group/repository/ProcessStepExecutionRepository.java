package server_group.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server_group.model.CustomUser;
import server_group.model.OrderStatus;
import server_group.model.ProcessStepExecution;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProcessStepExecutionRepository extends JpaRepository<ProcessStepExecution, Long> {
    List<ProcessStepExecution> findByOrderIdOrderByStepIndex(Long orderId);

    @Query("""
        SELECT e FROM ProcessStepExecution e
        WHERE e.order.status = :status
        ORDER BY e.order.startDate, e.stepIndex
        """)
    List<ProcessStepExecution> findByOrderStatus(@Param("status") OrderStatus status);

    List<ProcessStepExecution> findByOrderId(Long orderId);

    /*
    @Transactional
    @Modifying
    @Query("DELETE FROM ProcessStepExecution p WHERE p.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
    */

    //è la versione automatica di spring, se non funziona usa quella sopra
    @Transactional
    void deleteByOrderId(Long orderId);

    @Query("""
            SELECT p FROM ProcessStepExecution p
            WHERE p.assignedEmployee.email = :email
            AND :today BETWEEN p.actualStart AND p.actualEnd
           """)
    List<ProcessStepExecution> findAssignedToday(@Param("email") String email, @Param("today") LocalDate today);

    @Query("""
            SELECT p FROM ProcessStepExecution p
            WHERE p.assignedEmployee.email = :email
            ORDER BY p.actualStart, p.actualEnd
           """)
    List<ProcessStepExecution> findAssigned(@Param("email") String email);

}
