package io.github.jhipster.grpcsample.service.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import javax.persistence.Lob;
import io.github.jhipster.grpcsample.domain.enumeration.BankAccountType;

/**
 * A DTO for the BankAccount entity.
 */
public class BankAccountDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    private Integer bankNumber;

    private Long agencyNumber;

    private Float lastOperationDuration;

    private Double meanOperationDuration;

    @NotNull
    private BigDecimal balance;

    private LocalDate openingDay;

    private ZonedDateTime lastOperationDate;

    private Boolean active;

    private BankAccountType accountType;

    @Lob
    private byte[] attachment;
    private String attachmentContentType;

    @Lob
    private String description;

    private Long userId;

    private String userLogin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Integer getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(Integer bankNumber) {
        this.bankNumber = bankNumber;
    }
    public Long getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(Long agencyNumber) {
        this.agencyNumber = agencyNumber;
    }
    public Float getLastOperationDuration() {
        return lastOperationDuration;
    }

    public void setLastOperationDuration(Float lastOperationDuration) {
        this.lastOperationDuration = lastOperationDuration;
    }
    public Double getMeanOperationDuration() {
        return meanOperationDuration;
    }

    public void setMeanOperationDuration(Double meanOperationDuration) {
        this.meanOperationDuration = meanOperationDuration;
    }
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public LocalDate getOpeningDay() {
        return openingDay;
    }

    public void setOpeningDay(LocalDate openingDay) {
        this.openingDay = openingDay;
    }
    public ZonedDateTime getLastOperationDate() {
        return lastOperationDate;
    }

    public void setLastOperationDate(ZonedDateTime lastOperationDate) {
        this.lastOperationDate = lastOperationDate;
    }
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    public BankAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(BankAccountType accountType) {
        this.accountType = accountType;
    }
    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public String getAttachmentContentType() {
        return attachmentContentType;
    }

    public void setAttachmentContentType(String attachmentContentType) {
        this.attachmentContentType = attachmentContentType;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BankAccountDTO bankAccountDTO = (BankAccountDTO) o;

        if ( ! Objects.equals(id, bankAccountDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "BankAccountDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", bankNumber='" + bankNumber + "'" +
            ", agencyNumber='" + agencyNumber + "'" +
            ", lastOperationDuration='" + lastOperationDuration + "'" +
            ", meanOperationDuration='" + meanOperationDuration + "'" +
            ", balance='" + balance + "'" +
            ", openingDay='" + openingDay + "'" +
            ", lastOperationDate='" + lastOperationDate + "'" +
            ", active='" + active + "'" +
            ", accountType='" + accountType + "'" +
            ", attachment='" + attachment + "'" +
            ", description='" + description + "'" +
            '}';
    }
}
