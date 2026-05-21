package com.sifap.beneficiary.domain;

/**
 * How the CPF was accepted into the system.
 *
 * <ul>
 *   <li>{@link #VALID} — passed strict Mod-11 (the default).</li>
 *   <li>{@link #LEGACY_BACKDOOR} — accepted via the 8 special prefixes
 *       or all-equal-digit rule preserved from {@code VALBENEF.NSN}
 *       (BR-028, BR-029, MYS-007, EGG-002). Migrated rows only.
 *       See ADR-008.</li>
 *   <li>{@link #TEST} — synthetic CPF for non-prod automated tests.
 *       Refused at boot in production by {@code CpfBackdoorBootGuard}.</li>
 * </ul>
 */
public enum CpfValidationStatus { VALID, LEGACY_BACKDOOR, TEST }
