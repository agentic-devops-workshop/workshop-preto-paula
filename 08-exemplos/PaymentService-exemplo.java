// ============================================================================
// PaymentService.java — EXEMPLO PREENCHIDO
// ============================================================================
// Implementa REQ-PAY-001/002 (teto de descontos não judiciais a 30% do bruto).
//
// Rastreabilidade:
//   - Regra legada: BR-001 (business-rules-catalog.md)
//   - Programa Natural: CALCDSCT.NSN linhas 142-148
//   - Requisitos modernos: REQ-PAY-001 e REQ-PAY-002 (SPECIFICATION.md)
//   - Migração de banco: V1__init_payment_module.sql
//
// Commit que adiciona este código deve carregar:
//   feat(payment): cap non-judicial deductions at 30% — Implements REQ-PAY-001
// ============================================================================

package br.gov.client.sifap.payment.application;

import br.gov.client.sifap.payment.domain.Deduction;
import br.gov.client.sifap.payment.domain.DeductionType;
import br.gov.client.sifap.payment.domain.Payment;
import br.gov.client.sifap.payment.domain.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentService {

    /**
     * Teto fixo aplicado a descontos NÃO judiciais.
     * Vem de CALCDSCT.NSN#L142-L148 — multiplicador 0.30 hardcoded no legado.
     * Documentado em BR-001 e REQ-PAY-001.
     */
    private static final BigDecimal NON_JUDICIAL_CAP = new BigDecimal("0.30");

    private final PaymentRepository payments;

    public PaymentService(PaymentRepository payments) {
        this.payments = Objects.requireNonNull(payments);
    }

    /**
     * Calcula total de descontos aplicando o teto de 30% APENAS aos não judiciais.
     * Judiciais (DeductionType.JUDICIAL) somam integralmente — sem teto (REQ-PAY-002).
     *
     * @param deductions  lista de descontos solicitados
     * @param grossAmount valor bruto do pagamento
     * @return total efetivamente aplicado, com judiciais integrais + não-judiciais truncados
     */
    public BigDecimal calculateTotalDeductions(
            List<Deduction> deductions,
            BigDecimal grossAmount) {

        if (deductions == null || deductions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal judicialTotal = deductions.stream()
                .filter(d -> d.type() == DeductionType.JUDICIAL)
                .map(Deduction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal otherTotal = deductions.stream()
                .filter(d -> d.type() != DeductionType.JUDICIAL)
                .map(Deduction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cap = grossAmount
                .multiply(NON_JUDICIAL_CAP)
                .setScale(2, RoundingMode.HALF_EVEN);

        // REQ-PAY-001: trunca não-judiciais em 30% do bruto.
        BigDecimal cappedOther = otherTotal.min(cap);

        // REQ-PAY-002: judiciais somam integralmente, sem teto.
        return cappedOther.add(judicialTotal);
    }

    /**
     * Aplica descontos a um pagamento e persiste.
     * Camada infrastructure cuida do controller + DTO; aqui só lógica de domínio.
     */
    @Transactional
    public Payment applyDeductions(Long paymentId, List<Deduction> deductions) {
        Payment payment = payments.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        BigDecimal total = calculateTotalDeductions(deductions, payment.grossAmount());
        payment.applyDeductionTotal(total);  // domain method — valida transição

        return payments.save(payment);
    }
}

// ============================================================================
// O que torna este service "bom":
//   ✅ Cabeçalho com rastreabilidade BR-001 → REQ-PAY-001/002 → CALCDSCT.NSN#L142-L148
//   ✅ Constante NON_JUDICIAL_CAP nomeada (não usa 0.30 espalhado pelo código)
//   ✅ BigDecimal com escala fixa e arredondamento explícito (HALF_EVEN — banco aceita)
//   ✅ Lógica em domain/application; controller (não mostrado) é fino
//   ✅ Sem dependência circular — só importa de payment.domain
//   ✅ @Transactional só onde escreve (applyDeductions); calculate é puro
//
// O que TESTAR (PaymentServiceTest):
//   - Desconto não judicial 35% do bruto → truncado em 30%
//   - Desconto judicial 50% do bruto → aceito integralmente
//   - Mix: judicial 20% + não-judicial 25% → judicial 20% + 25% = 45% aceito
//   - Lista vazia → 0
//   - Lista null → 0
//   - Arredondamento: 333.335 → 333.34 (HALF_EVEN)
// ============================================================================
